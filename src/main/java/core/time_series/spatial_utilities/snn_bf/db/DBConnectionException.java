package core.time_series.spatial_utilities.snn_bf.db;

public class DBConnectionException extends Exception{

	private static final long serialVersionUID = 1L;

	public DBConnectionException(){
        super("Error while accessing/connecting with the database.");
    }
}
