package core.time_series.spatial_utilities.snn_bf.snn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import core.time_series.spatial_utilities.snn_bf.util.Pair;

/**
 * Java implementation of the SNN algorithm with a core approach.
 * 
 * @author Bruno Filipe Faustino
 * @since 2012-04-24
 */
public class SNNGraph implements SNN{

	/**
	 * Number of neighbours.
	 */
	private int k;

	/**
	 * Similarity threshold. 
	 */
	private int eps;

	/**
	 * Density threshold.
	 */
	private int minPts;

	/**
	 * Map with the objects id and their respective flags.
	 */
	private Map<Long,SNNObject> clusters;

	/**
	 * Set with the clusters ids.
	 */
	private Set<Long> clustersIds;

	/**
	 * Number of objects defined as noise.
	 */
	private long numNoise;

	/**
	 * Map with the K nearest neighbours of each object.
	 */
	private Map<Long, Set<Pair<Long,Double>>> kNeighbours;

	/**
	 * Map with similarities between objects.
	 */
	private ConnectionsMap<Long> connections;

	/**
	 * Graph with the representation of the clusters.
	 */
	private UndirectedGraph<Long, DefaultEdge> coreClusters;

	/**
	 * Map with unclustered objects after the clustering of non-core objects.
	 */
	private Set<Long> notClustered;

	/**
	 * Constructor of the SNN algorithm.
	 * 
	 * @param tree Metric tree that contains the points to be clustered.
	 * @param k Number of neighbours.
	 * @param eps Similarity threshold.
	 * @param minPts Density threshold.
	 */
	public SNNGraph() {}

	/**
	 * Get method for the clustering results.
	 * 
	 * @return the map with the clustering results.
	 */
	public Map<Long, SNNObject> getClusters() {
		return clusters;
	}

	/**
	 * Get method for the set with the clusters ids.
	 * 
	 * @return the set with the clusters ids.
	 */
	public Set<Long> getClustersIds() {
		return clustersIds;
	}

	public long getNumNoise(){
		return numNoise;
	}

	private void init(){
		this.clusters = new HashMap<Long, SNNObject>();
		for(long i = 0; i < kNeighbours.size(); i++)
			clusters.put(i, new SNNObject());
		numNoise = kNeighbours.size();

		coreClusters = 
				new SimpleGraph<Long, DefaultEdge>(DefaultEdge.class);
		this.clustersIds = new HashSet<Long>();
		this.connections = new ConnectionsMap<Long>();
		this.notClustered = new HashSet<Long>();
	}

	/**
	 * Method that runs the SNN algorithm.
	 * 
	 * Runs all the needed methods to execute the SNN algorithm. It starts by 
	 * getting the K nearest neighbours of each point, if the K of the new run 
	 * is bigger than the K of the previous run. Then, it calculates the 
	 * similarity between connected points, if the K of the previous run is 
	 * equal to the K of the new run. Then, it identifies the noise, clusters
	 * the core points and in the end clusters the non-core and non-noise 
	 * points.
	 * 
	 * @param getKNN If the K of the new run is bigger than the K of the 
	 * previous run.
	 * @param calcSimilarity If the K of the previous run is equal to the K of 
	 * the new run.
	 */
	public void run(Map<Long, Set<Pair<Long,Double>>> kNeighbours, int k, 
			int eps, int minPts){
		this.kNeighbours = kNeighbours;
		this.k = k;
		this.eps = eps;
		this.minPts = minPts;

		init();
		calculateTotalWeight();
		identifyNoise();
		clusterCorePoints();
		clusterRemaining();
		clusterNotClustered();
	}

