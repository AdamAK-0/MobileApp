<?php
require_once 'connection.php';

$company_id = $_POST['company_id'];

$query = "SELECT * FROM internships WHERE company_id = '$company_id'";
$result = mysqli_query($con, $query);

$internships = [];

while ($row = mysqli_fetch_assoc($result)) {
    $internships[] = [
        "internship_id" => $row["internship_id"],
        "company_id" => $row["company_id"],
        "name" => $row["name"],
        "description" => $row["description"],
        "photo" => $row["photo"],
        "rating" => $row["rating"],
        "start_date" => $row["start_date"],
        "end_date" => $row["end_date"],
        "type" => $row["type"],
        "max_slots" => $row["max_slots"],
        "created_at" => $row["created_at"]
    ];
}

echo json_encode($internships);
?>
