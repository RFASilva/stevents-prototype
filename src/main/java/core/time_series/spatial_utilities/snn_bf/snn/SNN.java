package core.time_series.spatial_utilities.snn_bf.snn;

import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.util.Pair;

public interface SNN {
	
	public Map<Long, SNNObject> getClusters();
	public Set<Long> getClustersIds();
	public long getNumNoise();
	public void run(Map<Long, Set<Pair<Long,Double>>> kNeighbours,
			int k, int eps, int minPts);
}
