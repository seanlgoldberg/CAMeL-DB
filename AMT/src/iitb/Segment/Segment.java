package iitb.Segment;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import iitb.CRF.*;
import iitb.Model.*;
import iitb.Utils.*;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Sunita Sarawagi
 * 
 */

class ClampingData {
	public int node;
	public int label;
}

public class Segment {
	int method = 1;
	String inName;
	String outDir;
	String baseDir = "";
	int nlabels;

	String delimit = " \t"; // used to define token boundaries
	String tagDelimit = "|"; // seperator between tokens and tag number
	String impDelimit = ""; // delimiters to be retained for tagging
	String groupDelimit = null;

	boolean confuseSet[] = null;
	boolean validate = false;
	String mapTagString = null;
	String smoothType = "";

	String modelArgs = "";
	String featureArgs = "";
	String modelGraphType = "naive";

	LabelMap labelMap;
	Options options;

	CRF crfModel;
	FeatureGenImpl featureGen;

	FileWriter f;
        FileWriter f2;
        FileWriter f3;
        FileWriter facc;
        
        int totalToks;
        int rightToks;

	public FeatureGenerator featureGenerator() {
		return featureGen;
	}

	public static void main(String argv[]) throws Exception {
		if (argv.length < 3) {
			System.out
					.println("Usage: java Tagger train|test|calc -f <conf-file>");
			return;
                        //argv[0] = "test";
                        //argv[1] = "-f";
                        //argv[2] = "/home/sean/CAMeL-DB/AMT/samples/us50.conf";                         
		}
		Segment segment = new Segment();
                segment.parseConf(argv);
                if (argv[0].toLowerCase().equals("all")) {
			segment.train();
			segment.doTest();
			segment.calc();
		}
		if (argv[0].toLowerCase().equals("train")) {
			segment.train();
		}
		if (argv[0].toLowerCase().equals("test")) {			
                        segment.test();
		}
		if (argv[0].toLowerCase().equals("calc")) {
			segment.calc();
		}
	}

	public void parseConf(String argv[]) throws Exception {
		options = new Options();
		int startIndex = 1;
		if ((argv.length >= 2) && argv[1].toLowerCase().equals("-f")) {
			options.load(new FileInputStream(argv[2]));
		}
		options.add(3, argv);
		
                processArgs();
	}

	public void processArgs() throws Exception {
		String value = null;
		if ((value = options.getMandatoryProperty("numlabels")) != null) {
			nlabels = Integer.parseInt(value);
		}
		if ((value = options.getProperty("binary")) != null) {
			nlabels = 2;
			labelMap = new BinaryLabelMap(options.getInt("binary"));
		} else {
			labelMap = new LabelMap();
		}
		if ((value = options.getMandatoryProperty("inname")) != null) {
			inName = new String(value);
		}
		if ((value = options.getMandatoryProperty("outdir")) != null) {
			outDir = new String(value);
		}
		if ((value = options.getProperty("basedir")) != null) {
			baseDir = new String(value);
		}
		if ((value = options.getProperty("tagdelimit")) != null) {
			tagDelimit = new String(value);
		}
		// delimiters that will be ignored.
		if ((value = options.getProperty("delimit")) != null) {
			delimit = new String(value);
		}
		if ((value = options.getProperty("impdelimit")) != null) {
			impDelimit = new String(value);
		}
		if ((value = options.getProperty("groupdelimit")) != null) {
			groupDelimit = value;
		}
		if ((value = options.getProperty("confusion")) != null) {
			StringTokenizer confuse = new StringTokenizer(value, ", ");
			int confuseSize = confuse.countTokens();
			confuseSet = new boolean[nlabels + 1];
			for (int i = 0; i < confuseSize; i++) {
				confuseSet[Integer.parseInt(confuse.nextToken())] = true;
			}
		}
		if ((value = options.getProperty("map-tags")) != null) {
			mapTagString = value;
		}
		if ((value = options.getProperty("validate")) != null) {
			validate = true;
		}
		if ((value = options.getProperty("model-args")) != null) {
			modelArgs = value;
			System.out.println(modelArgs);
		}
		if ((value = options.getProperty("feature-args")) != null) {
			featureArgs = value;
		}
		if ((value = options.getProperty("modelGraph")) != null) {
			modelGraphType = value;
		}
	}

	void allocModel() throws Exception {
		// add any code related to dependency/consistency amongst paramter
		// values here..
		if (modelGraphType.equals("semi-markov")) {
			if (options.getInt("debugLvl") > 1) {
				Util.printDbg("Creating semi-markov model");
			}
			NestedFeatureGenImpl nfgen = new NestedFeatureGenImpl(nlabels,
					options);
			featureGen = nfgen;
			crfModel = new NestedCRF(featureGen.numStates(), nfgen, options);
		} else {
			featureGen = new FeatureGenImpl(modelGraphType, nlabels);
			crfModel = new CRF(featureGen.numStates(), featureGen, options);
		}
	}

	class TestRecord implements SegmentDataSequence {
		String seq[];
		int path[];

		TestRecord(String s[]) {
			seq = s;
			path = new int[seq.length];
		}

		void init(String s[]) {
			seq = s;
			//if ((path == null) || (path.length < seq.length)) {
				path = new int[seq.length];
			//}
		}

		public void set_y(int i, int l) {
			path[i] = l;
		} // not applicable for training data.

		public int y(int i) {
			return path[i];
		}

		public int length() {
			return seq.length;
		}

