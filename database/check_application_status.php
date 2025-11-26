<?php
require_once 'connection.php';

if (!isset($_POST['user_id']) || !isset($_POST['internship_id'])) {
    echo json_encode(["error" => "missing_parameters"]);
    exit();
}

$user_id = mysqli_real_escape_string($con, $_POST['user_id']);
$internship_id = mysqli_real_escape_string($con, $_POST['internship_id']);

$query = "SELECT application_id, status 
          FROM user_applications 
          WHERE user_id = '$user_id' AND internship_id = '$internship_id'
          LIMIT 1";

$result = mysqli_query($con, $query);

header('Content-Type: application/json');

if ($result && mysqli_num_rows($result) > 0) {
    $row = mysqli_fetch_assoc($result);
    echo json_encode([
        "applied" => true,
        "status" => $row['status'],
        "application_id" => $row['application_id']
    ]);
} else {
    echo json_encode([
        "applied" => false
    ]);
}
?>
