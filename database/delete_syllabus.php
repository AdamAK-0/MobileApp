<?php
require_once 'connection.php';

$syllabus_id = $_GET['syllabus_id'];

$query = "DELETE FROM syllabi WHERE syllabus_id='$syllabus_id'";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
