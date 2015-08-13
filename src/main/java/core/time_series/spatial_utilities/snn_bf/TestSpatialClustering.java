package core.time_series.spatial_utilities.snn_bf;

import java.io.IOException;
import java.sql.SQLException;

import core.time_series.spatial_utilities.snn_bf.db.DBConnectionException;
import core.time_series.spatial_utilities.snn_bf.managers.DbKValueException;
import core.time_series.spatial_utilities.snn_bf.managers.SNNController;
import core.time_series.spatial_utilities.snn_bf.util.UserMessages;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

public class TestSpatialClustering {

	/**
	 * @param useTestScript - 1 to use a file with the user input (automatic)
	 * or 0 to use console (manual).
	 * @param pmStorage - 1 to work with the primary storage system or 0
	 * to work with the secondary storage system.
	 * @param allowsRep - 1 to accept and handle repetitions in the dataset 
	 * or 0 otherwise.
	 */
	public static void main(String[] args) {
		
		try{
			if(args.length == 2){
				boolean pmStorage = (Integer.parseInt(args[0]) == 1);
				boolean allowsRep = (Integer.parseInt(args[1]) == 1);

				SNNController ctrl = 
						new SNNController(pmStorage, allowsRep);

				ctrl.run();
			}
			else if(args.length == 4){
				boolean pmStorage = (Integer.parseInt(args[0]) == 1);
				boolean allowsRep = (Integer.parseInt(args[1]) == 1);
				String testScript = args[2];
				int numRepetitions = Integer.parseInt(args[3]);
				
				SNNController ctrl = 
						new SNNController(pmStorage, allowsRep, testScript, 
								numRepetitions);

				ctrl.run();
			}
			else UserMessages.errorInitalParameters();
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		} catch (SQLException e) {
			UserMessages.errorAccessingDB();
		} catch (DBConnectionException e) {
			UserMessages.errorAccessingDB();
		} catch (DbKValueException e) {
			UserMessages.errorInKValue();
		} catch (WekaPlotException e) {
			UserMessages.errorInPlotting();
		}
	}
}
