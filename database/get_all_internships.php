<?php
require_once 'connection.php';

// Count how many "active" applications each internship has
// active statuses = applied, in_review, accepted
$query = "
    SELECT 
        i.*,
        c.name AS company_name,
        c.rating AS company_rating,
        COALESCE(active_apps.active_count, 0) AS slots
    FROM internships i
    JOIN companies c 
        ON i.company_id = c.company_id
    LEFT JOIN (
        SELECT 
            internship_id,
            COUNT(*) AS active_count
        FROM user_applications
        WHERE status IN ('applied','in_review','accepted')
        GROUP BY internship_id
    ) AS active_apps
        ON active_apps.internship_id = i.internship_id
";

$result = mysqli_query($con, $query);

$internships = array();

while ($row = mysqli_fetch_assoc($result)) {
    if (isset($row['company_rating'])) {
        $row['rating'] = (float)$row['company_rating'];
    }
    $internships[] = $row;
}


echo json_encode($internships);
?>
