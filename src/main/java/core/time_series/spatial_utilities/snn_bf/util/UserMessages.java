package core.time_series.spatial_utilities.snn_bf.util;

public final class UserMessages {
	
	public static void pleaseInsert(String insert){
		System.out.println("[SNN] Input: " + insert);
	}
	
	public static void buildingTree(long size){
		System.out.println("[SNN] Building the Metric Tree with " + size + 
				" objects read from file.");
	}
	
	public static void builtTree(long size){
		System.out.println("[SNN] Built the Metric Tree. Tree has " + size + 
				" unique objects.");
	}
	
	public static void gettingKNNFromDB(){
		System.out.println("[SNN] Getting all the K Nearest Neighbours " +
				"stored in the database.");
	}
	
	public static void gettingKNNs(int k){
		System.out.println("[SNN] Getting " + k + " Nearest Neighbours.");
	}
	
	public static void hasKNNs(int k){
		System.out.println("[SNN] Has " + k + " Nearest Neighbours.");
	}

	public static void startingSNN(int k, int eps, int minPts){
		System.out.println("[SNN] Starting SNN algorithm with k = " + k + 
				", eps = " + eps + " and minPts = " + minPts);
	}
	
	public static void endedSNN(long numClusters, long numObjects,
			long numNoise){
		System.out.println("[SNN] Ended SNN algorithm with " + numClusters + 
				" clusters constituted by " + numObjects + " objects. " + 
				numNoise + " objects were defined as noise.");
	}
	
	public static void givingOutputFilePlot(String outputName, 
			boolean resToPlot){
		if(!outputName.equals("") && resToPlot)
			System.out.println("[SNN] Writing file and Plotting the clustering " +
					"results.");
		else if(!outputName.equals("") && !resToPlot)
			System.out.println("[SNN] Writing file with the clustering results.");
		else if(outputName.equals("") && resToPlot)
			System.out.println("[SNN] Plotting the clustering results via Weka.");
	}
	
	public static void gaveOutputFilePlot(String outputName, 
			boolean resToPlot){
		if(!outputName.equals("") && resToPlot)
			System.out.println("[SNN] Wrote file and did the Plot with the clustering " +
					"results.");
		else if(!outputName.equals("") && !resToPlot)
			System.out.println("[SNN] Wrote file with the clustering results.");
		else if(outputName.equals("") && resToPlot)
			System.out.println("[SNN] Did the Plot with the clustering results via Weka.");
	}
	
	public static void nextIteration(){
		System.out.println("[SNN] Input: Perform another iteration of the SNN algorithm?");
	}
	
	public static void errorAccessingDB(){
		System.out.println("[SNN] Error: While connecting/accessing the " +
				"database.");
	}
	
	public static void errorInKValue(){
		System.out.println("[SNN] Error: Number of Neighbours stored in the " +
				"database was exceeded.");
	}
	
	public static void errorInPlotting(){
		System.out.println("[SNN] Error: While plotting the clustering " +
				"results.");
	}
	
	public static void errorIOToFile(){
		System.out.println("[SNN] Error: While reading/writing to file.");
	}
	
	public static void errorInitalParameters(){
		System.out.println("[SNN] Error: useTestScript pmStorage allowsRep");
	}
}
