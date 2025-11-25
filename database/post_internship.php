<?php
require_once 'connection.php';

$company_id = $_POST['company_id'];
$name = $_POST['name'];
$description = $_POST['description'];
$photo = $_POST['photo']; 
$rating = $_POST['rating'];
$start_date = $_POST['start_date'];
$end_date = $_POST['end_date'];
$type = $_POST['type'];
$max_slots = $_POST['max_slots'];

$query = "INSERT INTO internships (company_id, name, description, photo, rating, start_date, end_date, type, max_slots)
          VALUES ('$company_id','$name','$description','$photo','$rating','$start_date','$end_date','$type','$max_slots')";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
