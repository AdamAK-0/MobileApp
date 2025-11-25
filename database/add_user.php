<?php
require_once 'connection.php';

$first = $_POST['first_name'];
$middle = $_POST['middle_name'];
$last = $_POST['last_name'];
$birth = $_POST['birth_year'];
$email = $_POST['email'];
$password = password_hash($_POST['password'], PASSWORD_DEFAULT);

// Insert user
$query = "INSERT INTO users (first_name, middle_name, last_name, birth_year, email, password)
          VALUES ('$first', '$middle', '$last', '$birth', '$email', '$password')";

if (mysqli_query($con, $query)) {
    $user_id = mysqli_insert_id($con); // Get the last inserted ID
    echo json_encode(["status" => "success", "user_id" => $user_id]);
} else {
    echo json_encode(["status" => "fail", "error" => mysqli_error($con)]);
}
?>
