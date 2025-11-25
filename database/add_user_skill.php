<?php
require_once 'connection.php';

$user_id = $_POST['user_id'];
$skill_name = $_POST['skill_name'];

/* 1. Insert skill if not exists */
$check = mysqli_query($con, "SELECT skill_id FROM skills WHERE skill_name='$skill_name'");
if(mysqli_num_rows($check) > 0){
    $row = mysqli_fetch_assoc($check);
    $skill_id = $row['skill_id'];
} else {
    mysqli_query($con, "INSERT INTO skills (skill_name) VALUES ('$skill_name')");
    $skill_id = mysqli_insert_id($con);
}

/* 2. Link skill to user */
$query = "INSERT INTO user_skills (user_id, skill_id) VALUES ('$user_id', '$skill_id')";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
