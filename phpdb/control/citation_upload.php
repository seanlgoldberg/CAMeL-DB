<?php
if ($_FILES["citationFile"]["error"] > 0) {
	echo "Error: " . $_FILES["citationFile"]["error"] . "<br />";
} else {
	$fp = fopen($_FILES["citationFile"]["tmp_name"], 'r');
	$i = 0;
	while (($data = fgets($fp)) !== FALSE) {
		$index = strpos($data, ',');
		$id = substr($data, 0, $index);
		$text = addslashes(trim(substr($data, $index + 1)));
		doQuery("INSERT INTO citations (citation_id, citation_text) VALUES ($id, '$text')");
		$i++;
	}
}
print "<P>Upload Complete.  $i records added.";
?>