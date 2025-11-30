package com.lau.portin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgPhoto;
    TextView tvTitle, tvName, tvEmail, tvCreated, tvExtra1, tvExtra2, tvExtra3;

    private static final String BASE_URL = HomeActivity.BASE_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgPhoto = findViewById(R.id.imgProfile);
        tvTitle = findViewById(R.id.tvTitle);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvCreated = findViewById(R.id.tvCreated);
        tvExtra1 = findViewById(R.id.tvExtra1);
        tvExtra2 = findViewById(R.id.tvExtra2);
        tvExtra3 = findViewById(R.id.tvExtra3);

        String type = getIntent().getStringExtra("type");

        if ("User".equals(type)) {
            loadUser();
        } else {
            loadCompany();
        }
    }

    private void loadUser() {
        // Prefer full object if it's already passed (e.g., from profile screen)
        User u = (User) getIntent().getSerializableExtra("user");
        if (u != null) {
            bindUserToUi(
                    u.getFirst_name(),
                    u.getMiddle_name(),
                    u.getLast_name(),
                    u.getEmail(),
                    u.getCreated_at(),
                    String.valueOf(u.getBirth_year()),
                    (u.getTranscript() != null && !u.getTranscript().isEmpty()) ? "Available" : "None",
                    u.getPhoto()
            );
            return;
        }

        int userId = getIntent().getIntExtra("user_id", -1);
        if (userId <= 0) {
            tvTitle.setText("User Profile");
            tvName.setText("Unknown user");
            tvEmail.setText("");
            tvCreated.setText("");
            tvExtra1.setText("");
            tvExtra2.setText("");
            tvExtra3.setText("");
            Toast.makeText(this, "No user information provided", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "get_user_profile.php?user_id=" + userId;

        StringRequest req = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    response = response.trim();
                    if ("not_found".equalsIgnoreCase(response)) {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    try {
                        JSONObject o = new JSONObject(response);
                        String first = o.optString("first_name", "");
                        String middle = o.optString("middle_name", "");
                        String last = o.optString("last_name", "");
                        String email = o.optString("email", "");
                        String created = o.optString("created_at", "");
                        String birthYear = o.optString("birth_year", "");
                        String transcriptInfo = o.optString("transcript", "");
                        String transcriptStatus = (transcriptInfo == null || transcriptInfo.isEmpty())
                                ? "None" : "Available";
                        String photo = o.optString("photo", "");

                        bindUserToUi(first, middle, last, email, created, birthYear, transcriptStatus, photo);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to load user profile", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error while loading user profile", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void bindUserToUi(String first, String middle, String last,
                              String email, String created, String birthYear,
                              String transcriptStatus, String photo) {
        tvTitle.setText("User Profile");

        String fullName = (first + " " + (middle == null ? "" : middle) + " " + last)
                .replaceAll("\\s+", " ")
                .trim();
        tvName.setText(fullName);
        tvEmail.setText("Email: " + email);
        tvCreated.setText("Joined: " + created);

        tvExtra1.setText("Birth Year: " + birthYear);
        tvExtra2.setText("Transcript: " + transcriptStatus);
        tvExtra3.setText("");

        loadImage(photo);
    }

    private void loadCompany() {
        // Prefer full object if it was passed
        Company c = (Company) getIntent().getSerializableExtra("company");
        if (c != null) {
            bindCompanyToUi(
                    c.getName(),
                    c.getEmail(),
                    c.getCreated_at(),
                    String.valueOf(c.getRating()),
                    c.getDescription(),
                    0,
                    c.getPhoto()
            );
            return;
        }

        int companyId = getIntent().getIntExtra("company_id", -1);
        if (companyId <= 0) {
            tvTitle.setText("Company Profile");
            tvName.setText("Unknown company");
            tvEmail.setText("");
            tvCreated.setText("");
            tvExtra1.setText("");
            tvExtra2.setText("");
            tvExtra3.setText("");
            Toast.makeText(this, "No company information provided", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = BASE_URL + "get_company.php?company_id=" + companyId;

        StringRequest req = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONObject root = new JSONObject(response);
                        JSONObject cJson = root.getJSONObject("company");
                        JSONArray internships = root.optJSONArray("internships");

                        String name = cJson.optString("name", "");
                        String email = cJson.optString("email", "");
                        String created = cJson.optString("created_at", "");
                        String rating = cJson.optString("rating", "N/A");
                        String description = cJson.optString("description", "");
                        String photo = cJson.optString("photo", "");
                        int internshipsCount = internships != null ? internships.length() : 0;

                        bindCompanyToUi(name, email, created, rating, description, internshipsCount, photo);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Failed to load company profile", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Network error while loading company profile", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(this).add(req);
    }

    private void bindCompanyToUi(String name, String email, String created,
                                 String rating, String description, int internshipsCount,
                                 String photo) {
        tvTitle.setText("Company Profile");
        tvName.setText(name);
        tvEmail.setText("Email: " + email);
        tvCreated.setText("Created: " + created);

        tvExtra1.setText("Rating: " + rating);
        tvExtra2.setText("Description: " + description);
        tvExtra3.setText("Active internships: " + internshipsCount);

        loadImage(photo);
    }

    private void loadImage(String photo) {
        if (photo == null || photo.isEmpty()) {
            imgPhoto.setImageResource(R.drawable.ic_user);
            return;
        }

        String url = photo;

        if (!photo.startsWith("http")) {
            // Normalize relative paths like "uploads/..." or plain file names
            if (photo.startsWith("uploads/")) {
                photo = photo.substring("uploads/".length());
            }
            url = BASE_URL + "uploads/" + photo;
        }

        Glide.with(this)
                .load(url)
                .placeholder(R.drawable.ic_user)
                .into(imgPhoto);
    }
}
