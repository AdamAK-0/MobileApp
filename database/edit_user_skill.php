<?php
require_once 'connection.php';

$user_id = $_POST['user_id'];
$old_skill_id = $_POST['old_skill_id'];
$new_skill_name = $_POST['new_skill_name'];

$query_delete = "DELETE FROM user_skills WHERE user_id='$user_id' AND skill_id='$old_skill_id'";
mysqli_query($con, $query_delete);

$check = mysqli_query($con, "SELECT skill_id FROM skills WHERE skill_name='$new_skill_name'");
if(mysqli_num_rows($check) > 0){
    $row = mysqli_fetch_assoc($check);
    $new_skill_id = $row['skill_id'];
} else {
    mysqli_query($con, "INSERT INTO skills (skill_name) VALUES ('$new_skill_name')");
    $new_skill_id = mysqli_insert_id($con);
}

$query_add = "INSERT INTO user_skills (user_id, skill_id) VALUES ('$user_id', '$new_skill_id')";

if(mysqli_query($con,$query_add)){
    echo "success";
} else {
    echo "fail";
}
?>
