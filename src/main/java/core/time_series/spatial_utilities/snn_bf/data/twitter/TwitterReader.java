package core.time_series.spatial_utilities.snn_bf.data.twitter;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import mspace.KdTree;
import mspace.MetricTree;
import mspace.TwitterEuclideanDistance;
import util.Pair;

import com.savarese.spatial.Distance;

import file.Reader;


public class TwitterReader implements Reader<TwitterPoint>{

	private Scanner in;

	private Distance<Double, TwitterPoint> distance;

	private MetricTree<TwitterPoint> tree;

	private boolean allowsRep;

	private long numObjects;

	private long id;

	private int dimensions;
	
	public TwitterReader(FileReader file, boolean allowsRep){
		this.in = new Scanner(file);

		this.distance = new TwitterEuclideanDistance<Double, TwitterPoint>();
		this.tree = new KdTree<Double, TwitterPoint>(distance);
		this.allowsRep = allowsRep;

		this.numObjects = 0;
		this.id = 0;
		this.dimensions = 0;
	}
	
	public long getNumObjects() {
		return numObjects;
	}

	public long getNumObjectsIns() {
		return id;
	}

	public int getNumDimensions() {
		return dimensions;
	}

	public Iterator<TwitterPoint> getIterator() {
		return tree.iterator();
	}
	
	public long getNumberCalculations(){
		return distance.getNumberCalculations();
	}

	public void readNumObjsDim() {
		String[] firstLine = in.nextLine().split(" ");

		numObjects = Integer.parseInt(firstLine[0]);
		dimensions = Integer.parseInt(firstLine[1]);
	}

	public void readObjects(long numObjectsToRead){
		distance.resetNumberCalculations();
		
		for(long i = 0; i < numObjectsToRead; i++){
			String[] line = in.nextLine().split(" ");
			
			TwitterPoint point = new TwitterPoint(allowsRep);
			
			point.setId(id);

			point.setCoord(0, Double.parseDouble(line[0]));
			point.setCoord(1, Double.parseDouble(line[1]));
			point.setCoord(2, Double.parseDouble(line[2]));
			
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
