<?php
require_once 'connection.php';

$company_id = $_POST['company_id'];

// Include current used slots for each internship of this company
$query = "
    SELECT 
        i.*,
        c.name AS company_name,
        COALESCE(active_apps.active_count, 0) AS slots
    FROM internships i
    INNER JOIN companies c 
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
    WHERE i.company_id = '$company_id'
";

$result = mysqli_query($con, $query);

$internships = [];

while ($row = mysqli_fetch_assoc($result)) {
    $internships[] = [
        "internship_id" => (int)$row["internship_id"],
        "company_id"    => (int)$row["company_id"],
        "company_name"  => $row["company_name"],
        "name"          => $row["name"],
        "description"   => $row["description"],
        "photo"         => $row["photo"],
        "rating"        => (float)$row["rating"],
        "start_date"    => $row["start_date"],
        "end_date"      => $row["end_date"],
        "type"          => $row["type"],
        "max_slots"     => (int)$row["max_slots"],
        "slots"         => (int)$row["slots"],
        "created_at"    => $row["created_at"]
    ];
}

echo json_encode($internships);
?>
