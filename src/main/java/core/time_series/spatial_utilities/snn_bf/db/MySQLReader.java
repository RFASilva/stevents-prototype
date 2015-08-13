package core.time_series.spatial_utilities.snn_bf.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.util.Pair;

public class MySQLReader implements DBReader {

	private DBConnection connection;
	
	private int k;
	
	public MySQLReader(){
		this.connection = new MySQLConnection();
		this.k = Integer.MAX_VALUE;
	}
	
	public int getK(){
		return k;
	}
	
	public Map<Long, Set<Pair<Long, Double>>> getKNNs() throws SQLException, DBConnectionException {
		Map<Long, Set<Pair<Long, Double>>> result = 
				new HashMap<Long, Set<Pair<Long, Double>>>();
		
		Connection conn = connection.getConnection();
		PreparedStatement ps = null;
		ResultSet rs = null;

		ps = conn.prepareStatement(
				"SELECT * FROM knns ORDER BY id, distance;");
		rs = ps.executeQuery();

		Set<Pair<Long, Double>> knn = 
				new LinkedHashSet<Pair<Long, Double>>();
		long previousId = -1;
		while(rs.next()){
			Long id = rs.getLong("id");
			Long nn = rs.getLong("nn");
			Double distance = rs.getDouble("distance");

			if(previousId != id){
				if(previousId != -1){
					result.put(previousId, knn);
					
					if(knn.size() < k)
						k = knn.size();
				}

				knn = new LinkedHashSet<Pair<Long, Double>>();
			}

			knn.add(new Pair<Long,Double>(nn, distance));
			previousId = id;
		}
		result.put(previousId, knn);
		
		if(knn.size() < k)
			k = knn.size();
		
		conn.close();
		
		return result;
	}
}
