package com.lau.portin;

import static com.lau.portin.HomeActivity.BASE_URL;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class TopInternshipsActivity extends AppCompatActivity {
    User currentUser;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_internships);

        // TODO: Initialize currentUser and company if needed, or pass via Intent
        currentUser = (User) getIntent().getSerializableExtra("user");

        RecyclerView recyclerView = findViewById(R.id.recyclerViewTopInternships);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            String jsonStr = getIntent().getStringExtra("top_internships");
            JSONArray topInternshipsArray = new JSONArray(jsonStr);

            ArrayList<Internship> list = new ArrayList<>();
            for (int i = 0; i < topInternshipsArray.length(); i++) {
                JSONObject obj = topInternshipsArray.getJSONObject(i);

                Internship internship = new Internship(
                        obj.getInt("internship_id"),
                        obj.getInt("company_id"),
                        obj.getString("company_name"),
                        obj.getString("name"),
                        obj.optString("description", ""),
                        obj.optString("type", "remote"),
                        BASE_URL + "uploads/" + obj.optString("photo", ""),
                        obj.getDouble("rating"),
                        obj.getString("start_date"),
                        obj.getString("end_date"),
                        obj.getInt("max_slots"),
                        obj.optInt("slots", 0),
                        obj.getString("created_at")

                );

                list.add(internship);
            }

            InternshipAdapter adapter = new InternshipAdapter(list, "User", currentUser, null);
            recyclerView.setAdapter(adapter);

        } catch (Exception e) {
            Log.e("TopInternship", "Error loading top internships", e);
        }
    }

}
