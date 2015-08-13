package core.time_series.spatial_utilities.snn_bf.file;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.snn.SNNObject;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

public interface Output<P> {
	
	public void setIterator(Iterator<P> it);
	
	public void setClusters(Map<Long, SNNObject> clusters);
	
	public void setClustersIds(Set<Long> clustersIds);
	
	public void output(String outputName, boolean resToPlot) 
			throws IOException, WekaPlotException;
}
