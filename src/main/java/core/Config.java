package core;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {

	private static Properties config = null;

	static {
		try {
			// load a properties file
			if (config == null) {
				config = new Properties();
				config.load(new FileInputStream("conf/config.properties"));
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public static int getConfigInt(final String prop) {
		return Integer.parseInt(config.getProperty(prop));
	}

	public static String getConfigString(final String prop) {
		return config.getProperty(prop);
	}
}
