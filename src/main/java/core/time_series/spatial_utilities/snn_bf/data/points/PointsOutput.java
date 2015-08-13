package core.time_series.spatial_utilities.snn_bf.data.points;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.GenericPoint;
import core.time_series.spatial_utilities.snn_bf.file.Output;
import core.time_series.spatial_utilities.snn_bf.snn.SNNObject;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlot;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

public class PointsOutput implements Output<GenericPoint<Double>>{
	
	private String content;
	
	private Iterator<GenericPoint<Double>> it;
	
	private Map<Long,SNNObject> clusters;
	
	private Set<Long> clustersIds;

	public PointsOutput(){
		this.content = new String();
	}

	public void setIterator(Iterator<GenericPoint<Double>> it) {
		this.it = it;
	}

	public void setClusters(Map<Long, SNNObject> clusters) {
		this.clusters = clusters;
	}
	
	public void setClustersIds(Set<Long> clustersIds){
		this.clustersIds = clustersIds;
	}
	
	public void output(String outputName, boolean resToPlot) 
			throws IOException, WekaPlotException{
		content = new String();
		
		if(!outputName.equals("") || resToPlot){
			headerArff();
			contentArff();
		}
		
		if(!outputName.equals(""))
			outputArff(outputName);
		
		if(resToPlot)
			plot();
	}
	
	private void headerArff(){
		content += "@relation 'novos_pts4'\n";
		content += "@attribute X real\n";
		content += "@attribute Y real\n";
		content += "@attribute cluster {-1, " + parseClustersIds() + "}\n";
		content += "@data\n";
	}
	
	private String parseClustersIds(){
		String result = clustersIds.toString();
		result = result.substring(1, result.length()-1);
		
		return result;
	}
	
	private void contentArff(){
		while(it.hasNext()){
			GenericPoint<Double> current = it.next();
			content += outputPoint(current);
		}
	}
	
	private String outputPoint(GenericPoint<Double> point){
		String result = "";
		
		for(int i = 0; i < point.getDimensions(); i++){
			result += point.getCoord(i);
			result += ",";
		}
		
		result += clusters.get(point.getId()).getCluster();
		result += "\n";
		
		return result;
	}
	
	private void outputArff(String outputName) throws IOException{
		FileWriter fileOut = new FileWriter(outputName);
		BufferedWriter out = new BufferedWriter(fileOut);
		out.write(content);
		out.close();
	}
	
	private void plot() throws WekaPlotException {
		InputStream is = new ByteArrayInputStream(content.getBytes());
		WekaPlot wp = new WekaPlot(is);
		wp.plot();
	}
}
