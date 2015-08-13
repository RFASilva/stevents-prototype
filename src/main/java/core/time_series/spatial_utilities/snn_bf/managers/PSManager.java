package core.time_series.spatial_utilities.snn_bf.managers;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.GenericPoint;
import core.time_series.spatial_utilities.snn_bf.data.points.PointsOutput;
import core.time_series.spatial_utilities.snn_bf.data.points.PointsWKTReader;
import core.time_series.spatial_utilities.snn_bf.file.Output;
import core.time_series.spatial_utilities.snn_bf.file.Reader;
import core.time_series.spatial_utilities.snn_bf.snn.SNN;
import core.time_series.spatial_utilities.snn_bf.snn.SNNGraph;
import core.time_series.spatial_utilities.snn_bf.util.Pair;
import core.time_series.spatial_utilities.snn_bf.util.UserMessages;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

/**
 * Java implementation of the primary storage manager.
 * 
 * @author Bruno Filipe Faustino
 * @since 25-06-2012
 */
public class PSManager implements Manager {
	
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
	 * Output manager that creates a file arff file with the clustering.
	 */
	private Output<GenericPoint<Double>> output;
	
	/**
	 * Define reader, output and the distance function in the reader.
	 * @param fileIn
	 * @param numObjsToRead
	 * @param allowsRep
	 */
	public PSManager(String[] points, Long numObjsToRead, boolean allowsRep){
		this.reader = new PointsWKTReader(points, allowsRep);
		this.numObjsToRead = numObjsToRead;
		this.knns = new HashMap<Long, Set<Pair<Long,Double>>>();
		this.previousK = -1;
		this.numCalculations = new ArrayList<Long>();
		this.snn = new SNNGraph();
		this.output = new PointsOutput();
	}
	
	public void run(int k, int eps, int minPts, String outputName, 
			boolean resToPlot) throws IOException, WekaPlotException {
		readAndKnn(k);
		snn(k, eps, minPts);
		output(outputName, resToPlot);
		done(outputName);
	}
	
	private void readAndKnn(int k){
		if(previousK == -1){
			reader.readNumObjsDim();
			
			UserMessages.buildingTree(numObjsToRead);
			
			reader.readObjects(numObjsToRead);
			
			long numOfObjectsIns = reader.getNumObjectsIns();
			UserMessages.builtTree(numOfObjectsIns);
			
			numCalculations.add(reader.getNumberCalculations());
		}
		
		if(k > previousK){
			UserMessages.gettingKNNs(k);
			knns = reader.getKNNs(k);
			previousK = k;
			UserMessages.hasKNNs(k);
			
			numCalculations.add(reader.getNumberCalculations());
		}
		else numCalculations.add(new Long(0));
	}
	
	private void snn(int k, int eps, int minPts){
//		UserMessages.startingSNN(k, eps, minPts);
		
		snn.run(knns, k, eps, minPts);
		
		long numClusters = snn.getClustersIds().size();
		long numOfObjectsIns = reader.getNumObjectsIns();
		long numNoise = snn.getNumNoise();
		long numClustered = numOfObjectsIns - numNoise;
		
		
//		UserMessages.endedSNN(numClusters, numClustered, numNoise);
	}
	
	private void output(String outputName, boolean resToPlot) 
			throws IOException, WekaPlotException{
		UserMessages.givingOutputFilePlot(outputName, resToPlot);
		output.setIterator(reader.getIterator());
		output.setClusters(snn.getClusters());
		output.setClustersIds(snn.getClustersIds());
		output.output(outputName, resToPlot);
		UserMessages.gaveOutputFilePlot(outputName, resToPlot);
	}
	
	public void done(String outName) throws IOException{
		if(!outName.equals("")){
			String outputName = outName.substring(0, outName.indexOf("."));
			outputName += ".distanceStats.txt";
			
			FileWriter fileOut = new FileWriter(outputName);
			BufferedWriter out = new BufferedWriter(fileOut);
			
			for(Long dist : numCalculations)
				out.write(dist.toString() + "\n");
			
			out.close();
		}
	}
	
	public static void main(String[] args) {
		
		
		
	}
}
