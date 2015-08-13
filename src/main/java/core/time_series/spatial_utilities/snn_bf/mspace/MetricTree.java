package core.time_series.spatial_utilities.snn_bf.mspace;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.util.Pair;

public interface MetricTree<P> {

	public Long put(P point);
	
	public Iterator<P> iterator();
	
	public Set<Pair<Long,Double>> getKNN(P point, int k);
	
	public Map<Long,Set<Pair<Long,Double>>> getKNNs(int k);
}
