package core.time_series.spatial_utilities.snn_bf.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class SQLCreator {

	private BufferedWriter out;
	
	public SQLCreator() {
		setFile();
	}
	
	private void setFile() {
		FileWriter fileOut;
		try {
			fileOut = new FileWriter("data_db.sql");
			this.out = new BufferedWriter(fileOut);
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		}	
	}
	
	public void beginTransaction(){
		try {
			out.write("START TRANSACTION;\n\n");
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		}
	}
	
	public void endTransaction(){
		try {
			out.write("\nCOMMIT;\n");
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		}
	}
	
	public void createDataTable(ArrayList<String> dimensions){
		String dropTable = "DROP TABLE IF EXISTS objects;\n";
		
		String createTable = "CREATE TABLE objects (\n\tid BIGINT PRIMARY " +
				"KEY,\n";
		
		for(int i = 0; i < dimensions.size(); i++){
			createTable += "\t";
			createTable += dimensions.get(i);
			createTable += ",";
			createTable += "\n";
		}
		
		createTable = createTable.substring(0, createTable.lastIndexOf(','));
		
		dropTable += createTable;
		dropTable += "\n);\n\n";
		
		try {
			out.write(dropTable);
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		}
	}
	
	public void createKNNTable(){
		String dropTable = "\nDROP TABLE IF EXISTS knns;\n" ;
		
		String createTable = "CREATE TABLE knns (\n\tid BIGINT,\n\tnn BIGINT" +
				",\n\tdistance DOUBLE NOT NULL\n, PRIMARY KEY (id,nn));\n\n";
		
		dropTable += createTable;
		
		try {
			out.write(dropTable);
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		}
	}
	
	public void insertRow(String tableName, String rowContent){
		String row = "INSERT INTO ";
		row += tableName;
		row += " VALUES (";
		row += rowContent;
		row += ");\n";
		
		try {
			out.write(row);
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		}
	}
	
	public void close(){
		try {
			out.close();
		} catch (IOException e) {
			UserMessages.errorIOToFile();
		}
	}
}
