package core.time_series.spatial_utilities.snn_bf.db;

import java.sql.Connection;

public interface DBConnection {

	/**
	 * Returns a connection to be used.
	 * 
	 * @return the connection.
	 * @throws DBConnectionException 
	 */
	public Connection getConnection() throws DBConnectionException;

}
