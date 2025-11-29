<?php
$img = $_POST['image'];
$type = $_POST['type']; // user or company
$id = $_POST['id'];

if (!$img) { echo "fail"; exit; }

$data = base64_decode($img);

$filename = $type . "_" . $id . "_" . time() . ".jpg";
$filePath = "uploads/" . $filename;

if (file_put_contents($filePath, $data)) {
    echo $filename;
} else {
    echo "fail";
}
?>
