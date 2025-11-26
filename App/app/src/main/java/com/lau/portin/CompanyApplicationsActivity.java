package com.lau.portin;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CompanyApplicationsActivity extends AppCompatActivity {

    RecyclerView rv;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView tvTitle;
    ArrayList<Application> list = new ArrayList<>();
    ApplicationAdapter adapter;
    Internship internship;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_applications);

        rv = findViewById(R.id.recyclerApplications);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshApplications);
        tvTitle = findViewById(R.id.tvApplicationsTitle);

        internship = (Internship) getIntent().getSerializableExtra("internship");
        if (internship != null) {
            tvTitle.setText("Applications - " + internship.getName());
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter(this, list);
        rv.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadApplications);

        loadApplications();
    }

    private void loadApplications() {
        swipeRefreshLayout.setRefreshing(true);

        if (internship == null) {
            Toast.makeText(this, "No internship found", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        String url = HomeActivity.BASE_URL + "get_internship_applications.php?internship_id=" + internship.getId();

        StringRequest req = new StringRequest(
                Request.Method.GET,
                url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        list.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            list.add(new Application(
                                    o.getInt("application_id"),
                                    o.getString("user_name"),
                                    o.getString("user_email"),
                                    o.getString("status"),
                                    o.getString("applied_at"),
                                    o.optString("user_photo", "")
                            ));
                        }

                        adapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        Toast.makeText(this, "Parse error", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        Volley.newRequestQueue(this).add(req);
    }
}
