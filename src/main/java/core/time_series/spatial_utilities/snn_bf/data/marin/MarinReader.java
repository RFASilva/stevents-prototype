package core.time_series.spatial_utilities.snn_bf.data.marin;

import java.io.FileReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Distance;
import core.time_series.spatial_utilities.snn_bf.file.Reader;
import core.time_series.spatial_utilities.snn_bf.mspace.DistFunctionDistance;
import core.time_series.spatial_utilities.snn_bf.mspace.KdTree;
import core.time_series.spatial_utilities.snn_bf.mspace.MetricTree;
import core.time_series.spatial_utilities.snn_bf.util.Pair;

public class MarinReader implements Reader<MarinPoint>{

	private Scanner in;

	private Distance<Double, MarinPoint> distance;

	private MetricTree<MarinPoint> tree;

	private boolean allowsRep;

	private long numObjects;

	private long id;

	private int dimensions;

	public MarinReader(FileReader file, boolean allowsRep){
		this.in = new Scanner(file);

//		this.distance = new DistFunctionDistance(83466.49980299517, 0.90);
		this.distance = new DistFunctionDistance(82039.08643697982, 0.90);
//		this.distance = new EuclideanDistance<Double, MarinPoint>();
		this.tree = new KdTree<Double, MarinPoint>(distance);
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

	public Iterator<MarinPoint> getIterator() {
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
			String[] line = in.nextLine().split(",");

			int shipTypeID = Integer.parseInt(line[2]);

			if(shipTypeID >= 20 && shipTypeID <= 23){
				MarinPoint point = new MarinPoint(allowsRep);

				point.setId(id);

				point.setCoord(0, Double.parseDouble(line[4]));
				point.setCoord(1, Double.parseDouble(line[5]));
				point.setHeading(Integer.parseInt(line[6]));

				Long result = tree.put(point);
				if(result != null)
					point.setId(result);
				else id++;
			}
		}
	}

	public Map<Long, Set<Pair<Long,Double>>> getKNNs(int k){
		distance.resetNumberCalculations();
		
		Map<Long, Set<Pair<Long,Double>>> result = tree.getKNNs(k);
		
		return result;
	}
}
