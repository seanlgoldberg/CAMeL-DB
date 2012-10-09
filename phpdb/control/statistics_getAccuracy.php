<?php
$result = doQuery("SELECT SUM(if(tl_label = token_gold_standard, 1, 0)) AS numCorrect, COUNT(token_id) AS total FROM tokens JOIN token_labelings ON token_current_labeling=tl_id");
while ($row = mysql_fetch_assoc($result)) {
	$numCorrect = $row['numCorrect'];
	$total = $row['total'];
	$accuracy = number_format(($numCorrect/$total * 100), 2).'%';
	print "Number of correct labelings: $numCorrect out of total: $total for an accuracy of $accuracy<BR>";
}

$correctCitations = 0;
$totalCitations = 0;

$result = doQuery("SELECT SUM(if(tl_label = token_gold_standard, 1, 0)) AS numCorrect, COUNT(token_id) AS total FROM tokens JOIN token_labelings ON token_current_labeling=tl_id GROUP BY token_citation_id");
while ($row = mysql_fetch_assoc($result)) {
	$numCorrect = $row['numCorrect'];
	$total = $row['total'];
	if ($numCorrect == $total) {
		$correctCitations++;
	}
	$totalCitations++;

}
$accuracy = number_format(($correctCitations/$totalCitations * 100), 2).'%';
print "Number of correct citations: $correctCitations out of total: $totalCitations for an accuracy of $accuracy<BR>";
?>