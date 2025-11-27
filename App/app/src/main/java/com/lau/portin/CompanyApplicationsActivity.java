package com.lau.portin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
    Toolbar tvTitle;
    ArrayList<Application> list = new ArrayList<>();
    ApplicationAdapter adapter;
    Internship internship;
    SearchView searchView;
    AutoCompleteTextView filterStatus;
    String selectedStatus = "";
    ArrayList<Application> fullList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_applications);

        rv = findViewById(R.id.recyclerApplications);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshApplications);
        tvTitle = findViewById(R.id.toolbarApplications);
        searchView = findViewById(R.id.searchApplications);
        searchView.setOnClickListener(v -> searchView.setIconified(false));
        searchView.setQueryHint("Search user");
        filterStatus = findViewById(R.id.filterStatusApplications);

// Status list
        String[] statuses = {"all", "applied", "in_review", "accepted", "rejected"};

        ArrayAdapter<String> statusAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, statuses);

        filterStatus.setAdapter(statusAdapter);
        //filterStatus.setText("All", false);


        internship = (Internship) getIntent().getSerializableExtra("internship");
        if (internship != null) {
            tvTitle.setTitle("Applications - " + internship.getName());
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ApplicationAdapter(this, list);
        rv.setAdapter(adapter);

        swipeRefreshLayout.setOnRefreshListener(this::loadApplications);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                applyFilters();
                return true;
            }
        });
        filterStatus.setOnClickListener(v -> filterStatus.showDropDown());
        filterStatus.setOnItemClickListener((parent, view, position, id) ->
        {selectedStatus = statuses[position];
        if (selectedStatus.equals("all")) {
            selectedStatus = "";
        }
        applyFilters();});

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
                        fullList.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);
                            fullList.add(new Application(
                                    o.getInt("application_id"),
                                    o.getString("user_name"),
                                    o.getString("user_email"),
                                    o.getString("status"),
                                    o.getString("applied_at"),
                                    o.optString("user_photo", "")
                            ));
                        }
                        applyFilters();
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
    private void applyFilters() {
        String search = searchView.getQuery().toString().toLowerCase();

        list.clear();

        for (Application a : fullList) {

            boolean matchesSearch = a.getUserName().toLowerCase().contains(search) || a.getUserEmail().toLowerCase().contains(search);

            boolean matchesStatus =
                    selectedStatus.isEmpty()||
                            a.getStatus().equals(selectedStatus);

            if (matchesSearch && matchesStatus) {
                list.add(a);
            }
        }

        adapter.notifyDataSetChanged();
    }

}
