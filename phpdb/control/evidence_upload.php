<?php
if ($_FILES["evidenceFile"]["error"] > 0) {
	echo "Error: " . $_FILES["evidenceFile"]["error"] . "<br />";
} else {
	$fp = fopen($_FILES["evidenceFile"]["tmp_name"], 'r');
	$i = 0;
	while (($data = fgetcsv($fp, 1000, ',')) !== FALSE) {

		
		$i++;
	}
}
print "<P>Upload Complete.  $i records added.";
?>