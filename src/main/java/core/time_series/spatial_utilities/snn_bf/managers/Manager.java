package core.time_series.spatial_utilities.snn_bf.managers;

import java.io.IOException;
import java.sql.SQLException;

import core.time_series.spatial_utilities.snn_bf.db.DBConnectionException;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

/**
 * Interface of the primary and secondary memory modules.
 * 
 * @author Bruno Filipe Faustino
 * @since 25-06-2012
 */
public interface Manager {

	public void run(int k, int eps, int minPts, String outputName, 
			boolean resToPlot) throws SQLException, DbKValueException, 
			IOException, WekaPlotException, DBConnectionException;
	
	public void done(String outputName) throws IOException;
}
