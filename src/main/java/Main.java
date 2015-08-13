

import org.jboss.resteasy.jsapi.JSAPIServlet;
import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

import api_server.Context;
import api_server.ContextResource;
import api_server.SpatialDataResource;
import api_server.StaticResources;
import api_server.TimeDataResource;
import core.Config;
import core.shared.Column;
import core.shared.Table;

public class Main {

	public static Context context;
	
	public static void setContextAccidentsUSA(TJWSEmbeddedJaxrsServer webServer) {
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
	
	public static void setContextFiresPortugal(TJWSEmbeddedJaxrsServer webServer) {
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
		
		setContextFiresPortugal(webServer);
		Main.addResources(webServer);
		
		System.out.print("Web server started...");
	}
}
