<?php
require_once 'connection.php';

$id = $_POST['user_id'];

$query = "DELETE FROM users WHERE user_id='$id'";

echo mysqli_query($con, $query) ? "success" : "fail";
?>
