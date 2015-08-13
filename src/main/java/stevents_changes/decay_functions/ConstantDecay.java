package stevents_changes.decay_functions;

public class ConstantDecay implements IDecayFunction {

	private double valueDecay; // Number of time granules necessary for the the reduction factor gets zero
	
	public ConstantDecay(double valueDecay) {
		this.valueDecay = valueDecay;
	}
	
	@Override
	public double decay(int timeSpent, double value) {
		return valueDecay;
	}
}
