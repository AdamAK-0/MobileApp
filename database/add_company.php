<?php
require_once 'connection.php';

header("Content-Type: application/json");

$name = $_POST['name'];
$email = $_POST['email'];
$password = password_hash($_POST['password'], PASSWORD_DEFAULT);
$description = $_POST['description'];

// Check if email exists
$check = mysqli_query($con, "SELECT email FROM companies WHERE email='$email'");
if (mysqli_num_rows($check) > 0) {
    echo json_encode(["status" => "email_exists"]);
    exit();
}

$query = "INSERT INTO companies (name, email, password, description)
          VALUES ('$name', '$email', '$password', '$description')";

if (mysqli_query($con, $query)) {

    $company_id = mysqli_insert_id($con);

    // fetch the full inserted object
    $result = mysqli_query($con, "SELECT * FROM companies WHERE company_id = $company_id");
    $company = mysqli_fetch_assoc($result);

    echo json_encode([
        "status" => "success",
        "company" => $company
    ]);

} else {
    echo json_encode([
        "status" => "fail",
        "error" => mysqli_error($con)
    ]);
}
?>
