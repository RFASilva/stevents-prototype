package core.time_series.spatial_utilities.snn_bf.util;

public class Timer {

	private long startTime;
	private long endTime;
	
	public Timer(){
		this.startTime = 0;
		this.endTime = 0;
	}
	
	public void start(){
		startTime = System.nanoTime();
	}
	
	public void end(){
		endTime = System.nanoTime();
	}
	
	public float getTimeSpent(){
		return (float)(endTime - startTime) / 1000000000;
	}
}
