package core.time_series.spatial_utilities.snn_bf.mspace;

import java.util.Iterator;
import java.util.Map.Entry;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Point;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.RangeSearchTree;

public class KdTreeIterator<Coord extends Number & Comparable<? super Coord>,
P extends Point<Coord>> implements Iterator<P> {

	private Iterator<Entry<P, Long>> currentIterator;
	
	public KdTreeIterator(
			RangeSearchTree<Coord, P, Long> tree){
		currentIterator = tree.iterator(null, null);
	}
	
	@Override
	public boolean hasNext() {
		return currentIterator.hasNext(); 
	}

	@Override
	public P next() {
		return currentIterator.next().getKey();
	}

	@Override
	public void remove() {
		currentIterator.remove();
	}

}
