<?php
require_once 'connection.php';

$user_id = $_GET['user_id'];

$query = 
"SELECT 
    user_applications.application_id,
    user_applications.status,
    user_applications.applied_at,
    internships.name AS internship_name,
    companies.name AS company_name,
    internships.photo AS internship_photo
FROM user_applications
INNER JOIN internships 
    ON internships.internship_id = user_applications.internship_id
INNER JOIN companies
    ON companies.company_id = internships.company_id
WHERE user_applications.user_id = '$user_id'
ORDER BY user_applications.applied_at DESC";

$result = mysqli_query($con, $query);

$applications = array();

while($row = mysqli_fetch_assoc($result)){
    $applications[] = $row;
}

echo json_encode($applications);
?>
