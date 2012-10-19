<?php
	define("ALGORITHM_ID", 1);
	define("NUM_CLUSTERS", 140);
	$labelMapping = array(0=>'Title', 1=>'Author', 2=>'Conference', 3=>'ISBN', 4=>'Publisher', 5=>'Series', 6=>'Proceedings', 7=>'Year');
	
	header("Content-disposition: attachment; filename=top_clusters.csv");
	
	$nums = array(140);
	$algorithms = array(3);
	
	$clusterCount = 1;
	
	$errorCount = 0;
	
	foreach ($algorithms as $algorithm) {
		foreach ($nums as $numClusters) {
			$count = 0;
			doQuery("SET SESSION group_concat_max_len = 1000000");
			$result = doQuery("SELECT cluster_id, cluster_entropy, GROUP_CONCAT(cc_citation_id) AS citations, GROUP_CONCAT(citation_text SEPARATOR '~') AS citation_texts, ts_string, GROUP_CONCAT(token_start) AS starts, GROUP_CONCAT(token_gold_standard) AS golds, GROUP_CONCAT(tl_label) AS machineLabels, COUNT(cc_citation_id) AS citationCount FROM clusters JOIN citation_clusters ON cluster_algorithm=$algorithm AND cc_cluster_id=cluster_id JOIN citations ON cc_citation_id=citation_id JOIN tokens ON citation_highest_entropy_token=token_id JOIN token_strings ON ts_id=token_string_id JOIN token_labelings ON token_current_labeling=tl_id GROUP BY cluster_id ORDER BY cluster_entropy DESC LIMIT ".$numClusters);
			//print "<P>";
			while ($row = mysql_fetch_assoc($result)) {
				$clusterId = $row['cluster_id'];
				$entropy = $row['cluster_entropy'];
				$citations = $row['citations'];
				$starts = $row['starts'];
				$golds = $row['golds'];
				$machineLabels = $row['machineLabels'];
				$citationTexts = $row['citation_texts'];
				$citationArray = explode(',', $citations);
				$citationTextArray = explode('~', $citationTexts);
				$tokenString = $row['ts_string'];
				$startArray = explode(',', $starts);
				$goldArray = explode(',', $golds);
				$machineLabelArray = explode(',', $machineLabels);
				for ($i = 0; $i < count($citationArray); $i++) {
					$nextCitationId = $citationArray[$i];
					$nextCitationText = $citationTextArray[$i];
					$nextTokenStart = $startArray[$i];
					$nextGoldStandard = $goldArray[$i];
					$nextMachineLabel = $machineLabelArray[$i];
					if (strpos($nextCitationText, "\" ") == 0) {
						$nextCitationText = substr($nextCitationText, 2);
						$nextTokenStart -= 2;
					}
					if (strrpos($nextCitationText, "\"") == strlen($nextCitationText) - 1) {
						$nextCitationText = substr($nextCitationText, 0, strlen($nextCitationText) - 1);
					}
					$nextCitationText = substr($nextCitationText, 0, $nextTokenStart)."<b>".substr($nextCitationText, $nextTokenStart, strlen($tokenString))."</b>".substr($nextCitationText, $nextTokenStart + strlen($tokenString));
						
					$count++;
					print "$clusterCount, $nextCitationId, $nextTokenStart, $clusterId, $labelMapping[$nextGoldStandard], $labelMapping[$nextMachineLabel], \"$tokenString\", $nextCitationText\n";
					if ($nextGoldStandard != $nextMachineLabel) {
						$errorCount++;
					}
				}
				if ($i > 1) {
					$clusterCount++;
				}
				
				//print " $tokenString ($tokenGoldStandard: $count)";
			}
			//print "<BR><B>Algorithm</B>: $algorithm NUM CLUSTERS: $numClusters Count: $count</P>";
			//print "<P>Num Errors: $errorCount";
		}
	}
	
?>