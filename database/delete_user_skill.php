<?php
require_once 'connection.php';

$user_id = $_POST['user_id'];
$skill_id = $_POST['skill_id'];

$query = "DELETE FROM user_skills WHERE user_id='$user_id' AND skill_id='$skill_id'";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
