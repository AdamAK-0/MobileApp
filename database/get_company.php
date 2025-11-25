<?php
require_once 'connection.php';

$company_id = $_GET['company_id'];

$query_company = "SELECT * FROM companies WHERE company_id='$company_id'";
$result_company = mysqli_query($con, $query_company);

$company = mysqli_fetch_assoc($result_company);

$query_internships = "SELECT * FROM internships WHERE company_id='$company_id'";
$result_internships = mysqli_query($con, $query_internships);

$internships = array();
while($row = mysqli_fetch_assoc($result_internships)){
    $internships[] = $row;
}

$response = array(
    "company" => $company,
    "internships" => $internships
);

echo json_encode($response);
?>
