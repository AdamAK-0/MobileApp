<?php
require_once 'connection.php';

$internship_id = $_POST['internship_id'];
$skill_name = $_POST['skill_name'];

$check = mysqli_query($con, "SELECT skill_id FROM skills WHERE skill_name='$skill_name'");

if(mysqli_num_rows($check) > 0){
    $row = mysqli_fetch_assoc($check);
    $skill_id = $row['skill_id'];
} else {
    mysqli_query($con, "INSERT INTO skills (skill_name) VALUES ('$skill_name')");
    $skill_id = mysqli_insert_id($con);
}

$query = "INSERT INTO internship_skills (internship_id, skill_id)
          VALUES ('$internship_id', '$skill_id')";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
