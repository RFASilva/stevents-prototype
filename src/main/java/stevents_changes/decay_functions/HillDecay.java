package stevents_changes.decay_functions;

public class HillDecay implements IDecayFunction {

	public double L;
	public double K;

	public HillDecay(double l, double k) {
		L = l;
		K = k;
	}

	@Override
	public double decay(int timeSpent, double value) {
		return (1 / (1 + Math.pow(timeSpent/L, K))) * value;
	}
	
	

}