		public Object x(int i) {
			return seq[i];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see iitb.CRF.SegmentDataSequence#getSegmentEnd(int)
		 */
		public int getSegmentEnd(int segmentStart) {
			if ((segmentStart > 0) && (y(segmentStart) == y(segmentStart - 1)))
				return -1;
			for (int i = segmentStart + 1; i < length(); i++) {
				if (y(i) != y(segmentStart))
					return i - 1;
			}
			return length() - 1;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see iitb.CRF.SegmentDataSequence#setSegment(int, int, int)
		 */
		public void setSegment(int segmentStart, int segmentEnd, int y) {
			for (int i = segmentStart; i <= segmentEnd; i++)
				set_y(i, y);
		}
	};

	public int[] segment(TestRecord testRecord, int[] groupedToks,
			String collect[]) {
		for (int i = 0; i < testRecord.length(); i++)
                    testRecord.seq[i] = AlphaNumericPreprocessor
					.preprocess(testRecord.seq[i]);
		crfModel.apply(testRecord);
		featureGen.mapStatesToLabels(testRecord);
		int path[] = testRecord.path;
		for (int i = 0; i < nlabels; i++)
			collect[i] = null;
		for (int i = 0; i < testRecord.length(); i++) {
			// System.out.println(testRecord.seq[i] + " " + path[i]);
			int snew = path[i];
			if (snew >= 0) {
				if (collect[snew] == null) {
					collect[snew] = testRecord.seq[i];
				} else {
					collect[snew] = collect[snew] + " " + testRecord.seq[i];
				}
			}
		}
		return path;
	}
        
        public int[] segment(TestRecord testRecord, int[] groupedToks,
			String collect[], int pos, int label, int labelLeft, int labelRight) {
		for (int i = 0; i < testRecord.length(); i++)
			testRecord.seq[i] = AlphaNumericPreprocessor
					.preprocess(testRecord.seq[i]);
		crfModel.apply(testRecord, pos, label, labelLeft, labelRight);
		featureGen.mapStatesToLabels(testRecord);
		int path[] = testRecord.path;
		for (int i = 0; i < nlabels; i++)
			collect[i] = null;
		for (int i = 0; i < testRecord.length(); i++) {
			// System.out.println(testRecord.seq[i] + " " + path[i]);
			int snew = path[i];
			if (snew >= 0) {
				if (collect[snew] == null) {
					collect[snew] = testRecord.seq[i];
				} else {
					collect[snew] = collect[snew] + " " + testRecord.seq[i];
				}
			}
		}
		return path;
	}

	public int[] segment(TestRecord testRecord, int[] groupedToks,
			String collect[], int pos, int label) {
		for (int i = 0; i < testRecord.length(); i++)
			testRecord.seq[i] = AlphaNumericPreprocessor
					.preprocess(testRecord.seq[i]);
		crfModel.apply(testRecord, pos, label);
		featureGen.mapStatesToLabels(testRecord);
		int path[] = testRecord.path;
		for (int i = 0; i < nlabels; i++)
			collect[i] = null;
		for (int i = 0; i < testRecord.length(); i++) {
			// System.out.println(testRecord.seq[i] + " " + path[i]);
			int snew = path[i];
			if (snew >= 0) {
				if (collect[snew] == null) {
					collect[snew] = testRecord.seq[i];
				} else {
					collect[snew] = collect[snew] + " " + testRecord.seq[i];
				}
			}
		}
		return path;
	}
	
	public int[] segment(TestRecord testRecord, int[] groupedToks,
			String collect[], int pos, int pos2, int labelLeft, int labelRight, int flag) {
		for (int i = 0; i < testRecord.length(); i++)
			testRecord.seq[i] = AlphaNumericPreprocessor
					.preprocess(testRecord.seq[i]);
		crfModel.apply(testRecord, pos, pos2, labelLeft, labelRight, flag);
		featureGen.mapStatesToLabels(testRecord);
		int path[] = testRecord.path;
		for (int i = 0; i < nlabels; i++)
			collect[i] = null;
		for (int i = 0; i < testRecord.length(); i++) {
			// System.out.println(testRecord.seq[i] + " " + path[i]);
			int snew = path[i];
			if (snew >= 0) {
				if (collect[snew] == null) {
					collect[snew] = testRecord.seq[i];
				} else {
					collect[snew] = collect[snew] + " " + testRecord.seq[i];
				}
			}
		}
		return path;
	}

	public void train() throws Exception {
		DataCruncher.createRaw(baseDir + "/data/" + inName + "/" + inName
				+ ".train", tagDelimit);
		File dir = new File(baseDir + "/learntModels/" + outDir);
		dir.mkdirs();
		TrainData trainData = DataCruncher.readTagged(nlabels, baseDir
				+ "/data/" + inName + "/" + inName + ".train", baseDir
				+ "/data/" + inName + "/" + inName + ".train", delimit,
				tagDelimit, impDelimit, labelMap);
		AlphaNumericPreprocessor.preprocess(trainData, nlabels);

		allocModel();
		featureGen.train(trainData);
		double featureWts[] = crfModel.train(trainData);
		if (options.getInt("debugLvl") > 1) {
			//Util.printDbg("Training done");
		}
		crfModel.write(baseDir + "/learntModels/" + outDir + "/crf");
		featureGen.write(baseDir + "/learntModels/" + outDir + "/features");
		if (options.getInt("debugLvl") > 1) {
			//Util.printDbg("Writing model to " + baseDir + "/learntModels/"
					//+ outDir + "/crf");
		}
		if (options.getProperty("showModel") != null) {
			featureGen.displayModel(featureWts);
		}
	}

	/*
	 * public void computeMarginals(DataIter testData) { Marginals marginal =
	 * new Marginals(crfModel,testData,null); marginal.computeBeta(); }
	 */

	public void test() throws Exception {
		allocModel();
		featureGen.read(baseDir + "/learntModels/" + outDir + "/features");
		crfModel.read(baseDir + "/learntModels/" + outDir + "/crf");
		

                //facc = new FileWriter(baseDir + "/clamped12.csv", false);

                doTestWithClamping();
		//doTest();
	}

	public void testWithClamping(int pos, int label) throws Exception {
		allocModel();
		featureGen.read(baseDir + "/learntModels/" + outDir + "/features");
		crfModel.read(baseDir + "/learntModels/" + outDir + "/crf");
		doTestWithClamping();
	}

	public void doTest() throws Exception {
		String outDir = "out2";
                File dir = new File(baseDir + "/" + outDir + "/" + outDir);
		dir.mkdirs();
		TestData testData = new TestData(baseDir + "/data/" + inName + "/"
				+ inName + ".test", delimit, impDelimit, groupDelimit);
		TestDataWrite tdw = new TestDataWrite(baseDir + "/" + outDir + "/" + outDir + "/"
				+ inName + ".test", baseDir + "/data/" + inName + "/" + inName
				+ ".test", delimit, tagDelimit, impDelimit, labelMap);

		String collect[] = new String[nlabels];
		TestRecord testRecord = new TestRecord(collect);
		for (String seq[] = testData.nextRecord(); seq != null; seq = testData
				.nextRecord()) {
			testRecord.init(seq);
			if (options.getInt("debugLvl") > 1) {
				//Util.printDbg("Invoking segment on " + seq);
			}
			int path[] = segment(testRecord, testData.groupedTokens(), collect);
			tdw.writeRecord(path, testRecord.length());
		}
		tdw.close();
		testData = new TestData(baseDir + "/data/" + inName + "/" + inName
				+ ".test", delimit, impDelimit, groupDelimit);
		calc();
	}
        class CitationTable {
            int citationID;
            String rawText;
            
            CitationTable(int citID, String raw) {
                this.citationID = citID;
                this.rawText = raw;
            }
        }

	

	//List<CRFoutput> CRFoutput;

	////////////////////////////////////////////
	///////////////////////////////////////////
	///////////////////////////////////////////
	
	public void doTestWithClamping() throws Exception {
		totalToks = 0;
                rightToks = 0;
                int flag1 = 0;
                //String combo = "pubmed_cluster6";
                //String comboNum = "5";
                String filename = "xPubMed_ByHighestEntropy_t0l0l1l1_totalEntropy_3.csv";
                
                
            
                //TreeMap<Integer, Integer> NodeCountMap = new TreeMap<Integer, Integer>();
		
		//Initialize List of CRFoutput to be sorted
		//CRFoutput = new ArrayList<CRFoutput>();

                CRFoutput CRFout = new CRFoutput();
		
		//Make output directory
		String outDirec = "out2";
                File dir = new File(baseDir + "/" + outDirec + "/" + outDir);
		dir.mkdirs();
		
		//Opens file that will read in tagged test data
//		TestData testData = new TestData(baseDir + "/data/" + inName + "/"
//				+ inName + ".test", delimit, impDelimit, groupDelimit);
                TestData testData = new TestData(baseDir + "/data/PubMed_HighEntropy/" 
                        + filename + ".test", delimit, impDelimit, groupDelimit);
		
		//Writes a tagged exp test in the same format as the tagged data
//		TestDataWrite tdw = new TestDataWrite(baseDir + "/" + outDirec + "/" + outDir + "/"
//				+ "tempTagged" + ".test", baseDir + "/data/" + inName + "/"
//				+ inName + ".test", delimit, tagDelimit, impDelimit, labelMap);
                TestDataWrite tdw = new TestDataWrite(baseDir + "/" + outDirec + "/" + outDir + "/"
				+ "tempTagged" + ".test", baseDir + "/data/PubMed_HighEntropy/"
				+ filename + ".test", delimit, tagDelimit, impDelimit, labelMap);

		//Reads in the original tagged testing set
//		TrainData taggedTestData = DataCruncher.readTagged(nlabels, baseDir
//				+ "/data/" + inName + "/" + inName + ".test", baseDir
//				+ "/data/" + inName + "/" + inName + ".test", delimit,
//				tagDelimit, impDelimit, labelMap);
                TrainData taggedTestData = DataCruncher.readTagged(nlabels, baseDir
				+ "/data/PubMed_HighEntropy/" + filename + ".test", baseDir
				+ "/data/PubMed_HighEntropy/" + filename + ".test", delimit,
				tagDelimit, impDelimit, labelMap);

		//testRecord holds String arrays of tagged data organized by tags 
                String collect[] = new String[nlabels];
		TestRecord testRecord = new TestRecord(collect);
                
                //Create structures for storing information about top clusters
                String citation;
                Hashtable<Integer, ArrayList<String>> topClusters = new Hashtable<Integer, ArrayList<String>>();
                
                //Initialize files to be written
                f = new FileWriter(baseDir + "/CRFoutput2.csv", false);
                f2 = new FileWriter(baseDir + "/citationTable2.csv", false);
                f3 = new FileWriter(baseDir + "/labelTable2.csv", false);

                //facc = new FileWriter(baseDir + "/data/PubMed_HighEntropy/clamped_" + combo + "_" + comboNum
                //+ ".csv", false);
                facc = new FileWriter(baseDir + "/data/PubMed_HighEntropy/clamped_" + filename,false);
                      
                
                //Read in top clusters to be clamped and add to Hashtable
                FileInputStream fInStream = new FileInputStream(baseDir + "/data/PubMed_HighEntropy/" + filename);                        
                DataInputStream in = new DataInputStream(fInStream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));               
//                String algorithm = br.readLine();
                while ((citation = br.readLine()) != null) {
                    StringTokenizer strTok = new StringTokenizer(citation,",");
                    //if (strTok.countTokens()!=7){
			//throw new Exception ("Expected 7 tokens in CSV line, got "+ strTok.countTokens());
                    //}
                    //Relative cluster ID (not needed)
                    String relClusterID = strTok.nextToken();
                    int citID = Integer.parseInt(strTok.nextToken().trim());
                    ArrayList<String> clusterValues = new ArrayList<String>();
                    for (int idx=0; idx<4; idx++) {
                        clusterValues.add(idx,strTok.nextToken().replaceAll("\"",""));
                    }
                    clusterValues.add(4,relClusterID);
                    
                    topClusters.put(citID, clusterValues);  
                }
                
		int instance = 1;
		
                
                
		//Loop takes in tagged data record by record
		// seq[] is an array of tokens
                for (String seq[] = testData.nextRecord(); seq != null && seq.length != 0; seq = testData
				.nextRecord()) {
			
                         //Print timestamp
                         System.out.println("CURRENT TIME IS: " + System.currentTimeMillis() + " " + instance + "\n");
                    
                        //Create tokenized record object
			testRecord.init(seq);
                        
                        //Set values for output classes, CRFout and CitationTable
                       
                            CRFout.setCitationID(instance);
                            CRFout.setSeqs(seq);
                            
                        FileInputStream fInStream2 = new FileInputStream(baseDir + "/data/PubMed_HighEntropy/" + filename);                        
                        DataInputStream in2 = new DataInputStream(fInStream2);
                        BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
                        for (int line=0; line<instance; line++) {
                            citation = br2.readLine();
                        }
                        StringTokenizer strTok = new StringTokenizer(citation,",");
                        String relClusterID = strTok.nextToken();
                        int citID = Integer.parseInt(strTok.nextToken().trim());
                        
                        //CitationTable citTable = new CitationTable(instance, testData.line);
                        CitationTable citTable = new CitationTable(citID, testData.line);
			
			if (options.getInt("debugLvl") > 1) {
				//Util.printDbg("Invoking segment on " + seq);
			}

                        // token[] is an array of labels for seq[]
			int[] token = allLabels(taggedTestData.nextRecord());                        
                        
                        
                            CRFout.setToks(token);

//			tdw = new TestDataWrite(baseDir + "/" + outDirec + "/" + outDir + "/" + inName
//					+ ".test", baseDir + "/data/" + inName + "/" + inName
//					+ ".test", delimit, tagDelimit, impDelimit, labelMap);
                            tdw = new TestDataWrite(baseDir + "/" + outDirec + "/" + outDir + "/" + inName
					+ ".test", baseDir + "/data/PubMed_HighEntropy/" + filename
					+ ".test", delimit, tagDelimit, impDelimit, labelMap);

			/* Without Clamping */
                        //Use raw record and CRF model to populate collect array with path labels
			int path[] = segment(testRecord, testData.groupedTokens(), collect);
			/////////////////////

			//for (int ii = 1; ii < path.length; ii++) {
			//	System.out.println (path[ii] + " ");
			//}
			//System.out.println("\n");
			
			tdw.writeRecord(path, testRecord.length(), instance);
			tdw.close();
                        
                        //Sunny code: outputting tokens?
//			TestDataWrite tdw1 = new TestDataWrite(baseDir + "/data/" + inName + "/"
//					+ "tempTagged2" + ".test", baseDir + "/data/" + inName + "/"
//					+ inName + ".test", delimit, tagDelimit, impDelimit,
//					labelMap);
                        TestDataWrite tdw1 = new TestDataWrite(baseDir + "/data/" + inName + "/"
					+ "tempTagged2" + ".test", baseDir + "/data/PubMed_HighEntropy/" 
					+ filename + ".test", delimit, tagDelimit, impDelimit,
					labelMap);

			tdw1.writeRecord(token, token.length, instance);
			tdw1.close();
                        
                        if (flag1==1) {
                            //Computing and determining all marginals
                            Marginals marginal = new Marginals(crfModel, testRecord, null);
                            marginal.compute();
                            marginal.printBetaVector();
                            marginal.computeMarginal();
                            marginal.normalize();
                            //marginal.printNormalized();
                            marginal.calculateEntropy(method);
                            //System.out.println("\n");

                            //Compute entropy
                            Double[][] totalMarginal = new Double[token.length][nlabels];
                            double[] ent = new double[token.length];
                            for (int i=0; i<token.length; i++) {
                                Double[] marg = new Double[nlabels];
                                marg = marginal.getMarginalProb(i);
                                System.arraycopy(marg, 0, totalMarginal[i], 0, nlabels);
                                ent[i] = marginal.getEntropy(i);
                            }

                            //Set Marginal and Entropy values
                            CRFout.setTotalMarginal(totalMarginal);
                            CRFout.setEntropy(ent);
                        }
			///////////////
			//NodeCountMap.put(instance, marginal.numHighEntropyNodes(0.2));
			//////////////
			
			
			//////////////
			//double THRESH = 0.2;
			//turkerData.add(new TurkerData(rawline, seq, marginal
			//		.getNumMaxEntropyNodes(THRESH), marginal.getMaxEntropyNode()));
			/////////////
			
			// marginal.printEntropy();

			// f.write(marginal.getMaxEntropyNode() + " ");
			
			
			/* without clamping */
			/*Exp 2*/
			/////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////
			//double f1Score = calcWith(testRecord);
			double f1Before = calcWith(testRecord);
			////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////

			double[] f1 = new double[token.length]; 
			
			/* clamping */
			//System.out.println("Length:" + token.length);
			
//                        TrainData tdMan = DataCruncher.readTagged(nlabels, baseDir + "/data/"
//				+ inName + "/" + "tempTagged2" + ".test", baseDir + "/data/"
//				+ inName + "/" + inName + ".test", delimit, tagDelimit,
//				impDelimit, labelMap);
//                        TrainData tdAuto = DataCruncher.readTagged(nlabels, baseDir + "/" + outDirec + "/"
//				+ outDir + "/" + inName + ".test", baseDir + "/data/" + inName
//				+ "/" + inName + ".test", delimit, tagDelimit, impDelimit,
//				labelMap);
                        TrainData tdMan = DataCruncher.readTagged(nlabels, baseDir + "/data/"
				+ inName + "/" + "tempTagged2" + ".test", baseDir + "/data/PubMed_HighEntropy/"
				+ filename + ".test", delimit, tagDelimit,
				impDelimit, labelMap);
                        TrainData tdAuto = DataCruncher.readTagged(nlabels, baseDir + "/" + outDirec + "/"
				+ outDir + "/" + inName + ".test", baseDir + "/data/PubMed_HighEntropy/"
				+ filename + ".test", delimit, tagDelimit, impDelimit,
				labelMap);
                        
                        //Accuracy Calculation for Clamping
//                        for (int idx=0; idx<tdAuto.size(); idx++) {
//                            TrainRecord trMan = tdMan.nextRecord();
//                            TrainRecord trAuto = tdAuto.nextRecord();
//                            //if (idx==(CRFout.citationID-1)) {
//                                int tokenMan[] = allLabels(trMan);
//                                int tokenAuto[] = allLabels(trAuto);
//                                CRFout.setTrueLabels(tokenMan);
//                                CRFout.setAutoLabels(tokenAuto);
//                                for (int idx2=0; idx2<tokenMan.length; idx2++) {
//                                    totalToks++;
//                                    if (tokenMan[idx2]==tokenAuto[idx2])
//                                        rightToks++;
//                                }
//                            //}
//                        }
                        
///////////////////////////////////////////////////////////////////////////////////////////////
/////
/////
/////                              CLAMPING CODE
/////
///// Clamping Plan: 
////       1. Read in list of selected citations and tokens to be clamped
////       2. Only clamp if citationID is in list
///////////////////////////////////////////////////////////////////////////////////////////////                        
                        
                        
                        if (topClusters.containsKey(citTable.citationID)) {
                            ArrayList<String> citValues = topClusters.get(citTable.citationID);
                            
                            FileInputStream fInStream3 = new FileInputStream(baseDir + "/data/PubMed_HighEntropy/" 
                                    + filename + ".test.raw");                        
                            DataInputStream in3 = new DataInputStream(fInStream3);
                            BufferedReader br3 = new BufferedReader(new InputStreamReader(in3));
                            for (int line=0; line<instance; line++) {
                                citation = br3.readLine();
                            }
                            StringTokenizer tok = new StringTokenizer(citation.toLowerCase(),
                                                    " ", true);
                            
                            //FIGURE OUT TOKEN POSITION FROM CHARACTER POSITION HERE
                            if (instance==26) 
                                {
                                    System.out.println("STOP");
                                }
                            int fflag = 1;
                            int charPos = Integer.parseInt(citValues.get(0).trim());
                            int charLength = 0;
                            int pos = 0;
                            while (charLength < charPos) {
                               //System.out.println(CRFout.seqs.length);
//                                if(pos<CRFout.seqs.length)  {
//                                    charLength += CRFout.seqs[pos].length() + 1;
//                                    pos++;
//                               }
                                if (tok.hasMoreTokens()) {
                                    String tt = tok.nextToken();
                                    int count = StringUtils.countMatches(tt, "-");
                                    int countb = StringUtils.countMatches(tt, "--");
                                    int count2 = StringUtils.countMatches(tt, ",");
                                    int count3 = StringUtils.countMatches(tt, ":");
                                    
                                    charLength += tt.length();
                                    //System.out.println(tt);
                                    if (!tt.equals(" "))
                                        pos++;
                                    for (int i=0; i<(count-2*countb); i++) {
                                        pos++;
                                    }
                                    for (int i=0; i<countb; i++) {
                                        pos++;
                                    }
                                    for (int i=0; i<count2; i++) {
                                        pos++;
                                    }
                                }
                                else{
                                    break;
                               }
                            }
                            

//                            path = segment(testRecord, testData.groupedTokens(), collect,pos, 
//                                    Integer.parseInt(citValues.get(2).trim()));
                            
                            //Convert from text to truth label
                            String lab = citValues.get(2).toLowerCase().trim();
                            int label = -1;
//                            if (lab.equals("title")) {
//                                label = 0;
//                            }
//                            else if (lab.equals("author")) {
//                                label = 1;
//                            }
//                            else if (lab.equals("conference")) {
//                                label = 2;
//                            }
//                            else if (lab.equals("isbn")) {
//                                label = 3;
//                            }
//                            else if (lab.equals("publisher")) {
//                                label = 4;
//                            }
//                            else if (lab.equals("series")) {
//                                label = 5;
//                            }
//                            else if (lab.equals("proceedings")) {
//                                label = 6;
//                            }
//                            else if (lab.equals("year")) {
//                                label = 7;
//                            }
                            
                            if (lab.equals("title")) {
                                label = 0;
                            }
                            else if (lab.equals("source")) {
                                label = 1;
                            }
                            else if (lab.equals("author")) {
                                label = 2;
                            }
                            else if (lab.equals("issue")) {
                                label = 3;
                            }
                            else if (lab.equals("volume")) {
                                label = 4;
                            }
                            else if (lab.equals("pages")) {
                                label = 5;
                            }
                            else if (lab.equals("year")) {
                                label = 6;
                            }
                            
                            path = segment(testRecord, testData.groupedTokens(), collect, pos, label);

                        //FileWriter testf = new FileWriter(baseDir + "/data/testf.txt", true);
                        
                        //testf.write(citValues.get(3) + " - " + CRFout.seqs[pos] + "\n");
                        //testf.close();
                        }
                        //			for (int i = 0; i < token.length; i++) {
//				if (i == marginal.getMaxEntropyNode()) {
//					tdw1 = new TestDataWrite(baseDir + "/data/" + inName + "/"
//						+ "tempTagged" + ".test", baseDir + "/data/" + inName
//						+ "/" + inName + ".test", delimit, tagDelimit,
//						impDelimit, labelMap);
//
//					tdw1.writeRecord(token, token.length, instance);
//					tdw1.close();
//
//					/* With Clamping */
//					
//					if (i==0) {
//						path = segment(testRecord, testData.groupedTokens(), collect,
//								i, token[i], token[i+1],1);
//					}
//					else if (i==token.length) {
//						path = segment(testRecord, testData.groupedTokens(), collect,
//								i, token[i-1], token[i],1);
//					}
//					else {
//						////////////////////
//						path = segment(testRecord, testData.groupedTokens(), collect,
//								i, token[i], token[i-1], token[i+1]);
//						////////////////////
//					}
//              			TestDataWrite tdw2 = new TestDataWrite(baseDir + "/" + outDirec + "/" + outDir + "/"
//						+ inName + ".test", baseDir + "/data/" + inName + "/"
//						+ inName + ".test", delimit, tagDelimit, impDelimit,
//						labelMap);
                                        TestDataWrite tdw2 = new TestDataWrite(baseDir + "/" + outDirec + "/" + outDir + "/"
						+ inName + ".test", baseDir + "/data/PubMed_HighEntropy/" 
						+ filename + ".test", delimit, tagDelimit, impDelimit,
						labelMap);
					tdw2.writeRecord(path, testRecord.length(), instance);
					tdw2.close();
//
					double f1After = calcWith(testRecord);
//				
//				
//				// Prints the accuracy after clamping the maximum entropy node //
//				
//					//f.write(f1[i] + " ");
//					Marginals _marginal = new Marginals(crfModel, testRecord,
//							null);
//					_marginal.compute(i, token[i]);
//					_marginal.computeMarginal();
//					_marginal.normalize();
//					_marginal.calculateEntropy(i,method);
//					//f.write(_marginal.averageEntropy() + " ");
//					/////////////////////////////////////////////////////////////////////////////////////
//					////////////////////////////////////////////////////////////////////////////////////
//					double THRESH = 0.13;
//					double f1After = calcWith(testRecord);
//					
//							//marginal.getNumMaxEntropyNodes(THRESH) + " ");
//					//////////////
//					
//					turkerData.add(new TurkerData(rawline, seq, marginal
//							.getNumMaxEntropyNodes(THRESH), marginal.getMaxEntropyNode(),
//							f1Before,f1After,marginal.getEntropy(i), marginal.getNumMaxEntropyNodes(THRESH)));
//					/////////////
//					////////////////////////////////////////////////////////////////////////////////////
//					////////////////////////////////////////////////////////////////////////////////////
//				}
//			}

			instance++;
     ////////////////////////////////////////////////////////////////////////////////////////////////////                   
//                          f2.write(citTable.citationID + ", \"" + citTable.rawText + "\"\n");
//                        for (int line=0; line<CRFout.seqs.length; line++) {
//                            f.write(CRFout.citationID + ", " 
//                                //+ citTable.rawText.indexOf(CRFout.seqs[line]) + ", \""
//                                + line + ", \""
//                                + CRFout.seqs[line] + "\", " 
//                                + Integer.toString(CRFout.trueLabels[line]) + ", "
//                                + Integer.toString(CRFout.autoLabels[line]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][0]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][1]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][2]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][3]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][4]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][5]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][6]) + ", "
//                                + Double.toString(CRFout.totalMarginal[line][7]) + ", "
//                                + Double.toString(CRFout.entropy[line]) + "\n"
//                                );
//                        }
                        
//                        for (int line=0; line<CRFout.seqs.length; line++) {
//                            f3.write(citTable.citationID + ", "
//                                    + line + ", "
//                                    + "\"" + CRFout.seqs[line] + "\", "
//                                    + CRFout.token[line] + "\n");
//                        }
                        
       //////////////////////////////////////////////////////////////////////////////////////                

                        int clamped = -1;

                        if (topClusters.containsKey(citTable.citationID)) {
                            clamped = 1;
                        }
                        else {
                            clamped = 0;
                        }
                        
//                        facc.write(citTable.citationID + ", " + f1Before + ", " + f1After + ", " + clamped + "\n");
                        

                        ArrayList<String> citValue = topClusters.get(citTable.citationID);
                        if (citValue.size()>=4) {
                        if (citValue.get(4)!=null) {
                            facc.write(citTable.citationID + ", " + f1Before + ", " + f1After + ", "
                                + citValue.get(4) + ", " + citValue.get(1) + "\n");
                        }
                        }
                        //if (marginal.getMaxEntropyNode() < token.length){
				//f.write(Double.toString(f1[marginal.getMaxEntropyNode()]) + " ");
			//}
			//int count = 0;

			/* count of the number of nodes with accuracy improvement more than
			 * that of the maximum entropy node.
			 */
			//for (int i = 0; i < token.length; i++) {
			//	if ((f1[marginal.getMaxEntropyNode()] <= f1[i])
			//			&& (i != marginal.getMaxEntropyNode()))
			//		count++;
			//}
			//f.write("\n");

			//f.write(Integer.toString(count) + "\n");
		}
		// tdw1.close();
		// tdw.close();
		// f.write("\n");
		
		// tdw.close();
		// testData = new TestData(baseDir + "/data/" + inName + "/" + inName
		// + ".test", delimit, impDelimit, groupDelimit);
		// calc();

		//f2.write("\n\n");
		// Sort the turkerdata
		
//		Collections.sort(turkerData, new Comparator<TurkerData>() {
//			public int compare(TurkerData data1, TurkerData data2) {
//				return (data2.maxEntropy > data1.maxEntropy) ? 1 : 0;
//			}
//		});
//		double BUDGET = 1;
//		for (int i=0; i<BUDGET; i++) {
//			//f_3.write(turkerData.get(i).beforeClampingScore + " " + 
//					//turkerData.get(i).afterClampingScore + " " +
//					//turkerData.get(i).maxEntropy + " " +
//					//turkerData.get(i).maxEntropyNode + "\n");
//		}
//		//f2.write("\n\n");
//		// Sort the turkerdata
//		Collections.sort(turkerData, new Comparator<TurkerData>() {
//			public int compare(TurkerData data1, TurkerData data2) {
//				return (data2.maxNum > data1.maxNum) ? 1 : 0;
//			}
//		});
//		for (int i=0; i<BUDGET; i++) {
//			//f_3.write(turkerData.get(i).beforeClampingScore + " " + 
//					//turkerData.get(i).afterClampingScore + " " +
//					//turkerData.get(i).maxNum + " " +
//					//turkerData.get(i).maxEntropy + "\n");
//		}
		//f3.write("\n\n");
		//generateTopK(2);
	

		
		f.close();
                f2.close();
                f3.close();
//                facc.close();
//                double acc = (double)rightToks/(double)totalToks;
//                System.out.println("Correct tokens: " + rightToks);
//                System.out.println("Total tokens: " + totalToks);
//                System.out.println("Accuracy: " + acc);
	}

	
////////////////////	
	/*
	public void generateTopK(int k) throws ParserConfigurationException,
			TransformerException {
		int count = 0;
		XMLGenerator xmlGenerator;
		TurkerData tData;

		Iterator it = turkerData.iterator();
		while (count < k) {
			if (it.hasNext()) {
				tData = (TurkerData) it.next();
				System.out.println(tData.numNodes + " " + tData.maxEntropyNode
						+ " " + Arrays.toString(tData.seq));
				xmlGenerator = new XMLGenerator();
				xmlGenerator.generateDocument(tData.rawLine, tData.seq,
						tData.maxEntropyNode, count + 1);
				count++;
			} else
				break;
		}
	}
*/
//////////////////////////
	/*
	 * public void doTestWithClamping() throws Exception { // f.write("\n");
	 * File dir = new File(baseDir + "/out/" + outDir); dir.mkdirs(); TestData
	 * testData = new TestData(baseDir + "/data/" + inName + "/" + inName +
	 * ".test", delimit, impDelimit, groupDelimit); TestDataWrite tdw; String
	 * collect[] = new String[nlabels]; TestRecord testRecord = new
	 * TestRecord(collect); TrainData taggedTestData =
	 * DataCruncher.readTagged(nlabels, baseDir + "/data/" + inName + "/" +
	 * inName + ".test", baseDir + "/data/" + inName + "/" + inName + ".test",
	 * delimit, tagDelimit, impDelimit, labelMap); TestDataWrite tdw1; tdw1 =
	 * new TestDataWrite(baseDir + "/data/" + inName + "/" + "tempTagged" +
	 * ".test", baseDir + "/data/" + inName + "/" + inName + ".test", delimit,
	 * tagDelimit, impDelimit, labelMap); for (String seq[] =
	 * testData.nextRecord(); seq != null; seq = testData .nextRecord()) {
	 * testRecord.init(seq); if (options.getInt("debugLvl") > 1) {
	 * Util.printDbg("Invoking segment on " + seq); }
	 * 
	 * int token[] = allLabels(taggedTestData.nextRecord());
	 * tdw1.writeRecord(token, testRecord.length()); tdw1.close();
	 * 
	 * tdw = new TestDataWrite(baseDir + "/out/" + outDir + "/" + inName +
	 * ".test", baseDir + "/data/" + inName + "/" + inName + ".test", delimit,
	 * tagDelimit, impDelimit, labelMap); int path[] = segment(testRecord,
	 * testData.groupedTokens(), collect); tdw.writeRecord(path,
	 * testRecord.length()); tdw.close();
	 * 
	 * calc();
	 * 
	 * f.write(" "); Marginals marginal = new Marginals(crfModel, testRecord,
	 * null); marginal.compute(); marginal.computeMarginal();
	 * marginal.normalize(); marginal.calculateEntropy();
	 * 
	 * for (int i = 0; i < token.length; i++) { /* tdw1=new
	 * TestDataWrite(baseDir + "/data/" + inName + "/" + "tempTagged" + ".test",
	 * baseDir + "/data/" + inName + "/" + inName + ".test", delimit,
	 * tagDelimit, impDelimit, labelMap); tdw1.writeRecord(token,
	 * testRecord.length()); tdw1.close();
	 * 
	 * 
	 * tdw = new TestDataWrite(baseDir + "/out/" + outDir + "/" + inName +
	 * ".test", baseDir + "/data/" + inName + "/" + inName + ".test", delimit,
	 * tagDelimit, impDelimit, labelMap); path = segment(testRecord,
	 * testData.groupedTokens(), collect, i, token[i]); tdw.writeRecord(path,
	 * testRecord.length()); tdw.close(); calc(); f.write(" "); } f.write("\n");
	 * } f.close(); }
	 */

