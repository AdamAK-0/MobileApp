<?php
require_once 'connection.php';

if (!isset($_POST['user_id']) || !isset($_POST['internship_id'])) {
    echo json_encode(["status" => "error", "message" => "missing_parameters"]);
    exit();
}

$user_id = mysqli_real_escape_string($con, $_POST['user_id']);
$internship_id = mysqli_real_escape_string($con, $_POST['internship_id']);

// Check if this user already applied to this internship
$checkQuery = "SELECT application_id, status 
               FROM user_applications 
               WHERE user_id = '$user_id' AND internship_id = '$internship_id'
               LIMIT 1";

$checkResult = mysqli_query($con, $checkQuery);

if ($checkResult && mysqli_num_rows($checkResult) > 0) {
    $row = mysqli_fetch_assoc($checkResult);
    echo json_encode([
        "status" => "already_applied",
        "application_id" => $row['application_id'],
        "current_status" => $row['status']
    ]);
    exit();
}

// Otherwise insert a new application
$insertQuery = "INSERT INTO user_applications (user_id, internship_id, status)
                VALUES ('$user_id', '$internship_id', 'applied')";

if (mysqli_query($con, $insertQuery)) {
    echo json_encode(["status" => "success"]);
} else {
    echo json_encode([
        "status" => "error",
        "message" => "insert_failed",
        "details" => mysqli_error($con)
    ]);
}
?>
