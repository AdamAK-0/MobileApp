<?php
require_once 'connection.php';

$id = $_POST['company_id'];
$name = $_POST['name'];
$email = $_POST['email'];
$description = $_POST['description'];

$query = "UPDATE companies SET 
            name='$name',
            email='$email',
            description='$description'
          WHERE company_id='$id'";

echo mysqli_query($con, $query) ? "success" : "fail";
?>
