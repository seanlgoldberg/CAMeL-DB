package postQuestions;
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



import com.amazonaws.mturk.service.axis.RequesterService;


import com.amazonaws.mturk.service.exception.ServiceException;
import com.amazonaws.mturk.util.PropertiesClientConfig;
import com.amazonaws.mturk.requester.*;
import com.amazonaws.mturk.service.axis.*;
import com.amazonaws.mturk.addon.*;
import postQuestions.AMTWorkflow;
import java.io.*;
import java.util.Scanner;
import java.lang.*;

/**
 * The MTurk Hello World sample application creates a simple HIT via the Mechanical Turk 
 * Java SDK. mturk.properties must be found in the current file path.
 */
public class AMTpost {

  private int NUM_QUESTIONS = 50;	
	
  private RequesterService service;
  private RequesterServiceRaw serviceRaw;

  // Defining the attributes of the HIT to be created
  private String title = "Bibliographic Labeling";
  private String description = 
    "Tagging a set of bibliographic data.";
  private int numAssignments = 7;
  private double reward = 0.15;

  
  //Defining the attributes of the Pre-Qualification
  //private String qualTitle = "Answer a Pre-Qualification Question";
  //private String qualDescription = "This is a pre-qualification question.";
  
  /**
   * Constructor
   * 
   */
  public AMTpost() {
    System.out.println(AMTWorkflow.MTURK_CONFIG_FILE);
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
	  String Q = "";
	  try{
		  FileInputStream fstream = new FileInputStream(file);
		  DataInputStream in = new DataInputStream(fstream);
		  BufferedReader br = new BufferedReader(new InputStreamReader(in));
		  
		  Q = "";
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
  public String postQuestion(String fileName) throws Exception {
	  String hitID="";
    try {

  
        	
        	// Each HIT has format "Q#.xml"
        	String question = AMTpost.getXML(fileName);
        	
        	//String PreQualQuestion = AMTpost.getXML("PreQual.xml");
        	//String answerKey = AMTpost.getXML("PreQualAnswer.xml");
    	
        	//System.out.println(question);
        	//QualificationType qual = service.createQualificationType("Bibliography Tagging", "tagging", qualDescription,
        	//															QualificationTypeStatus.Active,
        	//															(long) 0,
    		//															PreQualQuestion,
        	//															answerKey,
        	//															(long) 60*60, false, null);
    	
        	QualificationRequirement qualRec = new QualificationRequirement();
        	//String ID = qual.getQualificationTypeId();
        	
        	// Users can only answer questions with the following qualification type ID
        	// Replace with QualificationTypeID obtained from "qualTestPost.java"
        	//String ID = "2BTXULRGNTBJ4CMZRS6PAN62KJHS6C";
        	String ID = "2UTSPW1INMHGZG45XA1BDBFMFGS4HI";
                
        	// Only users with a score of 25 or greater on the qualification test may complete the HIT
        	qualRec.setQualificationTypeId(ID);
        	qualRec.setComparator(Comparator.GreaterThan);
        	qualRec.setIntegerValue(25);
    	
        	// AMT requires qualifications to be in array format
        	QualificationRequirement[] qualRecArray = new QualificationRequirement[1];
        	qualRecArray[0] = qualRec;
    	    
        	
        	// TODO: Update this part to the set a proper qualification requirement
        	 /*QualificationRequirement qualReq = new QualificationRequirement();
             qualReq.setQualificationTypeId(RequesterService.LOCALE_QUALIFICATION_TYPE_ID);
             qualReq.setComparator(Comparator.EqualTo);
             Locale country = new Locale();
             country.setCountry("US");
             qualReq.setLocaleValue(country);
        	
            QualificationRequirement[] qualReqs = null;
            qualReqs = new QualificationRequirement[] { qualReq };
        	*/
        	// Create the HIT
        	HIT hit = service.createHIT(
    			null, //
                title,
                description,
                null,
                question,
                reward,
                (long) 60*60, // Duration in seconds (1 hr to answer question)
                (long) 60*15, // Approval Delay in seconds (Automatic Approval after 15 min)
                (long) 60*60*24*7, // Lifetime in seconds (HIT lasts for 7 days)
                numAssignments, // How many Turkers will answer question
                null,
                null,
                //qualRecArray, // Qualification test users must pass
                null);
        	hitID=hit.getHITId();
        	System.out.println("Created HIT: " + hitID);

        	System.out.println("You may see your HIT with HITTypeId '" 
        			+ hit.getHITTypeId() + "' here: ");
        	System.out.println(service.getWebsiteURL() 
        			+ "/mturk/preview?groupId=" + hit.getHITTypeId());
        	
        return hitID;

    } catch (ServiceException e) {
      System.err.println(e.getLocalizedMessage());
      return hitID;
    }
  }

  /**
   * Main method
   * 
   * @param args
 * @throws Exception 
   */
 /* public static void main(String[] args) throws Exception {

    AMTpost app = new AMTpost();
    
    
    if (app.hasEnoughFund()) {
      app.postQuestion();
      System.out.println("Success.");
    } else {
      System.out.println("You do not have enough funds to create the HIT.");
    }
  }*/
}
