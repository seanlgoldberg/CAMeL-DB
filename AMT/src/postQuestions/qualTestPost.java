/*
 * Copyright 2007-2008 Amazon Technologies, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://aws.amazon.com/apache2.0
 * 
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */ 


package postQuestions;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import com.amazonaws.mturk.requester.QualificationType;
import com.amazonaws.mturk.requester.QualificationTypeStatus;
import com.amazonaws.mturk.service.axis.RequesterService;
import com.amazonaws.mturk.service.axis.RequesterServiceRaw;
import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;
/**
 * The MTurk Hello World sample application creates a simple HIT via the Mechanical Turk 
 * Java SDK. mturk.properties must be found in the current file path.
 */
public class qualTestPost {

  private int NUM_QUESTIONS = 50;	
	
  private RequesterService service;
  

  // Defining the attributes of the HIT to be created
  //private String title = "Bibliographic Labeling";
  //private String description = 
  //  "Tagging a set of bibliographic data.";
  //private int numAssignments = 5;
  //private double reward = 0.10;

  
  private String qualDescription = "This is a pre-qualification question.";
  
  /**
   * Constructor
   * 
   */
  public qualTestPost() {
    service = new RequesterService(new PropertiesClientConfig(AMTWorkflow.MTURK_CONFIG_FILE));
  }

  /**
   * Check if there are enough funds in your account in order to create the HIT
   * on Mechanical Turk
   * 
   * @return true if there are sufficient funds. False if not.
   */
  public boolean hasEnoughFund() {
    double balance = service.getAccountBalance();
    System.out.println("Got account balance: " + RequesterService.formatCurrency(balance));
    return balance > 0;
  }
  
  // Convert XML document into a string
  public static String getXML(String file) {
	  try{
		  FileInputStream fstream = new FileInputStream(file);
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  
		  String Q = "";
		  String Qline;
		  while ((Qline = br.readLine()) != null) {
			  Q += Qline;
		  }
		 in.close();
		 return Q;
	  }
	 catch (Exception e) {
		 System.err.println("Error: " + e.getMessage());
	 }
	 return "Error";
}
	  

  /**
   * Creates the simple HIT.
 * @throws Exception 
   * 
   */
  public void postQuestions() throws Exception {
    try {

        for(int i=1; i<=NUM_QUESTIONS; i++) {
        	
        	// Each HIT has format "Q#.xml"
        	//String question = qualTestPost.getXML("Q" + i + ".xml");
        	
        	String PreQualQuestion = qualTestPost.getXML("PreQual.xml");
        	String answerKey = qualTestPost.getXML("PreQualAnswer.xml");
    	
        	//System.out.println(question);
        	QualificationType qual = service.createQualificationType("Bibliography Tagging", "tagging", qualDescription,
        																QualificationTypeStatus.Active,
        																(long) 0,
    																	PreQualQuestion,
        																answerKey,
        																(long) 60*60, false, null);
    	
        	//QualificationRequirement qualRec = new QualificationRequirement();
        	//String ID = qual.getQualificationTypeId();
        	
        	// Users can only answer questions with the following qualification type ID
        	//String ID = "2BTXULRGNTBJ4CMZRS6PAN62KJHS6C";
        	
        	// Only users with a score of 25 or greater on the qualification test may complete the HIT
        	//qualRec.setQualificationTypeId(ID);
        	//qualRec.setComparator(Comparator.GreaterThan);
        	//qualRec.setIntegerValue(25);
    	
        	// AMT requires qualifications to be in array format
        	//QualificationRequirement[] qualRecArray = new QualificationRequirement[1];
        	//qualRecArray[0] = qualRec;
    	
        	// Create the HIT
        	//HIT hit = service.createHIT(
    		//	null, //
            //    title,
            //    description,
            //    null,
            //    question,
            //    reward,
            //    (long) 60*60, // Duration in seconds (1 hr to answer question)
            //    (long) 60*60*24*15, // Approval Delay in seconds (Qualification lasts for 15 days)
            //    (long) 60*60*24*5, // Lifetime in seconds (HIT lasts for 5 days)
            //    numAssignments, // How many Turkers will answer question
            //    null,
            //    qualRecArray, // Qualification test users must pass
            //    null);
    	
        	System.out.println("Created HIT: " + qual.getQualificationTypeId());

        	System.out.println("You may see your HIT with HITTypeId '" 
        			+ qual.getQualificationTypeId() + "' here: ");
        	System.out.println(service.getWebsiteURL() 
        			+ "/mturk/preview?groupId=" + qual.getQualificationTypeId());
        
        }

    } catch (ServiceException e) {
      System.err.println(e.getLocalizedMessage());
    }
  }

  /**
   * Main method
   * 
   * @param args
 * @throws Exception 
   */
  public static void main(String[] args) throws Exception {

    qualTestPost app = new qualTestPost();
    
    
    if (app.hasEnoughFund()) {
      app.postQuestions();
      System.out.println("Success.");
    } else {
      System.out.println("You do not have enough funds to create the HIT.");
    }
  }
}
