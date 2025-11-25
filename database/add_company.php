<?php
require_once 'connection.php';

$name = $_POST['name'];
$email = $_POST['email'];
$password = password_hash($_POST['password'], PASSWORD_DEFAULT);
$description = $_POST['description'];

$query = "INSERT INTO companies (name, email, password, description)
          VALUES ('$name', '$email', '$password', '$description')";

if (mysqli_query($con, $query)) {
    $company_id = mysqli_insert_id($con); // Get the last inserted ID
    echo json_encode(["status" => "success", "user_id" => $company_id]);
} else {
    echo json_encode(["status" => "fail", "error" => mysqli_error($con)]);
}
?>
