package core.time_series.spatial_utilities.snn_bf.mspace;


import core.time_series.spatial_utilities.snn_bf.com.savarese.spatial.Distance;
import core.time_series.spatial_utilities.snn_bf.data.marin.MarinPoint;

public class DistFunctionDistance implements Distance<Double, MarinPoint> {

	private double mDist;
	private int mBearing;
	private double weight;
	
	long numberCalculations;
	
	public DistFunctionDistance(Double mDist, Double weight){
		this.mDist = mDist;
		this.mBearing = 180;
		this.weight = weight;
	}
	
	@Override
	public double distance(MarinPoint from, MarinPoint to) {
		return StrictMath.sqrt(distance2(from, to));
	}

	@Override
	public double distance2(MarinPoint from, MarinPoint to) {
		double dist = geoDistance(from, to) / mDist;
		
		double bearing = phi(from, to) / mBearing; 
		
		double result = weight * dist + (1 - weight) * bearing;
		
		updateNumberCalculations();
		
		return result * result;
	}	

	public double geoDistance(MarinPoint from, MarinPoint to){
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
		
		return radius * c; 
	}
	
	public double phi(MarinPoint from, MarinPoint to){
		double abs = StrictMath.abs(from.getHeading() - to.getHeading());
		
		if(abs <= 180)
			return abs;
		else return 360 - abs;
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
