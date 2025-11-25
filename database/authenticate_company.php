<?php
require_once 'connection.php';

$email = $_POST['email'];
$password = $_POST['password'];

$query = "SELECT * FROM companies WHERE email='$email'";
$result = mysqli_query($con, $query);

if ($row = mysqli_fetch_assoc($result)) {
    if (password_verify($password, $row['password'])) {
        echo json_encode(["status" => "success", "company_id" => $row['company_id']]);
    } else {
        echo "wrong_password";
    }
} else {
    echo "not_found";
}
?>
