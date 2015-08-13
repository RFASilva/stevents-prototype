package core.time_series.spatial_utilities.snn_bf.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.snn.SNNObject;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

public interface DBOutput {
	
	public void setClusters(Map<Long, SNNObject> clusters);
	
	public void setClustersIds(Set<Long> clustersIds);
	
	public void output(String outputName, boolean resToPlot) 
			throws SQLException, IOException, WekaPlotException, 
			DBConnectionException;
}
