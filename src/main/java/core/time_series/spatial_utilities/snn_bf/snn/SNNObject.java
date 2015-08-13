package core.time_series.spatial_utilities.snn_bf.snn;

public class SNNObject {

	private long cluster;
	private int density;
	private boolean noise;
	
	public SNNObject(){
		cluster = -1;
		density = -1;
		noise = true;
	}
	
	public long getCluster() {
		return cluster;
	}
	
	public void setCluster(long cluster) {
		this.cluster = cluster;
	}
	
	public int getDensity() {
		return density;
	}
	
	public void setDensity(int density) {
		this.density = density;
	}
	
	public boolean isNoise() {
		return noise;
	}
	
	public void setNoise(boolean noise) {
		this.noise = noise;
	}	
}