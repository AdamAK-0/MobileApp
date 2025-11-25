<?php
require_once 'connection.php';

$history_id = $_POST['history_id'];
$status = $_POST['status'];
$actual_start = $_POST['actual_start'];
$actual_end = $_POST['actual_end'];

$query = "UPDATE user_internship_history 
          SET status='$status', actual_start='$actual_start', actual_end='$actual_end'
          WHERE history_id='$history_id'";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
