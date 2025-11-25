<?php
require_once 'connection.php';

$user_id = $_GET['user_id'];

$query = "SELECT * FROM syllabi WHERE user_id='$user_id'";
$result = mysqli_query($con, $query);

$syllabi = array();

while($row = mysqli_fetch_assoc($result)){
    $syllabi[] = $row;
}

echo json_encode($syllabi);
?>
