<?php
require_once('utilities.php');
?>

Upload crfs:
<form action='controller.php' method='post' enctype='multipart/form-data'><input type='hidden' name='type' value='crf' /><input type='hidden' name='command' value='upload' /> <input type='file' name='crfFile' class='footerButton' onchange="submit();" /></form>

Upload citations:
<form action='controller.php' method='post' enctype='multipart/form-data'><input type='hidden' name='type' value='citation' /><input type='hidden' name='command' value='upload' /> <input type='file' name='citationFile' class='footerButton' onchange="submit();" /></form>

Upload evidence:
<form action='controller.php' method='post' enctype='multipart/form-data'><input type='hidden' name='type' value='evidence' /><input type='hidden' name='command' value='upload' /> <input type='file' name='evidenceFile' class='footerButton' onchange="submit();" /></form>

<P><form action='controller.php' method='post'><input type='hidden' name='type' value='list' /><input type='hidden' name='command' value='listHighestEntropy' /><input type='submit' value='List Highest Entropy Tokens' /></form>

<P><form action='controller.php' method='post'><input type='hidden' name='type' value='list' /><input type='hidden' name='command' value='groupHighestEntropy' /><input type='submit' value='List Grouped Highest Entropy Tokens' /></form>
