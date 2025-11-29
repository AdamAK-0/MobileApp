package com.lau.portin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    RecyclerView rv;
    ArrayList<Internship> list = new ArrayList<>();
    InternshipAdapter adapter;
    SwipeRefreshLayout swipeRefreshLayout;
    SearchView searchBar;
    AutoCompleteTextView filterStatus, filterMode;
    String selectedStatus = "";
    String selectedMode = "";

    public static final String BASE_URL = "http://10.0.2.2/portin/";

    protected User currentUser;
    protected Company company;
    protected String type;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_home, container, false);

        // Get parent activity values
        if (getActivity() instanceof BaseFragmentActivity) {
            BaseFragmentActivity main = (BaseFragmentActivity) getActivity();
            currentUser = main.currentUser;
            company = main.company;
            type = main.type;
        }

        rv = view.findViewById(R.id.recyclerInternships);
        searchBar = view.findViewById(R.id.searchBar);
        searchBar.setOnClickListener(v -> searchBar.setIconified(false));
        filterStatus = view.findViewById(R.id.filterStatus);
        filterMode = view.findViewById(R.id.filterMode);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        FloatingActionButton fab = view.findViewById(R.id.btnAddInternship);

        // Status options
        String[] statuses = {"all", "apply", "applied", "accepted", "rejected", "in_review"};
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, statuses);
        filterStatus.setAdapter(statusAdapter);

        // Mode options
        String[] modes = {"all", "in-person", "remote", "hybrid"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, modes);
        filterMode.setAdapter(modeAdapter);

        filterStatus.setOnClickListener(v -> filterStatus.showDropDown());
        filterMode.setOnClickListener(v -> filterMode.showDropDown());

        filterStatus.setOnItemClickListener((parent, v, position, id) -> {
            selectedStatus = statuses[position].equals("all") ? "" : statuses[position];
            applyFilters();
        });

        filterMode.setOnItemClickListener((parent, v, position, id) -> {
            selectedMode = modes[position].equals("all") ? "" : modes[position];
            applyFilters();
        });

        fab.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), AddInternshipActivity.class);
            i.putExtra("type", type);
            i.putExtra("company", company);
            startActivity(i);
        });

        if (type.equals("Company")) fab.setVisibility(View.VISIBLE);
        else fab.setVisibility(View.GONE);

        // Recycler setup
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new InternshipAdapter(list, type, currentUser, company);
        rv.setAdapter(adapter);

        // Load data
        if (type.equals("Company") && company != null) loadCompanyInternships(company.getCompany_id());
        else loadInternships();

        // Swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (type.equals("Company") && company != null) loadCompanyInternships(company.getCompany_id());
            else loadInternships();
        });

        // Search filter
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

        if (type.equals("Company")) filterStatus.setVisibility(View.GONE);

        return view;
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
                        Toast.makeText(getContext(), "JSON error", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                });
        Volley.newRequestQueue(requireContext()).add(req);
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
                        Toast.makeText(getContext(), "JSON error", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefreshLayout.setRefreshing(false);
                },
                error -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(false);
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("company_id", String.valueOf(companyId));
                return map;
            }
        };
        Volley.newRequestQueue(requireContext()).add(req);
    }

    void applyFilters() {
        ArrayList<Internship> filtered = new ArrayList<>();
        for (Internship i : list) {
            boolean matchSearch = i.getName().toLowerCase().contains(searchBar.getQuery().toString().toLowerCase());

            // Status filter:
            // - "" (all): show everything
            // - "apply": only show internships that are still available to apply
            //            (user has not applied yet AND slots are not full)
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

            boolean matchMode = selectedMode.isEmpty() || i.type.equalsIgnoreCase(selectedMode);
            if (matchSearch && matchStatus && matchMode) filtered.add(i);
        }
        adapter.updateList(filtered);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (type.equals("Company") && company != null) loadCompanyInternships(company.getCompany_id());
        else loadInternships();
    }
}
