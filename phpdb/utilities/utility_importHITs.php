<?php
	require('../utilities.php');
	date_default_timezone_set("EST");
	$labelTypes = array('title'=>0, 'author'=>1, 'conference'=>2, 'isbn'=>3, 'publisher'=>4, 'series'=>5, 'proceedings'=>6, 'year'=>7);

	doQuery("INSERT INTO experiments (experiment_description, experiment_dataset) VALUES ('Turker analysis of 2% of DBLP dataset.', 1)");
	$experimentId = mysql_insert_id();	
	$hits = array();
	
	if (($handle = fopen("questionOutput.csv", "r")) !== FALSE) {
    		while (($data = fgetcsv($handle, 1000, ",")) !== FALSE) {
			if (!isset($hits[$data[2]])) {
				$hits[$data[2]] = array();
			}
			$hits[$data[2]][] = $data[0].':'.$data[1]; 		
		}
	}
	
	if (($handle = fopen("HITResults_DBLP140_10302012.csv", "r")) !== FALSE) {
    	$count = 0;
	while (($data = fgetcsv($handle, 5000, ",")) !== FALSE) {
		if ($count == 0) {
			$count = 1;
			continue;
		}	
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
			$citationTokens = $hits[$hitId];
			//$answers = array();
			for ($i = 0; $i < 10; $i++) {			
				$parts = explode(':', $citationTokens[$i]);
				$citationId = $parts[0];
				$position = $parts[1] + 2;
				//print "Citation Id: $citationId Start: $position\n";
				$result = doQuery("SELECT token_id FROM tokens WHERE token_citation_id=$citationId AND token_start=$position");
				$tokenId = 0;
				if ($row = mysql_fetch_assoc($result)) {
					$tokenId = $row['token_id'];
				}
				if (strlen($data[7 + $i]) <= 1) {
					continue;
				}
				$labelId = $labelTypes[$data[7 + $i]];
				doQuery("INSERT INTO evidence (evidence_token_id, evidence_turker_id, evidence_hit_id, evidence_label, evidence_experiment, evidence_time_elapsed) VALUES ($tokenId, $turkerId, '$hitId', $labelId, $experimentId, $timePerQuestion)"); 
				//$answers[] = $data[7+$i];
			}
			$count++; 		
		}
	}

?>
