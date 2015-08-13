package core.time_series;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

import core.load_data.DataStoreInfo;
import core.load_data.Functions;
import core.load_data.Loader;
import core.shared.Table;

public class TimeSeriesManager {

	public static final String[] TIME_GRANULARITY = { "YEAR", "MONTH", "DAY", "HOUR", "MINUTE"};

	private static int minYear;

	private static int maxYear;

	public static String[] minYearMonthDay;

	public static String[] minHourMinSec;

	public static String[] maxYearMonthDay;

	public static String[] maxHourMinSec;
	
	public static int[] maxDate;
	
	public static int[] minDate;

	private String timeGranularty;

	public static int pos;

	private Loader loader;

	public static Table tableToStore;

	public static Table tableToStoreMeta;

	public void setTableToStore(Table tableToStore) {
		this.tableToStore = tableToStore;
	}

	public void setTableToStoreMeta(Table tableToStoreMeta) {
		this.tableToStoreMeta = tableToStoreMeta;
	}

	// Hold the length for each timeseries that will be created
	private long lengthTime;

	public TimeSeriesManager(String timeGranularity, Loader loader) {
		this.timeGranularty = timeGranularity;
		this.pos = findGranularityPos(timeGranularity);
		findMinMaxDate();
		lenghtTimeSeries(); //TODO
	}

	public String getTimeGranularty() {
		return timeGranularty;
	}

