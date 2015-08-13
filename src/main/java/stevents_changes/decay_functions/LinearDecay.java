package stevents_changes.decay_functions;

public class LinearDecay implements IDecayFunction {

	private int L; // Number of time granules necessary for the the reduction factor gets zero
	
	public LinearDecay(int L) {
		this.L = L;
	}
	
	@Override
	public double decay(int timeSpent, double value) {
		double temp = (1 -((double)timeSpent/L)) * value; 
		
//		System.out.println(((double)timeSpent/L));
//		System.out.println( temp);
		
		return temp;
	}
}
