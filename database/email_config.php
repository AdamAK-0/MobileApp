<?php
// Gmail SMTP configuration for sending notification emails.
// IMPORTANT:
// 1. Replace the placeholders below with your own Gmail address and APP PASSWORD.
// 2. For security, enable 2‑step verification on your Google account
//    and create an "App password" for "Mail". Use that app password here.
// 3. Do NOT commit your real password to public repositories.

define('SMTP_HOST', 'smtp.gmail.com');
define('SMTP_PORT', 587);
define('SMTP_USERNAME', 'your_gmail_address@gmail.com');     // TODO: change this
define('SMTP_PASSWORD', 'your_gmail_app_password_here');     // TODO: change this (App password)
define('FROM_EMAIL', SMTP_USERNAME);                         // Sender address
define('FROM_NAME',  'PortIn Internship Portal');            // Sender name shown to students
?>
