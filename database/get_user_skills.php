<?php
require_once 'connection.php';

$user_id = $_GET['user_id'];

$query = "SELECT skills.skill_name, user_skills.score
          FROM skills 
          INNER JOIN user_skills ON skills.skill_id = user_skills.skill_id
          WHERE user_skills.user_id = '$user_id'";

$result = mysqli_query($con, $query);

$skills = array();

while($row = mysqli_fetch_assoc($result)){
    // return both skill_name and score
    $skills[] = array(
        'skill_name' => $row['skill_name'],
        'score' => floatval($row['score'])
    );
}

echo json_encode(array(
    'status' => 'success',
    'skills' => $skills
));
?>
