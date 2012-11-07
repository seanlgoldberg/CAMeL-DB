<?php
$iterator = new RecursiveDirectoryIterator($argv[1]);
$iterator = new RecursiveIteratorIterator($iterator);
$handle = fopen($argv[2], "w");
$rawHandle = fopen("{$argv[2]}.raw", "w");
$count = 0;
foreach ($iterator as $path) {
		//print $path."\n";
		$json = json_decode(file_get_contents($path));
		$citation = '';
		$fakeCitation = '';
		$labels = array('title'=>0, 'source'=>1, 'author'=>2, 'issue'=>3, 'volume'=>4, 'pages'=>5, 'year'=>6);
		$parts = array('title'=>'', 'source'=>'', 'author'=>'', 'issue'=>'', 'volume'=>'', 'pages'=>'', 'year'=>'');
		$indexes = array('title'=>0, 'source'=>0, 'author'=>'', 'issue'=>0, 'volume'=>0, 'year'=>0);
		$skipCitation = 0;		
		$buffer = '';
	
		foreach ($json->recordList as $record) {
			read($record);			
		}
				
		if (strlen($citation) > 2) {
			foreach ($parts as $key=>$value) {
				if (strlen($value) <= 0) {
				//	print "Skipping because of $key\n";
					$skipCitation = 1;
					break;
				}
			}
			if ($skipCitation) {
				continue;
			}
			/*	if ($key === 'author') {
					continue;
				}	
				if (strlen($value) == 0) {
					$skipCitation = 1;
					break;
				}
				$index = stripos($citation, $value);
				if ($index === FALSE) {
					print "Could not find $key=>$value";
					$skipCitation = 1;	
					break;
				} else {
					$indexes[$key] = $index;
				}
				
			}
			if ($skipCitation) {
				continue;	
			}
			/*asort($indexes);
			$currentIndex = 0;
			fwrite($handle, "\n$count\n");
			foreach ($indexes as $key=>$index) {
				$buffer = $citation;
				$value = $parts[$key];	
				if ($index > $currentIndex + 1) {
					$other = substr($citation, $currentIndex, $index - $currentIndex);
					$currentIndex = $index + strlen($value);
					fwrite($handle, "other|$value\n");
				}
				fwrite($handle, "$key|$value\n");		
			}*/	
			
						
			fwrite($handle, "$buffer\n");	
			//fwrite($handle, "\n{$parts['title']}|0\n{$parts['author']}|1\n{$parts['source']}|2\n{$parts['year']}|3\n{$parts['issue']}|4\n{$parts['volume']}|5\n{$parts['pages']}|6\n");
			fwrite($rawHandle, "$fakeCitation\n");
		}
		if ($count == 300000) {
			break;
		}
}
fclose($handle);
fclose($rawHandle);

print "Number of interesting citations: $count";

function read( $array ) {
	global $citation, $labels, $buffer, $handle, $fakeCitation, $parts, $latestSurname, $nextOutput, $indexInCitation;
	foreach( (array) $array as $key => $value ) {
		if( is_array( $value ) ) {
			read( $value );
		} else {
			if ($key === 'citation') {
				$citation = $value;
			} elseif ($key === 'title') {
				if ($parts['title'] === '') {
					$parts['title'] = $value;
					$fakeCitation .= ' '.$value;
					$buffer .= $value.'|'.$labels[$key]."\n"; 
				}
			} elseif ($key === 'x-surname') {
				$latestSurname = $value;
			} elseif ($key === 'x-given-names') {
				if (strlen($latestSurname) > 1 && strpos($parts['author'], $latestSurname.' '.$value) === FALSE) {
					$parts["author"] .= $latestSurname.' '.$value.' ';
					$fakeCitation .= ' '.$latestSurname.' '.$value;
					
					$buffer .= $latestSurname.' '.$value.'|'.$labels['author']."\n"; 
				}
			} elseif ($key === 'source') {
				if ($parts['source'] === '') {
				$parts['source'] = $value;
				$fakeCitation .= ' '.$value;
				$buffer .= $value.'|'.$labels[$key]."\n"; 
				}
			} elseif ($key === 'issue') {
				if ($parts['issue'] === '') {
				$parts['issue'] = $value;
				$fakeCitation .= ' '.$value;
				$buffer .= $value.'|'.$labels[$key]."\n"; 
				}
			} elseif ($key === 'volume') {
				if ($parts['volume'] === '') {
				$parts['volume'] = $value;
				$fakeCitation .= ' '.$value;
				$buffer .= $value.'|'.$labels[$key]."\n"; 
				}
			} elseif ($key === 'pages') {
				if ($parts['pages'] === '') {
				$parts['pages'] = $value;
				$fakeCitation .= ' '.$value;
				$buffer .= $value.'|'.$labels[$key]."\n";
				} 
			} elseif ($key === 'year') {
				if ($parts['year'] === '') {
				$parts['year'] = $value;
				$fakeCitation .= ' '.$value;
				$buffer .= $value.'|'.$labels[$key]."\n"; 
				}
			}
		}
	}
}


?>