	/**
	 * Method that calculates the similarity between connected points, in order
	 * to evaluate the density of each point.
	 * 
	 * Iterates over all the points in the metric tree. For each point, 
	 * iterates all of its K nearest neighbours, if the point and a neighbour
	 * are mutual neighbours, then it evaluates the similarity between the two
	 * points. If the similarity is equal or bigger than eps, it increments the
	 * density of the point.
	 */
	private void calculateTotalWeight(){
		Iterator<Long> it = kNeighbours.keySet().iterator();

		while(it.hasNext()){
			int density = 0;

			Long current = it.next();
			SNNObject currentSNN = clusters.get(current);
			Set<Pair<Long,Double>> currentNN = kNeighbours.get(current);

			Iterator<Pair<Long,Double>> itNeighbours = currentNN.iterator();

			int length = (k > currentNN.size()) ? currentNN.size() : k; //alterado por ricardo porque por vezes um ponto pode nao ter vizinhos no meu contexto

			for(int i = 0; i < length; i++){ 

				Pair<Long,Double> neighbour = itNeighbours.next();

				int similarity = connections.get(current, neighbour.getLeft());

				if(similarity == -1){
					Set<Pair<Long,Double>> neighbourNN = 
							kNeighbours.get(neighbour.getLeft());

					if(hasConnection(current,neighbourNN)) {
						similarity = calculateSimilarity(current, currentNN, neighbour.getLeft(), neighbourNN);
//						System.out.println("similaridade: " + similarity);
					}
					else similarity = 0;
				}

				if(similarity >= eps)
					density++;
			}
			currentSNN.setDensity(density);
		}
	}

	/**
	 * Method that verifies if two points are mutual neighbours.
	 * 
	 * Checks if the K nearest neighbours of a neighbour contain the current
	 * point in analysis.
	 * 
	 * @param current The current point in analysis.
	 * @param neighbourNN The Set of K nearest neighbours of a neighbour of the
	 * current point.
	 * @return true if the current point is contained in the Set. Otherwise, it
	 * returns false.
	 */
	private boolean hasConnection(Long current, 
			Set<Pair<Long,Double>> neighbourNN){
		return contains(current, neighbourNN);
	}

	/**
	 * Method that calculates the similarity between two points.
	 * 
	 * Intersects the two Sets of K nearest neighbours of two points, in order
	 * to find the similarity between them that is the number of shared nearest
	 * neighbours. After calculating the intersection, stores the similarity
	 * in the Map with the similarities between points.
	 * 
	 * @param current The current point in analysis.
	 * @param currentNN The K nearest neighbours of the current point.
	 * @param neighbour A neighbour of the current point.
	 * @param neighbourNN The K nearest neighbours of the current neighbour.
	 * @return The similarity between the current point and its neighbour, i.e.
	 * the number of shared nearest neighbours.
	 */
	private int calculateSimilarity(Long current, 
			Set<Pair<Long,Double>> currentNN,
			Long neighbour, Set<Pair<Long,Double>> neighbourNN) {

		int similarity = intersect(currentNN,neighbourNN);

		connections.put(current, neighbour, similarity);

		return similarity;
	}

	/**
	 * Method that intersects two sets of points.
	 * 
	 * Iterates the first set, while checking if the current point is contained
	 * in the second set.
	 * 
	 * @param set1 The first set of points.
	 * @param set2 The second set of points.
	 * @return The number of common points in the two sets.
	 */
	private int intersect(Set<Pair<Long,Double>> set1, 
			Set<Pair<Long,Double>> set2) {
		int numIntersect = 0;

		Iterator<Pair<Long,Double>> it = set1.iterator();

		int length = (k > set1.size()) ? set1.size() : k;

		for(int i = 0; i < length; i++)
			if (contains(it.next().getLeft(), set2))
				numIntersect++;

		return numIntersect;
	}

	/**
	 * Method that verifies if a point is contained in a set.
	 * 
	 * Iterates over the set, until it finds the point.
	 * 
	 * @param point
	 * @param set
	 * @return true if the point is contained in the set. Otherwise, it returns
	 * false.
	 */
	private boolean contains(Long point, Set<Pair<Long,Double>> set) {
		Iterator<Pair<Long,Double>> it = set.iterator();

		int length = (k > set.size()) ? set.size() : k; //adicionei isto

		for(int i = 0; i < length; i++) {
			Pair<Long,Double> next = it.next();

			if(next.getLeft() != null) {
				if(next.getLeft().equals(point))
					return true;
			}
		}

		return false;
	}

