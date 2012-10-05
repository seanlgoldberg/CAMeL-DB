package postQuestions;

import java.util.Vector;
import java.io.*;

import au.com.bytecode.opencsv.*;

public class CreateQuestionTemplate {
	
	

	public CreateQuestionTemplate(){
		
	}
	
	public static void generateQuestion(Vector<String> citationID, Vector<String> citationText, Vector<String> positionToken,int index,String outputfile,String questionFileDir) {
	//public void generateQuestion() {	
		
		String XMLfileName="Q" + index + ".xml";
			try{
				//FileWriter f1 = new FileWriter("test.txt");	
				
				
				FileWriter fstream = new FileWriter(questionFileDir+"/" + XMLfileName);
				BufferedWriter out = new BufferedWriter(fstream);
				
				FileWriter outputWriter = new FileWriter(outputfile,true);
				BufferedWriter outuptBuffer = new BufferedWriter(outputWriter);
				
				out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
				out.write("  <QuestionForm xmlns=\"http://mechanicalturk.amazonaws.com/AWSMechanicalTurkDataSchemas/2005-10-01/QuestionForm.xsd\n\">\n");
				out.write("    <Overview>\n");
				out.write("      <Title>Bibliographic Labeling</Title>\n");
				out.write("      <Text>In referencing published academic papers, a shortened citation containing the pertinent information is normally used.  The fields that are traditionally given are:</Text>\n");
				out.write("      <List>\n");
				out.write("        <ListItem>Title - The title of the paper.</ListItem>\n");
				out.write("        <ListItem>Author - The author of the paper.</ListItem>\n\n");
				out.write("        <ListItem>Conference - Organization where the paper was presented.  Usually, but not always, listed as an acronym.</ListItem>\n");
				out.write("        <ListItem>Proceedings - Title of the published conference document.  Usually contains the conference acronym and date of the conference.</ListItem>\n");
				out.write("        <ListItem>Series - Higher level general topic of which the proceedings is a part.</ListItem>\n");
				out.write("		   <ListItem>Publisher - Company publishing the proceedings.</ListItem>\n");
				out.write("		   <ListItem>ISBN - String of 10 or 13 digits uniquely identifying the book the paper was published in.</ListItem>\n");
				out.write("        <ListItem>Year - Year the paper was published.</ListItem>\n");
				out.write("		 </List>\n");
				out.write("		 <Text>A tagged example is given below with different colors representing different labels for each piece of information.</Text>\n");
				/*
				out.write("      <Text>Benefits of Case-Based Reasoning in Color Matching. |Title</Text>\n");
				out.write("      <Text>William Cheetham |Author</Text>\n");
				out.write("      <Text>ICCBR |Conference</Text>\n");
				out.write("      <Text>3-540-42358-3 |ISBN</Text>\n");
				out.write("      <Text>Springer |Publisher</Text>\n");
				out.write("      <Text>Lecture Notes in Computer Science |Series</Text>\n");
				out.write("      <Text>Case-Based Reasoning Research and Development, 4th International Conference on Case-Based Reasoning, ICCBR 2001, Vancouver, BC, Canada, July 30 - August 2, 2001, Proceedings |Proceedings</Text>\n");
				out.write("      <Text>2001 |Year</Text>\n");
				*/
				out.write("      <FormattedContent>\n");
				out.write("        <![CDATA[\n");
				out.write("      <br></br>\n");
				
				out.write("        <p><font color=\"red\">Generating Robust Partial Order Schedules.</font><font color=\"blue\"> Amedeo Cesta, Angelo Oddi, Stephen F. " +
						"Smith, Nicola Policella</font><font color=\"green\"> CP </font><font color=\"chocolate\">3-540-23241-9 </font><font color=\"fuchsia\">Springer </font><font color=\"mediumpurple\">Lecture Notes in Computer Science Principles and " +
						"Practice of Constraint Programming </font><font color=\"yellowgreen\">- CP 2004, 10th International Conference, CP 2004, Toronto, Canada, " +
						"September 27 - October 1, 2004, Proceedings </font><font color=\"teal\">2004</font></p>");
				out.write("        <br></br>\n");
				out.write("        <p>Key:</p>");
				out.write("        <p><font color=\"red\">Title</font></p>");
				out.write("        <p><font color=\"blue\">Author</font></p>");
				out.write("        <p><font color=\"green\">Conference</font></p>");
				out.write("        <p><font color=\"chocolate\">ISBN</font></p>");
				out.write("        <p><font color=\"fuchsia\">Publisher</font></p>");
				out.write("        <p><font color=\"mediumpurple\">Series</font></p>");
				out.write("        <p><font color=\"yellowgreen\">Proceedings</font></p>");
				out.write("        <p><font color=\"teal\">Year</font></p>");
						
				out.write("        <br></br>\n");
				out.write("        <p>Please look at the bibliographic sequences below and select the appropriate label for the <b>bolded</b> token. Note: If more than one word is bolded, select the label corresponding to the FIRST one in the sequence.</p>]]>\n");
				out.write("      </FormattedContent>\n");
				out.write("    </Overview>\n");
				
				for (int i=0; i<citationID.size(); i++) {
					outuptBuffer.write(citationID.get(i)+","+positionToken.get(i)+","+XMLfileName+","+(i+1)+"\n");	
					out.write("    <Question>\n");
					out.write("    <QuestionIdentifier>"+(i+1)+"</QuestionIdentifier>\n");
					out.write("    <QuestionContent>\n");
					out.write("        <FormattedContent>\n");
					out.write("          <![CDATA[\n");
					out.write("          <br></br>\n");
					out.write("          <p><font size=\"2\">");
					
					out.write(citationText.get(i));
					
					out.write("</font></p>\n");
					out.write("          <br></br>]]>\n");
					out.write("        </FormattedContent>\n");
					out.write("      </QuestionContent>\n");
					out.write("      <AnswerSpecification>\n");
					out.write("        <SelectionAnswer>\n");
					out.write("          <StyleSuggestion>radiobutton</StyleSuggestion>\n");
					out.write("          <Selections>\n");
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>author</SelectionIdentifier>\n");
					out.write("              <Text>Author</Text>\n");
					out.write("            </Selection>\n");       
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>title</SelectionIdentifier>\n");
					out.write("              <Text>Title</Text>\n");
					out.write("            </Selection>\n");
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>conference</SelectionIdentifier>\n");
					out.write("              <Text>Conference</Text>\n");
					out.write("            </Selection>\n");
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>proceedings</SelectionIdentifier>\n");
					out.write("              <Text>Proceedings</Text>\n");
					out.write("            </Selection>\n");
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>series</SelectionIdentifier>\n");
					out.write("              <Text>Series</Text>\n");
					out.write("            </Selection>\n");
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>publisher</SelectionIdentifier>\n");
					out.write("              <Text>Publisher</Text>\n");
					out.write("            </Selection>\n");
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>isbn</SelectionIdentifier>\n");
					out.write("              <Text>ISBN</Text>\n");
					out.write("            </Selection>\n");
					out.write("            <Selection>\n");
					out.write("              <SelectionIdentifier>year</SelectionIdentifier>\n");
					out.write("              <Text>Year</Text>\n");
					out.write("            </Selection>\n");        
					out.write("          </Selections>\n");
					out.write("        </SelectionAnswer>\n");
					out.write("      </AnswerSpecification>\n");
					out.write("    </Question>\n");
							
				}
				
				out.write("  </QuestionForm>\n");

				
				out.close();
				//f1.close();
				outuptBuffer.close();
				
				
			}catch (Exception e) {
				System.out.println(" Error generated in Question file -:"+ XMLfileName);
				System.err.println("Error: " + e.getMessage());
			}
		//}
	}
	
