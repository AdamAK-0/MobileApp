<?php
require_once 'connection.php';

$company_id    = isset($_POST['company_id']) ? intval($_POST['company_id']) : 0;
$user_id       = isset($_POST['user_id']) ? intval($_POST['user_id']) : 0;
$rating        = isset($_POST['rating']) ? intval($_POST['rating']) : 0;
$internship_id = isset($_POST['internship_id']) ? intval($_POST['internship_id']) : 0;

if ($company_id <= 0 || $user_id <= 0 || $rating < 1 || $rating > 5) {
    echo json_encode([
        "status"  => "error",
        "message" => "Invalid input"
    ]);
    exit;
}

// Extra safety: if an internship_id is provided, make sure the user is ACCEPTED there
if ($internship_id > 0) {
    $checkSql = "
        SELECT ua.application_id
        FROM user_applications ua
        WHERE ua.user_id = $user_id
          AND ua.internship_id = $internship_id
          AND ua.status = 'accepted'
        LIMIT 1
    ";
} else {
    // Fallback: user must have at least one accepted application for ANY internship of this company
    $checkSql = "
        SELECT ua.application_id
        FROM user_applications ua
        INNER JOIN internships i ON ua.internship_id = i.internship_id
        WHERE ua.user_id = $user_id
          AND i.company_id = $company_id
          AND ua.status = 'accepted'
        LIMIT 1
    ";
}

$checkRes = mysqli_query($con, $checkSql);
if (!$checkRes || mysqli_num_rows($checkRes) === 0) {
    echo json_encode([
        "status"  => "forbidden",
        "message" => "Only accepted students can rate this company."
    ]);
    exit;
}

// Insert or update this user's rating for the company
$sql = "
    INSERT INTO company_ratings (company_id, user_id, rating)
    VALUES ($company_id, $user_id, $rating)
    ON DUPLICATE KEY UPDATE
        rating = VALUES(rating),
        created_at = CURRENT_TIMESTAMP
";

if (!mysqli_query($con, $sql)) {
    echo json_encode([
        "status"  => "error",
        "message" => "DB error while saving rating"
    ]);
    exit;
}

// Recompute average rating and count for the company
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

// Store the average on the companies table for fast reads
$updateCompany = "UPDATE companies SET rating = $avg WHERE company_id = $company_id";
mysqli_query($con, $updateCompany);

echo json_encode([
    "status"  => "success",
    "average" => $avg,
    "count"   => $count
]);
?>
