package core.shared;

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

import core.time_series.TimeseriesType;
import edu.hawaii.jmotif.datatype.Timeseries;

public class SpatialGranule extends Entity {

	private static final long serialVersionUID = 1L;
	
	//Maybe i will need this for others indicators
	private int precision;
	
	// We can have several timeseries for each spatial granule
	private Map<TimeseriesType, Timeseries> timeseries;
	
	// Central point of the spatial granules
	private Geometry spatialObject;
	
	public SpatialGranule() {
		this.timeseries = new HashMap<TimeseriesType, Timeseries>();
	}
	
	public SpatialGranule(Geometry spatialObject) {
		this.timeseries = new HashMap<TimeseriesType, Timeseries>();
		this.spatialObject = spatialObject;
	}
	
	public void addTimeSeries(TimeseriesType type, Timeseries t) {
		timeseries.put(type,t);
	}
	
	public void addNTimeSeries(Map<TimeseriesType, Timeseries> series) {
		timeseries.putAll(series);
	}
	
	public Timeseries getTimeSerieByType(TimeseriesType type) {
		return timeseries.get(type);
	}

	public Map<TimeseriesType, Timeseries> getTimeseries() {
		return timeseries;
	}

}