	/**
	 * Method that defines the points as non-noise and maintains the remaining
	 * as noise.
	 * 
	 * Iterates over all the points in the tree. For each point with a density
	 * equal or bigger than minPts, it defines the point as non-noise and if it
	 * has a neighbour with density less than minPts that is in the range of 
	 * eps, the neighbour is also defined as non-noise.
	 */
	private void identifyNoise(){
		Iterator<Long> it = kNeighbours.keySet().iterator();

		while(it.hasNext()){			
			Long current = it.next();
			SNNObject currentSNN = clusters.get(current);

			if(currentSNN.getDensity() >= minPts) {
				currentSNN.setNoise(false);
				numNoise--;
				coreClusters.addVertex(current);

				Set<Pair<Long,Double>> currentNN = kNeighbours.get(current);

				Iterator<Pair<Long,Double>> itNeighbours = 
						currentNN.iterator();

				int length = (k > currentNN.size()) ? currentNN.size() : k; //adicionei isto
				
				//alterado por ricardo porque por vezes um ponto pode nao ter vizinhos no meu contexto
				for(int i = 0; i < length; i++){
					Pair<Long, Double> neighbourPair = itNeighbours.next();
					Long neighbour = neighbourPair.getLeft();
					SNNObject neighbourSNN = clusters.get(neighbour);
					Double neighbourDist = neighbourPair.getRight();

					if(neighbourSNN.getDensity() < minPts && 
							neighbourDist <= eps){
						if(neighbourSNN.isNoise()){
							neighbourSNN.setNoise(false);
							numNoise--;
						}
					}
				}
			}
		}
	}

	/**
	 * Method that clusters the core points.
	 * 
	 * Iterates over all the points in the tree. For every point with a density
	 * equal or bigger than minPts, checks if it has already a cluster, it it 
	 * does not have, then it assigns a new cluster to it. Then, iterates over
	 * all of its neighbours, if a neighbour has density equal or bigger than
	 * minPts, the point and its neighbour are in different clusters and they 
	 * are mutual neighbours, it checks if the neighbour has already a cluster 
	 * assigned, if it has, then the current point and its cluster, join the 
	 * cluster of the neighbour. Otherwise, the neighbour joins the cluster of 
	 * the point.
	 */
	private void clusterCorePoints(){
		Iterator<Long> it = kNeighbours.keySet().iterator();

		while(it.hasNext()){			
			Long current = it.next();
			SNNObject currentSNN = clusters.get(current);

			if(currentSNN.getDensity() >= minPts){
				Set<Pair<Long,Double>> currentNN = kNeighbours.get(current);

				Iterator<Pair<Long,Double>> itNeighbours = 
						currentNN.iterator();

				int length = (k > currentNN.size()) ? currentNN.size() : k; //adicionei isto
				
				
				for(int i = 0; i < length; i++){
					Pair<Long, Double> neighbourPair = itNeighbours.next();
					Long neighbour = neighbourPair.getLeft();
					SNNObject neighbourSNN = clusters.get(neighbour);

					if(neighbourSNN.getDensity() >= minPts){
						int similarity = connections.get(current, neighbour);

						if(similarity >= eps){
							coreClusters.addEdge(current, neighbour);
						}
					}
				}	
			}
		}
		setClustersFlags();
	}

	private void setClustersFlags(){
		ConnectivityInspector<Long, DefaultEdge> ci = 
				new ConnectivityInspector<Long, DefaultEdge>(coreClusters);

		List<Set<Long>> connectedSets = ci.connectedSets();

		long clusterId = 0;
		for(Set<Long> connSet : connectedSets){
			clusterId++;
			for(Long current : connSet){
				SNNObject currentSNN = clusters.get(current);
				currentSNN.setCluster(clusterId);
			}
		}
		setClustersIds(clusterId);
	}

	private void setClustersIds(long lastId){
		for(long i = 1; i <= lastId; i++){
			clustersIds.add(i);
		}
	}

