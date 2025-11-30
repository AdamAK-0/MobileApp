<?php
require_once 'connection.php';

$query = "SELECT skill_name FROM skills ORDER BY skill_name ASC";
$result = mysqli_query($con, $query);

$skills = array();

while ($row = mysqli_fetch_assoc($result)) {
    $skills[] = $row['skill_name'];
}

echo json_encode(array(
    "status" => "success",
    "skills" => $skills
));
?>
