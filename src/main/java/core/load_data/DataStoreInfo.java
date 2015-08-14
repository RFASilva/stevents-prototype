package core.load_data;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import core.Config;


public class DataStoreInfo {

	public static Connection getMetaStore() {
//		return getConnection(Config.getConfigString("meta_store_url"));
		return getConnection();
	}
	
	private static Connection getConnection()  {
	    URI dbUri;
		try {
			dbUri = new URI(System.getenv("DATABASE_URL"));
		

	    String username = dbUri.getUserInfo().split(":")[0];
	    String password = dbUri.getUserInfo().split(":")[1];
	    String dbUrl = "jdbc:postgresql://" + dbUri.getHost() + ':' + dbUri.getPort() + dbUri.getPath();
	    return DriverManager.getConnection(dbUrl, username, password);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	  
	}
	
//	private static Connection getConnection(final String url) {
//		Connection connection = null;
//		try {
//			Class.forName("org.postgresql.Driver");
//			connection = DriverManager.getConnection(url);
//		} catch (Exception exception) {
//			exception.printStackTrace();
//		}
//
//		return connection;
//	}
}
