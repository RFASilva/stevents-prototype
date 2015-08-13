package core.time_series.spatial_utilities.snn_bf.db;

import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.util.Pair;

public interface DBReader {

	public int getK();
	
	public Map<Long, Set<Pair<Long,Double>>> getKNNs() throws SQLException, 
			DBConnectionException;
}
