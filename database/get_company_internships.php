<?php
require_once 'connection.php';

$company_id = $_POST['company_id'];

$query = "
    SELECT i.*, c.name AS company_name
    FROM internships i
    INNER JOIN companies c ON i.company_id = c.company_id
    WHERE i.company_id = '$company_id'
";
$result = mysqli_query($con, $query);

$internships = [];

while ($row = mysqli_fetch_assoc($result)) {
    $internships[] = [
        "internship_id" => $row["internship_id"],
        "company_id" => $row["company_id"],
        "company_name" => $row["company_name"],
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
