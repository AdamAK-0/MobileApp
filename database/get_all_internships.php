<?php
require_once 'connection.php';

$query = "SELECT * FROM internships";
$result = mysqli_query($con, $query);

$internships = array();

while($row = mysqli_fetch_assoc($result)){
    $internships[] = $row;
}

echo json_encode($internships);
?>
