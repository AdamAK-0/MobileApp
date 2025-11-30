<?php
require_once 'connection.php';

// Fetch all internships with their skills, company name, and taken slots
$query = "
    SELECT 
        i.internship_id, 
        i.company_id, 
        c.name AS company_name, 
        i.name, 
        i.description, 
        i.photo, 
        i.rating, 
        i.start_date, 
        i.end_date, 
        i.type, 
        i.max_slots,
        i.created_at,
        COALESCE(active_apps.active_count, 0) AS slots,
        GROUP_CONCAT(s.skill_name) AS skills
    FROM internships i
    INNER JOIN companies c ON i.company_id = c.company_id
    LEFT JOIN internship_skills iskill ON i.internship_id = iskill.internship_id
    LEFT JOIN skills s ON iskill.skill_id = s.skill_id
    LEFT JOIN (
        SELECT 
            internship_id,
            COUNT(*) AS active_count
        FROM user_applications
        WHERE status IN ('applied','in_review','accepted')
        GROUP BY internship_id
    ) AS active_apps ON active_apps.internship_id = i.internship_id
    GROUP BY i.internship_id
    ORDER BY i.created_at DESC
";

$result = mysqli_query($con, $query);

$internships = array();

while($row = mysqli_fetch_assoc($result)){
    // Convert skills string to array
    $row['skills'] = $row['skills'] ? explode(',', $row['skills']) : array();
    $row['slots'] = (int)$row['slots']; // ensure slots is integer
    $internships[] = $row;
}

echo json_encode(array(
    "status" => "success",
    "internships" => $internships
));
?>
