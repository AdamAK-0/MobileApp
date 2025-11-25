<?php
require_once 'connection.php';

$id = $_GET['internship_id'];

$query = "DELETE FROM internships WHERE internship_id='$id'";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
