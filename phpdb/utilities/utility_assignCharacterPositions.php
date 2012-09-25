<?php 
	require('../utilities.php');
	$result = doQuery("SELECT GROUP_CONCAT(token_id) AS token_ids, GROUP_CONCAT(ts_string SEPARATOR '~') AS tokens, citation_text FROM tokens JOIN token_strings ON token_string_id=ts_id JOIN citations ON token_citation_id=citation_id GROUP BY citation_id");
	while ($row = mysql_fetch_assoc($result)) {
		$tokenIds = $row['token_ids'];
		$tokens = $row['tokens'];
		$text = $row['citation_text'];
		
		$tokenArray = explode('~', $tokens);
		$tokenIdArray = explode(',', $tokenIds);
		$currentPos = 0;
		$i = 0;
		foreach ($tokenArray AS $token) {
			$index = stripos($text, $token, $currentPos);
			doQuery("UPDATE tokens SET token_start=$index WHERE token_id={$tokenIdArray[$i]}");
			$currentPos = $index + 1;
			$i++;
		}
	}
?>