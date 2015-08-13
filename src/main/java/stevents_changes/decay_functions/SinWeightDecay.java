package stevents_changes.decay_functions;

public class SinWeightDecay implements IDecayFunction {
	
	public SinWeightDecay() {
		
	}

	@Override
	public double decay(int timeSpent, double value) {
		
		if(timeSpent == 0)
			return 0;
		return Math.sin(1/Math.pow(timeSpent, 0.5)) * value;
	}

}
