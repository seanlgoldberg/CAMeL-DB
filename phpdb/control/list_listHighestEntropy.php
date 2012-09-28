<?php
header("Content-disposition: attachment; filename=highest_entropies.csv");

$result = doQuery("SELECT token_string_id, token_start, ts_string, tl_entropy, tl_label, citation_id, citation_text FROM tokens JOIN token_labelings ON token_current_labeling=tl_id JOIN token_strings ON ts_id=token_string_id JOIN citations ON token_citation_id=citation_id "/*ORDER BY token_citation_id, token_start"*/);

$lastCitation = 0;
$highestEntropy = 0;
$highestEntropyTokenId = 0;
$highestEntropyToken = '';
$highestEntropyText = '';
$highestEntropyStart = 0;
$highestEntropyLabel = 0;
$highestEntropyContextLabel1 = 0;
$highestEntropyContextLabel2 = 0;
$highestEntropyContextLabel3 = 0;
$highestEntropyContextLabel4 = 0;

$previousPreviousLabel = 0;
$previousLabel = 0;
$currentLabel = 0;

$tokenClassifierMapping = array();

$skipped = 0;
$retained = 0;

$distanceFromHighestEntropy = 0;

while ($row = mysql_fetch_assoc($result)) {
	$tokenId = $row['token_string_id'];
	$tokenString = $row['ts_string'];
	$citationId = $row['citation_id'];
	$citationText = $row['citation_text'];
	$tokenStart = $row['token_start'];
	$entropy = $row['tl_entropy'];
	$label = $row['tl_label'];
	
	if ($lastCitation != $citationId) {
		if ($lastCitation != 0) {
			$citationText = substr($highestEntropyText, 0, $highestEntropyStart)."<B>".substr($highestEntropyText, $highestEntropyStart, strlen($highestEntropyToken))."</B>".substr($highestEntropyText, $highestEntropyStart + strlen($highestEntropyToken));
			
			$skip = 0;
			if (isset($tokenClassifierMapping[$highestEntropyTokenId])) {
				if (isset($tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel])) {
					if (isset($tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1])) {
						if (isset($tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2])) {
							if (isset($tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3])) {
								if (isset($tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4])) {
									$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4]++;	
									$skip = 1;
								} else {
									$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4] = 1;
								}
							} else {
								$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3] = array();
								$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4] = 1;
							}
						} else {
							$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2] = array();
							$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3] = array();
							$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4] = 1;		
						}
					} else {
						$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1] = array();
						$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2] = array();
						$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3] = array();
						$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4] = 1;		
					}
				} else {
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel] = array();
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1] = array();
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2] = array();
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3] = array();
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4] = 1;		
				}
			} else {
				$tokenClassifierMapping[$highestEntropyTokenId] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextLabel1][$highestEntropyContextLabel2][$highestEntropyContextLabel3][$highestEntropyContextLabel4] = 1;		
			}
			
			if (!$skip) {
				$citationText = addSlashes($citationText);
				$tokenString = addSlashes($highestEntropyToken);
				print "\"$tokenString\",$highestEntropy,$lastCitation,\"$citationText\"\n";
				$retained++;
			} else {
				$skipped++;
			}
		}
		$previousPreviousLabel = $previousLabel = 0;
		$currentLabel = $label;
		$highestEntropy = 0;
		$distanceFromHighestEntropy = -1000;
	} 
	if ($entropy > $highestEntropy) {
		$highestEntropy = $entropy;
		$highestEntropyTokenId = $tokenId;
		$highestEntropyToken = $tokenString;
		$highestEntropyText = $citationText;
		$highestEntropyStart = $tokenStart;
		$highestEntropyLabel = $label;
		$highestEntropyContextLabel1 = $previousLabel;
		$highestEntropyContextLabel2 = $previousPreviousLabel;
		$highestEntropyContextLabel3 = 0;
		$highestEntropyContextLabel4 = 0;
		$distanceFromHighestEntropy = 0;
	} else {
		$distanceFromHighestEntropy++;
		if ($distanceFromHighestEntropy == 1) {
			$highestEntropyContextLabel3 = $label;
		} elseif ($distanceFromHighestEntropy == 2) {
			$highestEntropyContextToken4 = $label;
		}
	}
	$lastCitation = $citationId;
	
	if ($label != $currentLabel) {
		$previousPreviousLabel = $previousLabel;
		$previousLabel = $currentLabel;
		$currentLabel = $label;
	}
}

print "Retained: $retained Skipped: $skipped";
?>
