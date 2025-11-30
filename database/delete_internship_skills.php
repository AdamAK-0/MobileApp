<?php
require_once 'connection.php';

$internship_id = $_POST['internship_id'];

$query = "DELETE FROM internship_skills WHERE internship_id='$internship_id'";

if(mysqli_query($con, $query)){
    echo "success";
} else {
    echo "fail";
}
?>
