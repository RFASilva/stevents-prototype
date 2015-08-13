package core.time_series.implemented;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import core.load_data.DataStoreInfo;
import core.load_data.Functions;
import core.load_data.Loader;
import core.shared.Entity;
import core.time_series.TimeSeriesFactory;
import core.time_series.TimeSeriesManager;
import core.time_series.TimeseriesType;
import core.time_series.spatial_utilities.snn_bf.managers.PSWktManager;
import core.time_series.spatial_utilities.snn_bf.util.Pair;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;
import edu.hawaii.jmotif.datatype.Timeseries;

public class ClustersTimeSeries extends TimeSeriesFactory {

	@Override
	public  Map<TimeseriesType, Timeseries> buildNTimeSeries(String sql, Entity granule) {
		
		return null;
//		Map<TimeseriesType, Timeseries> result = new HashMap<TimeseriesType, Timeseries>();
//		result.put(TimeseriesType.NumberClusters,  super.initTime());
//		result.put(TimeseriesType.NNIndex,  super.initTime());
//
//		int pos = TimeSeriesManager.findGranularityPos(super.timeGranularity);
//		int[] times = new int[5];
//
//		Connection connection = DataStoreInfo.getMetaStore();
//		Statement st;
//		try {
//			st = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
//			st.setFetchSize(Loader.FETCH_SIZE);
//
//			ResultSet data = st.executeQuery(sql);
//			while(data.next()) {
//				int up_geo_hash = data.getInt(1);
//				for(int i= 0; i < pos + 1; i++) {
//					times[i] = data.getInt(i + 2);
//				}
//
//				String[] spatialPoints = (String[]) data.getArray(pos + 3).getArray();
//
//				String wkt_point = data.getString(pos + 4);
//				granule.setUpGeometry(new WKTReader(TimeSeriesFactory.geofact).read(wkt_point));
//
//
//				PSWktManager clusterManager = new PSWktManager(spatialPoints, (long) spatialPoints.length, false);
//				
//				int clusterValue = numberClusters(clusterManager);
//				double nni = nni(clusterManager, spatialPoints);
//
//
//				int index = super.findPos(times) - 1;// Timeseries objects starts index at 0 position
//
//				//					System.out.println("cluster: " + clusterValue);
//				result.get(TimeseriesType.NumberClusters).elementAt(index).setValue(clusterValue);
//				result.get(TimeseriesType.NNIndex).elementAt(index).setValue(nni);
//
//			}
//
//			connection.close();
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} catch (ParseException e) {
//			e.printStackTrace();
//		}
//
//		return result;
	}


	public List<Double> clusterResults(PSWktManager clusterManager) {
		try {
			List<Double> results = clusterManager.run(25, 7, 20);
			
			return results;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (WekaPlotException e) {
			e.printStackTrace();
		}
		return null;
	}

	public double nni(PSWktManager clusterManager, String[] points) {
		double area = convexHullArea(points);
		if(area==0)
			return 0;

		clusterManager.readAndKnn(4);

		Map<Long, Set<Pair<Long,Double>>> neighbors = clusterManager.knns();

		// Compute Average Nearest Neighbor
		double avg = 0.0;
		int  n = 0;
		Iterator<Entry<Long, Set<Pair<Long,Double>>>> it = neighbors.entrySet().iterator();
		while(it.hasNext()) {
			Entry<Long, Set<Pair<Long,Double>>> next = it.next();

			// Isto e super estranho deve de existir um erro qualquer porque num determinado momento um gajo nao tem vizinhos
			// TODO: Avaliar o que se passa aqui
			
			Iterator<Pair<Long, Double>> itValue = next.getValue().iterator(); 
			if(itValue.hasNext()) {
				double d = itValue.next().getRight();
				avg += d;
			}
			else avg += 0.0;
			n++;
		}

		avg = avg/n;

		double expectedAvgNN = (Math.sqrt(area / n)) / 2;
		return (avg/expectedAvgNN);
	}



	private double convexHullArea (String[] points) {
		GeometryCollection geometries = processCoords(points);
		Geometry convexHull = geometries.convexHull();

		return 	convexHull.getArea();
	}

	@Override
	public void buildNTimeSeriesBD(String sql, Entity granule, Map<String, Object> params) {

		Connection connection = DataStoreInfo.getMetaStore();
		int posTimeSeries = 1;
		try {

			//Makes the query for data
			Statement st = connection.createStatement();
			st.setFetchSize(Loader.FETCH_SIZE);
			ResultSet data = st.executeQuery(sql);

			int pos = TimeSeriesManager.findGranularityPos(super.timeGranularity);
			int[] actualDate = new int[pos+1];
			int[] minimumDate = super.firstDate();
			List<String> types = new LinkedList<String>();
			types.add(TimeseriesType.NumberClusters.name());
			types.add(TimeseriesType.NNIndex.name());
			types.add(TimeseriesType.NumberClustersAVG.name());
			

			while(data.next()) {
				int upGeomtryHash = data.getInt(1);
				for(int i= 0; i < pos + 1; i++) {
					actualDate[i] = data.getInt(i + 2);
				}

				String[] spatialPoints = (String[]) data.getArray(pos + 3).getArray();
				String wktUpGeometry = data.getString(pos + 4);

				PSWktManager clusterManager = new PSWktManager(spatialPoints, (long) spatialPoints.length, false); 
				List<Double> clusterResults = clusterResults(clusterManager);
				
				
				double nni = nni(clusterManager, spatialPoints);
				
				List<Double> values = new LinkedList<Double>();
				
				values.add(clusterResults.get(0));
				values.add(nni);
				values.add(clusterResults.get(1));
				
				if(data.isLast()) {
					posTimeSeries = super.writeTimeSeriesSoFar(wktUpGeometry, upGeomtryHash, types, minimumDate, actualDate, values, connection, posTimeSeries, false);
					minimumDate =  TimeSeriesManager.next(actualDate).clone();
					if(Functions.areEqual(actualDate, TimeSeriesManager.maxDate)) {
						break;
					}
					else {	
						posTimeSeries = super.writeTimeSeriesSoFar(wktUpGeometry, upGeomtryHash, types, minimumDate, TimeSeriesManager.maxDate, values, connection, posTimeSeries, true);
						break;
					}
				}
				else {
					posTimeSeries = super.writeTimeSeriesSoFar(wktUpGeometry, upGeomtryHash, types, minimumDate, actualDate, values, connection, posTimeSeries, false);
					minimumDate =  TimeSeriesManager.next(actualDate).clone();
				}
			}

			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Timeseries buildTimeSeries(String sql, Entity granule) {
		return null;
	}

	@Override
	public void buildTimeSeriesBD(String sql, Entity granule, Map<String, Object> params) {
		// TODO Auto-generated method stub
	}

}
