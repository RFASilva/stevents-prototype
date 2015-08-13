package core.time_series.spatial_utilities.snn_bf.data.points;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.db.DBConnection;
import core.time_series.spatial_utilities.snn_bf.db.DBConnectionException;
import core.time_series.spatial_utilities.snn_bf.db.DBOutput;
import core.time_series.spatial_utilities.snn_bf.db.MySQLConnection;
import core.time_series.spatial_utilities.snn_bf.snn.SNNObject;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlot;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

public class PointsDBOutput implements DBOutput {

	private String content;
	
	private DBConnection connection;
	
	private Map<Long,SNNObject> clusters;
	
	private Set<Long> clustersIds;
	
	public PointsDBOutput(){
		this.content = new String();
		
		this.connection = new MySQLConnection();
	}
	
	public void setClusters(Map<Long, SNNObject> clusters) {
		this.clusters = clusters;
	}
	
	public void setClustersIds(Set<Long> clustersIds){
		this.clustersIds = clustersIds;
	}
	
	public void output(String outputName, boolean resToPlot) 
			throws SQLException, IOException, WekaPlotException, 
			DBConnectionException{
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
	
	private void contentArff() throws SQLException, DBConnectionException{
		Connection conn = connection.getConnection();
		Iterator<Long> it = clusters.keySet().iterator();
		
		while(it.hasNext()){
			Long current = it.next();
			content += outputPoint(conn, current);
		}
		
		conn.close();
	}
	
	private void outputArff(String outputName) throws IOException{
		FileWriter fileOut = new FileWriter(outputName);
		BufferedWriter out = new BufferedWriter(fileOut);
		out.write(content);
		out.close();
	}
	
	private String outputPoint(Connection conn, Long id) throws SQLException {
		String result = "";
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		ps = conn.prepareStatement(
				"SELECT * FROM objects WHERE id = ?;");
		ps.setLong(1, id);
		
		rs = ps.executeQuery();

		rs.next();
		result += rs.getString("latitude");
		result += ", ";
		result += rs.getString("longitude");
		result += ", ";
		
		result += clusters.get(id).getCluster();
		result += "\n";
		
		return result;
	}
	
	private void plot() throws WekaPlotException {
		InputStream is = new ByteArrayInputStream(content.getBytes());
		WekaPlot wp = new WekaPlot(is);
		wp.plot();
	}
}
