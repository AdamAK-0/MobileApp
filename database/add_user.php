<?php
require_once 'connection.php';

$first   = $_POST['first_name'];
$middle  = $_POST['middle_name'];
$last    = $_POST['last_name'];
$birth   = $_POST['birth_year'];
$email   = $_POST['email'];
$password = password_hash($_POST['password'], PASSWORD_DEFAULT);

// Check if email exists
$check = mysqli_query($con, "SELECT email FROM users WHERE email='$email'");
if (mysqli_num_rows($check) > 0) {
    echo json_encode(["status" => "email_exists"]);
    exit();
}

$query = "INSERT INTO users (first_name, middle_name, last_name, birth_year, email, password)
          VALUES ('$first', '$middle', '$last', '$birth', '$email', '$password')";

if (mysqli_query($con, $query)) {
    $user_id = mysqli_insert_id($con);

    // Fetch the full user row
    $userQuery = "SELECT * FROM users WHERE user_id = $user_id";
    $result = mysqli_query($con, $userQuery);
    $user = mysqli_fetch_assoc($result);

    echo json_encode([
        "status" => "success",
        "user" => $user
    ]);
} else {
    echo json_encode([
        "status" => "fail",
        "error" => mysqli_error($con)
    ]);
}
?>
