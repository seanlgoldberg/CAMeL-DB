package postQuestions;

/**
 * @author abhiramj
 * This class stores mapping of citation to HIT 
 *
 */

public class CitationObject {
	private String citationID;
	private String position;
	private String filePosition;
	public String getFilePosition() {
		return filePosition;
	}

	public void setFilePosition(String filePosition) {
		this.filePosition = filePosition;
	}
	private String HITID;
	
	// Constructors
	public CitationObject() {
		citationID="";
		position="";
		HITID="";
	}
	
	// Getters and setters
public String getCitationID() {
		return citationID;
	}

	public void setCitationID(String citationID) {
		this.citationID = citationID;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	

	public String getHITID() {
		return HITID;
	}

	public void setHITID(String hITID) {
		HITID = hITID;
	}
	
	public String convertToOutSchema(){
		return citationID+","+position+","+HITID+"\n";
	}





}
