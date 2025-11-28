<?php
require_once 'email_config.php';

/**
 * Send an HTML email using Gmail SMTP (STARTTLS).
 * Returns true on success, false on failure.
 */
function smtp_send_mail($toEmail, $toName, $subject, $htmlBody) {
    $host      = SMTP_HOST;
    $port      = SMTP_PORT;
    $username  = SMTP_USERNAME;
    $password  = SMTP_PASSWORD;
    $fromEmail = FROM_EMAIL;
    $fromName  = FROM_NAME;

    if (empty($toEmail) || empty($subject) || empty($htmlBody)) {
        return false;
    }

    $context = stream_context_create([
        'ssl' => [
            'verify_peer'       => false,
            'verify_peer_name'  => false,
            'allow_self_signed' => true,
        ]
    ]);

    $socket = @stream_socket_client(
        "tcp://{$host}:{$port}",
        $errno,
        $errstr,
        30,
        STREAM_CLIENT_CONNECT,
        $context
    );

    if (!$socket) {
        error_log("SMTP connect failed: $errstr ($errno)");
        return false;
    }

    $read = function() use ($socket) {
        $data = '';
        while ($str = fgets($socket, 515)) {
            $data .= $str;
            if (strlen($str) < 4) {
                break;
            }
            if (substr($str, 3, 1) === ' ') {
                break;
            }
        }
        return $data;
    };

    $write = function($cmd) use ($socket, $read) {
        fwrite($socket, $cmd . "\r\n");
        return $read();
    };

    // Server greeting
    $read();

    // Say hello
    $write("EHLO localhost");

    // Upgrade to TLS
    $write("STARTTLS");
    if (!stream_socket_enable_crypto($socket, true, STREAM_CRYPTO_METHOD_TLS_CLIENT)) {
        error_log("Failed to start TLS");
        fclose($socket);
        return false;
    }

    // Say hello again over the encrypted channel
    $write("EHLO localhost");

    // Authenticate
    $write("AUTH LOGIN");
    $write(base64_encode($username));
    $write(base64_encode($password));

    // Sender and recipient
    $write("MAIL FROM:<{$fromEmail}>");
    $write("RCPT TO:<{$toEmail}>");

    // Start data
    $write("DATA");

    // Encode subject for UTF‑8 safely without mbstring
    $encodedSubject = '=?UTF-8?B?' . base64_encode($subject) . '?=';

    $headers = [];
    $headers[] = "From: {$fromName} <{$fromEmail}>";
    $headers[] = "To: {$toName} <{$toEmail}>";
    $headers[] = "Subject: {$encodedSubject}";
    $headers[] = "MIME-Version: 1.0";
    $headers[] = "Content-Type: text/html; charset=UTF-8";

    $message = implode("\r\n", $headers)
        . "\r\n\r\n"
        . $htmlBody
        . "\r\n.";

    $write($message);

    // Quit session
    $write("QUIT");
    fclose($socket);

    return true;
}
?>
