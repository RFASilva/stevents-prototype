import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import core.Config;
import core.load_data.DataStoreInfo;
import core.load_data.Functions;
import core.load_data.Loader;
import core.shared.Column;
import core.shared.Table;
import core.time_series.TimeSeriesManager;


public class BuildTimeSeriesUROP {

	private static final long BATCHINSERT_SIZE = Config.getConfigInt("insert_chunk_size");

	private static String insertStatement;

	public static void createTable() {

		Table urop = new Table("timeseriesurop", "pk_id");

		urop.add(new Column("pk_id", false, false, "NUMERIC"));
		urop.add(new Column("name", false, false, "TEXT"));
		urop.add(new Column("date", false, false, "TIMESTAMP WITHOUT TIME ZONE"));
		urop.add(new Column("pos", false, false, "NUMERIC"));
		urop.add(new Column("value", false, false, "NUMERIC"));
		urop.add(new Column("keywords", false, false, "TEXT"));
		urop.add(new Column("region", false, false, "TEXT"));
		urop.add(new Column("granularity", false, false, "TEXT"));
		//
//						Connection connection = DataStoreInfo.getMetaStore();
//						urop.createTable(connection);

		insertStatement = urop.insertStatement();
		System.out.println(insertStatement);
	}

	public static void createTimeSeriesHour() {
		Connection connection = DataStoreInfo.getMetaStore();
		int posTimeSeries = 1;

		String name = "a";
		String granularity = "hour";
		String region = "Portugal";

		String sql = "select year, month, day, hour, sum(fgraves) as FG, sum(fleves) as FL, sum(mortos) as VM, count(*) as A "
				+ "from accidents_portugal group by year, month, day, hour order by year, month, day, hour";


		int[] previousDate = null;
		try {
			//Makes the query for data
			Statement st = connection.createStatement();
			st.setFetchSize(Loader.FETCH_SIZE);

			PreparedStatement ps = connection.prepareStatement(insertStatement);;
			int[] temp = null;
			ResultSet data = st.executeQuery(sql);
			int pos = 1;
			int batchCount = 0;
			while(data.next()) {

				int year = data.getInt(1);
				int month = data.getInt(2);
				int day = data.getInt(3);
				int hour = data.getInt(4);

				int[] date = new int[4];
				date[0] = year; date[1] = month; date[2] = day; date[3] = hour;

				if(data.isLast())
					System.out.println("ultimo");


				if(!data.isFirst() && !data.isLast()) {


					temp = TimeSeriesManager.next(previousDate).clone();

					while(Functions.areEqual(temp, date)==false) {
						//						System.out.println(temp[0] + " - " + temp[1] + " - " + temp[2] + " - " + temp[3]);
						//						System.out.println(date[0] + " - " + date[1] + " - " + date[2] + " - " + date[3]);
						ps.setString(1, name);
						String dateFormated = temp[0] + "-" + temp[1] + "-" + temp[2] + " " + temp[3] + ":" + "00" + ":" + "00";
						ps.setString(2, dateFormated);
						ps.setInt(3, pos);
						ps.setInt(4, 0);

						ps.setString(5, "portugal");
						ps.setString(6, region);
						ps.setString(7, granularity);

						pos++;
						ps.addBatch();

						if (batchCount == Loader.BATCHINSERT_SIZE) {
							ps.executeBatch();
							batchCount = 0;
						} 
						else batchCount++;

						temp = TimeSeriesManager.next(temp).clone();
						if(TimeSeriesManager.greater(temp, date))
							break;

					}
				}


				int fg = data.getInt(5);
				int fl = data.getInt(6);
				int m = data.getInt(7);
				int a = data.getInt(8);

				String dateFormated = year + "-" + month + "-" + day + " " + hour + ":" + "00" + ":" + "00";

				ps.setString(1, name);
				ps.setString(2, dateFormated);
				ps.setInt(3, pos);
				ps.setInt(4, a);

				ps.setString(5, "portugal");
				ps.setString(6, region);
				ps.setString(7, granularity);

				ps.addBatch();
				pos++;

				if (batchCount == Loader.BATCHINSERT_SIZE) {
					ps.executeBatch();
					batchCount = 0;
				} 
				else
					batchCount++;


				previousDate = date;
			}

			if (batchCount > 0) {
				ps.executeBatch();
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}






	public static void createTimeSeriesDay() {
		Connection connection = DataStoreInfo.getMetaStore();
	
		String[] names = {"fg", "fl", "m", "a"};
		for(int i=0 ; i< 4; i++) {

			String name = names[i];
			String granularity = "day";
			String region = "Braga";

			String sql = "select year, month, day, sum(fgraves) as FG, sum(fleves) as FL, sum(mortos) as VM, count(*) as A "
					+ "from accidents_portugal where distrito='"+ region + "' group by year, month, day order by year, month, day";
			
//			String sql = "select year, month, day, sum(fgraves) as FG, sum(fleves) as FL, sum(mortos) as VM, count(*) as A "
//					+ "from accidents_portugal where distrito='"+ region + "' group by year, month, day order by year";

			System.out.println(sql);

			int[] previousDate = null;
			try {
				//Makes the query for data
				Statement st = connection.createStatement();
				st.setFetchSize(Loader.FETCH_SIZE);

				PreparedStatement ps = connection.prepareStatement(insertStatement);;
				int[] temp = null;
				ResultSet data = st.executeQuery(sql);
				int pos = 1;
				int batchCount = 0;
				while(data.next()) {
					//				System.out.println("insertinhg.");
					int year = data.getInt(1);
					int month = data.getInt(2);
					int day = data.getInt(3);

					int[] date = new int[3];
					date[0] = year; date[1] = month; date[2] = day;

					if(data.isLast())
						System.out.println("ultimo");


					if(!data.isFirst() && !data.isLast()) {


						temp = TimeSeriesManager.next(previousDate).clone();

						while(Functions.areEqual(temp, date)==false) {
							//						System.out.println(temp[0] + " - " + temp[1] + " - " + temp[2] + " - " + temp[3]);
							//						System.out.println(date[0] + " - " + date[1] + " - " + date[2] + " - " + date[3]);
							ps.setString(1, name);
//							String dateFormated = temp[0] + "-" + temp[1] + "-" + temp[2] + " " + "00" + ":" + "00" + ":" + "00";
							
							String dateFormated = temp[0] + "-" + "00" + "-" + "00" + " " + "00" + ":" + "00" + ":" + "00";
							
							ps.setString(2, dateFormated);
							ps.setInt(3, pos);
							ps.setInt(4, 0);

							ps.setString(5, region);
							ps.setString(6, region);
							ps.setString(7, granularity);

							pos++;
							ps.addBatch();

							if (batchCount == Loader.BATCHINSERT_SIZE) {
								ps.executeBatch();
								batchCount = 0;
							} 
							else batchCount++;

							temp = TimeSeriesManager.next(temp).clone();
							if(TimeSeriesManager.greater(temp, date))
								break;

						}
					}


					int fg = data.getInt(4);
					int fl = data.getInt(5);
					int m = data.getInt(6);
					int a = data.getInt(7);

//					String dateFormated = year + "-" + month + "-" + day + " " + "00" + ":" + "00" + ":" + "00";
					String dateFormated = year + "-" + "00" + "-" + "00" + " " + "00" + ":" + "00" + ":" + "00";
					
					ps.setString(1, name);
					ps.setString(2, dateFormated);
					ps.setInt(3, pos);
					ps.setInt(4, data.getInt(i + 4));

					ps.setString(5, region);
					ps.setString(6, region);
					ps.setString(7, granularity);

					ps.addBatch();
					pos++;

					if (batchCount == Loader.BATCHINSERT_SIZE) {
						ps.executeBatch();
						batchCount = 0;
					} 
					else
						batchCount++;


					previousDate = date;
				}

				if (batchCount > 0) {
					ps.executeBatch();
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

		
	
	public static void main(String[] args) {

		BuildTimeSeriesUROP.createTable();
		BuildTimeSeriesUROP.createTimeSeriesDay();
	}




}
