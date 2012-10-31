<?php
	require('../utilities.php');
	doQuery("INSERT INTO experiments (experiment_description, experiment_dataset) VALUES ('Turker analysis of 2% of DBLP dataset.', 1)");
	$experimentId = mysql_insert_id();	
	$hits = array();
	
	if (($handle = fopen("questionOutput.csv", "r")) !== FALSE) {
    	while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
			$hits[$data[2]] = $data[0].':'.$data[1]; 		
		}
	}
	
	if (($handle = fopen("HITResults_DBLP140_10302012.csv", "r")) !== FALSE) {
    	while (($data = fgetcsv($handle, 5000, ",")) !== FALSE) {
			$hitId = $data[0];
			$assignmentId = $data[2];
			$turkerLabel = $data[3];
			
			$result = doQuery("SELECT turker_id FROM turkers WHERE turker_label='$turkerLabel'");
			if ($row = mysql_fetch_assoc($result)) {
				$turkerId = $row['turker_id'];
			} else {
				doQuery("INSERT INTO turkers (turker_label) VALUES ('$turkerLabel')");
				$turkerId = mysql_insert_id();
			}
			
			$acceptTime = strtotime($data[5]);
			$submitTime = strtotime($data[6]);
			$timePerQuestion = ($submitTime - $acceptTime) / 10;
			$citationToken = $hits[$hitId];
			$parts = explode(':', $citationToken);
			$citationId = $parts[0];
			$position = $parts[1];
			$result = doQuery("SELECT token_id FROM tokens WHERE token_citation_id=$citation AND token_start=$position");
			if ($row = mysql_fetch_assoc($result)) {
				$tokenId = $row['token_id'];
			}
			//$answers = array();
			for ($i = 0; $i < 10; $i++) {			
				doQuery("INSERT INTO evidence (evidence_token_id, evidence_turker_id, evidence_hit_id, evidence_label, evidence_experiment, evidence_time_elapsed) VALUES ($tokenId, $turkerId, '$hitId', '{$answers[$i]}', $experimentId, $timePerQuestion)"); 
				//$answers[] = $data[7+$i];
			}
			 		
		}
	}

'questionOutput.csv' contains (citationID, position, HITID) for all the citations in the database that had a question posted to AMT.

'HITResults_DBLP140_10302012.csv' contains an aggregated collection of all Turker answers sorted by HITID.  The schema is contained in the first row of the file.

Can you add both of these tables to the database and then do a join between them and the ground truth token table?  This is what I need for every citationID contained in questionOutput:

(CitationID, Position, HITID, WorkerID, Answer, GroundTruth)

?>