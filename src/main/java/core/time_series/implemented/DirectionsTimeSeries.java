package core.time_series.implemented;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import core.load_data.DataStoreInfo;
import core.load_data.Functions;
import core.load_data.Loader;
import core.shared.Entity;
import core.time_series.TimeSeriesFactory;
import core.time_series.TimeSeriesManager;
import core.time_series.TimeseriesType;
import edu.hawaii.jmotif.datatype.Timeseries;

public class DirectionsTimeSeries extends TimeSeriesFactory {

	private Coordinate center;
	
	//This class will count the objects
	private int northEast;
	private int northWest;
	private int southEast;
	private int southWest;
	
	public Coordinate getCenter() {
		return center;
	}


	public void setCenter(Coordinate center) {
		this.center = center;
	}


	@Override
	public  Map<TimeseriesType, Timeseries> buildNTimeSeries(String sql, Entity granule) {

		Map<TimeseriesType, Timeseries> result = new HashMap<TimeseriesType, Timeseries>();
		result.put(TimeseriesType.DNorthEast,  super.initTime());
		result.put(TimeseriesType.DNorthWest,  super.initTime());
		result.put(TimeseriesType.DSouthEast,  super.initTime());
		result.put(TimeseriesType.DSouthWest,  super.initTime());

		int pos = TimeSeriesManager.findGranularityPos(super.timeGranularity);
		int[] times = new int[5];

		Connection connection = DataStoreInfo.getMetaStore();
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			st.setFetchSize(Loader.FETCH_SIZE);

			ResultSet data = st.executeQuery(sql);
			while(data.next()) {
				int up_geo_hash = data.getInt(1);
				for(int i= 0; i < pos + 1; i++) {
					times[i] = data.getInt(i + 2);
				}

				String[] spatialPoints = (String[]) data.getArray(pos + 3).getArray();

				String wkt_point = data.getString(pos + 4);
				granule.setUpGeometry(new WKTReader(TimeSeriesFactory.geofact).read(wkt_point));
				
				countByDirection(spatialPoints);
				
				int index = super.findPos(times) - 1;// Timeseries objects starts index at 0 position

				result.get(TimeseriesType.DNorthEast).elementAt(index).setValue(northEast);
				result.get(TimeseriesType.DNorthWest).elementAt(index).setValue(northWest);
				result.get(TimeseriesType.DSouthEast).elementAt(index).setValue(southEast);
				result.get(TimeseriesType.DSouthWest).elementAt(index).setValue(southWest);

			}

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return result;
	}
	
	@Override
	public void buildNTimeSeriesBD(String sql, Entity granule, Map<String, Object> params) {
		System.out.println("building direction timeseries");
		
		Connection connection = DataStoreInfo.getMetaStore();
		int posTimeSeries = 1;
		try {
			
			//Makes the query for data
			Statement st = connection.createStatement();
			st.setFetchSize(Loader.FETCH_SIZE);
			ResultSet data = st.executeQuery(sql);
			
			int pos = TimeSeriesManager.findGranularityPos(super.timeGranularity);
			int[] actualDate = new int[pos+1];
			int[] minimumDate = super.firstDate();
			List<String> types = new LinkedList<String>();
			types.add(TimeseriesType.DNorthEast.name());
			types.add(TimeseriesType.DNorthWest.name());
			types.add(TimeseriesType.DSouthEast.name());
			types.add(TimeseriesType.DSouthWest.name());

			while(data.next()) {
				int upGeomtryHash = data.getInt(1);
				for(int i= 0; i < pos + 1; i++) {
					actualDate[i] = data.getInt(i + 2);
				}

				String[] spatialPoints = (String[]) data.getArray(pos + 3).getArray();
				String wktUpGeometry = data.getString(pos + 4);
				
				
				
//				center = new Coordinate(-8.0, 39.5); // TODO: SHOULD BE PARAMETERIZABLE
//				center = new Coordinate(-99, 39);//Center of united states states
				
				center = (Coordinate) params.get("centerPoint");
				
				countByDirection(spatialPoints);
				
				List<Double> values = new LinkedList<Double>();
				values.add((double)northEast);
				values.add((double)northWest);
				values.add((double)southEast);
				values.add((double)southWest);
				
				if(data.isLast()) {
					posTimeSeries = super.writeTimeSeriesSoFar(wktUpGeometry, upGeomtryHash, types, minimumDate, actualDate, values, connection, posTimeSeries, false);
					minimumDate =  TimeSeriesManager.next(actualDate).clone();
					if(Functions.areEqual(actualDate, TimeSeriesManager.maxDate)) {
						break;
					}
					else {	
						posTimeSeries = super.writeTimeSeriesSoFar(wktUpGeometry, upGeomtryHash, types, minimumDate, TimeSeriesManager.maxDate, values, connection, posTimeSeries, true);
						break;
					}
				}
				else {
					posTimeSeries = super.writeTimeSeriesSoFar(wktUpGeometry, upGeomtryHash, types, minimumDate, actualDate, values, connection, posTimeSeries, false);
					minimumDate =  TimeSeriesManager.next(actualDate).clone();
				}
			}

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void countByDirection (String[] points) {
		GeometryCollection geometries = processCoords(points);
		northEast = northWest = southWest = southEast = 0;
		for(int i = 0; i < geometries.getNumGeometries(); i++) {
			
			Geometry geom = geometries.getGeometryN(i);
			Coordinate point = geom.getCoordinates()[0];
			
			double direction = bearing(center, point);
			
			if(direction >= 0 && direction < 90) northEast++;
			if(direction >=90 && direction < 180) northWest++;
			if(direction >=180 && direction < 270) southWest++;
			if(direction >=270 && direction < 360) southEast++;
		}
		
	}
	
	public  double bearing(Coordinate centerPt, Coordinate targetPt) {
	    double theta = Math.atan2(targetPt.y - centerPt.y, targetPt.x - centerPt.x);
	    double angle = Math.toDegrees(theta);
	    if (angle < 0) {
	        angle += 360;
	    }
	    return angle;
	}
	

	@Override
	public Timeseries buildTimeSeries(String sql, Entity granule) {
		return null;
	}


	@Override
	public void buildTimeSeriesBD(String sql, Entity granule, Map<String, Object> params) {
	}





}
