package stevents_changes;

import stevents_changes.decay_functions.IDecayFunction;

public class AAValue {
	
	private double value;
	
	private int timeSpent;

	public AAValue(double value) {
		super();
		this.value = value;
		this.timeSpent = 0;
	}
	
	// There are several ways to combine the previous value with a new ocurrence
	// Here, in this function, we use the simple adition
	public void sumValue(double value) {
		this.value = this.value + value;
	}

	public double incTimeSpent(IDecayFunction f) {
		this.timeSpent++;
		this.value =  f.decay(timeSpent, value);
		
//		this.value =  value - f.decay(timeSpent, value);
		if(this.value < 0.0) this.value = 0;
		
//		System.out.println(value);
		return value;
	}
	
	public void resetTimeSpent() {
		this.timeSpent = 0;
	}
	
	public double getValue() {
		return value;
	}


}
