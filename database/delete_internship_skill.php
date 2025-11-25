<?php
require_once 'connection.php';

$internship_id = $_POST['internship_id'];
$skill_id = $_POST['skill_id'];

$query = "DELETE FROM internship_skills 
          WHERE internship_id='$internship_id' AND skill_id='$skill_id'";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