	TrainData taggedData = null;

	int[] allLabels(TrainRecord tr) {
		int[] labs = new int[tr.length()];
		for (int i = 0; i < labs.length; i++)
			labs[i] = tr.y(i);
		return labs;
	}

	String arrayToString(Object[] ar) {
		String st = "";
		for (int i = 0; i < ar.length; i++)
			st += (ar[i] + " ");
		return st;
	}

	public double calcWith(TestRecord testRecord) throws Exception {
		Marginals marginal = new Marginals(crfModel, testRecord, null);
		marginal.compute();
		marginal.printBetaVector();
		marginal.computeMarginal();
		marginal.normalize();
		//marginal.printNormalized();
		marginal.calculateEntropy(method);
		Vector s = new Vector();
                String outDirec = "out2";
                String filename = "xPubMed_ByHighestEntropy_t0l0l1l1_totalEntropy_3.csv";
//		TrainData tdMan = DataCruncher.readTagged(nlabels, baseDir + "/data/"
//				+ inName + "/" + "tempTagged2" + ".test", baseDir + "/data/"
//				+ inName + "/" + inName + ".test", delimit, tagDelimit,
//				impDelimit, labelMap);
//		TrainData tdAuto = DataCruncher.readTagged(nlabels, baseDir + "/" + outDirec + "/"
//				+ outDir + "/" + inName + ".test", baseDir + "/data/" + inName
//				+ "/" + inName + ".test", delimit, tagDelimit, impDelimit,
//				labelMap);
//                DataCruncher.readRaw(s, baseDir + "/data/" + inName + "/" + inName
//				+ ".test", "", "");
                TrainData tdMan = DataCruncher.readTagged(nlabels, baseDir + "/data/"
				+ inName + "/" + "tempTagged2" + ".test", baseDir + "/data/PubMed_HighEntropy/"
				+ filename + ".test", delimit, tagDelimit,
				impDelimit, labelMap);
		TrainData tdAuto = DataCruncher.readTagged(nlabels, baseDir + "/" + outDirec + "/"
				+ outDir + "/" + inName + ".test", baseDir + "/data/PubMed_HighEntropy/"
				+ filename + ".test", delimit, tagDelimit, impDelimit,
				labelMap);
		DataCruncher.readRaw(s, baseDir + "/data/PubMed_HighEntropy/" + filename
				+ ".test", "", "");
                
                
		int len = tdAuto.size();
		int truePos[] = new int[nlabels + 1];
		int totalMarkedPos[] = new int[nlabels + 1];
		int totalPos[] = new int[nlabels + 1];
		int confuseMatrix[][] = new int[nlabels][nlabels];
		//boolean printDetails = (options.getInt("debugLvl") > 0);
		boolean printDetails = false;
                if (tdAuto.size() != tdMan.size()) {
			// Sanity Check
			System.out.println("Length Mismatch - Raw: " + len + " Auto: "
					+ tdAuto.size() + " Man: " + tdMan.size());
		}

		for (int i = 0; i < len; i++) {
			String raw[] = (String[]) (s.get(i));
			TrainRecord trMan = tdMan.nextRecord();
			TrainRecord trAuto = tdAuto.nextRecord();
			int tokenMan[] = allLabels(trMan);
			int tokenAuto[] = allLabels(trAuto);

			if (tokenMan.length != tokenAuto.length) {
				// Sanity Check
				System.out.println("Length Mismatch - Manual: "
						+ tokenMan.length + " Auto: " + tokenAuto.length);
				System.err.print("MISMATCH");
				System.exit(1);
				// continue;
			}
			// remove invalid tagging.
			boolean invalidMatch = false;
			int tlen = tokenMan.length;
			for (int j = 0; j < tlen; j++) {
				if (printDetails)
					System.err.println(tokenMan[j] + " " + tokenAuto[j]);
				if (tokenAuto[j] < 0) {
					invalidMatch = true;
					break;
				}
			}
			if (invalidMatch) {
				if (printDetails)
					System.err.println("No valid path");
				continue;
			}
			int correctTokens = 0;
			for (int j = 0; j < tlen; j++) {
				totalMarkedPos[tokenAuto[j]]++;
				totalMarkedPos[nlabels]++;
				totalPos[tokenMan[j]]++;
				totalPos[nlabels]++;
				confuseMatrix[tokenMan[j]][tokenAuto[j]]++;
				if (tokenAuto[j] == tokenMan[j]) {
					correctTokens++;
					truePos[tokenMan[j]]++;
					truePos[nlabels]++;
				
					
				}
				else {
					
				}
			}
			if (printDetails)
				System.err.println("Stats: " + correctTokens + " " + (tlen));
			int rlen = raw.length;
			for (int j = 0; j < rlen; j++) {
				if (printDetails)
					System.err.print(raw[j] + " ");
			}
			if (printDetails)
				System.err.println();
			for (int j = 0; j < nlabels; j++) {
				String mstr = "";
				for (int k = 0; k < trMan.numSegments(j); k++)
					mstr += arrayToString(trMan.tokens(j, k));
				String astr = "";
				for (int k = 0; k < trAuto.numSegments(j); k++)
					astr += arrayToString(trAuto.tokens(j, k));

				if (!mstr.equalsIgnoreCase(astr))
					if (printDetails)
						System.err.print("W");
				if (printDetails)
					System.err.println(j + ": " + mstr + " : " + astr);
			}
			if (printDetails)
				System.err.println();
		}

		if (confuseSet != null) {
			System.out.println("Confusion Matrix:");
			System.out.print("M\\A");
			for (int i = 0; i < nlabels; i++) {
				if (confuseSet[i]) {
					//System.out.print("\t" + (i));
				}
			}
			System.out.println();
			for (int i = 0; i < nlabels; i++) {
				if (confuseSet[i]) {
					System.out.print(i);
					for (int j = 0; j < nlabels; j++) {
						if (confuseSet[j]) {
							//System.out.print("\t" + confuseMatrix[i][j]);
						}
					}
					System.out.println();
				}
			}
		}
		// System.out.println("\n\nCalculations:");
		// System.out.println();
		// System.out.println("Label\tTrue+\tMarked+\tActual+\tPrec.\tRecall\tF1");
		double prec, recall;
		for (int i = 0; i < nlabels; i++) {
			prec = (totalMarkedPos[i] == 0) ? 0
					: ((double) (truePos[i] * 100000 / totalMarkedPos[i])) / 1000;
			recall = (totalPos[i] == 0) ? 0
					: ((double) (truePos[i] * 100000 / totalPos[i])) / 1000;
			// System.out.println((i) + ":\t" + truePos[i] + "\t"
			// + totalMarkedPos[i] + "\t" + totalPos[i] + "\t" + prec
			// + "\t" + recall + "\t" + 2 * prec * recall
			// / (prec + recall));
		}
		// System.out
		// .println("---------------------------------------------------------");
		prec = (totalMarkedPos[nlabels] == 0) ? 0
				: ((double) (truePos[nlabels] * 100000 / totalMarkedPos[nlabels])) / 1000;
		recall = (totalPos[nlabels] == 0) ? 0
				: ((double) (truePos[nlabels] * 100000 / totalPos[nlabels])) / 1000;
		// System.out.println("Ov:\t" + truePos[nlabels] + "\t"
		// + totalMarkedPos[nlabels] + "\t" + totalPos[nlabels] + "\t"
		// + prec + "\t" + recall + "\t" + 2 * prec * recall
		// / (prec + recall));
		return ((2 * prec * recall) / (prec + recall));
	}

