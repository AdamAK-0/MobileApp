package com.lau.portin;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.ParseException;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.lau.portin.ai.InternshipSkillExtractor;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddInternshipActivity extends AppCompatActivity {

    EditText name, desc, start, end, slots;
    Spinner type;
    Button btnSubmit, btnSelectImage;
    ImageView imgPreview;

    Bitmap selectedBitmap = null;
    boolean editMode = false;
    String imageFileName = "";
    Internship internship;

    Company c;

    public static final String BASE_URL = "http://10.0.2.2/portin/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_internship);

        name = findViewById(R.id.internName);
        desc = findViewById(R.id.internDesc);
        start = findViewById(R.id.internStart);
        end = findViewById(R.id.internEnd);
        slots = findViewById(R.id.internSlots);
        type = findViewById(R.id.spinnerType);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imgPreview = findViewById(R.id.imgPreview);

        setupSpinner();
        start.setOnClickListener(v -> showDatePicker(start));
        end.setOnClickListener(v -> showDatePicker(end));
        btnSelectImage.setOnClickListener(v -> pickImage());
        btnSubmit.setOnClickListener(v -> validateAndSubmit());
        Intent intent = getIntent();
        editMode = intent.getBooleanExtra("edit_mode", false);
        internship =(Internship) intent.getSerializableExtra("internship");

        Log.d("editMode", String.valueOf(editMode));

        c = (Company) intent.getSerializableExtra("company");

        if (editMode && internship != null) {
            name.setText(internship.getName());
            desc.setText(internship.getDescription());
            //rating.setText(String.valueOf(internship.getRating()));
            start.setText(internship.getStartDate());
            end.setText(internship.getEndDate());
            slots.setText(String.valueOf(internship.getMaxSlots()));
            type.setSelection(getSpinnerIndex(type, internship.getType()));
            Glide.with(this).load(internship.getPhoto()).into(imgPreview);
            btnSubmit.setText("Update Internship");
            imageFileName = internship.getPhoto();
        }

    }
    private void showDatePicker(EditText target) {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    String date = year + "-" + (month+1) + "-" + day;
                    target.setText(date);
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
        );
        dp.show();
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

        // ---- NAME ----
        if (name.getText().toString().trim().isEmpty()) {
            name.setError("Internship name is required");
            return;
        }

        // ---- DESCRIPTION ----
        if (desc.getText().toString().trim().isEmpty()) {
            desc.setError("Description is required");
            return;
        }

        // ---- SLOTS ----
        String slotStr = slots.getText().toString().trim();
        if (slotStr.isEmpty()) {
            slots.setError("Max slots required");
            return;
        }

        int slotNumber;
        try {
            slotNumber = Integer.parseInt(slotStr);
            if (slotNumber <= 0) {
                slots.setError("Slots must be greater than 0");
                return;
            }
        } catch (Exception e) {
            slots.setError("Enter a valid number");
            return;
        }

        // ---- DATES ----
        String startStr = start.getText().toString().trim();
        String endStr   = end.getText().toString().trim();

        if (startStr.isEmpty()) {
            start.setError("Start date required");
            return;
        }

        if (endStr.isEmpty()) {
            end.setError("End date required");
            return;
        }

        // Validate format YYYY-MM-DD
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setLenient(false);

        Date startDate, endDate;
        try {
            startDate = sdf.parse(startStr);
        } catch (Exception e) {
            start.setError("Invalid format (YYYY-MM-DD)");
            return;
        }

        try {
            endDate = sdf.parse(endStr);
        } catch (Exception e) {
            end.setError("Invalid format (YYYY-MM-DD)");
            return;
        }

        // Compare dates
        if (!startDate.before(endDate)) {
            end.setError("End date must be AFTER start date");
            return;
        }

        // ---- IMAGE LOGIC ----
        if (selectedBitmap != null) {
            uploadImage();  // upload then submit automatically
        } else {
            // No bitmap, check filename
            if (imageFileName.isEmpty()) {
                submitData("ic_image.jpg");  // default placeholder
            } else {
                // Clean URL prefix if exists
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
        if (editMode) url = BASE_URL + "edit_internship.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        int internshipId;
                        JSONObject obj = new JSONObject(response);
                        if (obj.optString("status").equals("success")) {
                            if(!editMode)
                                internshipId = obj.getInt("internship_id");
                            else
                                internshipId = internship.getId();

                            Toast.makeText(this, editMode ? "Internship Updated" : "Internship Added", Toast.LENGTH_SHORT).show();
                            if(editMode) {
                                // Before saving new skills after editing
                                deleteAllSkills(internshipId, () -> {
                                    // Now save the new skills
                                    InternshipSkillExtractor extractor = new InternshipSkillExtractor();
                                    extractor.extractSkills(desc.getText().toString(), result -> {
                                        try {
                                            JSONObject json = new JSONObject(result);
                                            JSONArray skills = json.getJSONArray("skills");

                                            for (int i = 0; i < skills.length(); i++) {
                                                String skill = skills.getString(i);
                                                saveSkillToServer(internshipId, skill);
                                            }
                                        } catch (Exception e) {
                                            Log.e("SkillExtract", "Parsing failed: " + result);
                                        }
                                    });
                                });

                            } else {

                            // Extract skills and save them using internshipId
                            InternshipSkillExtractor extractor = new InternshipSkillExtractor();
                            extractor.extractSkills(desc.getText().toString(), result -> {
                                try {
                                    JSONObject json = new JSONObject(result);
                                    JSONArray skills = json.getJSONArray("skills");

                                    for (int i = 0; i < skills.length(); i++) {
                                        String skill = skills.getString(i);
                                        saveSkillToServer(internshipId, skill); // send ID with each skill
                                    }

                                } catch (Exception e) {
                                    Log.e("SkillExtract", "Parsing failed: " + result, e);
                                }

                            });}

                            finish();

                        } else {
                            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Parsing Error", Toast.LENGTH_SHORT).show();
                        Log.e("VOLLEY", "Parsing error: " + e.getMessage());
                    }
                },
                error -> Toast.makeText(this, "Network Error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("company_id", String.valueOf(c.getCompany_id()));
                map.put("name", name.getText().toString());
                map.put("description", desc.getText().toString());
                map.put("rating", "0");
                map.put("start_date", start.getText().toString());
                map.put("end_date", end.getText().toString());
                map.put("type", type.getSelectedItem().toString());
                map.put("max_slots", slots.getText().toString());
                map.put("photo", imageFileName);

                if (editMode && internship != null) {
                    map.put("internship_id", String.valueOf(internship.getId()));
                }
                return map;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }
    private void saveSkillToServer(int internshipId, String skillName) {
        String url = BASE_URL + "add_internship_skill.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.trim().equals("success")) {
                        Log.d("VOLLEY", "Skill saved successfully");
                    } else {
                        Log.e("VOLLEY", "Failed: " + response);
                    }
                },
                error -> Log.e("VOLLEY", "Volley error: " + error.getMessage())
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("internship_id", String.valueOf(internshipId));
                params.put("skill_name", skillName); // EXACT matching your PHP
                return params;
            }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }
    private void deleteAllSkills(int internshipId, Runnable onComplete) {
        String url = BASE_URL + "delete_internship_skills.php"; // your PHP script

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.contains("success")) {
                        Log.d("DeleteSkills", "All skills deleted for internship " + internshipId);
                        if (onComplete != null) {
                            onComplete.run(); // Continue with adding new skills
                        }
                    } else {
                        Toast.makeText(this, "Failed to delete previous skills", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error while deleting skills", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected java.util.Map<String, String> getParams() {
                java.util.Map<String, String> map = new java.util.HashMap<>();
                map.put("internship_id", String.valueOf(internshipId));
                return map;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }


}
