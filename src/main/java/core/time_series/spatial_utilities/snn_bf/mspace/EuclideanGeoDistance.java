package core.time_series.spatial_utilities.snn_bf.mspace;

import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Distance;
import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Point;

public class EuclideanGeoDistance<Coord extends Number & Comparable<? super Coord>,
P extends Point<Coord>>
implements Distance<Coord, P>
{
	long numberCalculations;

	public double distance(P from, P to) {
		return StrictMath.sqrt(distance2(from, to));
	}
	
	@Override
	public double distance2(P from, P to) {
		updateNumberCalculations();
		
		return geoDistance(from,to);
	}

	public double geoDistance(P from, P to){
		int radius = 6371000;
		
		double dLat = StrictMath.toRadians((to.getCoord(0).doubleValue() - 
				from.getCoord(0).doubleValue()));
		double dLong = StrictMath.toRadians((to.getCoord(1).doubleValue() - 
				from.getCoord(1).doubleValue()));
		double fromLat = StrictMath.toRadians(from.getCoord(0).doubleValue());
		double toLat = StrictMath.toRadians(to.getCoord(0).doubleValue());
		
		double aux = StrictMath.sin(dLat/2) * StrictMath.sin(dLat/2) + 
				StrictMath.sin(dLong/2) * StrictMath.sin(dLong/2) * 
				StrictMath.cos(fromLat) * StrictMath.cos(toLat);
		double c = 2 * StrictMath.atan2(StrictMath.sqrt(aux), 
				StrictMath.sqrt(1-aux));
		
		return StrictMath.pow((radius * c), 2); 
	}
	
	/**
	 * Returns the number of distance calculations.
	 * 
	 * @return The number of distance calculations.
	 */
	public long getNumberCalculations(){
		return numberCalculations;
	}
	
	/**
	 * Increments the number of distance calculations.
	 */
	public void updateNumberCalculations(){
		numberCalculations++;
	}
	
	/**
	 * Resets the number of distance calculations.
	 */
	public void resetNumberCalculations(){
		numberCalculations = 0;
	}
}