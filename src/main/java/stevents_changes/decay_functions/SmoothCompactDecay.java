package stevents_changes.decay_functions;

public class SmoothCompactDecay implements IDecayFunction {

	public double L;
	public double K;
	
	public SmoothCompactDecay(double l, double k) {
		L = l;
		K = k;
	}

	@Override
	public double decay(int timeSpent, double value) {
		return Math.exp(K - (K / (1 - Math.pow((timeSpent/L), 2)) )) * value;
	}

}
