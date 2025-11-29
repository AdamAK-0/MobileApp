<?php
require_once 'connection.php';

$id = $_POST['user_id'];
$first = $_POST['first_name'];
$middle = $_POST['middle_name'];
$last = $_POST['last_name'];
$birth = $_POST['birth_year'];
$password = password_hash($_POST['password'], PASSWORD_DEFAULT);
$photo = $_POST['photo'];

$query = "UPDATE users SET 
            first_name='$first',
            middle_name='$middle',
            last_name='$last',
            birth_year='$birth',
            photo='$photo',
            password='$password'
          WHERE user_id='$id'";

echo mysqli_query($con, $query) ? "success" : "fail";
?>
