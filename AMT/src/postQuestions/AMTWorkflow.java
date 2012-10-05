package postQuestions;

import java.io.*;
import java.util.*;

public class AMTWorkflow {

	
	
	private static int NUM_QUESTIONS = 50;
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
			
			// Assume that files are csv
			// Populate a list of citations
			Hashtable<String,ArrayList<CitationObject>> citationTable = new Hashtable<String,ArrayList<CitationObject>>();
			FileInputStream fInStream=null;
			
			int citationCount=0;
			try{
				fInStream = new FileInputStream(args[0]);
				System.out.println("Reading inputCSV to get list of questions to post");
				// Get the object of DataInputStream
				  DataInputStream in = new DataInputStream(fInStream);
				  BufferedReader br = new BufferedReader(new InputStreamReader(in));
				  String citationString="";
				  
				  // Opening file and reading input
				  
				  while ((citationString = br.readLine()) != null && citationCount < NUM_QUESTIONS)   {
					  if (!readCitationObject(citationString,citationTable))
						  throw new Exception("Error in CSV format");
					  citationCount++;
					  }
				  System.out.println("Read "+ citationCount + " citations from " + args[0]);
				 
				  br.close();
				  in.close();
				  fInStream.close();
				}
				catch (Exception e){
					printDelimiter();
					System.err.println("Error in reading file: "+ args[0]);
					System.out.println("Read "+ citationCount + " citations from " + args[0]);
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
				  writeOutCSVFile(args[1],citationTable);
			
	
	
	}
	private static void printDelimiter(){
		System.out.println(delimiter);
	}
	
	private static boolean readCitationObject(String citationString, Hashtable<String,ArrayList<CitationObject>> citationTable) throws Exception{
		StringTokenizer strTok=new StringTokenizer(citationString,",");
		if (strTok.countTokens()!=4){
			throw new Exception ("Expected 4 tokens in CSV line, got "+ strTok.countTokens());
		}
		CitationObject currCitation = new CitationObject(); 
		currCitation.setCitationID(strTok.nextToken());
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
		
		return true;
	}
	
	private static void postHITByXML(Hashtable<String,ArrayList<CitationObject>> citationTable) throws Exception{
		  AMTpost AMTposter = new AMTpost();
			for (String fileName:citationTable.keySet()){
			System.out.println("Posting file " +fileName +" as a HIT");
			if (AMTposter.hasEnoughFund()) {
				String currHitID=AMTposter.postQuestion(fileName);
				if (currHitID!=""){
			      System.out.println("Success posting file "+ fileName);
			      printDelimiter();
			      for (CitationObject citation:citationTable.get(fileName)){
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
	}
	
	private static void writeOutCSVFile(String fileName, Hashtable<String,ArrayList<CitationObject>> citationTable) throws IOException {
		   System.out.println("Writing to file named " + fileName );
		    Writer out = new OutputStreamWriter(new FileOutputStream(fileName));
		    try {
		    	for (ArrayList<CitationObject> question:citationTable.values()){
		    		for (CitationObject citation:question){
		    			out.write(citation.convertToOutSchema());
		    		}
		    	}
		    }
		    finally {
		      out.close();
		    }
	  }
}
