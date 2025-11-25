<?php
require_once 'connection.php';

$user_id = $_GET['user_id'];

$query = "SELECT skill_name 
          FROM skills 
          INNER JOIN user_skills ON skills.skill_id = user_skills.skill_id
          WHERE user_skills.user_id = '$user_id'";

$result = mysqli_query($con, $query);

$skills = array();

while($row = mysqli_fetch_assoc($result)){
    $skills[] = $row;
}

echo json_encode($skills);
?>
