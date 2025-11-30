<?php
require_once 'connection.php';

// Fetch all internships with their skills
$query = "
    SELECT i.internship_id, i.name, i.description, i.photo, i.rating, i.start_date, i.end_date, i.type, i.max_slots,
           GROUP_CONCAT(s.skill_name) AS skills
    FROM internships i
    LEFT JOIN internship_skills iskill ON i.internship_id = iskill.internship_id
    LEFT JOIN skills s ON iskill.skill_id = s.skill_id
    GROUP BY i.internship_id
    ORDER BY i.created_at DESC
";

$result = mysqli_query($con, $query);

$internships = array();

while($row = mysqli_fetch_assoc($result)){
    // Convert skills string to array
    $row['skills'] = $row['skills'] ? explode(',', $row['skills']) : array();
    $internships[] = $row;
}

echo json_encode(array(
    "status" => "success",
    "internships" => $internships
));
?>
