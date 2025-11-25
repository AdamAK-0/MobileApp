<?php
require_once 'connection.php';

$id = $_POST['user_id'];
$first = $_POST['first_name'];
$middle = $_POST['middle_name'];
$last = $_POST['last_name'];
$birth = $_POST['birth_year'];
$email = $_POST['email'];

$query = "UPDATE users SET 
            first_name='$first',
            middle_name='$middle',
            last_name='$last',
            birth_year='$birth',
            email='$email'
          WHERE user_id='$id'";

echo mysqli_query($con, $query) ? "success" : "fail";
?>
