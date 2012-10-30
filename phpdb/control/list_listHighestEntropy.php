<?php
//header("Content-disposition: attachment; filename=highest_entropies.csv");

$patterns = array("t 0,l 0,t-1,t+1", "t 0,l 0,l-1,l+1", "E 0,L 0,L-1,L+1", "t 0,l 0,t-1,l-1,t+1,l+1");

foreach ($patterns as $pattern) {
	$result = doQuery("SELECT ca_id FROM cluster_algorithms WHERE ca_pattern='$pattern'");
	if ($row = mysql_fetch_assoc($result)) {
		$patternId = $row['ca_id'];
	} else {
		doQuery("INSERT INTO cluster_algorithms (ca_pattern) VALUES ('$pattern')");
		$patternId = mysql_insert_id();
	}

	doQuery("INSERT INTO clusterings (clustering_algorithm) VALUES ($patternId)");
	$clusteringId = mysql_insert_id();
	$nextClusterIndex = 1;
	
	$result = doQuery("SELECT token_id, token_start, ts_string, tl_entropy, tl_label, citation_id, citation_text, token_gold_standard FROM tokens JOIN token_labelings ON token_current_labeling=tl_id JOIN token_strings ON ts_id=token_string_id JOIN citations ON token_citation_id=citation_id"/*ORDER BY token_citation_id, token_start"*/);
	
	$lastCitation = 0;
	$lastCitationText = 0;
	$highestEntropy = 0;
	$highestEntropyTokenIndex = -1;
	$highestEntropyEntityIndex = -1;
	$highestEntropyStart = 0;
	$highestEntropyCriteria = array();
	$highestEntropyGoldStandard = -1;
	$highestEntropyTokenId = -1;
	
	$tokenClassifierMapping = array();
	
	$skipped = 0;
	$retained = 0;
	
	$tokens = array();
	$tokenLabels = array();
	$entities = array();
	$entityLabels = array();
	
	$currentEntity = '';
	$currentEntityLabel = -1;
	
	$tokenIndex = 0;
	$entityIndex = 0;
	
	$errors = 0;
	
	$tokenHisto = array();
	
	$labelMapping = array(0=>'Title', 1=>'Author', 2=>'Conference', 3=>'ISBN', 4=>'Publisher', 5=>'Series', 6=>'Proceedings', 7=>'Year');
	$colorMapping = array(0=>'red', 1=>'blue', 2=>'green', 3=>'grey', 4=>'orange', 5=>'violet', 6=>'pink', 7=>'maroon');
	//print "<H2>$pattern</H2>";
	//print "<B>Color Map</B><BR>";
	foreach ($colorMapping as $key=>$color) {
		//print "{$labelMapping[$key]} --&gt; <font color='$color'>$color</font><BR>";
	}
	while ($row = mysql_fetch_assoc($result)) {
		$tokenId = $row['token_id'];
		$tokenString = $row['ts_string'];
		$citationId = $row['citation_id'];
		$citationText = $row['citation_text'];
		$tokenStart = $row['token_start'];
		$entropy = $row['tl_entropy'];
		$label = $row['tl_label'];
		$goldStandard = $row['token_gold_standard'];
		
		if ($lastCitation != $citationId) {
			if ($lastCitation != 0) {
				if (isset($tokenHisto[$tokens[$highestEntropyTokenIndex]])) {
					$tokenHisto[$tokens[$highestEntropyTokenIndex]]++;
				} else {
					$tokenHisto[$tokens[$highestEntropyTokenIndex]] = 1;
				}
				
				
				$entities[] = trim($currentEntity);
				$entityLabels[] = $currentEntityLabel;
				$entityIndex++;
				$currentEntityLabel = $label;
				$currentEntity = '';
				
				$citationText = '';
				for ($i = 0; $i < count($entities); $i++) {
					$citationText .= "<font color='{$colorMapping[$entityLabels[$i]]}'>";
					if ($i == $highestEntropyEntityIndex) {
						$entityText = str_replace(" ".$tokens[$highestEntropyTokenIndex]." ", " *{$tokens[$highestEntropyTokenIndex]}* ", $entities[$i]);
						$citationText .= "<B>$entityText</B>";
					} else {
						$citationText .= $entities[$i];
					}
					$citationText .= "</font> ";
				}
				$skip = 0;
				
				$criteria = explode(',', $pattern);
				$highestEntropyCriteria = array();
				foreach ($criteria as $criterion) {
					$target = substr($criterion, 0, 1);
					$sign = substr($criterion, 1, 1);
					$offset = substr($criterion, 2, 1);
					
					if ($sign == '-') {
						$offset *= -1;
					}
					
					if ($target == 'l' || $target == 't') {
						$index = $highestEntropyTokenIndex + $offset;
						if ($index >= $tokenIndex || $index < 0) {
							$highestEntropyCriteria[] = 0;
						} elseif ($target == 'l') {
							$highestEntropyCriteria[] = $tokenLabels[$index];
						} elseif ($target == 't') {
							$highestEntropyCriteria[] = $tokens[$index];
						}
					} elseif ($target == 'L' || $target == 'E') {
						$index = $highestEntropyEntityIndex + $offset;
						if ($index >= $entityIndex || $index < 0) {
							$highestEntropyCriteria[] = 0;
						} elseif ($target == 'L') {
							$highestEntropyCriteria[] = $entityLabels[$index];
						} elseif ($target == 'E') {
							$highestEntropyCriteria[] = $entities[$index];
						}
					}
				}
				
				$targetArray = &$tokenClassifierMapping;
				foreach ($highestEntropyCriteria as $criterion) {
					if (!isset($targetArray[$criterion])) {
						$targetArray[$criterion] = array();
						$targetArray[$criterion]['averageEntropy'] = $highestEntropy;
						$targetArray[$criterion]['highestEntropy'] = $highestEntropy;
						$targetArray[$criterion]['totalEntropy'] = $highestEntropy;
						$targetArray[$criterion]['count'] = 1;
						$targetArray[$criterion]['match'] = $criterion;
						$targetArray[$criterion]['members'] = array();
					} else {
						$targetArray[$criterion]['count']++;
						$targetArray[$criterion]['averageEntropy'] = ($targetArray[$criterion]['averageEntropy'] * ($targetArray[$criterion]['count'] - 1) + $highestEntropy) / $targetArray[$criterion]['count'];
						$targetArray[$criterion]['highestEntropy'] = max($targetArray[$criterion]['highestEntropy'], $highestEntropy);
						$targetArray[$criterion]['totalEntropy'] += $highestEntropy;
					}
					$targetArray = &$targetArray[$criterion]['members']; 
				}
				if (isset($targetArray['count'])) {
					$targetArray['count']++;
					$targetArray['averageEntropy'] = ($targetArray['averageEntropy'] * ($targetArray['count'] - 1) + $highestEntropy) / $targetArray['count'];
					$targetArray['highestEntropy'] = max($targetArray['highestEntropy'], $highestEntropy);
					$targetArray['totalEntropy'] += $highestEntropy; 
					$targetArray['citations'][] = $citationText;
							
					doQuery("INSERT INTO citation_clusters (cc_cluster_id, cc_citation_id, cc_clustering_id) VALUES ({$targetArray['clusterId']}, $lastCitation, $clusteringId)");
	
					doQuery("UPDATE cluster_rankings SET cr_value={$targetArray['highestEntropy']} WHERE cr_cluster_id={$targetArray['clusterId']} AND cr_function=1 AND cr_clustering_id=$clusteringId");
					doQuery("UPDATE cluster_rankings SET cr_value={$targetArray['count']} WHERE cr_cluster_id={$targetArray['clusterId']} AND cr_function=2 AND cr_clustering_id=$clusteringId");
					doQuery("UPDATE cluster_rankings SET cr_value={$targetArray['totalEntropy']} WHERE cr_cluster_id={$targetArray['clusterId']} AND cr_function=3 AND cr_clustering_id=$clusteringId");
					if ($targetArray['goldStandard'] != $highestEntropyGoldStandard) {
						//print "<P>Error - this citation has gold standard of <B>{$labelMapping[$highestEntropyGoldStandard]}</B>, whereas the first in the sequence had <B>{$labelMapping[$targetArray['goldStandard']]}</B><BR>This citation: $citationText<BR>Original in series: {$targetArray['citations'][0]}</P>";
						
						$errors++;
					}
					$skip = 1;
				} else {
					$targetArray['count'] = 1;
					$targetArray['averageEntropy'] = $highestEntropy;
					$targetArray['citations'] = array($citationText);
					$targetArray['goldStandard'] = $highestEntropyGoldStandard;
					$targetArray['highestEntropy'] = $highestEntropy;
					$targetArray['totalEntropy'] = $highestEntropy; 
					
					//print "NEXT CLUSTER INDEX: $nextClusterIndex\n";
					//print_r($targetArray);
					$targetArray['clusterId'] = $nextClusterIndex;
					doQuery("INSERT INTO citation_clusters (cc_cluster_id, cc_citation_id, cc_clustering_id) VALUES ($nextClusterIndex, $lastCitation, $clusteringId)");	
					doQuery("INSERT INTO cluster_rankings (cr_cluster_id, cr_value, cr_function, cr_clustering_id) VALUES ($nextClusterIndex, $highestEntropy, 1, $clusteringId)");
					doQuery("INSERT INTO cluster_rankings (cr_cluster_id, cr_value, cr_function, cr_clustering_id) VALUES ($nextClusterIndex, 1, 2, $clusteringId)");	
					doQuery("INSERT INTO cluster_rankings (cr_cluster_id, cr_value, cr_function, cr_clustering_id) VALUES ($nextClusterIndex, $highestEntropy, 3, $clusteringId)");					
					$nextClusterIndex++;
				}
				doQuery("UPDATE citations SET citation_highest_entropy_token=$highestEntropyTokenId WHERE citation_id=$lastCitation");
				
				if (!$skip) {
					$citationText = addSlashes($citationText);
					$tokenString = addSlashes($tokens[$highestEntropyTokenIndex]);
					//print "\"$tokenString\",$highestEntropyStart,$highestEntropy,$lastCitation,\"$citationText\"\n";
					$retained++;
				} else {
					$skipped++;
				}
			}
			$highestEntropy = 0;
			$tokenIndex = 0;
			$entityIndex = 0;
			$currentEntityLabel = -1;
			$tokens = array();
			$tokenLabels = array();
			$entities = array();
			$entityLabels = array();
		} 
		
		$tokens[] = $tokenString;
		$tokenLabels[] = $label;
		if ($label != $currentEntityLabel) {
			if ($currentEntityLabel != -1) {
				$entities[] = trim($currentEntity);
				$entityLabels[] = $currentEntityLabel;
				$entityIndex++;
			}
			$currentEntityLabel = $label;
			$currentEntity = '';
		}
		$currentEntity .= ' '.$tokenString;
		
		if ($entropy > $highestEntropy && $tokenString != ',') {
			$highestEntropy = $entropy;
			$highestEntropyTokenIndex = $tokenIndex;
			$highestEntropyEntityIndex = $entityIndex;
			$highestEntropyStart = $tokenStart;
			$highestEntropyGoldStandard = $goldStandard;
			$highestEntropyTokenId = $tokenId;
		} 
		$lastCitation = $citationId;
		$lastCitationText = $citationText;
		$tokenIndex++;
	}
/*	
	function cmp($a, $b)
	{
		if ($a['averageEntropy'] == $b['averageEntropy']) {
			return 0;
		}
		return ($a['averageEntropy'] > $b['averageEntropy']) ? -1 : 1;
	}
	usort($tokenClassifierMapping, "cmp");
	
	print "<HTML><BODY>";
	print "Retained: $retained Skipped: $skipped Errors: $errors<P>";
	print "<P>Tokens:";
	asort($tokenHisto, SORT_DESC);
	print_r($tokenHisto);
	print "</P>";
	print "<PRE>";
	print_r($tokenClassifierMapping);
	print "</PRE></BODY></HTML>";
*/
}
?>
