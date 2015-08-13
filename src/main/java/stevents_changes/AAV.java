package stevents_changes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import stevents_changes.decay_functions.IDecayFunction;
import stevents_changes.decay_functions.LinearDecay;
import stevents_changes.decay_functions.SinWeightDecay;
import stevents_changes.decay_functions.SmoothCompactDecay;
import core.Config;
import core.load_data.DataStoreInfo;
import core.load_data.Functions;
import core.load_data.Loader;
import core.shared.Column;
import core.shared.Table;
import core.time_series.TimeSeriesManager;

/*
 * Atenuation Accumulation Values
 */
public class AAV {

	
	public Loader loader;
	public TimeSeriesManager timeInformation;
	public IDecayFunction f; // Function that will be used to atenuate values throughout the time.

	public Map<Integer, AAValue> aavalues;

	public Connection connection;
	public Table table;
	public String insertStatement;
	
	/*
	 * Table is already at some spatial granularity, and we can vary the temporal granularity. 
	 */
	AAV(String tableName, String timeGranularity) {
		Table tableToRead= new Table(tableName, "pk_id");
		tableToRead.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToRead.add(new Column("geometry", false, false, "GEOMETRY"));
		tableToRead.add(new Column("minute", false, false, "NUMERIC"));
		tableToRead.add(new Column("hour", false, false, "NUMERIC"));
		tableToRead.add(new Column("day", false, false, "NUMERIC"));
		tableToRead.add(new Column("month", false, false, "NUMERIC"));
		tableToRead.add(new Column("year", false, false, "NUMERIC"));
		tableToRead.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToRead.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToRead.add(new Column("date", false, false, "TIMESTAMP WITHOUT TIME ZONE"));

		this.loader = new Loader(tableToRead);
		this.timeInformation = new TimeSeriesManager(timeGranularity, loader);
		this.f = new SinWeightDecay(); // In this moment, we will use the linear decay which takes 15 temporal granules to reach the 
		// the zero factor value.

		aavalues = new HashMap<Integer, AAValue>();
		this.connection = DataStoreInfo.getMetaStore();

		buildTableToStore(tableName, timeGranularity);
		this.insertStatement = table.insertStatement();

	}

	private void buildTableToStore(String tableName, String timeGranularity) {
		Table tableToStore = new Table(tableName + "aac", "pk_id");

		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));

		for (int i = 0; i < timeInformation.pos+1; i++) {
			tableToStore.add(new Column(timeInformation.TIME_GRANULARITY[i], false, false, "NUMERIC"));
		}

		tableToStore.add(new Column("value", false, false, "NUMERIC"));

		tableToStore.createTable(this.connection);
		this.table = tableToStore;
	}

	public void buildAAV() {
		
		int[] nextTime = new int[timeInformation.pos + 1];
		for(int i= 0; i < timeInformation.pos + 1; i++)
			nextTime[i] = timeInformation.minDate[i];
		
		String query = "";

		try {
			Connection connection = DataStoreInfo.getMetaStore();
			do {
				nextTime = timeInformation.next(nextTime);
				query = generateTemplateQuery(nextTime);
				Statement st = connection.createStatement();
				st.setFetchSize(Manager.FETCH_SIZE);
				ResultSet resultSet = st.executeQuery(query);

				updateData(resultSet, nextTime);

				pushDataDB(nextTime); //TODO: After the appropriate updates data is stored back to the database

			} while(Functions.areEqual(nextTime, timeInformation.maxDate)==false);

			connection.close();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void pushDataDB(int[] nextTime) {

		int batchCount = 0;
		PreparedStatement ps;
		try {
//			System.out.println(this.insertStatement);
			ps = connection.prepareStatement(this.insertStatement);
			Iterator<Integer> it = aavalues.keySet().iterator();

			while(it.hasNext()) {
				Integer next = it.next();

				ps.setInt(1, next);
				
				for(int i = 0; i < nextTime.length; i++)
					ps.setInt(i+2, nextTime[i]);
				ps.setDouble(nextTime.length + 2, aavalues.get(next).getValue());
				
				ps.addBatch();
				if (batchCount == Loader.BATCHINSERT_SIZE) {
					ps.executeBatch();
					batchCount = 0;
				} else
					batchCount++;
			}
			ps.executeBatch();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void updateData(ResultSet resultSet, int[] date) throws Exception {

		Map<Integer, Object> temp = new HashMap<Integer, Object>();

		//Update spatial granules in which at this time granule occurred events on them
		while (resultSet.next()) {
			int idSpatialGranule = resultSet.getInt(1);

			//TODO: Change such computation in the SQL generation or something else
			int intensity = resultSet.getInt(2); // Assumption: each event has an equal intensity with value 1;
			if(aavalues.get(idSpatialGranule)==null)
				aavalues.put(new Integer(idSpatialGranule), new AAValue((double) intensity));
			else {
//				aavalues.get(idSpatialGranule).incTimeSpent(f);
				aavalues.get(idSpatialGranule).sumValue(intensity);
				aavalues.get(idSpatialGranule).resetTimeSpent();
			}

			temp.put(idSpatialGranule, null);
		}

		//Update spatial granules in which at this time granule there are no events on them
		Iterator<Integer> it = aavalues.keySet().iterator();
		while(it.hasNext()) {
			Integer next = it.next();

			if(!temp.containsKey(next))
				aavalues.get(next).incTimeSpent(this.f);
		}

	}

	private String generateTemplateQuery(int[] time) {
		String result = "SELECT up_geo_hash, count(*) FROM " + loader.tableToStore.getName() + " WHERE ";

		int pos = timeInformation.pos + 1;
		String temp = "";
		for (int i = 0; i < pos; i++) {
			if (i == pos-1) 
				result += timeInformation.TIME_GRANULARITY[i].toLowerCase() + "=" + time[i] + " ";
			else 
				result += timeInformation.TIME_GRANULARITY[i].toLowerCase() + "=" + time[i] + " and ";
			temp += timeInformation.TIME_GRANULARITY[i] + ",";
		}

		result+= "GROUP BY " + temp + "up_geo_hash";
		return result;
	}

	public void printArray(int[] array) {
		for (int i = 0; i < array.length; i++) {
			System.out.print(array[i] + " - ");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		AAV aav = new AAV("fires_portugal_up256", "day");
		aav.buildAAV();
	}
}
