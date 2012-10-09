<?php
	header("Content-disposition: attachment; filename=top_clusters.csv");
	
	$result = doQuery("SELECT cluster_id, cluster_entropy, GROUP_CONCAT(cc_citation_id) AS citations, GROUP_CONCAT(citation_text SEPARATOR '~') AS citation_texts, GROUP_CONCAT(token_start) AS starts, GROUP_CONCAT(token_gold_standard) AS golds FROM citation_clusters JOIN clusters ON cc_cluster_id=cluster_id JOIN citations ON cc_citation_id=citation_id JOIN tokens ON citation_highest_entropy_token=token_id GROUP BY cluster_id ORDER BY cluster_entropy DESC LIMIT 130");
	
	while ($row = mysql_fetch_assoc($result)) {
		$clusterId = $row['cluster_id'];
		$entropy = $row['cluster_entropy'];
		$citations = $row['citations'];
		$starts = $row['starts'];
		$golds = $row['golds'];
		$citationTexts = $row['citation_texts'];
		$citationArray = explode(',', $citations);
		$citationTextArray = explode('~', $citationTexts);
		$startArray = explode(',', $starts);
		$goldArray = explode(',', $golds);
		for ($i = 0; $i < count($citationArray); $i++) {
			$nextCitationId = $citationArray[$i];
			$nextCitationText = $citationTextArray[$i];
			$nextTokenStart = $startArray[$i];
			$nextGoldStandard = $goldArray[$i];
			print "$nextCitationId, ~$nextCitationText~, $nextTokenStart, $nextGoldStandard, $clusterId\n";
		}
	}
	
?>