package core.time_series;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.xerces.internal.impl.dv.xs.DayDV;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import core.load_data.Functions;
import core.load_data.Loader;
import core.shared.Entity;
import edu.hawaii.jmotif.datatype.TPoint;
import edu.hawaii.jmotif.datatype.Timeseries;

abstract public class TimeSeriesFactory {

	public static final GeometryFactory geofact = new GeometryFactory(new PrecisionModel(), 4326);

	protected String timeGranularity;

	protected long length;


	public abstract Timeseries buildTimeSeries(String sql, Entity granule);

	public abstract void buildTimeSeriesBD(String sql, Entity granule,Map<String, Object> params); //Same version of the previous but stores directly the time series in the database


	public abstract Map<TimeseriesType, Timeseries> buildNTimeSeries(String sql, Entity granule);

	public abstract void buildNTimeSeriesBD(String sql, Entity granule, Map<String, Object> params); //Same version of the previous but stores directly the time series in the database


	public void newTimeSeriesBD(String sql, String timeGranularity, Entity granule, Map<String, Object> params) {
		this.timeGranularity = timeGranularity;
		this.buildTimeSeriesBD(sql, granule, params);
	}
	
	public void newNTimeSeriesBD(String sql, String timeGranularity, Entity granule, Map<String, Object> params) {
		this.timeGranularity = timeGranularity;
		this.buildNTimeSeriesBD(sql, granule, params);
	}


	/*
	 *  Functions initially developed returning a timeseries object for each granule.
	 */

	public Timeseries newTimeSeries(String sql, String timeGranularity, Entity granule, long length) {
		this.timeGranularity = timeGranularity;
		this.length = length;
		return this.buildTimeSeries(sql, granule);
	}

	public Map<TimeseriesType, Timeseries> newNTimeSeries(String sql, String timeGranularity, Entity granule, long length) {
		this.timeGranularity = timeGranularity;
		this.length = length;
		return this.buildNTimeSeries(sql, granule);
	}

	public Timeseries initTime() {
		Timeseries result = new Timeseries();
		for(int i=1; i <= length; i++)
			result.add(new TPoint(0, i));

		return result;
	}

	public int findPos(int[] times) {
		return TimeSeriesManager.findPosInTimeSerie(times[4], times[3], times[2], times[1], times[0]);
	}

	/*
	 * End
	 */

	//Used in time series construction
	protected  GeometryCollection processCoords(String[] points) {
		Geometry[] geometries = new Geometry[points.length];
		WKTReader reader = new WKTReader(TimeSeriesFactory.geofact);
		try {

			for(int i = 0; i < points.length; i++) 
				geometries[i] = reader.read(points[i]);

		} catch (ParseException e) {
			e.printStackTrace();
		}

		return  new GeometryCollection(geometries, TimeSeriesFactory.geofact);
	}


	protected int[] firstDate() {
		int pos = TimeSeriesManager.findGranularityPos(timeGranularity);
		int[] minimumDate = new int[pos + 1];

		minimumDate[0] = TimeSeriesManager.getMinYear();
		for (int i = 1; i < minimumDate.length; i++) {
			if(i < 3)
				minimumDate[i] = Integer.parseInt(TimeSeriesManager.minYearMonthDay[i]);
			if(i >= 3 && i < 6)
				minimumDate[i] = Integer.parseInt(TimeSeriesManager.minHourMinSec[i-3]);
		}

		return minimumDate;
	}

	public int writeTimeSeriesSoFar(String wktUpGeometry, int upGeomtryHash,
			List<String> types, int[] minimumDate, int[] actualDate,
			List<Double> values, Connection connection, int pos, boolean isLastRecord) {

//		System.out.println(minimumDate[0] + " - " + minimumDate[1] );
//		System.out.println(actualDate[0] + " - " + actualDate[1] );
		
		
		String timeSeriesStatement = TimeSeriesManager.tableToStore.insertStatement(); // timeseries insert statement

		int batchCount = 0;
		int result = pos;
		PreparedStatement ps;
		try {
			ps = connection.prepareStatement(timeSeriesStatement);

			while(Functions.areEqual(minimumDate, actualDate)==false) {
				
				for(String type: types) {
					ps.setString(1, "POINT(-20 -20)");
					ps.setInt(2, upGeomtryHash);
					ps.setString(3, type);
					ps.setString(4, dateTime(minimumDate));
					ps.setInt(5, result);
					ps.setInt(6, 0);
					ps.addBatch();

					if (batchCount == Loader.BATCHINSERT_SIZE) {
						ps.executeBatch();
						batchCount = 0;
					} else
						batchCount++;
				}
				result++;
				minimumDate = TimeSeriesManager.next(minimumDate).clone();
				
				
			}
			
			if(isLastRecord) {
				if(batchCount > 0) {
					
					//TODO: Refactorizar algum codigo
					for(String type: types) {
						ps.setString(1, "POINT(-20 -20)");
						ps.setInt(2, upGeomtryHash);
						ps.setString(3, type);
						ps.setString(4, dateTime(minimumDate));
						ps.setInt(5, result);
						ps.setInt(6, 0);
						ps.addBatch();

						if (batchCount == Loader.BATCHINSERT_SIZE) {
							ps.executeBatch();
							batchCount = 0;
						} else
							batchCount++;
					}
					
					ps.executeBatch();
					
					result++;
//					System.out.println("escreveu as ultimas datas: " + actualDate[0] + "- " + actualDate[1] + " - " +  actualDate[2]);
				}
				return result;
			}

			for(int i = 0; i < types.size(); i++) {
				ps.setString(1, wktUpGeometry);
				ps.setInt(2, upGeomtryHash);
				ps.setString(3, types.get(i));
				ps.setString(4, dateTime(actualDate));
				ps.setInt(5, result);
				ps.setDouble(6, values.get(i));

				ps.addBatch();
				if (batchCount == Loader.BATCHINSERT_SIZE) {
					ps.executeBatch();
					batchCount = 0;
				} else
					batchCount++;
			}

			ps.executeBatch();
			result++;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
	}




	private String dateTime(int[] minimumDate) {
		int size = minimumDate.length;
		
		if(size == 5) return String.format("\"DateTime\":\"%s-%s-%s %s:%s:0\"",minimumDate[0], minimumDate[1], minimumDate[2], minimumDate[3], minimumDate[4]) ;
		if(size == 4) return String.format("\"DateTime\":\"%s-%s-%s %s:0:0\"",minimumDate[0], minimumDate[1], minimumDate[2], minimumDate[3]);
		if(size == 3) return String.format("\"DateTime\":\"%s-%s-%s\"",minimumDate[0], minimumDate[1], minimumDate[2]);
		if(size == 2) {
			return String.format("\"DateTime\":\"%s-%s-0\"",minimumDate[0], minimumDate[1]);
		}
		if(size == 1) {
			return String.format("\"DateTime\":\"%s-0-0\"",minimumDate[0]);
		}

		return "";
	}
}
