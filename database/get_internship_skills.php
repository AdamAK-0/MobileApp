<?php
require_once 'connection.php';

$internship_id = $_GET['internship_id'];

$query = "SELECT skills.skill_id, skills.skill_name
          FROM internship_skills
          INNER JOIN skills ON internship_skills.skill_id = skills.skill_id
          WHERE internship_skills.internship_id = '$internship_id'";

$result = mysqli_query($con, $query);

$skills = array();
while($row = mysqli_fetch_assoc($result)){
    $skills[] = $row;
}

echo json_encode($skills);
?>
