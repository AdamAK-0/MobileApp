<?php
require_once 'connection.php';

if (!isset($_GET['internship_id'])) {
    http_response_code(400);
    echo json_encode(["error" => "missing_internship_id"]);
    exit();
}

$internship_id = mysqli_real_escape_string($con, $_GET['internship_id']);

// Fetch all applications for a given internship with user profile info
$query = "
SELECT 
    ua.application_id,
    ua.status,
    ua.applied_at,
    ua.cv_path AS cv_url,
    CONCAT(u.first_name, ' ', u.last_name) AS user_name,
    u.email AS user_email,
    u.photo AS user_photo
FROM user_applications AS ua
INNER JOIN users AS u 
    ON u.user_id = ua.user_id
WHERE ua.internship_id = '$internship_id'
ORDER BY ua.applied_at DESC
";

$result = mysqli_query($con, $query);

$applications = array();

if ($result) {
    while ($row = mysqli_fetch_assoc($result)) {
        $applications[] = $row;
    }
} else {
    // In case of SQL error, return a JSON error instead of a blank response
    echo json_encode(["error" => "query_failed", "details" => mysqli_error($con)]);
    exit();
}

header('Content-Type: application/json');
echo json_encode($applications);
?>
