<?php
require_once 'connection.php';
require_once 'email_sender.php';

header('Content-Type: application/json');

if (!isset($_POST['user_id']) || !isset($_POST['internship_id'])) {
    echo json_encode(["status" => "error", "message" => "missing_parameters"]);
    exit();
}

$user_id       = mysqli_real_escape_string($con, $_POST['user_id']);
$internship_id = mysqli_real_escape_string($con, $_POST['internship_id']);

// 1) If this user already applied => treat as success (no error toast)
$checkQuery = "SELECT application_id, status 
               FROM user_applications 
               WHERE user_id = '$user_id' AND internship_id = '$internship_id'
               LIMIT 1";

$checkResult = mysqli_query($con, $checkQuery);

if ($checkResult && mysqli_num_rows($checkResult) > 0) {
    $existing = mysqli_fetch_assoc($checkResult);

    echo json_encode([
        "status"          => "success",
        "already_applied" => true,
        "application_id"  => $existing['application_id'],
        "current_status"  => $existing['status']
    ]);
    exit();
}

// 2) Check current used slots vs max_slots
$capQuery = "
    SELECT 
        max_slots,
        (
            SELECT COUNT(*) 
            FROM user_applications 
            WHERE internship_id = '$internship_id'
              AND status IN ('applied','in_review','accepted')
        ) AS used_slots
    FROM internships
    WHERE internship_id = '$internship_id'
    LIMIT 1
";

$capResult = mysqli_query($con, $capQuery);

if ($capResult && $capRow = mysqli_fetch_assoc($capResult)) {
    $maxSlots  = (int)$capRow['max_slots'];
    $usedSlots = (int)$capRow['used_slots'];

    if ($maxSlots > 0 && $usedSlots >= $maxSlots) {
        // Internship is full -> block application
        echo json_encode([
            "status"       => "full",
            "message"      => "no_slots_available",
            "max_slots"    => $maxSlots,
            "used_slots"   => $usedSlots
        ]);
        exit();
    }
}

// 3) Otherwise insert a new application (default status = applied)
$insertQuery = "INSERT INTO user_applications (user_id, internship_id, status, applied_at)
                VALUES ('$user_id', '$internship_id', 'applied', NOW())";

if (mysqli_query($con, $insertQuery)) {
    $applicationId = mysqli_insert_id($con);

    // Fetch user + internship + company info for the email
    $infoQuery = "
        SELECT 
            u.email        AS user_email,
            u.first_name   AS first_name,
            COALESCE(u.middle_name, '') AS middle_name,
            u.last_name    AS last_name,
            i.name         AS internship_name,
            c.name         AS company_name
        FROM user_applications ua
        JOIN users       u ON ua.user_id = u.user_id
        JOIN internships i ON ua.internship_id = i.internship_id
        JOIN companies   c ON i.company_id = c.company_id
        WHERE ua.application_id = '$applicationId'
        LIMIT 1
    ";

    if ($infoResult = mysqli_query($con, $infoQuery)) {
        if ($infoRow = mysqli_fetch_assoc($infoResult)) {
            $toEmail  = $infoRow['user_email'];
            $fullName = trim(
                $infoRow['first_name'] . ' ' .
                $infoRow['middle_name'] . ' ' .
                $infoRow['last_name']
            );

            $safeInternship = htmlspecialchars($infoRow['internship_name'], ENT_QUOTES, 'UTF-8');
            $safeCompany    = htmlspecialchars($infoRow['company_name'], ENT_QUOTES, 'UTF-8');

            $subject = "Application received: " . $safeInternship;

            $body = "
                <p>Dear " . htmlspecialchars($fullName, ENT_QUOTES, 'UTF-8') . ",</p>
                <p>Thank you for applying to the <strong>{$safeInternship}</strong> internship at 
                <strong>{$safeCompany}</strong>.</p>
                <p>Your application ID is <strong>{$applicationId}</strong> and its current status is 
                <strong>applied</strong>.</p>
                <p>You will receive another email whenever the company changes the status of your application.</p>
                <p>Best of luck!<br>PortIn Internship Portal</p>
            ";

            // Best-effort: if sending fails, we still keep the application.
            @smtp_send_mail($toEmail, $fullName, $subject, $body);
        }
    }

    echo json_encode(["status" => "success"]);
} else {
    echo json_encode([
        "status"  => "error",
        "message" => "insert_failed",
        "details" => mysqli_error($con)
    ]);
}
?>