	public void calc() throws Exception {
		Vector s = new Vector();
		TrainData tdMan = DataCruncher.readTagged(nlabels, baseDir + "/data/"
				+ inName + "/" + inName + ".test", baseDir + "/data/" + inName
				+ "/" + inName + ".test", delimit, tagDelimit, impDelimit,
				labelMap);
		TrainData tdAuto = DataCruncher.readTagged(nlabels, baseDir + "/out/"
				+ outDir + "/" + inName + ".test", baseDir + "/data/" + inName
				+ "/" + inName + ".test", delimit, tagDelimit, impDelimit,
				labelMap);
		DataCruncher.readRaw(s, baseDir + "/data/" + inName + "/" + inName
				+ ".test", "", "");
		int len = tdAuto.size();
		int truePos[] = new int[nlabels + 1];
		int totalMarkedPos[] = new int[nlabels + 1];
		int totalPos[] = new int[nlabels + 1];
		int confuseMatrix[][] = new int[nlabels][nlabels];
		boolean printDetails = (options.getInt("debugLvl") > 0);
		if (tdAuto.size() != tdMan.size()) {
			// Sanity Check
			System.out.println("Length Mismatch - Raw: " + len + " Auto: "
					+ tdAuto.size() + " Man: " + tdMan.size());
		}

		for (int i = 0; i < len; i++) {
			String raw[] = (String[]) (s.get(i));
			TrainRecord trMan = tdMan.nextRecord();
			TrainRecord trAuto = tdAuto.nextRecord();
			int tokenMan[] = allLabels(trMan);
			int tokenAuto[] = allLabels(trAuto);

			if (tokenMan.length != tokenAuto.length) {
				// Sanity Check
				System.out.println("Length Mismatch - Manual: "
						+ tokenMan.length + " Auto: " + tokenAuto.length);
				// continue;
			}
			// remove invalid tagging.
			boolean invalidMatch = false;
			int tlen = tokenMan.length;
			for (int j = 0; j < tlen; j++) {
				if (printDetails)
					System.err.println(tokenMan[j] + " " + tokenAuto[j]);
				if (tokenAuto[j] < 0) {
					invalidMatch = true;
					break;
				}
			}
			if (invalidMatch) {
				if (printDetails)
					System.err.println("No valid path");
				continue;
			}
			int correctTokens = 0;
			for (int j = 0; j < tlen; j++) {
				totalMarkedPos[tokenAuto[j]]++;
				totalMarkedPos[nlabels]++;
				totalPos[tokenMan[j]]++;
				totalPos[nlabels]++;
				confuseMatrix[tokenMan[j]][tokenAuto[j]]++;
				if (tokenAuto[j] == tokenMan[j]) {
					correctTokens++;
					truePos[tokenMan[j]]++;
					truePos[nlabels]++;
				}
			}
			if (printDetails)
				System.err.println("Stats: " + correctTokens + " " + (tlen));
			int rlen = raw.length;
			for (int j = 0; j < rlen; j++) {
				if (printDetails)
					System.err.print(raw[j] + " ");
			}
			if (printDetails)
				System.err.println();
			for (int j = 0; j < nlabels; j++) {
				String mstr = "";
				for (int k = 0; k < trMan.numSegments(j); k++)
					mstr += arrayToString(trMan.tokens(j, k));
				String astr = "";
				for (int k = 0; k < trAuto.numSegments(j); k++)
					astr += arrayToString(trAuto.tokens(j, k));

				if (!mstr.equalsIgnoreCase(astr))
					if (printDetails)
						System.err.print("W");
				if (printDetails)
					System.err.println(j + ": " + mstr + " : " + astr);
			}
			if (printDetails)
				System.err.println();
		}

		if (confuseSet != null) {
			System.out.println("Confusion Matrix:");
			System.out.print("M\\A");
			for (int i = 0; i < nlabels; i++) {
				if (confuseSet[i]) {
					///System.out.print("\t" + (i));
				}
			}
			System.out.println();
			for (int i = 0; i < nlabels; i++) {
				if (confuseSet[i]) {
					System.out.print(i);
					for (int j = 0; j < nlabels; j++) {
						if (confuseSet[j]) {
							//System.out.print("\t" + confuseMatrix[i][j]);
						}
					}
					System.out.println();
				}
			}
		}
		System.out.println("\n\nCalculations:");
		System.out.println();
		System.out.println("Label\tTrue+\tMarked+\tActual+\tPrec.\tRecall\tF1");
		double prec, recall;
		for (int i = 0; i < nlabels; i++) {
			prec = (totalMarkedPos[i] == 0) ? 0
					: ((double) (truePos[i] * 100000 / totalMarkedPos[i])) / 1000;
			recall = (totalPos[i] == 0) ? 0
					: ((double) (truePos[i] * 100000 / totalPos[i])) / 1000;
			System.out.println((i) + ":\t" + truePos[i] + "\t"
					+ totalMarkedPos[i] + "\t" + totalPos[i] + "\t" + prec
					+ "\t" + recall + "\t" + 2 * prec * recall
					/ (prec + recall));
		}
		System.out
				.println("---------------------------------------------------------");
		prec = (totalMarkedPos[nlabels] == 0) ? 0
				: ((double) (truePos[nlabels] * 100000 / totalMarkedPos[nlabels])) / 1000;
		recall = (totalPos[nlabels] == 0) ? 0
				: ((double) (truePos[nlabels] * 100000 / totalPos[nlabels])) / 1000;
		System.out.println("Ov:\t" + truePos[nlabels] + "\t"
				+ totalMarkedPos[nlabels] + "\t" + totalPos[nlabels] + "\t"
				+ prec + "\t" + recall + "\t" + 2 * prec * recall
				/ (prec + recall));
		//f.write(Double.toString((2 * prec * recall) / (prec + recall)));

	}
};

class CRFoutput {
		int tokenID[];
                int citationID;
                //String rawLine;
		String seqs[];
                int token[];
                int trueLabels[];
                Double totalMarginal[][];
                double entropy[];
		//int numNodes;
		//int maxEntropyNode;
		//double maxEntropy;
		//double beforeClampingScore;
		//double afterClampingScore;
		//double maxNum;
                int autoLabels[];