	public void findMinMaxDate() {
		String sql = "select min(year), max(year) from " +  loader.tableToStore.getName();

		Connection connection = DataStoreInfo.getMetaStore();
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			ResultSet resultSet = st.executeQuery(sql);
			resultSet.next();

			this.minYear = resultSet.getInt(1);
			this.maxYear = resultSet.getInt(2);


		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private long lenghtTimeSeries() {
		long result = 0;
		String sql = "select max(date), min(date) from " +  loader.tableToStore.getName();

		Connection connection = DataStoreInfo.getMetaStore();
		Statement st;
		try {
			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

			ResultSet resultSet = st.executeQuery(sql);
			resultSet.next();

			String maxDate = resultSet.getString(1);

			String[] splittedMax = maxDate.split(" ");
			String[] yearMonthDayMax = splittedMax[0].split("-");
			String[] hourMinSecMax = splittedMax[1].split(":");

			String minDate = resultSet.getString(2);

			String[] splittedMin = minDate.split(" ");
			String[] yearMonthDayMin = splittedMin[0].split("-");
			String[] hourMinSecMin = splittedMin[1].split(":");

			this.maxYearMonthDay = yearMonthDayMax;
			this.maxHourMinSec = hourMinSecMax;
			this.minYearMonthDay = yearMonthDayMin;
			this.minHourMinSec = hourMinSecMin;

			int[] minVDate = new int[pos + 1];
			int[] maxVDate = new int[pos + 1];

			//minDate
			minVDate[0] = this.minYear;
			for (int i = 1; i < pos + 1; i++) {
				if(i < 3)
					minVDate[i] = Integer.parseInt(TimeSeriesManager.minYearMonthDay[i]);
				if(i >= 3 && i < 6)
					minVDate[i] = Integer.parseInt(TimeSeriesManager.minHourMinSec[i-3]);
			}
			this.minDate = minVDate;
			
			//maxDate
			maxVDate[0] = this.maxYear;
			for (int i = 1; i < pos + 1; i++) {
				if(i < 3)
					maxVDate[i] = Integer.parseInt(TimeSeriesManager.maxYearMonthDay[i]);
				if(i >= 3 && i < 6)
					maxVDate[i] = Integer.parseInt(TimeSeriesManager.maxHourMinSec[i-3]);
			}

			this.maxDate = maxVDate;
//			System.out.println(minVDate[0] + "-" + minVDate[1] + "-" + minVDate[2] + "- " +  minVDate[3] + "-");
//			System.out.println(maxVDate[0] + "-" + maxVDate[1] + "-" + maxVDate[2] + "- " +  maxVDate[3] + "-");
			
			while(Functions.areEqual(minVDate, maxVDate) == false) {
				lengthTime++;
				minVDate = this.next(minVDate).clone();
			}
			lengthTime++;

		} catch (SQLException e) {

		}

		System.out.println("delta time: " + lengthTime);
		return lengthTime;
	}

	public long getLengthTime() {
		return lengthTime;
	}

	public static int dayMonth(int month, int year) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12 )
			return 31;
		else if (month == 4 || month == 6 || month == 9 || month == 11)
			return 30;
		else {
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				return 29;
			else
				return 28;
		}
	}

	public static int findPosInTimeSerie(int minute, int hour, int day, int month, int year) {

		int result = 0;
		if(pos == 0)
			result = (year - minYear) + 1;
		else {
			for(int i = minYear;  i < year; i++) {
				if (pos == 1) result += 12;
				if (pos == 2) result += numberDays(i);
				if (pos == 3) result += numberHours(i);
				if (pos == 4) result += numberMinutes(i);
			}

			if (pos == 1) result += month;
			if (pos == 2) result += dayInYear(day, month, year);
			if (pos == 3) result += hourInYear(hour, day, month, year);
			if (pos == 4) result += minuteInYear(minute, hour, day, month, year);
		}
		return result;
	}

	public static int dayInYear(int day, int month, int year) 	{
		int ordem = 0;
		int i;

		for( i = 1; i<month; i++) {
			ordem = ordem + dayMonth(i,year);
		}

		ordem = ordem + day;
		return ordem;
	}

	public static int hourInYear(int hour, int day, int month, int year) 	{
		int temp = dayInYear(day, month, year);
		return (temp - 1)*24 + hour;
	}

	public static int minuteInYear(int minute, int hour,int day, int month, int year) 	{
		int temp = dayInYear(day, month, year);
		return (temp - 1)*24*60 + (hour*60) + 60;
	}

	public static int numberDays(int year) {
		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
			return 366;
		else
			return 365;
	}

	public static int numberHours(int year) {
		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
			return 366*24;
		else
			return 365*24;
	}

	public static int numberMinutes(int year) {
		if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
			return 366*24*60;
		else
			return 365*24*60;
	}

	public static int getMinYear() {
		return minYear;
	}

	public static int getMaxYear() {
		return maxYear;
	}

	public static int findGranularityPos(String timeGranularity) {
		timeGranularity = timeGranularity.toUpperCase();

		for(int i = 0; i < TIME_GRANULARITY.length; i++)
			if(timeGranularity.equals(TIME_GRANULARITY[i]))
				return i;

		return -1;
	}

	//deprecated
	public String[] generatesDateTimes() {
		String[] result = new String[(int)lengthTime];

		String minute = "\"DateTime\":\"%s-%s-%sT%s:%s\"";
		String hour = "\"DateTime\":\"%s-%s-%sT%s\"";
		String day = "\"DateTime\":\"%s-%s-%s\"";
		String month = "\"DateTime\":\"%s-%s\"";
		String year = "\"DateTime\":\"%s\"";

		int index = 0;
		//For year granularity
		if(pos == 0) {
			for(int i = minYear; i <= maxYear; i++) {
				result[index] = String.format(year, i);
				index++;
			}
		}
		String value;
		//For month granularity
		if(pos == 1) {
			for(int i = minYear; i <= maxYear; i++) {
				for(int j = 1; j <= 12; j++) {
					if(j < 10)
						value = "0" + j;
					else value = j + "";
					result[index] = String.format(month, i, value);
					index++;
				}
			}
		}
		String valueM;
		String valueD;
		//For day granularity
		if(pos == 2) {
			for(int i = minYear; i <= maxYear; i++) 
				for(int j= 1; j <= 12; j++) 
					for (int d = 1; d <= dayMonth(j, i); d++) {

						if(j < 10)
							valueM = "0" + j;
						else valueM = j + "";

						if(d < 10)
							valueD = "0" + d;
						else valueD = d + "";

						result[index] = String.format(day, i, valueM, valueD);
						index++;
					}
		}

		//For hour granularity
		if(pos == 3) {
			for(int i = minYear; i <= maxYear; i++) 
				for(int j= 1; j <= 12; j++) 
					for (int d = 1; d <= dayMonth(j, i); d++) 
						for(int h = 0; h <=23; h++) {
							result[index] = String.format(hour, i, j, d, h);
							index++;
						}
		}

		//For minute granularity
		if(pos == 3) {
			for(int i = minYear; i <= maxYear; i++) 
				for(int j= 1; j <= 12; j++) 
					for (int d = 1; d <= dayMonth(j, i); d++) 
						for(int h = 0; h <=23; h++)
							for(int m= 0; m <= 59; m++) {
								result[index] = String.format(minute, i, j, d, h, m);
								index++;
							}
		}

		return result;
	}

	public List<String> generatesDateTimesTest() {
		List<String> result = new LinkedList<String>();

		String minute = "\"DateTime\":\"%s-%s-%sT%s:%s\"";
		String hour = "\"DateTime\":\"%s-%s-%sT%s\"";
		String day = "\"DateTime\":\"%s-%s-%s\"";
		String month = "\"DateTime\":\"%s-%s\"";
		String year = "\"DateTime\":\"%s\"";

		int index = 0;
		//For year granularity
		if(pos == 0) {
			for(int i = minYear; i <= maxYear; i++) {
				result.add(String.format(year, i));
				index++;
			}
		}
		String value;
		//For month granularity
		if(pos == 1) {
			for(int i = minYear; i <= maxYear; i++) {
				for(int j = 1; j <= 12; j++) {
					if(j < 10)
						value = "0" + j;
					else value = j + "";
					result.add(String.format(month, i, value));
					index++;
				}
			}
		}
		String valueM;
		String valueD;
		//For day granularity
		if(pos == 2) {
			for(int i = minYear; i <= maxYear; i++) 
				for(int j= 1; j <= 12; j++) 
					for (int d = 1; d <= dayMonth(j, i); d++) {

						if(j < 10)
							valueM = "0" + j;
						else valueM = j + "";

						if(d < 10)
							valueD = "0" + d;
						else valueD = d + "";

						result.add(String.format(day, i, valueM, valueD));
						index++;
					}
		}

		//For hour granularity
		if(pos == 3) {
			for(int i = minYear; i <= maxYear; i++) 
				for(int j= 1; j <= 12; j++) 
					for (int d = 1; d <= dayMonth(j, i); d++) 
						for(int h = 0; h <=23; h++) {
							result.add(String.format(hour, i, j, d, h));
							index++;
						}
		}

		//For minute granularity
		if(pos == 3) {
			for(int i = minYear; i <= maxYear; i++) 
				for(int j= 1; j <= 12; j++) 
					for (int d = 1; d <= dayMonth(j, i); d++) 
						for(int h = 0; h <=23; h++)
							for(int m= 0; m <= 59; m++) {
								result.add(String.format(minute, i, j, d, h, m));
								index++;
							}
		}

		return result;
	}

	public static int[] next(int[] timestamp) {
		int size = timestamp.length;
		int[] result = new int[size];
		Calendar calendar;

		if(size == 1) {
			//			System.out.println("add year");
			calendar =  new GregorianCalendar(timestamp[0], 1, 1);
			calendar.add(Calendar.YEAR, 1);
			result[0] = calendar.get(Calendar.YEAR);
		}
		if(size == 2) {
			//			System.out.println("add month");
			calendar = new GregorianCalendar(timestamp[0], timestamp[1]-1, 1);
			calendar.add(Calendar.MONTH, 1);
			result[0] = calendar.get(Calendar.YEAR);
			result[1] = calendar.get(Calendar.MONTH) + 1;
		}
		if(size == 3) {
			//			System.out.println("add day");
			calendar = new GregorianCalendar(timestamp[0], timestamp[1]-1, timestamp[2]);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			result[0] = calendar.get(Calendar.YEAR);
			result[1] = calendar.get(Calendar.MONTH) + 1;
			result[2] = calendar.get(Calendar.DATE);
		}
		if(size == 4) {
			//			System.out.println("add hour");
			calendar = new GregorianCalendar(timestamp[0], timestamp[1]-1, timestamp[2],timestamp[3], 0 );
			calendar.add(Calendar.HOUR_OF_DAY, 1);
			result[0] = calendar.get(Calendar.YEAR);
			result[1] = calendar.get(Calendar.MONTH) + 1;
			result[2] = calendar.get(Calendar.DATE);
			result[3] = calendar.get(Calendar.HOUR_OF_DAY);
		}
		if(size == 5) {
			//			System.out.println("add hour");
			calendar = new GregorianCalendar(timestamp[0], timestamp[1]-1, timestamp[2],timestamp[3], timestamp[4]);
			calendar.add(Calendar.MINUTE, 1);
			result[0] = calendar.get(Calendar.YEAR);
			result[1] = calendar.get(Calendar.MONTH) + 1;
			result[2] = calendar.get(Calendar.DATE);
			result[3] = calendar.get(Calendar.HOUR_OF_DAY);
			result[4] = calendar.get(Calendar.MINUTE);
		}
		return result;
	}

	
	public static boolean greater(int[] date1, int[] date2) {
		
		int n = date1.length;
		
		for(int i = 0; i < n; i++) {
			if(date1[i] > date2[i])
				return true;
		}
		
		return false;
	}
	
	public static int[] castToInt(String[] array) {
		int[] result = new int[array.length];
		
		for (int i = 0; i < array.length; i++) 
			result[i] = Integer.parseInt(array[i]);
		
		return result;
	}
	
	public static void main(final String[] args) {


		//Mini Testes 
		String template = "\"DateTime\":\"%s-%s-%s\"";

		int[] date1 = new int[3];
		date1[0] = 2006;
		date1[1] = 12;
		date1[2] = 31;
		//		date1[3] = 23;
		//		date1[4] = 59;

		System.out.println(String.format(template, date1[0], date1[1],date1[2] ));


		//		int[] date2 = new int[5];
		//		date2[0] = 2006;
		//		date2[1] = 12;
		//		date2[2] = 31;
		//		date2[3] = 23;
		//		date2[4] = 59;
		//		
		//		System.out.println(date1.equals(date2));

		//		
		//		int[] bla = TimeSeriesManager.next(date);
		//		System.out.println(bla[0] + "-" + bla[1] + "-" + bla[2] + "-" +  bla[3] + "-" + bla[4]);
		//		System.out.println(bla[0] + "-" + bla[1]);
	}
}
