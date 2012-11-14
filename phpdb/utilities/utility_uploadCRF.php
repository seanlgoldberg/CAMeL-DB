<?php

require('../utilities.php');

$dataset = 3;

	$fp = fopen("{$argv[1]}", 'r');
	if (!$fp) {
		return;
	}
	$i = 0;
	while (($data = fgetcsv($fp, 5000, ',')) !== FALSE) {
		if ($data[2] == '' || count($data) < 14) {
			continue;
		}

		$result = doQuery("SELECT ts_id FROM token_strings WHERE ts_string='{$data[2]}'");
		if ($row = mysql_fetch_assoc($result)) {
			$stringId = $row['ts_id'];
		} else {
			doQuery("INSERT INTO token_strings (ts_string) VALUES ('{$data[2]}')");
			$stringId = mysql_insert_id();
		}
		$start = $data[1];
		$start = substr($start, 0, strlen($start) - 1);
		doQuery("INSERT INTO tokens (token_citation_id, token_start, token_string_id, token_gold_standard, token_dataset) VALUES ({$data[0]}, $start, $stringId, {$data[3]}, $dataset)");
		$j = 0;

		$id = mysql_insert_id();
		doQuery("INSERT INTO token_labelings (tl_token_id, tl_label, tl_marginal_0, tl_marginal_1, tl_marginal_2, tl_marginal_3, tl_marginal_4, tl_marginal_5, tl_marginal_6, tl_marginal_7, tl_entropy) VALUES ($id, {$data[4]}, {$data[5]}, {$data[6]} ,{$data[7]}, {$data[8]}, {$data[9]}, {$data[10]}, {$data[11]}, {$data[12]}, {$data[13]})");
			
		$labeling = mysql_insert_id();
		doQuery("UPDATE tokens SET token_current_labeling=$labeling WHERE token_id=$id");
			
		$i++;
	}
print "<P>Upload Complete.  $i records added.";
?>