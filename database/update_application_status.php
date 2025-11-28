<?php
require_once 'connection.php';
require_once 'email_sender.php';

$application_id = isset($_POST['application_id']) ? mysqli_real_escape_string($con, $_POST['application_id']) : null;
$status         = isset($_POST['status']) ? mysqli_real_escape_string($con, $_POST['status']) : null; // applied, in_review, accepted, rejected, withdrawn

if (!$application_id || !$status) {
    echo "fail";
    exit();
}

$query = "UPDATE user_applications 
          SET status='$status'
          WHERE application_id='$application_id'";

if (mysqli_query($con, $query)) {

    // Fetch application + user + internship info to notify the student
    $infoQuery = "
        SELECT 
            ua.application_id,
            ua.status,
            u.email      AS user_email,
            u.first_name AS first_name,
            COALESCE(u.middle_name, '') AS middle_name,
            u.last_name  AS last_name,
            i.name       AS internship_name,
            c.name       AS company_name
        FROM user_applications ua
        JOIN users       u ON ua.user_id = u.user_id
        JOIN internships i ON ua.internship_id = i.internship_id
        JOIN companies   c ON i.company_id = c.company_id
        WHERE ua.application_id = '$application_id'
        LIMIT 1
    ";

    if ($infoResult = mysqli_query($con, $infoQuery)) {
        if ($info = mysqli_fetch_assoc($infoResult)) {

            $toEmail  = $info['user_email'];
            $fullName = trim($info['first_name'] . ' ' . $info['middle_name'] . ' ' . $info['last_name']);

            $safeInternship = htmlspecialchars($info['internship_name'], ENT_QUOTES, 'UTF-8');
            $safeCompany    = htmlspecialchars($info['company_name'], ENT_QUOTES, 'UTF-8');
            $prettyStatus   = prettify_status($status);

            $subject = "Application status updated: " . $prettyStatus;

            $body = "
                <p>Dear " . htmlspecialchars($fullName, ENT_QUOTES, 'UTF-8') . ",</p>
                <p>The status of your application for the <strong>{$safeInternship}</strong> internship at 
                <strong>{$safeCompany}</strong> has been updated.</p>
                <p>New status: <strong>" . htmlspecialchars($prettyStatus, ENT_QUOTES, 'UTF-8') . "</strong></p>
                <p>You can open the PortIn app to see more details.</p>
                <p>Best regards,<br>PortIn Internship Portal</p>
            ";

            // Best‑effort notification (do not block status update if email fails)
            @smtp_send_mail($toEmail, $fullName, $subject, $body);
        }
    }

    echo "success";
} else {
    echo "fail";
}

/**
 * Convert raw status code to human‑friendly label.
 */
function prettify_status($code) {
    switch ($code) {
        case 'in_review':
            return 'In review';
        case 'accepted':
            return 'Accepted';
        case 'rejected':
            return 'Rejected';
        case 'withdrawn':
            return 'Withdrawn';
        case 'applied':
        default:
            return 'Applied';
    }
}
?>
