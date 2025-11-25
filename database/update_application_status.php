<?php
require_once 'connection.php';

$application_id = $_POST['application_id'];
$status = $_POST['status']; // applied, in_review, accepted, rejected, withdrawn

$query = "UPDATE user_applications 
          SET status='$status'
          WHERE application_id='$application_id'";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
