<?php
$img = $_POST['image'];

$img_name = "internship_" . time() . ".jpg";
$path = "uploads/" . $img_name;

file_put_contents($path, base64_decode($img));

echo $img_name;
?>
