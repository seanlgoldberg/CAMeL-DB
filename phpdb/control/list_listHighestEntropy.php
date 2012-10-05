<?php
//header("Content-disposition: attachment; filename=highest_entropies.csv");

//$pattern = "t 0,l 0,t-1,t+1";
//$pattern = "t 0,l 0,l-1,l+1";
$pattern = "E 0,L 0,L-1,L+1";
//$pattern = "t 0,l 0,t-1,l-1,t+1,l+1";
//$pattern = "t 0,l 0";
$result = doQuery("SELECT token_string_id, token_start, ts_string, tl_entropy, tl_label, citation_id, citation_text, token_gold_standard FROM tokens JOIN token_labelings ON token_current_labeling=tl_id JOIN token_strings ON ts_id=token_string_id JOIN citations ON token_citation_id=citation_id"/*ORDER BY token_citation_id, token_start"*/);

$lastCitation = 0;
$lastCitationText = 0;
$highestEntropy = 0;
$highestEntropyTokenIndex = -1;
$highestEntropyEntityIndex = -1;
$highestEntropyStart = 0;
$highestEntropyCriteria = array();
$highestEntropyGoldStandard = -1;

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

$labelMapping = array(0=>'Title', 1=>'Author', 2=>'Conference', 3=>'ISBN', 4=>'Publisher', 5=>'Series', 6=>'Proceedings', 7=>'Year');
$colorMapping = array(0=>'red', 1=>'blue', 2=>'green', 3=>'grey', 4=>'orange', 5=>'violet', 6=>'pink', 7=>'maroon');
print "<B>Color Map</B><BR>";
foreach ($colorMapping as $key=>$color) {
	print "{$labelMapping[$key]} --&gt; <font color='$color'>$color</font><BR>";
}
while ($row = mysql_fetch_assoc($result)) {
	$tokenId = $row['token_string_id'];
	$tokenString = $row['ts_string'];
	$citationId = $row['citation_id'];
	$citationText = $row['citation_text'];
	$tokenStart = $row['token_start'];
	$entropy = $row['tl_entropy'];
	$label = $row['tl_label'];
	$goldStandard = $row['token_gold_standard'];
	
	if ($lastCitation != $citationId) {
		if ($lastCitation != 0) {
			$entities[] = trim($currentEntity);
			$entityLabels[] = $currentEntityLabel;
			$entityIndex++;
			$currentEntityLabel = $label;
			$currentEntity = '';
			
			$citationText = '';
			for ($i = 0; $i < count($entities); $i++) {
				$citationText .= "<font color='{$colorMapping[$entityLabels[$i]]}'>";
				if ($i == $highestEntropyEntityIndex) {
					$citationText .= "<B>{$entities[$i]}</B>";
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
					$targetArray[$criterion]['count'] = 1;
					$targetArray[$criterion]['match'] = $criterion;
					$targetArray[$criterion]['members'] = array();
				} else {
					$targetArray[$criterion]['count']++;
					$targetArray[$criterion]['averageEntropy'] = ($targetArray[$criterion]['averageEntropy'] * ($targetArray[$criterion]['count'] - 1) + $highestEntropy) / $targetArray[$criterion]['count'];
				}
				$targetArray = &$targetArray[$criterion]['members']; 
			}
			if (isset($targetArray['count'])) {
				$targetArray['count']++;
				$targetArray['averageEntropy'] = ($targetArray['averageEntropy'] * ($targetArray['count'] - 1) + $highestEntropy) / $targetArray['count'];
				$targetArray['citations'][] = $citationText;
				if ($targetArray['goldStandard'] != $highestEntropyGoldStandard) {
					print "<P>Error - this citation has gold standard of <B>{$labelMapping[$highestEntropyGoldStandard]}</B>, whereas the first in the sequence had <B>{$labelMapping[$targetArray['goldStandard']]}</B><BR>This citation: $citationText<BR>Original in series: {$targetArray['citations'][0]}</P>";
					
					$errors++;
				}
				$skip = 1;
			} else {
				$targetArray['count'] = 1;
				$targetArray['averageEntropy'] = $highestEntropy;
				$targetArray['citations'] = array($citationText);
				$targetArray['goldStandard'] = $highestEntropyGoldStandard;
			}
			
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
	} 
	$lastCitation = $citationId;
	$lastCitationText = $citationText;
	$tokenIndex++;
}

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
print "<PRE>";
print_r($tokenClassifierMapping);
print "</PRE></BODY></HTML>";

?>
