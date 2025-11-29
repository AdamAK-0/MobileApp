<?php
require_once 'connection.php';

$id = $_POST['company_id'];
$name = $_POST['name'];
$password = password_hash($_POST['password'], PASSWORD_DEFAULT);
$description = $_POST['description'];
$photo = $_POST['photo'];


$query = "UPDATE companies SET 
            name='$name',
            password='$password',
            description='$description',
            photo='$photo'
          WHERE company_id='$id'";

echo mysqli_query($con, $query) ? "success" : "fail";
?>
