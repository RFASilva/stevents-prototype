package core.time_series.spatial_utilities.snn_bf.data.twitter;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import snn.SNNObject;
import weka.WekaPlot;
import weka.WekaPlotException;
import file.Output;

public class TwitterOutput implements Output<TwitterPoint>{

	private String content;
	
	private Iterator<TwitterPoint> it;
	
	private Map<Long,SNNObject> clusters;
	
	private Set<Long> clustersIds;
	
	public TwitterOutput(){
		this.content = new String();
	}
	
	public void setIterator(Iterator<TwitterPoint> it) {
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
		content += "@relation 'twitter'\n";
		content += "@attribute LAT real\n";
		content += "@attribute LONG real\n";
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
			TwitterPoint current = it.next();
			content += outputPoint(current);
		}
	}
	
	private String outputPoint(TwitterPoint point){
		String result = "";
		
		for(int i = 0; i < 3; i++){
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
