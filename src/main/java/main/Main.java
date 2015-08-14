package main;


import java.util.HashMap;
import java.util.Map;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import core.Context;
import core.shared.Column;
import core.shared.Table;
//import org.jboss.resteasy.jsapi.JSAPIServlet;
//import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;

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
//		context = new Context("hour", 1, tableToStore, "fires_portugal");
	}

//	private static void addResources(TJWSEmbeddedJaxrsServer webServer) {
//
//		webServer.getDeployment().getRegistry().addPerRequestResource(StaticResources.class);
//		webServer.getDeployment().getRegistry().addPerRequestResource(SpatialDataResource.class);
//		webServer.getDeployment().getRegistry().addPerRequestResource(TimeDataResource.class);
//		webServer.getDeployment().getRegistry().addPerRequestResource(ContextResource.class);
//
//		webServer.addServlet("/rest-js", new JSAPIServlet());
//	}

	//	public static void main(final String[] args) {
	//		TJWSEmbeddedJaxrsServer webServer = new TJWSEmbeddedJaxrsServer();
	//		
	//		webServer.setPort(Config.getConfigInt("server_port"));
	//		webServer.setRootResourcePath("/");
	//		webServer.start();
	//		
	//		context = new Context();
	//		
	//		setContextFiresPortugal(webServer);
	//		Main.addResources(webServer);
	//		
	//		System.out.print("Web server started...");
	//	}

	public static void main(final String[] args) throws Exception {
		final String baseUri = "http://localhost:"+(System.getenv("PORT")!=null?System.getenv("PORT"):"9998")+"/";
        final Map<String, String> initParams = new HashMap<String, String>();
        setContextFiresPortugal(); 
        initParams.put("com.sun.jersey.config.property.packages","api_server");
        
        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
        System.out.println(String.format("Jersey started with WADL available at %sapplication.wadl.",baseUri, baseUri));
	}
}
