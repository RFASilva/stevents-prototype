package core.time_series;

import java.util.Map;

import core.shared.Entity;


public interface ITime {

	public void fillTimeSeries(String sql, String timeGranularity, Entity granule,
			TimeSeriesManager timeManager, Map<String, Object> params);
}
