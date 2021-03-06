package api_server; 

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import main.Main;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.Cache;

import core.Context;
import core.shared.Table;


@Path("spatialdata")
public class SpatialDataResource {

	public class DataResponse implements StreamingOutput {

		private int posInit;
		private int posEnd;

		public DataResponse(String posInit,String posEnd) {
			super();
			this.posInit = Integer.parseInt(posInit); 
			this.posEnd = Integer.parseInt(posEnd);
		}

		@Override
		public void write(final OutputStream os) throws IOException,
		WebApplicationException {

			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
			String ret = null;
			Context context = Main.context;
			String geojson = "";

			if(posEnd ==-1) 
				geojson = context.getLoader().getInstSpatialEvents(context, posInit);
			else
				geojson = context.getLoader().getRangeSpatialEvents(context, posInit, posEnd);

			writer.write(geojson);

			writer.flush();
			writer.close();
		}
	}

	@GZIP
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Cache(maxAge = 0)
	public Response getSpatial(@QueryParam("posInit") final String posInit,
			@QueryParam("posEnd") final String posEnd, @QueryParam("tableName") final String tableName, @QueryParam("granularity") final String granularity) {
		try {

			Main.context.setTimeGranularity(granularity);
			Main.context.setFromTable(tableName);

			if(tableName.equals("accidents_usa")) {
				Main.context.setTableToRead( new Table("accidents_usa_up", "pk_id"));
			}
			else if(tableName.equals("fires_portugal")) {
				Main.context.setTableToRead( new Table("fires_portugal_up", "pk_id"));
			}
			else if(tableName.equals("accidents_portugal")) {
				Main.context.setTableToRead( new Table("accidents_portugal_up", "pk_id"));
			}

			Main.context.updateTimeManager();

			DataResponse info = new DataResponse(posInit, posEnd);
			return Response.ok(info).build();
		} catch (Exception exception) {
			throw new WebApplicationException(exception);
		}
	}



}