	/**
	 * Method that clusters the remaining non-core and non-noise points.
	 * 
	 * Iterates over all the points in the tree. For each point that is not 
	 * noise and has a density less than minPts, iterates over its neighbours,
	 * in order to find out which ones are core points and with which it is 
	 * most similar. If it finds the most similar core point, it assigns the
	 * point to the cluster of the core point. Otherwise, it discovers the 
	 * predominant cluster in all its neighbours and assigns the point to it.
	 * If non of the neighbours are already clustered, then it leaves the point
	 * to be clustered later.
	 */
	private void clusterRemaining(){
		Iterator<Long> it = kNeighbours.keySet().iterator();

		while(it.hasNext()){
			Long current = it.next();
			SNNObject currentSNN = clusters.get(current);

			if(!currentSNN.isNoise() && currentSNN.getDensity() < minPts){
				long mostSimilarCluster = -1;
				int mostSimilarWeight = -1;

				Set<Pair<Long,Double>> currentNN = kNeighbours.get(current);

				Iterator<Pair<Long,Double>> itNeighbours =
						currentNN.iterator();

				for(int i = 0; i < k; i++){
					Pair<Long, Double> neighbourPair = itNeighbours.next();
					Long neighbour = neighbourPair.getLeft();
					SNNObject neighbourSNN = clusters.get(neighbour);

					if(neighbourSNN.getDensity() >= minPts){
						int similarity = connections.get(current, neighbour);

						if(similarity > mostSimilarWeight){
							mostSimilarWeight = similarity;
							mostSimilarCluster = neighbourSNN.getCluster();
						}
					}
				}

				if(mostSimilarCluster != -1)
					currentSNN.setCluster(mostSimilarCluster);
				else {
					Map<Long, Integer> occur = getNeighboursCluster(current);
					long mostFreqCluster = getMostFreqCluster(occur);

					if(mostFreqCluster != -1)
						currentSNN.setCluster(mostFreqCluster);
					else notClustered.add(current);
				}
			}
		}
	}

	/**
	 * Method that clusters the non-core and non-noise points that could not be
	 * clustered due to the lack of previously clustered neighbours.
	 * 
	 * Iterates over all the points that could not be clustered due to the lack
	 * of previously clustered neighbours, checking if there is a predominant 
	 * cluster in all its neighbours. If there is one, then it assigns the 
	 * point to it. Otherwise, the point is declared as noise. 
	 */
	private void clusterNotClustered(){
		Iterator<Long> it = notClustered.iterator();

		while(it.hasNext()){
			Long current = it.next();
			SNNObject currentSNN = clusters.get(current);

			Map<Long, Integer> occur = getNeighboursCluster(current);
			long mostFreqCluster = getMostFreqCluster(occur);

			if(mostFreqCluster != -1)
				currentSNN.setCluster(mostFreqCluster);
			else {
				currentSNN.setCluster(-1);
				currentSNN.setNoise(true);
			}
		}
	}

	/**
	 * Method that calculates the number of occurrences of a cluster.
	 * 
	 * Iterates over all the neighbours of the current point, creating a map
	 * where the entry is the cluster number and the value is the number of 
	 * occurrences of that cluster in all the neighbours.
	 * 
	 * @param current The current point that needs to know the predominant 
	 * cluster in all its neighbours.
	 * @return A map with number of occurrences of a cluster in all the 
	 * neighbours of the current point.
	 */
	private Map<Long,Integer> getNeighboursCluster(Long current){
		Set<Pair<Long,Double>> currentNN = kNeighbours.get(current);

		Iterator<Pair<Long,Double>> itNeighbours = currentNN.iterator();

		Map<Long,Integer> occur = new HashMap<Long,Integer>();

		for(int i = 0; i < k; i++){
			Pair<Long, Double> neighbourPair = itNeighbours.next();
			Long neighbour = neighbourPair.getLeft();
			SNNObject neighbourSNN = clusters.get(neighbour);

			if(occur.containsKey(neighbourSNN.getCluster())){
				int inc = occur.get(neighbourSNN.getCluster())+ 1;
				occur.put(neighbourSNN.getCluster(), inc);
			}
			else occur.put(neighbourSNN.getCluster(), 1);

		}

		return occur;
	}

	/**
	 * Method that finds the predominant cluster.
	 * 
	 * Iterates over the map, in order to find the number of the predominant
	 * cluster.
	 * 
	 * @param occur A map with the number of occurrences of a cluster.
	 * @return The number of the predominant cluster.
	 */
	private long getMostFreqCluster(Map<Long, Integer> occur){
		Iterator<Map.Entry<Long, Integer>> it = occur.entrySet().iterator();

		long mostFreqCluster = -1;
		int freq = 0;
		while(it.hasNext()){
			Map.Entry<Long, Integer> next = it.next();
			if(next.getValue() > freq && next.getKey() != -1){
				mostFreqCluster = next.getKey();
				freq = next.getValue(); 
			}
		}

		return mostFreqCluster;
	}
}