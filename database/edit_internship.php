<?php
require_once 'connection.php';

$internship_id = $_POST['internship_id'];
$name = $_POST['name'];
$description = $_POST['description'];
$photo = $_POST['photo'];
$rating = $_POST['rating'];
$start_date = $_POST['start_date'];
$end_date = $_POST['end_date'];
$type = $_POST['type'];
$max_slots = $_POST['max_slots'];

$query = "UPDATE internships 
          SET name='$name', description='$description', photo='$photo', rating='$rating',
              start_date='$start_date', end_date='$end_date', type='$type', max_slots='$max_slots'
          WHERE internship_id='$internship_id'";

if(mysqli_query($con,$query)){
    echo json_encode([
        "status" => "success",
        "internship_id" => $internship_id
    ]);
} else {
    echo json_encode([
        "status" => "fail"
    ]);
}
?>
