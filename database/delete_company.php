<?php
require_once 'connection.php';

$id = $_POST['company_id'];

$query = "DELETE FROM companies WHERE company_id='$id'";

echo mysqli_query($con, $query) ? "success" : "fail";
?>
