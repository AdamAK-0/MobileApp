<?php
// Gmail SMTP configuration for sending notification emails.

define('SMTP_HOST', 'smtp.gmail.com');
define('SMTP_PORT', 587);

// Your Gmail account that will send the emails
define('SMTP_USERNAME', 'husseinzeineddine806@gmail.com');

// IMPORTANT:
//  - Do NOT put your normal Gmail login password here.
//  - Put the NEW 16-character Gmail App Password you generated in
//    Google Account → Security → 2-Step Verification → App passwords.
//  - Example: Google shows "abcd efgh ijkl mnop" → you can paste
//    either with spaces or without: "abcdefghijklemnop".
define('SMTP_PASSWORD', 'zfbu zxyk qcft jovp');

// Sender info that students will see
define('FROM_EMAIL', 'husseinzeineddine806@gmail.com');
define('FROM_NAME',  'PortIn Internship Portal');
?>
