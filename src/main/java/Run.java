import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;

import core.load_data.Loader;
import core.shared.Column;
import core.shared.Table;
import core.time_series.TimeSeriesManager;


public class Run {

	private String timeGranularity;

	private int toGridSize;

	private Table tableToStore;

	private String fromTable;

	private Loader loader;

	private TimeSeriesManager timeManager;

	public Run(String timeGranularity, int toGridSize, String fromTable ) {
		this.timeGranularity = timeGranularity;
		this.toGridSize = toGridSize;
		this.fromTable = fromTable;
	}

	// This function makes the up for data allowing to further
	// create the time series for each spatial granule.
	public void upAccidentsUSAData() {
		this.fromTable = "accidents_usa";
		String sql = "SELECT minute, hour, day, month, year, latitude, longitud from " + fromTable + 
				" where (latitude>= -90 and latitude <=90) and (longitud>=-180 and longitud<=180)";

		tableToStore = new Table("accidents_usa_up", "pk_id");

		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("minute", false, false, "NUMERIC"));
		tableToStore.add(new Column("hour", false, false, "NUMERIC"));
		tableToStore.add(new Column("day", false, false, "NUMERIC"));
		tableToStore.add(new Column("month", false, false, "NUMERIC"));
		tableToStore.add(new Column("year", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("date", false, false, "TIMESTAMP WITHOUT TIME ZONE"));
		this.loader = new Loader(tableToStore);

		//		loader.uploadData(fromTable, new Long(toGridSize), sql);
	}


	public void upFiresPortugalData() {
		fromTable = "fires_portugal";

		tableToStore= new Table("fires_portugal_up", "pk_id");
		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("minute", false, false, "NUMERIC"));
		tableToStore.add(new Column("hour", false, false, "NUMERIC"));
		tableToStore.add(new Column("day", false, false, "NUMERIC"));
		tableToStore.add(new Column("month", false, false, "NUMERIC"));
		tableToStore.add(new Column("year", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("date", false, false, "TIMESTAMP WITHOUT TIME ZONE"));
		this.loader = new Loader(tableToStore);

		String sql = "select minute, hour, day, month, year, latitude, longitud from " + fromTable +" where tipo='Florestal' and (latitude>= -90 and latitude <=90) and (longitud>=-180 and longitud<=180)";
//		loader.uploadData(fromTable, new Long(toGridSize), sql);
	}


	public void upAccidentsPortugalData() {
		fromTable = "accidents_portugal";

		tableToStore= new Table("accidents_portugal_up", "pk_id");
		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("minute", false, false, "NUMERIC"));
		tableToStore.add(new Column("hour", false, false, "NUMERIC"));
		tableToStore.add(new Column("day", false, false, "NUMERIC"));
		tableToStore.add(new Column("month", false, false, "NUMERIC"));
		tableToStore.add(new Column("year", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("date", false, false, "TIMESTAMP WITHOUT TIME ZONE"));
		this.loader = new Loader(tableToStore);

		String sql = "select minute, hour, day, month, year, latitude, longitud from " + fromTable +" where (latitude>= -90 and latitude <=90) and (longitud>=-180 and longitud<=180)";
		//				loader.uploadData(fromTable, new Long(toGridSize), sql);
	}

	public void createTimeSeries() {
		this.timeManager = new TimeSeriesManager(timeGranularity, loader);

		System.out.println("length: " + timeManager.getLengthTime());

		Table tableToStore = new Table("timeseries" + fromTable + timeGranularity, "pk_id");
		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("type", false, false, "TEXT"));
		tableToStore.add(new Column("datetime", false, false, "TEXT"));
		tableToStore.add(new Column("pos", false, false, "NUMERIC"));
		tableToStore.add(new Column("value", false, false, "NUMERIC"));

		Table tableToStoreMeta = new Table("timeseries" + fromTable + timeGranularity + "meta", "pk_id2");
		tableToStoreMeta.add(new Column("pk_id2", false, false, "NUMERIC"));
		tableToStoreMeta.add(new Column("tableName", false, false, "TEXT"));
		tableToStoreMeta.add(new Column("granularity", false, false, "TEXT"));
		tableToStoreMeta.add(new Column("minValue", false, false, "NUMERIC"));
		tableToStoreMeta.add(new Column("maxValue", false, false, "NUMERIC"));
		tableToStoreMeta.add(new Column("length", false, false, "NUMERIC"));

		this.timeManager.setTableToStore(tableToStore);
		this.timeManager.setTableToStoreMeta(tableToStoreMeta);

		Map<String, Object> params = new HashMap<String, Object>();
		//		params.put("centerPoint", new Coordinate(-99, 39));// USA
		params.put("centerPoint",new Coordinate(-8.0, 39.5));// Portugal

		loader.buildGranulesBD(timeManager, tableToStore, tableToStoreMeta, false, params);

	}


	public void upfiresDataTest() {
		this.fromTable = "fires_portugal";
		String sql = "SELECT minute, hour, day, month, year, latitude, longitud from " + fromTable + 
				" where tipo='Florestal' and (latitude>= -90 and latitude <=90) and (longitud>=-180 and longitud<=180)";

		tableToStore = new Table("fires_portugal_up" + this.toGridSize , "pk_id");

		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("minute", false, false, "NUMERIC"));
		tableToStore.add(new Column("hour", false, false, "NUMERIC"));
		tableToStore.add(new Column("day", false, false, "NUMERIC"));
		tableToStore.add(new Column("month", false, false, "NUMERIC"));
		tableToStore.add(new Column("year", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("date", false, false, "TIMESTAMP WITHOUT TIME ZONE"));
		this.loader = new Loader(tableToStore);

		//		loader.uploadData(fromTable, new Long(toGridSize), sql);
	}

	public static void main(String[] args) {

		Run core = new Run("day", 1, "fires_portugal");
		core.upFiresPortugalData();
		core.createTimeSeries();

//		core = new Run("month", 1, "fires_portugal");
//		core.upFiresPortugalData();
//		core.createTimeSeries();
//
//		core = new Run("day", 1, "fires_portugal");
//		core.upFiresPortugalData();
//		core.createTimeSeries();
		
		
		
		
		//		Run core = new Run("day", 256, "fires_portugal");
		//		core.upfiresDataTest();
		//		
		//		core.createTimeSeries();

		//		Run core = new Run("year", 1, "accidents_portugal");
		//		core.upAccidentsPortugalData();
		//		core.createTimeSeries();
		//
		//		core = new Run("month", 1, "accidents_portugal");
		//		core.upAccidentsPortugalData();
		//		core.createTimeSeries();
		//
		//		core = new Run("day", 1, "accidents_portugal");
		//		core.upAccidentsPortugalData();
		//		core.createTimeSeries();


		//		Run core = new Run("year", 1, "accidents_usa");
		//		core.upAccidentsUSAData();
		//		core.createTimeSeries();
		//		
		//		core = new Run("month", 1, "accidents_usa");
		//		core.upAccidentsUSAData();
		//		core.createTimeSeries();
		//		
		//		
		//		core = new Run("day", 1, "accidents_usa");
		//		core.upAccidentsUSAData();
		//		core.createTimeSeries();

		//		core = new Run("hour", 1, "accidents_usa");
		//		core.upAccidentsUSAData();
		//		core.createTimeSeries();


	


	}

}
