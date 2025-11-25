<?php
require_once 'connection.php';

$user_id = $_GET['user_id'];

$query = "SELECT 
            user_internship_history.*, 
            internships.name AS internship_name,
            companies.name AS company_name
          FROM user_internship_history
          INNER JOIN internships ON internships.internship_id = user_internship_history.internship_id
          INNER JOIN companies ON companies.company_id = internships.company_id
          WHERE user_internship_history.user_id='$user_id'";

$result = mysqli_query($con, $query);

$history = array();

while($row = mysqli_fetch_assoc($result)){
    $history[] = $row;
}

echo json_encode($history);
?>
