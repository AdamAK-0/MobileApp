package com.lau.portin;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class AddInternshipActivity extends AppCompatActivity {

    EditText name, desc, rating, start, end, slots;
    Spinner type;
    Button btnSubmit, btnSelectImage;
    ImageView imgPreview;

    Bitmap selectedBitmap = null;
    boolean editMode = false;
    String imageFileName = "";
    Internship internship;



    public static final String BASE_URL = "http://10.0.2.2/portin/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_internship);

        name = findViewById(R.id.internName);
        desc = findViewById(R.id.internDesc);
        rating = findViewById(R.id.internRating);
        start = findViewById(R.id.internStart);
        end = findViewById(R.id.internEnd);
        slots = findViewById(R.id.internSlots);
        type = findViewById(R.id.spinnerType);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imgPreview = findViewById(R.id.imgPreview);

        setupSpinner();
        btnSelectImage.setOnClickListener(v -> pickImage());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
        editMode = getIntent().getBooleanExtra("edit_mode", false);
        internship =(Internship) getIntent().getSerializableExtra("internship");
        Log.d("editMode", String.valueOf(editMode));


        if (editMode && internship != null) {
            name.setText(internship.getName());
            desc.setText(internship.getDescription());
            //rating.setText(String.valueOf(internship.getRating()));
            //start.setText(internship.getStartDate());
            //end.setText(internship.getEndDate());
            //slots.setText(String.valueOf(internship.getMaxSlots()));
            type.setSelection(getSpinnerIndex(type, internship.getType()));
            Glide.with(this).load(internship.getPhoto()).into(imgPreview);
            btnSubmit.setText("Update Internship");
            imageFileName = internship.getPhoto();
        }

    }
    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0; // default to first item if not found
    }


    void setupSpinner() {
        String[] items = {"remote", "in-person", "hybrid"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                items);
        type.setAdapter(adapter);
    }

    void pickImage() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == 100 && res == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                imgPreview.setImageBitmap(selectedBitmap);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    void validateAndSubmit() {
        if (name.getText().toString().isEmpty()) {
            name.setError("Required");
            return;
        }
        if (desc.getText().toString().isEmpty()) {
            desc.setError("Required");
            return;
        }

        // if bitmap is selected, upload it; otherwise, use placeholder filename
        if (selectedBitmap != null) {
            uploadImage();
        } else {
            if(imageFileName.isEmpty())
                submitData("ic_image.jpg"); // just pass placeholder name
            else {
                if (imageFileName.startsWith(BASE_URL + "uploads/")) {
                    imageFileName = imageFileName.replace(BASE_URL + "uploads/", "");
                }
                submitData(imageFileName);
            }
        }
    }


    void uploadImage() {
        String url = BASE_URL + "upload_image.php";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        String imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> submitData(response),
                error -> Toast.makeText(this, "Image upload failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("image", imageBase64);
                return map;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    void submitData(String imageFileName) {
        String url = BASE_URL + "post_internship.php";
        if(editMode) url = BASE_URL + "edit_internship.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.contains("success"))
                        Toast.makeText(this, "Internship Added", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                },
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> map = new java.util.HashMap<>();

                map.put("company_id", "1"); // temporary
                map.put("name", name.getText().toString());
                map.put("description", desc.getText().toString());
                map.put("rating", rating.getText().toString());
                map.put("start_date", start.getText().toString());
                map.put("end_date", end.getText().toString());
                map.put("type", type.getSelectedItem().toString());
                map.put("max_slots", slots.getText().toString());
                map.put("photo", imageFileName);
                if(editMode && internship != null)
                    map.put("internship_id", String.valueOf(internship.getId()));
                return map;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
}
