package postQuestions;
import postQuestions.AMTWorkflow;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import com.amazonaws.mturk.requester.Assignment;
import com.amazonaws.mturk.requester.HIT;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.util.PropertiesClientConfig;


public class HITProcessor {

	private static  ArrayList<Tuple<String,Boolean>> hitTracker = null; 
	private static RequesterService requestPoller;
	private static BufferedWriter bufferedWriter;
	
	HITProcessor(){
		requestPoller = new RequesterService(new PropertiesClientConfig(AMTWorkflow.MTURK_CONFIG_FILE));
		hitTracker = new ArrayList<Tuple<String,Boolean>>();
	}
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		HITProcessor hitProcessor = new HITProcessor();
		int numAvailable=1;
		// time delay is 6 seconds
		final int timedelay=3000;
		
		if (args.length != 1){
			throw new Exception("Incorrect Usage. Usage is: java GetResultsForHITS fileNameWithHITIDS");
		}
		
		String inputFile=AMTWorkflow.CURRENT_RESOURCE_DIRECTORY+args[0];
		// Setting up file input reading 
		//hitProcessor.getReviewableHITs();
		FileInputStream fstream=null;
		try{
		fstream = new FileInputStream(inputFile);
		System.out.println("Reading HIT ID's input from file");
		// Get the object of DataInputStream
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  String citationText="";
		  
		  
		  
		  // Opening file and reading input
		  Set<String> hitList= new TreeSet<String>();
		  while ((citationText = br.readLine()) != null)   {
			  citationText.replaceAll("\\s","");
			  String [] citation=citationText.split(",");
			  if (citation!=null && citation.length==3){
				  String hitID= citation[2];
				  if (!hitList.contains(hitID)){
					  hitList.add(hitID);
					  hitTracker.add(new Tuple<String, Boolean>(hitID, false));
				  }
			  }
			  }
		 
		  br.close();
		  in.close();
		  fstream.close();
		}
		catch (Exception e){
			System.err.println("Error in reading file: "+ inputFile);
			e.printStackTrace();
			fstream.close();
		}
		  System.out.println("Reading HIT's finished");
		  System.out.println("No of HIT ID's :"  + hitTracker.size());
		
		 
		  
		  
		  // Looping and reading each HIT
		  while (hitTracker.size()>0){
		for (Tuple<String,Boolean> hitStatus:hitTracker){
			if ( hitStatus.y == false ){
				System.out.println("Getting HIT Results for Result ID :" + hitStatus.x);
				numAvailable=hitProcessor.getHITAnswer(hitStatus.x);
				System.out.println("Num available "+ numAvailable + "  hitTracker size :" + hitTracker.size());
				if (numAvailable==0){
						hitStatus.y=true;
						hitProcessor.writeHITOutput (hitStatus.x);	
				}
			}
		
		
//		try
//		  {
//		  Thread.sleep(timedelay*10000);  
//		 
//		  }catch (InterruptedException ie)
//		  {
//		  System.out.println(ie.getMessage());
//		  }
		
		}
		
		
		}
		
		//MessageFactory factory = MessageFactory.newInstance(SOAPConstants.DYNAMIC_SOAP_PROTOCOL);
		//SOAPMessage message = factory.createMessage();
	}
	
	// Write the XML section
	private void writeHITOutput (String hitID) throws IOException{
		//JAXBContext context = JAXBContext.newInstance(HITResults.class);
		//Marshaller m = context.createMarshaller();
		//m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		Assignment [] hitRetreived= requestPoller.getAllAssignmentsForHIT(hitID);
		FileWriter fwriter=null;
		
		
		try {
			fwriter = new FileWriter(AMTWorkflow.CURRENT_RESOURCE_DIRECTORY+hitID+".xml");
			bufferedWriter = new BufferedWriter(fwriter);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fwriter.close();
			bufferedWriter.close();
			return;
		}
		System.out.println("Writing to XML");
		for (Assignment currAssignment:hitRetreived){
			//System.out.println("Answer   "  + currAssignment.getAnswer());
			try {
				fwriter.write(currAssignment.getAnswer());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				fwriter.close();
				bufferedWriter.close();
				return;
			}
			
		    //m.marshal(currAssignment, System.out);
		}
		
		fwriter.close();
		bufferedWriter.close();
	} 
	

	 private int getHITAnswer(String hitID){
		 	int numberAssignmentsRemaining= Integer.MAX_VALUE;
			int numberAssignmentsPending= Integer.MAX_VALUE;
				
			HIT hitRetreived= requestPoller.getHIT(hitID);
			System.out.println("Status of this HIT"+ hitRetreived.getHITReviewStatus());
			numberAssignmentsRemaining=hitRetreived.getNumberOfAssignmentsAvailable();
			numberAssignmentsPending=hitRetreived.getNumberOfAssignmentsPending();
			if (numberAssignmentsRemaining==0 && numberAssignmentsPending==0){
				System.out.println("All assignments completed, writing..");
				//HIT.getDeserializer(arg0, arg1, arg2)
			}
			else System.out.println("No of assignments available for this hit: " + numberAssignmentsRemaining);
			return numberAssignmentsRemaining+numberAssignmentsPending;
		
	}
	 
	 // wrapper for HIT call to get all reviewable HIT's
	 private void getReviewableHITs(){
		 int reviewPageSize=0;
		 int pageNumber=1;
		 
		 do{
			 try{
			 HIT [] hitArray=requestPoller.getReviewableHITs(null, pageNumber);
			 reviewPageSize=hitArray.length;
			 pageNumber++;
			 for (HIT currHIT:hitArray){
				 hitTracker.add(new Tuple<String,Boolean>(currHIT.getHITId(),false));
			 }
			 }
			 catch (NullPointerException n){
				 break;
			 }
		 }while (reviewPageSize>0);
	 }

}
 	