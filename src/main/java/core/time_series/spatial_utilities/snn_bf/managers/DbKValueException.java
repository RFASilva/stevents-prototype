package core.time_series.spatial_utilities.snn_bf.managers;

public class DbKValueException extends Exception {

	private static final long serialVersionUID = 1L;

	public DbKValueException(int k){
        super("Number of Neighbours (" + k + 
        		") stored in the database was exceeded.");
    }
}
