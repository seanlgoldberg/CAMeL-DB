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
$highestEntropyContextToken1 = 0;
$highestEntropyContextToken2 = 0;

$previousPreviousToken = 0;
$previousPreviousLabel = 0;
$previousToken = 0;
$previousLabel = 0;

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
					if (isset($tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1])) {
						if (isset($tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1][$highestEntropyContextToken2])) {
							$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1][$highestEntropyContextToken2]++;	
							$skip = 1;
						} else {
							$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1][$highestEntropyContextToken2] = 1;
						}
					} else {
						$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1] = array();
						$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1][$highestEntropyContextToken2] = 1;
					}
				} else {
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel] = array();
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1] = array();  
					$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1][$highestEntropyContextToken2] = 1;
				}
			} else {
				$tokenClassifierMapping[$highestEntropyTokenId] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1] = array();
				$tokenClassifierMapping[$highestEntropyTokenId][$highestEntropyLabel][$highestEntropyContextToken1][$highestEntropyContextToken2] = 1;
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
		$previousPreviousToken = $previousPreviousLabel = $previousToken = $previousLabel = 0;
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
		$highestEntropyContextToken1 = $previousToken;
		$highestEntropyContextToken2 = $previousPreviousToken;
		$distanceFromHighestEntropy = 0;
	} else {
		$distanceFromHighestEntropy++;
		if ($distanceFromHighestEntropy == 1) {
			$highestEntropyContextToken2 = $tokenId;
		} elseif ($distanceFromHighestEntropy == 2 && $highestEntropyContextToken1 == 0) {
			$highestEntropyContextToken1 = $tokenId;
		}
	}
	$lastCitation = $citationId;
	
	$previousPreviousToken = $previousToken;
	$previousPreviousLabel = $previousLabel;
	$previousToken = $tokenId;
	$previousLabel = $label;
}

print "Retained: $retained Skipped: $skipped";
?>
