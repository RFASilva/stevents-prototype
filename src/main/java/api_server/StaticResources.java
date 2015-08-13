package api_server;

import java.io.File;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.jboss.resteasy.annotations.GZIP;

@Path("app")
public class StaticResources {

	@GZIP
	@GET
	@Path("{file:.*}")
	public File load(@PathParam("file") final String filename) {
		System.out.println("here: + " + filename);
		return new File("web_development/" + filename);
	}

}
