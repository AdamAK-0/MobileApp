<?php
require_once 'connection.php';

// Allow both GET and POST
$company_id = isset($_POST['company_id']) ? intval($_POST['company_id']) : 0;
if ($company_id <= 0 && isset($_GET['company_id'])) {
    $company_id = intval($_GET['company_id']);
}

$user_id = 0;
if (isset($_POST['user_id'])) {
    $user_id = intval($_POST['user_id']);
} else if (isset($_GET['user_id'])) {
    $user_id = intval($_GET['user_id']);
}

if ($company_id <= 0) {
    echo json_encode([
        "status"  => "error",
        "message" => "Missing company_id"
    ]);
    exit;
}

// Aggregate ratings for this company
$avgQuery = "
    SELECT 
        AVG(rating) AS avg_rating,
        COUNT(*) AS rating_count
    FROM company_ratings
    WHERE company_id = $company_id
";

$result = mysqli_query($con, $avgQuery);
$row = mysqli_fetch_assoc($result);

$avg   = $row && $row['avg_rating'] !== null ? floatval($row['avg_rating']) : 0.0;
$count = $row ? intval($row['rating_count']) : 0;

// Also fetch this user's rating if requested
$userRating = 0;
if ($user_id > 0) {
    $userQuery = "
        SELECT rating 
        FROM company_ratings
        WHERE company_id = $company_id
          AND user_id = $user_id
        LIMIT 1
    ";
    $userResult = mysqli_query($con, $userQuery);
    if ($userRow = mysqli_fetch_assoc($userResult)) {
        $userRating = intval($userRow['rating']);
    }
}

echo json_encode([
    "status"       => "success",
    "average"      => $avg,
    "count"        => $count,
    "user_rating"  => $userRating
]);
?>
