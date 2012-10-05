/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package iitb.Segment;
import iitb.CRF.*;
import iitb.Model.*;
import iitb.Utils.*;

/**
 *
 * @author sean
 */
public class NewClass {
    public static void main(String argv[]) throws Exception {
        String test = "Test";
        String[] TEST = new String[2];
        TEST[0] = "Test1";
        if (argv.length < 3) {
			System.out
					.println("Usage: java Tagger train|test|calc -f <conf-file>");
			//return;
                        argv[0] = "test";
                        argv[1] = "-f";
                        argv[2] = "/home/sean/EclipseWorkspace2/AMT/samples/us50.conf";                         
		}
        System.out.println("This is a test.");
	Segment segment = new Segment();
	System.out.println("1");
        segment.parseConf(argv);
        System.out.println("2");
        if (argv[0].toLowerCase().equals("test")) {
		System.out.println("3");	
                segment.test();
    }
   }
}
