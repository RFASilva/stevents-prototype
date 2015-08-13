package core.time_series.spatial_utilities.snn_bf.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class MySQLConnection implements DBConnection{

	private String bd, port, host, user, pass;
	
	public MySQLConnection(){
		this.bd = "tese";
		this.port = "3306"; 
		this.host = "localhost";
		this.user = "root";
		this.pass = "root";
	}

	public MySQLConnection(String bd, String port, String host, String user, 
			String pass){
		this.bd = bd;
		if(port.equals("")) 
			this.port = "3306"; 
		else this.port = port;
		this.host = host;
		this.user = user;
		this.pass = pass;
	}

	/**
	 * Returns a MySQL connection.
	 * 
	 * @return the MySQL connection.
	 * @throws DBConnectionException 
	 */
	public Connection getConnection() throws DBConnectionException
	{
		try{
			Connection conn = null;
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection("jdbc:mysql://" + this.host + 
					":" + this.port + "/" + this.bd + "?user=" + this.user +
					"&password=" + this.pass);
			conn.setAutoCommit(false);
			
			return conn;
		}
		catch (Exception e){
			throw new DBConnectionException();
		}
	}
}