

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

import api_server.ContextResource;
import api_server.SpatialDataResource;
import api_server.StaticResources;
import api_server.TimeDataResource;
import core.Config;
import core.Context;
import core.shared.Column;
import core.shared.Table;

import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

public class Main {

	public static Context context;

	public static void setContextAccidentsUSA() {
		Table tableToStore = new Table("accidents_usa_up", "pk_id");

		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("minute", false, false, "NUMERIC"));
		tableToStore.add(new Column("hour", false, false, "NUMERIC"));
		tableToStore.add(new Column("day", false, false, "NUMERIC"));
		tableToStore.add(new Column("month", false, false, "NUMERIC"));
		tableToStore.add(new Column("year", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));

		context = new Context("day", 1, tableToStore, "accidents_usa");
	}

	public static void setContextFiresPortugal() {
		Table tableToStore = new Table("fires_portugal_up", "pk_id");

		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("minute", false, false, "NUMERIC"));
		tableToStore.add(new Column("hour", false, false, "NUMERIC"));
		tableToStore.add(new Column("day", false, false, "NUMERIC"));
		tableToStore.add(new Column("month", false, false, "NUMERIC"));
		tableToStore.add(new Column("year", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("date", false, false, "TIMESTAMP WITHOUT TIME ZONE"));
		context = new Context("hour", 1, tableToStore, "fires_portugal");
	}

	private static void addResources(TJWSEmbeddedJaxrsServer webServer) {

		webServer.getDeployment().getRegistry().addPerRequestResource(StaticResources.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(SpatialDataResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(TimeDataResource.class);
		webServer.getDeployment().getRegistry().addPerRequestResource(ContextResource.class);

		webServer.addServlet("/rest-js", new JSAPIServlet());
	}

		public static void main(final String[] args) {
			TJWSEmbeddedJaxrsServer webServer = new TJWSEmbeddedJaxrsServer();
			
			webServer.setPort(Config.getConfigInt("server_port"));
			webServer.setRootResourcePath("/");
			webServer.start();
			
			context = new Context();
			
			setContextFiresPortugal();
			Main.addResources(webServer);
			
			System.out.print("Web server started...");
		}

//	public static void main(final String[] args) throws Exception {
//		String webappDirLocation = "/web_development";
//
//		// The port that we should run on can be set into an environment variable
//		// Look for that variable and default to 8080 if it isn't there.
//		String webPort = System.getenv("PORT");
//		if (webPort == null || webPort.isEmpty()) {
//			webPort = "8080";
//		}
//
//		Server server = new Server(Integer.valueOf(webPort));
//		WebAppContext root = new WebAppContext();
//
//		root.setContextPath("/");
//		root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
//		root.setResourceBase(webappDirLocation);
//
//		// Parent loader priority is a class loader setting that Jetty accepts.
//		// By default Jetty will behave like most web containers in that it will
//		// allow your application to replace non-server libraries that are part of the
//		// container. Setting parent loader priority to true changes this behavior.
//		// Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
//		root.setParentLoaderPriority(true);
//
//		server.setHandler(root);
//
//		server.start();
//		server.join();
//	}
}
