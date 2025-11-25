<?php
require_once 'connection.php';

$user_id = $_POST['user_id'];
$internship_id = $_POST['internship_id'];

$query = "INSERT INTO user_applications (user_id, internship_id, status)
          VALUES ('$user_id', '$internship_id', 'applied')";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