	private void processInput(String inputCSVFile, String outputCSVFile, String questionFileDir, int noOfQuesPerHit) throws IOException{
		String tokenize[];
		Vector<String> citationText=new Vector<String>();
		Vector<String> citationID=new Vector<String>();
		Vector<String> positionToken=new Vector<String>();
		int index=1;
		
		System.out.println("Reading from current CSV file "+inputCSVFile);
		
		CSVReader reader = new CSVReader(new FileReader(inputCSVFile));
	
		int counter=0;
		
		while((tokenize=reader.readNext())!=null){
			positionToken.addElement(tokenize[1]);
			citationID.addElement(tokenize[3]);
			citationText.addElement(tokenize[4]);
			counter++;
			
			if(counter==noOfQuesPerHit){
				generateQuestion(citationID, citationText, positionToken, index++,outputCSVFile,questionFileDir);
				counter=0;
				citationText=new Vector<String>();
				citationID=new Vector<String>();
				positionToken=new Vector<String>();
			}
			
		}
		
		if(counter<noOfQuesPerHit)
			generateQuestion(citationID, citationText, positionToken, index++,outputCSVFile,questionFileDir);
		
		System.out.println("Output CSV fime "+outputCSVFile+ "is written successfully");
		reader.close();
		
	}
	
	public static void main(String[] args) throws Exception {
		
		CreateQuestionTemplate cqt=new CreateQuestionTemplate();
		String inputCSVFile="highest_entropies.csv";
		String outputCSVFile="outputCSV.csv";
		String questionFileDir="QuestionXML";
		File dir=new File(questionFileDir);
		questionFileDir=dir.getAbsolutePath();
		if (dir.exists() || dir.mkdirs())
			System.out.println("Question XML's are found at "+ questionFileDir);
		else
			throw new Exception("Directory could not be created, thus file not generated");
		
		int noOfQuesPerHit=10;
		cqt.processInput(inputCSVFile, outputCSVFile,questionFileDir,noOfQuesPerHit);
		
		
	}
	
}
