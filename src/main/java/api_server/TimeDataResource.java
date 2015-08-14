package api_server; 

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.jboss.resteasy.annotations.GZIP;
import org.jboss.resteasy.annotations.cache.Cache;

import core.Context;
import core.load_data.Loader;
import core.shared.Column;
import core.shared.Table;
import core.time_series.TimeSeriesManager;


@Path("timedata")
public class TimeDataResource {

	public class TimeDataResponse implements StreamingOutput {

		private final String tableName;
		private final Boolean isRestricted;
		private final String granularity;

		public TimeDataResponse(final String tableName, boolean isRestricted, String granularity) {
			super();
			this.tableName = tableName;
			this.isRestricted = isRestricted;
			this.granularity = granularity;
		}

		@Override
		public void write(final OutputStream os) throws IOException,
		WebApplicationException {
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(os));
			Context context = Server.context;
			
			String json = "";
			if(!isRestricted)
				json = Loader.getTimeSeries(tableName,false);
			else {
				
				String tableName = this.tableName + "region";
				
				
				setTables(tableName, granularity);
				Table tableToStore = context.getTimeManager().tableToStore;
				Table tableToStoreMeta = context.getTimeManager().tableToStoreMeta;
				
				// Set some paramters
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("centerPoint", context.getGeometryRestriction().getCentroid().getCoordinate());
				params.put("polygon", context.getGeometryRestriction());
				
				
				if(!context.isRegionComputed(tableName)) {
					System.out.println("computed region: ");
					context.getLoader().buildGranulesBD(context.getTimeManager(), tableToStore, tableToStoreMeta, true, params);
					context.setRegionComputed(tableName);
				}
				
				json = Loader.getTimeSeries(tableName, true);
			}

			writer.write(json);
			writer.flush();
			writer.close();
		}
	}

	@GZIP
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Cache(maxAge = 0)
	public Response getTimeSeries(@QueryParam("tableName") final String tableName, @QueryParam("granularity") final String granularity, 
			@QueryParam("isRestricted") final boolean isRestricted) {

		try {
			System.out.println("Pedido de series temporais");
			System.out.println("Parametros  isRestricted: " + isRestricted);
			System.out.println("Granularatiy: " + granularity);


			TimeDataResponse info = new TimeDataResponse(tableName, isRestricted, granularity);
			return Response.ok(info).build();
		} catch (Exception exception) {
			throw new WebApplicationException(exception);
		}
	}

	//This table name already embedded the granularity and region
	private void setTables(String tableName, String granularity) {

		//TODO: isto tem que ser melhorado
		Table tableToStore = new Table(tableName, "pk_id");
		tableToStore.add(new Column("pk_id", false, false, "NUMERIC"));
		tableToStore.add(new Column("up_geometry", false, false, "GEOMETRY"));
		tableToStore.add(new Column("up_geo_hash", false, false, "INTEGER"));
		tableToStore.add(new Column("type", false, false, "TEXT"));
		tableToStore.add(new Column("datetime", false, false, "TEXT"));
		tableToStore.add(new Column("pos", false, false, "NUMERIC"));
		tableToStore.add(new Column("value", false, false, "NUMERIC"));

		Table tableToStoreMeta = new Table(tableName + "meta", "pk_id2");
		tableToStoreMeta.add(new Column("pk_id2", false, false, "NUMERIC"));
		tableToStoreMeta.add(new Column("tableName", false, false, "TEXT"));
		tableToStoreMeta.add(new Column("granularity", false, false, "TEXT"));
		tableToStoreMeta.add(new Column("minValue", false, false, "NUMERIC"));
		tableToStoreMeta.add(new Column("maxValue", false, false, "NUMERIC"));
		tableToStoreMeta.add(new Column("length", false, false, "NUMERIC"));
		

		Server.context.setTimeManager(new TimeSeriesManager(granularity, Server.context.getLoader()));
		Server.context.getTimeManager().setTableToStore(tableToStore);
		Server.context.getTimeManager().setTableToStoreMeta(tableToStoreMeta);
	}

}
