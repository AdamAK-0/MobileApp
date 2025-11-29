package com.lau.portin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.Request;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends BaseActivity {

    RecyclerView rv;
    ArrayList<Internship> list = new ArrayList<>();
    InternshipAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    SearchView searchBar;
    AutoCompleteTextView filterStatus, filterMode;
    String selectedStatus = "";
    String selectedMode = "";
    //static BottomNavigationView bottomNav;


    public static final String BASE_URL = "http://10.0.2.2/portin/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set BaseActivity fields first
//        Intent intent = getIntent();
//        if (intent != null) {
//            type = intent.getStringExtra("type");
//            if (type != null) {
//                if (type.equals("Company")) {
//                    company = (Company) intent.getSerializableExtra("company");
//                }
//                if (type.equals("User")) {
//                    currentUser = (User) intent.getSerializableExtra("user");
//                }
//            }
//        }
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_home);
        setActivityLayout(R.layout.activity_home);

        // views
        rv = findViewById(R.id.recyclerInternships);
        searchBar = findViewById(R.id.searchBar);
        searchBar.setOnClickListener(v -> searchBar.setIconified(false));
        filterStatus = findViewById(R.id.filterStatus);
        filterMode = findViewById(R.id.filterMode);
        //bottomNav = findViewById(R.id.bottomNav);

// Status options
        String[] statuses = {"all", "apply", "applied", "accepted", "rejected", "in_review"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statuses);
        filterStatus.setAdapter(statusAdapter);

// Mode options
        String[] modes = {"all", "in-person", "remote", "hybrid"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, modes);
        filterMode.setAdapter(modeAdapter);

// On select
        filterStatus.setOnClickListener(v -> filterStatus.showDropDown());
        filterMode.setOnClickListener(v -> filterMode.showDropDown());

        filterStatus.setOnItemClickListener((parent, view, position, id) -> {
            selectedStatus = statuses[position];
            if (selectedStatus.equals("all")) {
                selectedStatus = "";
            }
            applyFilters();
        });

        filterMode.setOnItemClickListener((parent, view, position, id) -> {
            selectedMode = modes[position];
            if (selectedMode.equals("all")) {
                selectedMode = "";
            }
            applyFilters();
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefresh);
        FloatingActionButton fab = findViewById(R.id.btnAddInternship);

        fab.setOnClickListener(v -> {
            Intent i = new Intent(HomeActivity.this, AddInternshipActivity.class);
            i.putExtra("type", type);
            i.putExtra("company", company);
            startActivity(i);
        });

//        if (intent != null) {
//            type = intent.getStringExtra("type");
//            if (type != null) {
//                if (type.equals("Company")) {
//                    fab.setVisibility(View.VISIBLE);
//                    company = (Company) intent.getSerializableExtra("company");
//                }
//                else {
//                    fab.setVisibility(View.GONE);
//                }
//
//                if (type.equals("User")) {
//                    currentUser = (User) intent.getSerializableExtra("user");
//                }
//            }
//        }
        if (type.equals("Company")) {
            fab.setVisibility(View.VISIBLE);
        }
        else {
            fab.setVisibility(View.GONE);
        }
        // recycler setup
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InternshipAdapter(list, type, currentUser, company);
        rv.setAdapter(adapter);

        // load data
        if (type.equals("Company")) {
            //Company company = (Company) intent.getSerializableExtra("company");
            if (company != null) {
                loadCompanyInternships(company.getCompany_id());
            }
        } else {
            loadInternships();
        }

        // pull to refresh
        if(type.equals("Company")) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                //Company company = (Company) intent.getSerializableExtra("company");
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
        if(type.equals("Company")) {
            filterStatus.setVisibility(View.GONE);
        }
        // Hide Skills for Company type
        if (type.equals("Company")) {
            bottomNav.getMenu().findItem(R.id.nav_skills).setVisible(false);
        }

        /*bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                // Already here
                return true;
            }
            if (id == R.id.nav_skills) {
                Intent i = new Intent(HomeActivity.this, SkillActivity.class);
                i.putExtra("user", currentUser);
                startActivity(i);
                return true;
            }

            return false;
        });*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
        if (type.equals("Company")) {
            //Company company = (Company) getIntent().getSerializableExtra("company");
            if (company != null) {
                loadCompanyInternships(company.getCompany_id());
            }
        }
        else {
            loadInternships();
        }
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
                                    o.getInt("company_id"),
                                    o.getString("company_name"),
                                    o.getString("name"),
                                    o.getString("description"),
                                    o.getString("type"),
                                    BASE_URL + "uploads/" + o.getString("photo"),
                                    o.getInt("rating"),
                                    o.getString("start_date"),
                                    o.getString("end_date"),
                                    o.getInt("max_slots"),
                                    o.optInt("slots", 0),
                                    o.getString("created_at")
                            ));
                        }

                        adapter = new InternshipAdapter(list, type, currentUser, company);
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
                                    o.getInt("company_id"),
                                    o.getString("company_name"),
                                    o.getString("name"),
                                    o.getString("description"),
                                    o.getString("type"),
                                    BASE_URL + "uploads/" + o.getString("photo"),
                                    o.getInt("rating"),
                                    o.getString("start_date"),
                                    o.getString("end_date"),
                                    o.getInt("max_slots"),
                                    o.optInt("slots", 0),
                                    o.getString("created_at")
                            ));
                        }

                        adapter = new InternshipAdapter(list, type, currentUser, company);
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
    void applyFilters() {
        ArrayList<Internship> filtered = new ArrayList<>();

        for (Internship i : list) {

            boolean matchSearch = i.getName().toLowerCase().contains(searchBar.getQuery().toString().toLowerCase());

            // Status filter:
            // - "" (all): show everything
            // - "apply": only show internships that are still available to apply
            //              (user has not applied yet AND slots are not full)
            // - other statuses: match the internship status directly
            boolean matchStatus;
            if (selectedStatus.isEmpty()) {
                matchStatus = true;
            } else if ("apply".equals(selectedStatus)) {
                boolean hasSlots = i.getSlots() < i.getMaxSlots();
                boolean canApply = "apply".equals(i.getStatus());
                matchStatus = canApply && hasSlots;
            } else {
                matchStatus = selectedStatus.equals(i.getStatus());
            }

            boolean matchMode =
                    selectedMode.isEmpty() ||
                            i.type.equalsIgnoreCase(selectedMode);

            if (matchSearch && matchStatus && matchMode) {
                filtered.add(i);
            }
        }

        adapter.updateList(filtered);
    }


}
