<?php
require_once 'connection.php';

$user_id = $_POST['user_id'];
$course_name = $_POST['course_name'];
$syllabus_text = $_POST['syllabus_text'];

$query = "INSERT INTO syllabi (user_id, course_name, syllabus_text)
          VALUES ('$user_id', '$course_name', '$syllabus_text')";

if(mysqli_query($con,$query)){
    echo "success";
} else {
    echo "fail";
}
?>
