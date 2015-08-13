package core.time_series.spatial_utilities.snn_bf.mspace;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Distance;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.KDTree;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.NearestNeighbors;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Point;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.RangeSearchTree;
import core.time_series.spatial_utilities.snn_bf.util.Pair;

public class KdTree<Coord extends Number & Comparable<? super Coord>,
		P extends Point<Coord>> implements MetricTree<P>{

	private RangeSearchTree<Coord, P, Long> tree;
	private Distance<Coord, P> distance;
	
	public KdTree(Distance<Coord, P> distance){
		this.tree = new KDTree<Coord, P, Long>();
		this.distance = distance;
	}
	
	public Long put(P point) {
		return tree.put(point, point.getId());
	}

	public Iterator<P> iterator() {
		return new KdTreeIterator<Coord, P>(tree);
	}

	/**
	 * get -> O(log n) or O(n) for high dimensional data
	 * add elements to set -> O(k * O(1)) = O(k)
	 * final -> O(log n) or O(n)
	 */
	public Set<Pair<Long,Double>> getKNN(P point, int k) {
		Set<Pair<Long,Double>> result = 
				new LinkedHashSet<Pair<Long,Double>>(k);
		
		NearestNeighbors<Coord, P, Long> nn = 
				new NearestNeighbors<Coord,P, Long>(distance);
		
		NearestNeighbors.Entry<Coord, P,Long>[] knn 
			= nn.get((KDTree<Coord, P, Long>) tree, point, k);
		
		for(NearestNeighbors.Entry<Coord,P,Long> neighbour : knn) 
			result.add(new Pair<Long,Double>(
					neighbour.getNeighbor().getKey().getId(),
					neighbour.getDistance()));
		
		return result;
	}
	
	public Map<Long, Set<Pair<Long,Double>>> getKNNs(int k) {
		Map<Long, Set<Pair<Long,Double>>> kNeighbours = 
				new HashMap<Long, Set<Pair<Long,Double>>>();

		Iterator<P> it = this.iterator();

		while(it.hasNext()){
			P current = it.next();
			
			Set<Pair<Long,Double>> knns = this.getKNN(current, k);

			kNeighbours.put(current.getId(), knns);
		}

		return kNeighbours;
	}
}
