package core.time_series.implemented;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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

public class NElemsTimeSeries extends TimeSeriesFactory {

	@Override
	public Timeseries buildTimeSeries(String sql, Entity granule) {
		Timeseries result = super.initTime(); 


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

				String[] spatialTypes = (String[]) data.getArray(pos + 3).getArray();

				String wkt_point = data.getString(pos + 4);
				granule.setUpGeometry(new WKTReader(TimeSeriesFactory.geofact).read(wkt_point));

				int value = spatialTypes.length;
				int index = super.findPos(times) - 1;// Timeseries objects starts index at 0 position

				result.elementAt(index).setValue(value);
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
	public Map<TimeseriesType, Timeseries> buildNTimeSeries(String sql,
			Entity granule) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void buildTimeSeriesBD(String sql, Entity granule, Map<String, Object> params) {

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
			List<String> types = new LinkedList<>();
			types.add(TimeseriesType.NrElements.name());

			while(data.next()) {
				int upGeomtryHash = data.getInt(1);
				for(int i= 0; i < pos + 1; i++) {
					actualDate[i] = data.getInt(i + 2);
				}

				String[] spatialTypes = (String[]) data.getArray(pos + 3).getArray();
				String wktUpGeometry = data.getString(pos + 4);

				int actualValue = spatialTypes.length;

				List<Double> values = new LinkedList<>();
				values.add((double)actualValue);
				
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

	@Override
	public void buildNTimeSeriesBD(String sql, Entity granule, Map<String, Object> params) {
	}

}
