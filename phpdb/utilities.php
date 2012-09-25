<?php 

require_once('config.php');

// Once-per-load code section
getDBConn();
session_start(); 

// DB functions
function getDBConn() {
        $db_link = mysql_connect(DB_HOST, DB_USER, DB_PASSWORD);
        if (!$db_link) {
                emailAdmin("Could not connect", "Could not connect to database (utilities.getDBConn):".mysql_error());
                die('Could not connect: ' . mysql_error()); 
        }
        mysql_select_db(DB_DATABASE); 
        return $db_link;
}

function doQuery($query) {

        $escaped = mysql_real_escape_string($query);
        print mysql_error();    
        if (!($result = mysql_query($query))) {
                print "<P>details: ".mysql_error()." <BR>QUERY: $query</FONT>";
                
                die();
        }

        return $result;
}

// End DB functions

// Request processing
function getGet($key) {
        return getFromReq($key, $_GET);
}

function getPost($key) {
        return getFromReq($key, $_POST);
}

function getFromReq($key, $req) {
        return mysql_real_escape_string(stripslashes($req[$key]));
}
// End request processing


?>