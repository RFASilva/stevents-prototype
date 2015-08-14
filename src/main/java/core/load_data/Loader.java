package core.load_data;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import core.Config;
import core.Context;
import core.shared.Entity;
import core.shared.SpatialEvent;
import core.shared.SpatialGranule;
import core.shared.Table;
import core.time_series.ITime;
import core.time_series.TimeSeriesManager;
import core.time_series.TimeseriesType;
import core.time_series.implemented.ClustersTimeSeries;
import core.time_series.implemented.ConvexHullTimeSeries;
import core.time_series.implemented.DirectionsTimeSeries;
import core.time_series.implemented.NElemsTimeSeries;
import edu.hawaii.jmotif.datatype.TPoint;
import edu.hawaii.jmotif.datatype.Timeseries;

public class Loader {

	public static final int FETCH_SIZE = Config.getConfigInt("fetch_size");

	private static final Integer BATCH_SIZE = Config.getConfigInt("batch_work");

	public static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	// Table name from which we will load the data
	private static final String TABLE_NAME = Config.getConfigString("table_name");

	private static final String POINT_WKT = "POINT(%f %f)";

	private Long gridSize;

	private Envelope envelope;

	private GeometryFactory geofact;

	private Double precision = 0.00000001; 

	public static Table tableToStore;

	private List<Entity> events;

	public Loader(Table tabletoStore) {
		geofact = new GeometryFactory(new PrecisionModel(), 4326);
		this.envelope = new Envelope(); // Create the envelop that contains all the points
		this.tableToStore = tabletoStore;
		this.events = new LinkedList<Entity>();
	}
	
	public Loader() {
		geofact = new GeometryFactory(new PrecisionModel(), 4326);
		this.envelope = new Envelope(); // Create the envelop that contains all the points
		this.events = new LinkedList<Entity>();
	}

	public static void setTableToStore(Table tableToStore) {
		Loader.tableToStore = tableToStore;
	}

