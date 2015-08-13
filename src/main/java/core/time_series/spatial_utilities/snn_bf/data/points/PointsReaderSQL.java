package core.time_series.spatial_utilities.snn_bf.data.points;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Distance;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.GenericPoint;
import core.time_series.spatial_utilities.snn_bf.file.Reader;
import core.time_series.spatial_utilities.snn_bf.mspace.EuclideanGeoDistance;
import core.time_series.spatial_utilities.snn_bf.mspace.KdTree;
import core.time_series.spatial_utilities.snn_bf.mspace.MetricTree;
import core.time_series.spatial_utilities.snn_bf.util.Pair;
import core.time_series.spatial_utilities.snn_bf.util.SQLCreator;

public class PointsReaderSQL implements Reader<GenericPoint<Double>>{

	private Scanner in;
	
	private SQLCreator sqlCreator;
	
	private ArrayList<String> dimensionsSQL;
	
	private Distance<Double, GenericPoint<Double>> distance;
	
	private MetricTree<GenericPoint<Double>> tree;
	
	private boolean allowsRep;
	
	private long numObjects;
	
	private long id;
	
	private int dimensions;
	
	public PointsReaderSQL(FileReader file, boolean allowsRep){
		this.in = new Scanner(file);
		
		this.sqlCreator = new SQLCreator();
		this.dimensionsSQL = new ArrayList<String>();
		
		this.distance = new EuclideanGeoDistance<Double, 
				GenericPoint<Double>>();
		this.tree = new KdTree<Double, GenericPoint<Double>>(distance);
		this.allowsRep = allowsRep;
		
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
		String[] firstLine = in.nextLine().split(" ");

		numObjects = Integer.parseInt(firstLine[0]);
		dimensions = Integer.parseInt(firstLine[1]);
		
		String[] secondLine = in.nextLine().split(", ");
		
		for(int i = 0; i < secondLine.length; i++){
			dimensionsSQL.add(secondLine[i]);
		}
	}
	
	public void readObjects(long numObjectsToRead){
		distance.resetNumberCalculations();
		
		sqlCreator.createDataTable(dimensionsSQL);
		
		sqlCreator.beginTransaction();
		
		for(long i = 0; i < numObjectsToRead; i++){
			String[] line = in.nextLine().split(" ");
			
			GenericPoint<Double> point = 
					new GenericPoint<Double>(dimensions, allowsRep);
			
			point.setId(id);

			for(int j = 0; j < dimensions; j++)
				point.setCoord(j, Double.parseDouble(line[j]));
			
			Long result = tree.put(point);
			if(result != null)
				point.setId(result);
			else {
				String rowContent = String.format("'%d', '%f', '%f'",
						id, point.getCoord(0), point.getCoord(1));
				
				sqlCreator.insertRow("objects", rowContent);
				
				id++;
			}
		}
		
		sqlCreator.endTransaction();
	}
	
	public Map<Long, Set<Pair<Long,Double>>> getKNNs(int k){
		distance.resetNumberCalculations();
		
		Map<Long, Set<Pair<Long,Double>>> knns = tree.getKNNs(k);
		
		outputSQLKnns(knns);
		
		return knns;
	}
	
	private void outputSQLKnns(Map<Long, Set<Pair<Long,Double>>> knns){
		Map<Long, String> orderedKnns = new TreeMap<Long,String>();
		
		Iterator<Long> it = knns.keySet().iterator();
		
		String sqlKnn = "";
	
		while(it.hasNext()){
			Long current = it.next();
			Set<Pair<Long,Double>> ite = knns.get(current);
			
			for(Pair<Long,Double> one : ite){
				sqlKnn += "'";
				sqlKnn += one.getLeft();
				sqlKnn += "', '";
				sqlKnn += one.getRight();
				sqlKnn += "'";
				sqlKnn += ";";
			}
			
			orderedKnns.put(current, sqlKnn);
			sqlKnn = "";
		}
		
		it = orderedKnns.keySet().iterator();
		
		sqlCreator.createKNNTable();
		
		sqlCreator.beginTransaction();
		
		while(it.hasNext()){
			Long currentId = it.next();
			String stringKnns[] = orderedKnns.get(currentId).split(";");
			
			for(int i = 0; i < stringKnns.length; i++){
				String rowContent = String.format("'%d', ", currentId);
				rowContent += stringKnns[i];
				sqlCreator.insertRow("knns", rowContent);
			}
		}
		
		sqlCreator.endTransaction();
		
		sqlCreator.close();
	}
}
