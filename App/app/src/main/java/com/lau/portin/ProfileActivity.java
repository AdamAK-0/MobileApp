package com.lau.portin;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ProfileActivity extends AppCompatActivity {

    ImageView imgPhoto;
    TextView tvTitle, tvName, tvEmail, tvCreated, tvExtra1, tvExtra2, tvExtra3;

    String BASE_URL = "http://10.0.2.2/portin/uploads/";

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

        if (type.equals("User")) {
            loadUser();
        } else {
            loadCompany();
        }
    }

    private void loadUser() {
        User u = (User) getIntent().getSerializableExtra("user");

        tvTitle.setText("User Profile");

        tvName.setText(u.getFirst_name() + " " + u.getMiddle_name() + " " + u.getLast_name());
        tvEmail.setText("Email: " + u.getEmail());
        tvCreated.setText("Joined: " + u.getCreated_at());

        tvExtra1.setText("Birth Year: " + u.getBirth_year());
        tvExtra2.setText("Transcript: " + (u.getTranscript().isEmpty() ? "None" : "Available"));
        tvExtra3.setText("");

        loadImage(u.getPhoto());
    }

    private void loadCompany() {
        Company c = (Company) getIntent().getSerializableExtra("company");

        tvTitle.setText("Company Profile");

        tvName.setText(c.getName());
        tvEmail.setText("Email: " + c.getEmail());
        tvCreated.setText("Created: " + c.getCreated_at());

        tvExtra1.setText("Rating: " + c.getRating());
        tvExtra2.setText("Description: " + c.getDescription());
        tvExtra3.setText("");

        loadImage(c.getPhoto());
    }

    private void loadImage(String photo) {
        if (photo == null || photo.isEmpty()) {
            imgPhoto.setImageResource(R.drawable.ic_user);
            return;
        }

        // Clean url if needed
        if (photo.contains("http://10.0.2.2/portin/uploads/")) {
            photo = photo.replace("http://10.0.2.2/portin/uploads/", "");
        }

        Glide.with(this)
                .load(BASE_URL + photo)
                .placeholder(R.drawable.ic_user)
                .into(imgPhoto);
    }
}