	public void uploadData(String fromTable, Long toGridSize, String sql) {
		
		System.out.println(sql);
		
		try {
			Connection connection = DataStoreInfo.getMetaStore();
			
			Statement st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);

			// Build envelope and computes gridsize
			ResultSet resultSet = st.executeQuery(sql);

			Geometry geometry;
			double bestPrecision = 1;
			
			while(resultSet.next()) {
				Entity event = new SpatialEvent();

				int minute = resultSet.getInt(1);
				int hour = resultSet.getInt(2);
				int day = resultSet.getInt(3);
				int month = resultSet.getInt(4);
				int year = resultSet.getInt(5);
				
					
				double latitude = resultSet.getDouble(6);
				double longitude = resultSet.getDouble(7);

				String wkt_point = String.format(POINT_WKT, longitude, latitude);
				wkt_point = wkt_point.replace(",", ".");

				geometry = new WKTReader(geofact).read(wkt_point);
				bestPrecision = Functions.findBestPrecision(bestPrecision, geometry);
				precision = precision > bestPrecision ? precision : bestPrecision;

				expand(envelope, geometry);

				// Fill Entity
				event.addProperty("minute", new Integer(minute));
				event.addProperty("hour", new Integer(hour));
				event.addProperty("day", new Integer(day));
				event.addProperty("month", new Integer(month));
				event.addProperty("year", new Integer(year));

				event.setGeometry(geometry);
				// Add Entity
				events.add(event);
			}

			System.out.println("Events size: " + events.size());

			Coordinate minCoordinate = new Coordinate(envelope.getMinX(), envelope.getMinY());
			Coordinate maxCoordinate = new Coordinate(envelope.getMaxX(), envelope.getMaxY());

			// Based on precision computes the next gridsize which is power of two
			gridSize = Functions.computeGridSize(minCoordinate, maxCoordinate, precision);

			tableToStore.createTable(connection);// Create table 

			System.out.println("GridSize: " + gridSize);

			String insertStatement = tableToStore.insertStatement(); // Computes the string insert statement (which have inherent an specific columns order)
			PreparedStatement ps = connection.prepareStatement(insertStatement); 

			Iterator<Entity> it = events.iterator();
			int batchCount = 0;
			Geometry upGeometry = null;
			Integer upGeoHash = null;

			Double x = envelope.getMinX();
			Double y = envelope.getMinY();

			System.out.println("min envelop: " + x);
			System.out.println("max envelop: " + y);

			// Store data in the table specified in the constructor
			while(it.hasNext()) {
				Entity event = it.next();

				int minute = event.getProperty("minute");
				int hour = event.getProperty("hour");
				int day = event.getProperty("day");
				int month = event.getProperty("month");
				int year = event.getProperty("year");
				
				//Dados por vezes sao estranhos
				if(hour > 23 || minute > 60)
					continue;
				
				if(toGridSize >= 4) upGeometry = Functions.convertToUp(event.getGeometry(), new Coordinate(x,y), precision, gridSize, toGridSize);
				else upGeometry = new GeometryFactory().createPoint(envelope.centre());

				upGeoHash = upGeometry.hashCode();

				ps.setString(1, event.getGeometry().toText());
				ps.setInt(2, minute);
				ps.setInt(3, hour);
				ps.setInt(4, day);
				ps.setInt(5, month);
				ps.setInt(6, year);
				ps.setInt(7, upGeoHash);
				ps.setString(8, upGeometry.toText());
				
				String date = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + "00";
				ps.setString(9, date);

				ps.addBatch();
				if (batchCount == BATCHINSERT_SIZE) {
					ps.executeBatch();
					batchCount = 0;
				} else
					batchCount++;
			}
			ps.executeBatch();


			connection.close();
		} catch (Exception exception) {
			exception.printStackTrace();
		}

	}

	private void expand(final Envelope envelope, final Geometry geometry) {
		for (Coordinate coordinate : geometry.getCoordinates())
			envelope.expandToInclude(coordinate);
	}

	private List<String> loadDistinctGranules() {
		List<String> result = new LinkedList<String>();

		Connection connection = DataStoreInfo.getMetaStore();
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);

			String sql = "select distinct(up_geo_hash) from " + tableToStore.getName();

			// Build envelope and computes gridsize
			ResultSet resultSet = st.executeQuery(sql);

			while(resultSet.next())
				result.add(resultSet.getString(1));

			connection.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}


	public List<Entity> buildGranules(TimeSeriesManager timeManager) {
		List<Entity> result = new LinkedList<Entity>();
		List<String> spatialGranulesIds =  loadDistinctGranules(); 
		String timeGranularity = timeManager.getTimeGranularty().toUpperCase();


		//For each spatial granule i have to create the time-series
		for (String granule_hash: spatialGranulesIds) {

			Entity granule = new SpatialGranule();

			//Build query according to the time granularity
			String sql = buildQuery(timeGranularity, granule_hash, false, null);
			System.out.println("Granule SQL: " + sql);

			// Anonymous Classes in order to be flexible the timeseries construction
			// If we want to add more time series we just to have reimplement the fillTimeSeries method
			new ITime() {
				@Override
				public void fillTimeSeries(String sql, String timeGranularity, Entity granule, TimeSeriesManager timeManager, Map<String, Object> params) {

					Map<TimeseriesType, Timeseries> timeseries = new HashMap<TimeseriesType, Timeseries>();

					Timeseries t1 = new NElemsTimeSeries().newTimeSeries(sql, timeGranularity, granule, timeManager.getLengthTime());
					Timeseries t2 = new ConvexHullTimeSeries().newTimeSeries(sql, timeGranularity, granule, timeManager.getLengthTime());
					Map<TimeseriesType, Timeseries> series = new ClustersTimeSeries().newNTimeSeries(sql, timeGranularity, granule, timeManager.getLengthTime());


					DirectionsTimeSeries directions = new DirectionsTimeSeries();
					Map<TimeseriesType, Timeseries> directionseries = directions.newNTimeSeries(sql, timeGranularity, granule, timeManager.getLengthTime());

					((SpatialGranule)granule).addTimeSeries(TimeseriesType.NrElements, t1);
					((SpatialGranule)granule).addTimeSeries(TimeseriesType.ConvexHullArea, t2);
					((SpatialGranule)granule).addNTimeSeries(series);
					((SpatialGranule)granule).addNTimeSeries(directionseries);

					new NElemsTimeSeries().newTimeSeriesBD(sql, timeGranularity, granule, null);
				}
			}.fillTimeSeries(sql, timeGranularity, granule, timeManager, null);


			result.add(granule);
			System.out.println("Time Series Created");
		}

		return result;
	}

	public void buildGranulesBD(TimeSeriesManager timeManager, Table tableToStore, Table tableToStoreMeta, boolean isRestricted, Map<String, Object> params) {
		List<Entity> result = new LinkedList<Entity>();
		List<String> spatialGranulesIds =  loadDistinctGranules(); 
		String timeGranularity = timeManager.getTimeGranularty().toUpperCase();

		Connection connection; 
		try {
			connection = DataStoreInfo.getMetaStore();
			tableToStore.createTable(connection);

			System.out.println("starting computing up...");
			//For each spatial granule i have to create the time-series
			for (String granule_hash: spatialGranulesIds) {

				Entity granule = new SpatialGranule();

				//Build query according to the time granularity
				String sql = buildQuery(timeGranularity, granule_hash, isRestricted, (Polygon) params.get("polygon"));
				System.out.println("Granule SQL: " + sql);

				// Anonymous Classes in order to be flexible the timeseries construction
				// If we want to add more time series we just to have reimplement the fillTimeSeries method
				new ITime() {
					@Override
					public void fillTimeSeries(String sql, String timeGranularity, Entity granule, TimeSeriesManager timeManager, Map<String, Object> params) {

						new NElemsTimeSeries().newTimeSeriesBD(sql, timeGranularity, granule, params);
						new ConvexHullTimeSeries().newTimeSeriesBD(sql, timeGranularity, granule, params);
						new DirectionsTimeSeries().newNTimeSeriesBD(sql, timeGranularity, granule, params);
						new ClustersTimeSeries().newNTimeSeriesBD(sql, timeGranularity, granule, params);
					}
				}.fillTimeSeries(sql, timeGranularity, granule, timeManager, params);
			}

			// Store Meta information about the time series
			tableToStoreMeta.createTable(connection);
			String insertStatement = tableToStoreMeta.insertStatement();

			PreparedStatement ps = connection.prepareStatement(insertStatement);

			ps.setString(1, tableToStoreMeta.getName());
			ps.setString(2, timeManager.getTimeGranularty());
			ps.setInt(3, timeManager.getMinYear());
			ps.setInt(4, timeManager.getMaxYear());
			ps.setLong(5, timeManager.getLengthTime());

			ps.execute();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Time Series Stored");

	}

	/**
	 * 
	 * @param timeGranularity: time granularity can be year, month, day, hour, minute
	 * @param granule: the hashcode of a given spatial granule position
	 * @return
	 */
	public String buildQuery(String timeGranularity, String granule, boolean isRestricted, Polygon polygon) {
		timeGranularity = timeGranularity.toUpperCase();

		String sql = "select up_geo_hash, ";
		String temp = "";
		for(int i = 0; i < TimeSeriesManager.TIME_GRANULARITY.length; i++) {
			temp += TimeSeriesManager.TIME_GRANULARITY[i].toLowerCase();
			if(!TimeSeriesManager.TIME_GRANULARITY[i].equals(timeGranularity))	temp += ",";
			else break;
		}

		sql += temp;
		sql += ", array_agg(ST_AsText(geometry)) as SpatialObjects, ST_AsText(up_geometry)  from " + tableToStore.getName();
		sql += " where up_geo_hash='" + granule + "'";
		
		
		if(isRestricted) {
			sql += "and ";
			String template = "ST_Contains(ST_GeomFromText( 'POLYGON((%s))', 4326), geometry)";
			Coordinate[] vertexes = polygon.getCoordinates();
			
			String sqlcoords = "";
			for(int i = 0; i < vertexes.length; i++) {
				sqlcoords += vertexes[i].x + " " + vertexes[i].y;
				if (!(i == vertexes.length - 1))
					sqlcoords += ",";
			}
			
			sql += String.format(template, sqlcoords);
		}
		
		
		sql += " group by up_geo_hash, " + temp + ", ST_AsText(up_geometry) ";
		sql += " order by up_geo_hash, " + temp;

		return sql;
	}


	public void storeTimeSeries(Table toStore, List<Entity> spatialGranules, Table toStoreMeta, TimeSeriesManager timeManager) {

		Connection connection = DataStoreInfo.getMetaStore();
		toStore.createTable(connection);

		//It generates the datetime field
		String[] dateTimes = timeManager.generatesDateTimes();
		System.out.println("Storing time series");

		String insertStatement = toStore.insertStatement(); // Computes the string insert statement (which have inherent an specific columns order)
		int batchCount = 0;

		try {
			PreparedStatement ps = connection.prepareStatement(insertStatement);
			for(Entity entity: spatialGranules) {

				SpatialGranule temp = (SpatialGranule)entity;

				ps.setString(1, temp.getUpGeometry().toText());
				ps.setInt(2, temp.getUpGeometry().hashCode());


				for(Entry<TimeseriesType, Timeseries> t: temp.getTimeseries().entrySet()) {

					String type = t.getKey().name();
					ps.setString(3, type);

					Iterator<TPoint> it = t.getValue().iterator();
					int i = 0;
					while(it.hasNext()) {
						ps.setString(4, dateTimes[i]);
						ps.setInt(5, i);
						ps.setDouble(6, it.next().value());
						i++;

						ps.addBatch();
						if (batchCount == BATCHINSERT_SIZE) {
							ps.executeBatch();
							batchCount = 0;
						} else
							batchCount++;
					}
				}
			}
			ps.executeBatch();


			// Store Meta information about the time series
			toStoreMeta.createTable(connection);
			insertStatement = toStoreMeta.insertStatement();
			ps = connection.prepareStatement(insertStatement);

			ps.setString(1, toStore.getName());
			ps.setString(2, timeManager.getTimeGranularty());
			ps.setInt(3, timeManager.getMinYear());
			ps.setInt(4, timeManager.getMaxYear());
			ps.setLong(5, timeManager.getLengthTime());

			ps.execute();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} 
	}

	// This function assumes that we have global timeseries, i.e., only one spatial granule
	public static String getTimeSeries(String tableName, boolean isRestricted) {
		
		System.out.println("getting time series...");
		
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append("[");

		Connection connection = DataStoreInfo.getMetaStore();
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);

			String sqlmeta = "select length from " + tableName + "meta";
			
			ResultSet resultSetMeta = st.executeQuery(sqlmeta);
			resultSetMeta.next();

			long length = resultSetMeta.getLong(1); 
			System.out.println("length of the timeseries: " + length);

			String sql = "select type, datetime, value from " + tableName+ " order by type, pos";
			System.out.println("query para ir buscar as series tmeporais: " + sql);
			ResultSet resultSet = st.executeQuery(sql);

			String type="";
			int n = 0;
			while(resultSet.next()) {

				String timeseriesType = resultSet.getString(1);
				if(!timeseriesType.equals(type)) {
					strBuilder.append("{\"type\":\"" + timeseriesType + "\",\"data\":[");
					type = timeseriesType;
				}

				String datetime = resultSet.getString(2);
				BigDecimal value = resultSet.getBigDecimal(3);

				strBuilder.append("{" + datetime + "," + "\"value\":" + value.toString() + "}");
				n++;

				if(n!=length)
					strBuilder.append(",");
				else { 
					if(resultSet.isLast()) {
						strBuilder.append("]");	
					}
					else {
						n = 0; strBuilder.append("]},");	
					}

				}

			}

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		strBuilder.append("}]");
		return strBuilder.toString();
	}


	//Method to be called by server
	public String getInstSpatialEvents(Context context, int pos) {
		Connection connection = DataStoreInfo.getMetaStore();

		//Init json constrution
		StringBuilder strBuilder = new StringBuilder();
		String header= "{\"type\":\"FeatureCollection\",\"features\":[";
		strBuilder.append(header);
		String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]},\"properties\": {}}";

		// Query to find out for what date should i make the slice
		String timeSeriesTable = context.getTimeSeriesTableName(); 
		String findDate = "select datetime from " + timeSeriesTable + " where pos = "+pos+" LIMIT 1";

