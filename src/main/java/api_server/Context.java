package api_server;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Polygon;

import core.load_data.Loader;
import core.shared.Table;
import core.time_series.TimeSeriesManager;

public class Context implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String timeGranularity;
	
	private int toGridSize;
	
	private Table tableToStore;
	
	private String fromTable;
	
	private Loader loader;
	
	private TimeSeriesManager timeManager;
	
	private boolean isRestricted;
	
	private Polygon geometryRestriction;
	
	private Map<String, Object> regionsComputed;
	
	public void setTimeManager(TimeSeriesManager timeManager) {
		this.timeManager = timeManager;
	}

	public boolean isRegionComputed(String tableName) {
		return regionsComputed.containsKey(tableName);
	}

	public void setRegionComputed(String tableName) {
		regionsComputed.put(tableName, "0");
	}
	
	
	public void clearRegionsComputed() {
		regionsComputed.clear();
	}

	public Context(String timeGranularity, int toGridSize, Table tableToStore, String fromTable ) {
		this.timeGranularity = timeGranularity;
		this.toGridSize = toGridSize;
		this.tableToStore = tableToStore;
		this.fromTable = fromTable;
		this.loader = new Loader(tableToStore);
		this.timeManager = new TimeSeriesManager(timeGranularity, loader);
		
		this.isRestricted = false;
		this.geometryRestriction = null;
		this.regionsComputed = new HashMap<String, Object>();
	}
	
	public Context() {
		this.timeGranularity = "year";
//		this.toGridSize = toGridSize;
//		this.fromTable = fromTable;
		this.loader = new Loader();
//		this.timeManager = new TimeSeriesManager(timeGranularity, loader);
		
		this.isRestricted = false;
		this.geometryRestriction = null;
		this.regionsComputed = new HashMap<String, Object>();
	}
	
	public boolean isRestricted() {
		return isRestricted;
	}

	public void setRestricted(boolean isRestricted) {
		this.isRestricted = isRestricted;
	}

	public Polygon getGeometryRestriction() {
		return geometryRestriction;
	}

	public void setGeometryRestriction(Polygon geometryRestriction) {
		this.geometryRestriction = geometryRestriction;
	}

	public void updateTimeManager() {
		this.timeManager = new TimeSeriesManager(timeGranularity, this.loader);
	}
	
	public String getTimeGranularity() {
		return timeGranularity;
	}

	public int getToGridSize() {
		return toGridSize;
	}

	public Table getTableToStore() {
		return tableToStore;
	}

	public String getFromTable() {
		return fromTable;
	}

	public Loader getLoader() {
		return loader;
	}

	public TimeSeriesManager getTimeManager() {
		return timeManager;
	}
	
	public String getTimeSeriesTableName() {
		return "timeseries" + fromTable + timeGranularity;
	}
	
	public String getTimeSeriesMetaTableName() {
		return "timeseries" + fromTable + "meta";
	}
	
	public String getUpTableName() {
		return fromTable + "_up";
	}
	
	public void setTimeGranularity(String timeGranularity) {
		this.timeGranularity = timeGranularity;
	}

	public void setTableToRead(Table tableToStore) {
		this.tableToStore = tableToStore;
		this.loader = new Loader(tableToStore);
	}

	public void setFromTable(String fromTable) {
		this.fromTable = fromTable;
	}

	public void setLoader(Loader loader) {
		this.loader = loader;
	}

	

}
