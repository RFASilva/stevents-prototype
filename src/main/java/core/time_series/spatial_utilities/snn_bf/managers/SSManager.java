package core.time_series.spatial_utilities.snn_bf.managers;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.data.points.PointsDBOutput;
import core.time_series.spatial_utilities.snn_bf.db.DBConnectionException;
import core.time_series.spatial_utilities.snn_bf.db.DBOutput;
import core.time_series.spatial_utilities.snn_bf.db.DBReader;
import core.time_series.spatial_utilities.snn_bf.db.MySQLReader;
import core.time_series.spatial_utilities.snn_bf.snn.SNN;
import core.time_series.spatial_utilities.snn_bf.snn.SNNGraph;
import core.time_series.spatial_utilities.snn_bf.util.Pair;
import core.time_series.spatial_utilities.snn_bf.util.UserMessages;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

/**
 * Java implementation of the secondary storage module.
 * 
 * @author Bruno Filipe Faustino
 * @since 03-07-2012
 */
public class SSManager implements Manager{
	
	/**
	 * Map with the K nearest neighbours of each object P.
	 */
	private Map<Long, Set<Pair<Long,Double>>> knns;
	
	/**
	 * Number of nearest neighbours stored in the database.
	 */
	private int k;
	
	/**
	 * Reader for the file with the objects.
	 */
	private DBReader reader;
	
	/**
	 * The SNN algorithm.
	 */
	private SNN snn;
	
	/**
	 * Output manager that creates a file arff file with the clustering.
	 */
	private DBOutput output;

	public SSManager(){
		this.reader = new MySQLReader();
		this.knns = new HashMap<Long, Set<Pair<Long,Double>>>();
		this.snn = new SNNGraph();
		this.output = new PointsDBOutput();
	}
	
	public void run(int k, int eps, int minPts, String outputName, 
			boolean resToPlot) throws SQLException, DbKValueException, 
			IOException, WekaPlotException, DBConnectionException{
		knn();
		snn(k, eps, minPts);
		output(outputName, resToPlot);
	}
	
	private void knn() throws SQLException, DBConnectionException{
		if(knns.size() == 0){
			UserMessages.gettingKNNFromDB();
			knns = reader.getKNNs();
			setK();
			UserMessages.hasKNNs(k);
		}
	}
	
	private void setK(){
		k = reader.getK();
	}
	
	private void snn(int k, int eps, int minPts) throws DbKValueException{
		if(k <= this.k){
			UserMessages.startingSNN(k, eps, minPts);
			snn.run(knns, k, eps, minPts);
			
			long numClusters = snn.getClustersIds().size();
			long numOfObjectsIns = knns.size();
			long numNoise = snn.getNumNoise();
			long numClustered = numOfObjectsIns - numNoise;
			
			UserMessages.endedSNN(numClusters, numClustered, numNoise);
		}
		else throw new DbKValueException(this.k);
	}
	
	private void output(String outputName, boolean resToPlot) 
			throws SQLException, IOException, WekaPlotException, DBConnectionException{
		UserMessages.givingOutputFilePlot(outputName, resToPlot);
		output.setClusters(snn.getClusters());
		output.setClustersIds(snn.getClustersIds());
		output.output(outputName, resToPlot);
		UserMessages.gaveOutputFilePlot(outputName, resToPlot);
	}
	
	public void done(String outputName){}
}
