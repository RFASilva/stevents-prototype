package core.time_series.spatial_utilities.snn_bf.data.points;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import core.time_series.TimeSeriesFactory;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Distance;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.EuclideanDistance;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.GenericPoint;
import core.time_series.spatial_utilities.snn_bf.file.Reader;
import core.time_series.spatial_utilities.snn_bf.mspace.KdTree;
import core.time_series.spatial_utilities.snn_bf.mspace.MetricTree;
import core.time_series.spatial_utilities.snn_bf.util.Pair;

public class PointsWKTReader implements Reader<GenericPoint<Double>> {

	private String[] points;
	
	private Distance<Double, GenericPoint<Double>> distance;
	
	private MetricTree<GenericPoint<Double>> tree;
	
	private boolean allowsRep;
	
	private long numObjects;
	
	private long id;
	
	private int dimensions;
	
	public PointsWKTReader(String[] points, boolean allowsRep){
		this.distance = new EuclideanDistance<Double, GenericPoint<Double>>();
		this.tree = new KdTree<Double, GenericPoint<Double>>(distance);
		this.allowsRep = allowsRep;
		this.points = points;
		this.numObjects = 0;
		this.id = 0;
		this.dimensions = 0;
	}

	public long getNumObjects() {
		return numObjects;
	}
	
	public long getNumObjectsIns(){
		return id;
	}

	public int getNumDimensions() {
		return dimensions;
	}
	
	public Iterator<GenericPoint<Double>> getIterator() {
		return tree.iterator();
	}

	public long getNumberCalculations(){
		return distance.getNumberCalculations();
	}
	
	public void readNumObjsDim() {
//		String[] firstLine = in.nextLine().split(" ");

//		numObjects = Integer.parseInt(firstLine[0]);
//		dimensions = Integer.parseInt(firstLine[1]);
		
		numObjects = points.length;
		dimensions = 2;
	}
	
	public void readObjects(long numObjectsToRead){
		distance.resetNumberCalculations();
		WKTReader reader = new WKTReader(TimeSeriesFactory.geofact);
		for(int i = 0; i < numObjectsToRead; i++){
//			String[] line = in.nextLine().split(" ");
			
			Geometry geom = null;
			
			try {
				geom = reader.read(points[i]);
			} catch (ParseException e) {
			}
			
			GenericPoint<Double> point = 
					new GenericPoint<Double>(new Double(geom.getCoordinates()[0].x), new Double(geom.getCoordinates()[0].y), allowsRep);
			
			point.setId(id);

//			for(int j = 0; j < dimensions; j++) {
//				point.setCoord(j, Double.parseDouble(line[j]));
			
			Long result = tree.put(point);
			if(result != null)
				point.setId(result);
			else id++;
		}
	}
	
	public Map<Long, Set<Pair<Long,Double>>> getKNNs(int k){
		distance.resetNumberCalculations();
		
		Map<Long, Set<Pair<Long,Double>>> result = tree.getKNNs(k);
		
		return result;
	}
}
