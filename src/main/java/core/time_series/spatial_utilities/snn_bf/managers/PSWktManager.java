package core.time_series.spatial_utilities.snn_bf.managers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.GenericPoint;
import core.time_series.spatial_utilities.snn_bf.data.points.PointsWKTReader;
import core.time_series.spatial_utilities.snn_bf.file.Reader;
import core.time_series.spatial_utilities.snn_bf.snn.SNN;
import core.time_series.spatial_utilities.snn_bf.snn.SNNGraph;
import core.time_series.spatial_utilities.snn_bf.snn.SNNObject;
import core.time_series.spatial_utilities.snn_bf.util.Pair;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

/**
 * Java implementation of the primary storage manager.
 * 
 * @author Bruno Filipe Faustino
 * @since 25-06-2012
 */
public class PSWktManager  {
	
	/**
	 * Reader for the file with the objects.
	 */
	private Reader<GenericPoint<Double>> reader;
	
	/**
	 * Number of objects to be read from file.
	 */
	private Long numObjsToRead;
	
	/**
	 * Map with the K nearest neighbours of each object P.
	 */
	private Map<Long, Set<Pair<Long,Double>>> knns;
	
	/**
	 * Number of nearest neighbours used in the previous run.
	 */
	private int previousK;
	
	/**
	 * ArrayList with the number of distance calculations per operation.
	 */
	private ArrayList<Long> numCalculations;
	
	/**
	 * The SNN algorithm.
	 */
	private SNN snn;
	
	
	/**
	 * Define reader, output and the distance function in the reader.
	 * @param fileIn
	 * @param numObjsToRead
	 * @param allowsRep
	 */
	public PSWktManager(String[] points, Long numObjsToRead, boolean allowsRep){
		this.reader = new PointsWKTReader(points, allowsRep);
		this.numObjsToRead = numObjsToRead;
		this.knns = new HashMap<Long, Set<Pair<Long,Double>>>();
		this.previousK = -1;
		this.numCalculations = new ArrayList<Long>();
		this.snn = new SNNGraph();
	}
	
	public List<Double> run(int k, int eps, int minPts) throws IOException, WekaPlotException {
		readAndKnn(k);
		return snn(k, eps, minPts);
//		done(outputName);
	}
	
	public void readAndKnn(int k){
		if(previousK == -1){
			reader.readNumObjsDim();
			
//			UserMessages.buildingTree(numObjsToRead);
			
			reader.readObjects(numObjsToRead);
			
			long numOfObjectsIns = reader.getNumObjectsIns();
//			UserMessages.builtTree(numOfObjectsIns);
			
			numCalculations.add(reader.getNumberCalculations());
		}
		
		if(k > previousK){
//			UserMessages.gettingKNNs(k);
			knns = reader.getKNNs(k);
			
			previousK = k;
//			UserMessages.hasKNNs(k);
			
			numCalculations.add(reader.getNumberCalculations());
		}
		else numCalculations.add(new Long(0));
	}
	
	private List<Double> snn(int k, int eps, int minPts){
		List<Double> results = new LinkedList<Double>();
		
		snn.run(knns, k, eps, minPts);
		
		long numClusters = snn.getClustersIds().size();
		long numOfObjectsIns = reader.getNumObjectsIns();
		long numNoise = snn.getNumNoise();
		long numClustered = numOfObjectsIns - numNoise;

		double avgPointsCluster = avgPointsCluster(snn.getClusters(), numClusters);
		
		results.add((double)numClusters);
		results.add(avgPointsCluster);
		
//		results.add(numOfObjectsIns);
//		results.add(numNoise);
//		results.add(numClustered);
		
		return results;
	}
	
	private double avgPointsCluster(Map<Long, SNNObject> clusters, long numberCluster){
		
		if(numberCluster == 0)
			return 0.0;
		
		Iterator<GenericPoint<Double>> it = reader.getIterator();
		Map<Long, Integer> values = new HashMap<Long, Integer>();
		
		while(it.hasNext()) {
			
			GenericPoint<Double> point = it.next();
			
			long clusterID = clusters.get(point.getId()).getCluster();
			
//			System.out.println("id do cluster: " + clusterID);
			if(clusterID != -1) {
				if(values.get(clusterID) == null)
					values.put(clusterID, 0);
				
				int temp = values.get(clusterID);
				values.put(clusterID, temp + 1);
			}
		}
		
		int result = 0;
		for(Long l: values.keySet())
			result += values.get(l);
		
		return result/numberCluster;
		
	}
	
	public Map<Long, Set<Pair<Long,Double>>> knns() {
		return knns;
	}
}
