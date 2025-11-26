<?php
require_once 'connection.php';

$query = "SELECT i.*, c.name AS company_name 
          FROM internships i
          JOIN companies c ON i.company_id = c.company_id";

$result = mysqli_query($con, $query);

$internships = array();

while ($row = mysqli_fetch_assoc($result)) {
    $internships[] = $row;
}

echo json_encode($internships);
?>
