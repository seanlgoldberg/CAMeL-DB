<?php

require_once('utilities.php');

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
        $reqVar = $_POST;
} else {
        $reqVar = $_GET;
}

$error_message = 0;
$success_message = 0;

function param($key) {
        global $reqVar;
        return getFromReq($key, $reqVar);
}

function hasParam($key) {
        global $reqVar;
        return isset($reqVar[$key]);
}

function checked($key) {
        global $reqVar;
        return (isset($reqVar[$key]) ? 1 : 0);
}

$type = param('type');
$command = param('command');

$continue = 1;
while ($continue) {
        $continue = 0;
        switch ($type) {
        	case 'citation':
        		switch ($command) {
        			case 'upload':
        				include('control/citation_upload.php');
        				break;
        		}
        		break;
        	case 'crf':
        		switch ($command) {
        			case 'upload':
        				include('control/crf_upload.php');
        				break;
        		}
        		break;
        	case 'evidence':
        		switch ($command) {
        			case 'upload':
        				include('control/evidence_upload.php');
        				break;
        		}
        		break;
        	case 'list':
        		switch ($command) {
        			case 'listHighestEntropy':
        				include('control/list_listHighestEntropy.php');
        				break;
        			case 'groupHighestEntropy':
        				include('control/list_groupHighestEntropy.php');
        				break;
        		}
        		break;
        }
     
}
