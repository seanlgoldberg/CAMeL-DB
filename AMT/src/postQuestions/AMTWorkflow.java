package postQuestions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;


public class AMTWorkflow {
	public static final String MTURK_CONFIG_FILE="src/mturk.properties";
	public static final String CURRENT_RESOURCE_DIRECTORY="src/postQuestions/";
	public static final String postedCitationFileName=CURRENT_RESOURCE_DIRECTORY+"posted.txt";
        public static final String XML_DIRECTORY = "QuestionXML/";
	
	private static int NUM_QUESTIONS = 140;
	private static String delimiter = "***********************************";
	/**
	 * @param args inputCSV outputCSV
	 * @param inputCSV Input file - CSV file of schema (Citation_ID, position, filename,fileposition) 
	 * @param outputCSV Output file CSV file with a schema (Citation_ID,position,HITID).
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
			if (args.length!=2)
				throw new Exception("Enter the correct number of arguments 2- InputCSVfileName, OutputCSVFileName");
			
			// Assume that files are csv, set the correct relative path
			String currentInputFile=CURRENT_RESOURCE_DIRECTORY+args[0];
			String currentOutputFile=CURRENT_RESOURCE_DIRECTORY+args[1];
			// Populate a list of citations
			Hashtable<String,ArrayList<CitationObject>> citationTable = new Hashtable<String,ArrayList<CitationObject>>();
			FileInputStream fInStream=null;
			ArrayList<String> postedCitationList = readInPostedCitationFile(postedCitationFileName);
			int citationCount=0;
			try{
				fInStream = new FileInputStream(currentInputFile);
				System.out.println("Reading inputCSV to get list of questions to post");
				// Get the object of DataInputStream
				  DataInputStream in = new DataInputStream(fInStream);
				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
				  String citationString="";
				  
				  // Opening file and reading input
				  
				  while ((citationString = br.readLine()) != null && citationCount < NUM_QUESTIONS)   {
					  if (!readCitationObject(citationString, postedCitationList, citationTable))
						  throw new Exception("Error in CSV format");
					  citationCount++;
					  }
				  System.out.println("Read "+ citationCount + " citations from " +currentInputFile);
				  System.out.println("Number of new citations found :"+ postedCitationList.size());
				  br.close();
				  in.close();
				  fInStream.close();
				}
				catch (Exception e){
					printDelimiter();
					System.err.println("Error in reading file: "+currentInputFile);
					System.out.println("Read "+ citationCount + " citations from " +currentInputFile);
					e.printStackTrace();
					fInStream.close();
					System.exit(0);
				}
			
			
				  System.out.println("Reading question list file finished");
				  printDelimiter();
				  System.out.println("Now posting files.....");
				  printDelimiter();
				  
				  
				  //Start posting files here
				  postHITByXML(citationTable);
				  
				  // Saving to schema
				  writeOutCSVFile(currentOutputFile,citationTable);
			
	
	
	}
	/**
	 * Prints a generic delimiter to enhance output
	 */
	private static void printDelimiter(){
		System.out.println(delimiter);
	}
	/**
	 * 
	 * @param citationString A string which is a comma seperated string of (CitationID, position, filename, filenamePosition)
	 * @param postedCitationList A list of citationID's already posted
	 * @param citationTable A table into which a citation is read
	 * @return True if success in reading citation
	 * @throws Exception If incorrect no. of tokens found
	 */
	private static boolean readCitationObject(String citationString, List<String> postedCitationList,Hashtable<String,ArrayList<CitationObject>> citationTable) throws Exception{
		StringTokenizer strTok=new StringTokenizer(citationString,",");
		if (strTok.countTokens()!=4){
			throw new Exception ("Expected 4 tokens in CSV line, got "+ strTok.countTokens());
		}
		
		String citationID=strTok.nextToken();
		
		// If citation is already posted , dont add it
		if ( !postedCitationList.contains(citationID)){
		CitationObject currCitation = new CitationObject();
		currCitation.setCitationID(citationID);
		currCitation.setPosition(strTok.nextToken());
		
		
		//inserting the citation to a map indexed by question number
		ArrayList<CitationObject> citationList = null;
		String fileToPost =strTok.nextToken();
		if ((citationList=citationTable.get(fileToPost))!=null)
			citationList.add(currCitation);
		else {
			citationList= new ArrayList<CitationObject>();
			citationList.add(currCitation);
			citationTable.put(fileToPost, citationList);
		}
		}
		return true;
	}
	/**
	 * 
	 * @param citationTable Contains list of questions mapped to citations 
	 * @throws Exception If posting was unsuccesful
	 */
	private static void postHITByXML(Hashtable<String,ArrayList<CitationObject>> citationTable) throws Exception{
		String postedCitations="";
		  AMTpost AMTposter = new AMTpost();
			for (String fileName:citationTable.keySet()){
				
				
				// Setting the filename to the relative resource directory
				String currFileName= CURRENT_RESOURCE_DIRECTORY + XML_DIRECTORY + fileName;
				
			System.out.println("Posting file " +currFileName +" as a HIT");
			if (AMTposter.hasEnoughFund()) {
				String currHitID=AMTposter.postQuestion(currFileName);
				if (currHitID!=""){
			      System.out.println("Success posting file "+ currFileName);
			      printDelimiter();
			      // NOTE : Use original fileName to get citation from table
			      for (CitationObject citation:citationTable.get(fileName)){
			    	  postedCitations+=(citation.getCitationID()+System.getProperty("line.separator"));
			    	  citation.setHITID(currHitID);
			      }
				}
				else {
					
					System.out.println("Error in posting file, no valid hitID received");
				}
				printDelimiter();
			    } else {
			      System.out.println("You do not have enough funds to create the HIT.");
			}
			}
			System.out.println(postedCitations);
			writeToPostedCitationFile(postedCitationFileName, postedCitations);
	}
	/**
	 * 
	 * @param fileName File to write schema(citationid,position,hitID)
	 * @param citationTable Table containing map of citation objects to post by Filename
	 * @throws IOException If cannot read file
	 */
	private static void writeOutCSVFile(String fileName, Hashtable<String,ArrayList<CitationObject>> citationTable) throws IOException {
		   System.out.println("Writing to file named " + fileName );
		    Writer out=null;
		    File outCSVFile = new File(fileName);
			if ((outCSVFile.exists() && !outCSVFile.isDirectory())){
		    out = new OutputStreamWriter(new FileOutputStream(fileName,true));
			}
			else out = new OutputStreamWriter(new FileOutputStream(fileName));
		    try {
		    	for (ArrayList<CitationObject> question:citationTable.values()){
		    		for (CitationObject citation:question){
		    			if (!citation.getHITID().equals("")){
		    				out.write(citation.convertToOutSchema());
		    			}
		    		}
		    	}
		    }
		    finally {
		      out.close();
		    }
	  }
	/**
	 * 
	 * @param fileName File where posted citations are stored
	 * @return List of posted citations
	 * @throws IOException File does not exist or cannot be read correctly
	 */
	private static ArrayList<String> readInPostedCitationFile(String fileName) throws IOException{
		ArrayList<String> postedCitationsList= new ArrayList<String>() ;
		File postedCitationFile = new File(fileName);
		if ((postedCitationFile.exists() && !postedCitationFile.isDirectory())){
		FileInputStream fInStream=null;
		try{
			fInStream = new FileInputStream(fileName);
			System.out.println("Getting list of citations already posted");
			// Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fInStream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String postedCitations="";
			  
			  // Opening file and reading input
			  int postedCitationCount=0;
			  while ((postedCitations = br.readLine()) != null )   {
				  postedCitationsList.add(postedCitations);
				  postedCitationCount++;
				  }
			  //System.out.println("Read "+ postedCitationCount + " citations from " +fileName);
			 
			  br.close();
			  in.close();
			  fInStream.close();
			  printDelimiter();
			  return postedCitationsList;
			}
			catch (Exception e){
				
				System.out.println("Error in reading file: "+fileName);
				e.printStackTrace();
				fInStream.close();
				printDelimiter();
				return postedCitationsList;
			}
		}
		return postedCitationsList;
	}
	/**
	 * 
	 * @param fileName Name of file which has posted Citations 
	 * @param citationsPosted string of posted citations in file
	 * @throws IOException
	 */
	public static void writeToPostedCitationFile(String fileName, String citationsPosted) throws IOException{
		System.out.println("Writing posted citations to file named " + fileName );
		Writer out;
		File postedCitationFile = new File(fileName);
		if ((postedCitationFile.exists() && !postedCitationFile.isDirectory())){
	    out = new OutputStreamWriter(new FileOutputStream(fileName,true));
		}
		else out = new OutputStreamWriter(new FileOutputStream(fileName));
	    try {
	    	out.write(citationsPosted);
	    }
	    catch(Exception e){
	    	System.out.println("Error in adding list of posted citations to file: "+fileName);
	    }
	    finally {
	      out.close();
	      printDelimiter();
	    }

	}
}
