package com.lau.portin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    RecyclerView rv;
    ArrayList<Internship> list = new ArrayList<>();
    InternshipAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    SearchView searchBar;
    String type;
    public static final String BASE_URL = "http://10.0.2.2/portin/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // views
        rv = findViewById(R.id.recyclerInternships);
        searchBar = findViewById(R.id.searchBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        FloatingActionButton fab = findViewById(R.id.btnAddInternship);

        fab.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, AddInternshipActivity.class);
            i.putExtra("type", type);
            i.putExtra("company", (Company) getIntent().getSerializableExtra("company"));
            startActivity(i);
        });

        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getStringExtra("type");
            if (type != null) {
                if (type.equals("Company")) {
                    fab.setVisibility(View.VISIBLE);
                }
                else {
                    fab.setVisibility(View.GONE);
                }
            }
        }
        // recycler setup
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InternshipAdapter(list, type);
        rv.setAdapter(adapter);

        // load data
        if (type.equals("Company")) {
            Company company = (Company) intent.getSerializableExtra("company");
            if (company != null) {
                loadCompanyInternships(company.getCompany_id());
            }
        } else {
            loadInternships();
        }

        // pull to refresh
        if(type.equals("Company")) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                Company company = (Company) intent.getSerializableExtra("company");
                if (company != null) {
                    loadCompanyInternships(company.getCompany_id());
                }
            });
        }
        else {
            swipeRefreshLayout.setOnRefreshListener(this::loadInternships);
        }

        // search filter
        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return true;
            }
        });
    }

    void loadInternships() {
        swipeRefreshLayout.setRefreshing(true);

        String url = BASE_URL + "get_all_internships.php";

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        list.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);

                            list.add(new Internship(
                                    o.getInt("internship_id"),
                                    o.getString("company_id"),
                                    o.getString("name"),
                                    o.getString("description"),
                                    o.getString("type"),
                                    BASE_URL + "uploads/" + o.getString("photo")
                            ));
                        }

                        adapter = new InternshipAdapter(list, type);
                        rv.setAdapter(adapter);

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
                    }

                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });

        Volley.newRequestQueue(this).add(req);
    }
    void loadCompanyInternships(int companyId) {
        swipeRefreshLayout.setRefreshing(true);

        String url = BASE_URL + "get_company_internships.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONArray arr = new JSONArray(response);
                        list.clear();

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject o = arr.getJSONObject(i);

                            list.add(new Internship(
                                    o.getInt("internship_id"),
                                    o.getString("company_id"),
                                    o.getString("name"),
                                    o.getString("description"),
                                    o.getString("type"),
                                    BASE_URL + "uploads/" + o.getString("photo")
                            ));
                        }

                        adapter = new InternshipAdapter(list, type);
                        rv.setAdapter(adapter);

                    } catch (Exception e) {
                        Toast.makeText(this, "JSON error", Toast.LENGTH_SHORT).show();
                    }

                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("company_id", String.valueOf(companyId));
                return map;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

}