		CRFoutput(int tokenID[], int citationID, String seqs[], int token[], int trueLabels[], Double totalMarginal[][]) {
			//this.rawLine = rawLine;
			//seq = new String[sequence.length];
			//seq = sequence;
			//this.numNodes = numNodes;
			//this.maxEntropyNode = maxEntropyNode;
			//this.beforeClampingScore = beforeClampingScore;
			//this.afterClampingScore = afterClampingScore;
			//this.maxEntropy = maxEntropy;
			//this.maxNum = maxNum;
                        this.tokenID = tokenID;
                        this.citationID = citationID;
                        this.seqs = seqs;
                        this.token = token;
                        this.trueLabels = trueLabels;
                        this.totalMarginal = totalMarginal;
                }
                
                CRFoutput() {}
                
                public void setCitationID(int citationID) {
                    this.citationID = citationID;
                }
                
                public void setSeqs(String seqs[]) {
                    this.seqs = new String[seqs.length];
                    for (int ii=0; ii<seqs.length; ii++) {
                        this.seqs[ii] = seqs[ii];
                    }
                }
                
                public void setToks(int token[]) {
                    this.token = new int[token.length];
                    for (int ii=0; ii<token.length; ii++) {
                        this.token[ii] = token[ii];
                    }
                }
                
                public void setTrueLabels(int trueLabels[]) {
                    this.trueLabels = trueLabels;
                }
                
                public void setTotalMarginal(Double totalMarginal[][]) {
                    this.totalMarginal = totalMarginal;
                }
                
                public void setEntropy(double entropy[]) {
                    this.entropy = entropy;
                }
                
                public void setAutoLabels(int autoLabels[]) {
                        this.autoLabels = autoLabels;
                }
	}