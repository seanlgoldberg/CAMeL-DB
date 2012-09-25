package postQuestions;
import java.util.*;

public class Testing {

	/**
	 * @param args
	 */
	// Dummy program designed to test tokenizer
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		StringTokenizer strTok = new StringTokenizer("This, is a test.", " \t");
		System.out.println(strTok.countTokens());
		String[] strArray = new String[strTok.countTokens()];
		int i=0;
		while(strTok.hasMoreTokens()) {			
			//System.out.print(strTok.nextToken() + " ");
			strArray[i++] = strTok.nextToken();
			System.out.println(strArray[i-1]);
		}
		for (i=0; i<strTok.countTokens(); i++) {
			System.out.print(strArray[i] + " ");
		}
	}

}