//				System.out.println("find date: " + findDate);

		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);

			// Build envelope and computes gridsize
			ResultSet resultSet = st.executeQuery(findDate);
			resultSet.next();
			//example date format: 2013-10-05 11:42:50
			String date = resultSet.getString(1);
			String sql = buildInstSpatialQuery(date, context.getUpTableName());
			System.out.println("Query Espacial Instante de Tempo: " + sql);
			//			System.out.println("SQL INST SPATIAL EVENTS: " + sql);

			resultSet = st.executeQuery(sql);

			while(resultSet.next()) {
				String geom = resultSet.getString(1);
				Geometry geometry = new WKTReader(geofact).read(geom);

				strBuilder.append(String.format(featureTemplate, geometry.getCoordinates()[0].x, geometry.getCoordinates()[0].y));
				if(!resultSet.isLast())
					strBuilder.append(",");
			}

			strBuilder.append("]}");

			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strBuilder.toString();
	}


	//Method to be called by server
	public String getRangeSpatialEvents(Context context, int posInit, int posEnd) {
		Connection connection = DataStoreInfo.getMetaStore();

		//Init json constrution
		StringBuilder strBuilder = new StringBuilder();
		String header= "{\"type\":\"FeatureCollection\",\"features\":[";
		strBuilder.append(header);
		String featureTemplate = "{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[%s, %s]},\"properties\": {}}";

		String timeSeriesTable = context.getTimeSeriesTableName(); 

		String findDateInit = "select datetime from " + timeSeriesTable + " where pos = "+posInit+" LIMIT 1";
		String findDateFinal = "select datetime from " + timeSeriesTable + " where pos = "+posEnd+" LIMIT 1";

		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(FETCH_SIZE);

			ResultSet resultSetInit = st.executeQuery(findDateInit);
			resultSetInit.next();
			String dateInit = resultSetInit.getString(1);

			System.out.println("formato da data: " + dateInit);

			//example date format: 2013-10-05T11:42:50
			ResultSet resultSetEnd = st.executeQuery(findDateFinal);
			resultSetEnd.next();
			String dateEnd = resultSetEnd.getString(1);

			String sql = buildRangeSpatialQuery(dateInit, dateEnd, context.getUpTableName());

			System.out.println("SQL RANGE SPATIAL EVENTS: " + sql);

			ResultSet resultSet = st.executeQuery(sql);

			while(resultSet.next()) {
				String geom = resultSet.getString(1);
				Geometry geometry = new WKTReader(geofact).read(geom);

				strBuilder.append(String.format(featureTemplate, geometry.getCoordinates()[0].x, geometry.getCoordinates()[0].y));
				if(!resultSet.isLast())
					strBuilder.append(",");
			}

			strBuilder.append("]}");

			connection.close();
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}

		return strBuilder.toString();
	}

	private String buildRangeSpatialQuery(String dateInit, String dateEnd, String tableNameUp) {
		String[] splittedInit = dateInit.split(":", 2);
		String[] splittedEnd = dateEnd.split(":", 2);

		String begins = "", ends = "";
		
		begins = splittedInit[1].replace("\"","");
		ends = splittedEnd[1].replace("\"","");

		String sql = "select  ST_AsText(geometry) from " + tableNameUp + " where ";
		
		//Adjust query parameters based on the granularity position
		if(TimeSeriesManager.pos == 0) {
			begins = begins.replaceFirst("-0", "-1");
			begins = begins.replaceFirst("-0", "-1");
			
			ends = ends.replaceFirst("-0", "-12");
			ends = ends.replaceFirst("-0", "-31");
		}
		
		if(TimeSeriesManager.pos == 1) {
			String[] temp = ends.split("-");
			begins = begins.replaceFirst("-0", "-1");
			
			ends = ends.replaceFirst("-0", "-" + (TimeSeriesManager.dayMonth(Integer.parseInt(temp[1]), Integer.parseInt(temp[0])))+ "" );
		}
		//TODO: hour
		
		//TODO: Minute 
		
		
		
		sql += "date >= '" + begins + "' and date <='" + ends + "'";

		return sql;
	}

	//Auxiliary function to build the sql query to get spatial objects
	private String [] processDate(String date) {
		// Process Date Init
		String[] splitted = date.split(":", 2);
		splitted = splitted[1].split("T");

		String[] first = splitted[0].replace("\"", "").split("-");
		String[] second = null;
		String[] both = null;


		if(splitted.length > 1) {
			second = splitted[1].replace("\"", "").split(":");
			both = concat(first, second);
		} else both = first;

		return both;
	}

	private String[] concat(String[] A, String[] B) {
		int aLen = A.length;
		int bLen = B.length;
		String[] C= new String[aLen+bLen];
		System.arraycopy(A, 0, C, 0, aLen);
		System.arraycopy(B, 0, C, aLen, bLen);
		return C;
	}

	private String buildInstSpatialQuery(String dateInit, String tableNameUp) {
		String[] splitted = dateInit.split(":", 2);
		splitted = splitted[1].split(" ");

		String[] first = splitted[0].replace("\"", "").split("-");
		String[] second = null;
		String[] both = null;


		if(splitted.length > 1) {
			second = splitted[1].replace("\"", "").split(":");
			both = concat(first, second);
		} else both = first;
		
		String sql = "select  ST_AsText(geometry) from " + tableNameUp + " where ";
		
		int length = TimeSeriesManager.pos + 1;
		for(int i = 0; i <length; i++) {
			String temp = TimeSeriesManager.TIME_GRANULARITY[i].toLowerCase() + "=" + both[i];
			if(!(i == length - 1)) {
				temp += " and ";
			}
			sql += temp;
		}

		
		return sql;
	}



	public static void main(String[] args) {
		//		System.out.println(Loader.getTimeSeries("timeseriesaccidents_usa"));

		//		System.out.println(Loader.getTimeSeries("timeseriesaccidents_usa"));;


		String date = "\"DateTime\":\"2013-10-05\"";
		String[] splitted = date.split(":", 2);
		splitted = splitted[1].split("T");

		String[] first = splitted[0].replace("\"", "").split("-");
		String[] second = null;
		if(splitted.length > 1) {
			second = splitted[1].replace("\"", "").split(":");
		}

		System.out.println(first[0]);
		System.out.println(second[0]);
	}

}
