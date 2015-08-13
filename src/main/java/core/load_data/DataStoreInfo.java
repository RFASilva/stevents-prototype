package core.load_data;

import java.sql.Connection;
import java.sql.DriverManager;

import core.Config;


public class DataStoreInfo {

	public static Connection getMetaStore() {
		return getConnection(Config.getConfigString("meta_store_url"));
	}
	
	private static Connection getConnection(final String url) {
		Connection connection = null;
		try {
			Class.forName("org.postgresql.Driver");
			connection = DriverManager.getConnection(url);
		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return connection;
	}
}
