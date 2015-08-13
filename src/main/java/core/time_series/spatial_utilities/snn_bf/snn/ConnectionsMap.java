package core.time_series.spatial_utilities.snn_bf.snn;

import java.util.HashMap;
import java.util.Map;

import core.time_series.spatial_utilities.snn_bf.util.Pair;

public class ConnectionsMap<T> {
	
	private Map<Pair<T,T>,Integer> connections;
	
	public ConnectionsMap(){
		connections = new HashMap<Pair<T,T>, Integer>();
	}
	
	public void clear(){
		connections.clear();
	}
	
	public int get(T point1, T point2) {
		Pair<T,T> pair = new Pair<T,T> (point1,point2);

		if(connections.containsKey(pair))
			return connections.get(pair);
		else {
			Pair<T,T> secondPair = new Pair<T,T> (point2,point1);
			
			if(connections.containsKey(secondPair))
				return connections.get(secondPair);
			else return -1;
		}
	}
	
	public int put(T point1, T point2, int weight) {
		Pair<T, T> pair = new Pair<T, T> (point1,point2);
		connections.put(pair, weight);
		
		return weight;
	}
}
