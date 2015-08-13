package core.time_series.spatial_utilities.snn_bf.managers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import core.time_series.spatial_utilities.snn_bf.db.DBConnectionException;
import core.time_series.spatial_utilities.snn_bf.util.Timer;
import core.time_series.spatial_utilities.snn_bf.util.UserMessages;
import core.time_series.spatial_utilities.snn_bf.weka.WekaPlotException;

public class SNNController {

	private Manager mg;
	
	private Timer timer;
	
	private List<Set<Float>> times;
	
	private boolean pmStorage;
	
	private boolean handlesRep;
	
	private String testScript;
	
	private int numRepetitions;
	
	private Scanner in;
	
	private String datasetName;
	
	private Long numObjsToRead;
	
	public SNNController(boolean pmStorage, boolean handlesRep){
		this.timer = new Timer();
		this.times = new ArrayList<Set<Float>>();

		this.pmStorage = pmStorage;
		this.handlesRep = handlesRep;
		this.testScript = "";
		this.numRepetitions = 1;
		
		this.datasetName = "";
		this.numObjsToRead = new Long(0);
	}
	
	public SNNController(boolean pmStorage, boolean handlesRep, 
			String testScript, int numRepetitions){
		this.timer = new Timer();
		this.times = new ArrayList<Set<Float>>();

		this.pmStorage = pmStorage;
		this.handlesRep = handlesRep;
		this.testScript = testScript;
		this.numRepetitions = numRepetitions;
		
		this.datasetName = "";
		this.numObjsToRead = new Long(0);
	}
	
	public void run() throws IOException, SQLException, DbKValueException, 
			WekaPlotException, DBConnectionException {
		
		for(int i = 0; i < numRepetitions; i++){
			this.in = setScanner(testScript);
			datasetName = setUp();
			
			boolean nextIteration;
			int iterationNumber = 1;
			
			Set<Float> iterationTimes = new LinkedHashSet<Float>();
			
			do{
				UserMessages.pleaseInsert("k eps minPts");
				
				String[] args = in.nextLine().split(" ");
				
				int k = Integer.parseInt(args[0]);
				int eps = Integer.parseInt(args[1]);
				int minPts = Integer.parseInt(args[2]);
				
				UserMessages.pleaseInsert("resultToFile resultToPlot");
				
				String[] flags = in.nextLine().split(" ");
				
				boolean resToFile = (Integer.parseInt(flags[0]) == 1);
				boolean resToPlot = (Integer.parseInt(flags[1]) == 1);
				
				String outputName = setOutputName(resToFile, datasetName, iterationNumber);
				
				timer.start();
				
				mg.run(k, eps, minPts, outputName, resToPlot);
				
				timer.end();
				
				iterationTimes.add(timer.getTimeSpent());
				
				UserMessages.nextIteration();
				
				String answer = in.nextLine();
				
				nextIteration = (Integer.parseInt(answer) == 1);
		
				iterationNumber++;
			}
			while(nextIteration);
			
			times.add(iterationTimes);
		}
		
		outputTimes(datasetName, numObjsToRead);
	}
	
	private Scanner setScanner(String fileNameTestScript) 
			throws FileNotFoundException{

		if(!fileNameTestScript.isEmpty()){
			FileReader fileIn = new FileReader(new File(fileNameTestScript));
			
			return new Scanner(fileIn);
		}
		else return new Scanner(System.in);
	}
	
	private String setOutputName(boolean resToFile, String name,
			int iterationNumber) throws IOException{
		if(resToFile)
			return name + ".clus" + iterationNumber + ".arff";
		else return "";
	}
	
	private String setUp() throws IOException{
		if(pmStorage)
			return setUpPrimary();
		else return setUpSecondary();
	}
	
	private String setUpPrimary() throws IOException{
		UserMessages.pleaseInsert("fileNameToRead numObjsToRead");
		String[] fileProp = in.nextLine().split(" ");
		
		FileReader fileIn = new FileReader(new File(fileProp[0]));
		numObjsToRead = Long.parseLong(fileProp[1]);
		
		mg = new PSManager(fileIn, numObjsToRead, handlesRep);
		
		return fileProp[0];
	}
	
	private String setUpSecondary() throws IOException{	
		UserMessages.pleaseInsert("nameOfTheDataset");
		String fileToOutput = in.nextLine();
		
		mg = new SSManager();
		
		return fileToOutput;
	}
	
	private void outputTimes(String outputName, Long numObjsToRead) 
			throws IOException{
		FileWriter fileOut = new FileWriter(outputName + ".times." + 
				numObjsToRead + ".csv");
		BufferedWriter out = new BufferedWriter(fileOut);
		
		String content = getTimesContent();
		
		out.write(content);
		out.close();
	}
	
	private String getTimesContent(){
		Iterator<Set<Float>> it = times.iterator();
		String result = "";
		
		while(it.hasNext()){
			Iterator<Float> itFloat = it.next().iterator();

			while(itFloat.hasNext()){
				result += itFloat.next();
				result += "; ";
			}
			
			result = result.substring(0, result.length()-2);
			result += "\n";
		}
		
		return result;
	}
}
