<?php
require_once 'connection.php';

$id = $_GET['user_id'];

$query = "SELECT * FROM users WHERE user_id='$id'";
$result = mysqli_query($con, $query);

if ($row = mysqli_fetch_assoc($result)) {
    echo json_encode($row);
} else {
    echo "not_found";
}
?>
