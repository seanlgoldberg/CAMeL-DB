<?php
$iterator = new RecursiveDirectoryIterator($argv[1]);
$iterator = new RecursiveIteratorIterator($iterator);
$handle = fopen($argv[2], "w");
$rawHandle = fopen("{$argv[2]}.raw", "w");
$count = 0;
foreach ($iterator as $path) {
		$json = json_decode(file_get_contents($path));
		$citation = $json->{'citation'};
		foreach ($json->recordList as $record) {
			$title = $record->{'title'};
			$author = '';
			$conference = '';
			$isbn = '';
			$publisher = '';
			$series = '';
			$proceedings = '';
			$year = '';
			
			if (isset($record->conference)) {
				$conference = $record->conference;
			}
			if (isset($record->isbn)) {
				$isbn = $record->isbn;
			}
			if (isset($record->publisher)) {
				$publisher = $record->publisher;
			}
			if (isset($record->series)) {
				$series = $record['series'];
			}
			if (isset($record->proceedings)) {
				$proceedings = $record['proceedings'];
			} 
			if (isset($record->year)) {
				$year = $record->year;
			}
			
			fwrite($handle, "\n$title|1\n$author|2\n$conference|3\n$isbn|4\n$publisher|5\n$series|6\n$proceedings|7\n$year\n");
			fwrite($rawHandle, "$citation\n");
			$count++;
		}
	if ($count > 10) {
		break;
	}
}
fclose($handle);
fclose($rawHandle);

?>