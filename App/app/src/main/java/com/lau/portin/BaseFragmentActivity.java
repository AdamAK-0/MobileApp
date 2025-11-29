package com.lau.portin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseFragmentActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNav;
    protected User currentUser;
    protected Company company;
    protected String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_fragment);

        bottomNav = findViewById(R.id.bottomNav);

        // Get extras from intent
        Intent intent = getIntent();
        type = intent.getStringExtra("type");
        if ("User".equals(type)) currentUser = (User) intent.getSerializableExtra("user");
        else if ("Company".equals(type)) company = (Company) intent.getSerializableExtra("company");

        // Load default fragment
        if (type.equals("Company") || type.equals("User")) {
            loadFragment(new HomeFragment());
        }
        if(type.equals("Company")) bottomNav.getMenu().findItem(R.id.nav_skills).setVisible(false);

        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            }
                else if(item.getItemId() == R.id.nav_skills){
                if ("User".equals(type)) loadFragment(new SkillsFragment());
                return true;
            }
                else if(item.getItemId() == R.id.nav_profile){
                    ProfileFragment fragment = new ProfileFragment();

                    Bundle b = new Bundle();
                    b.putString("type", type);
                    b.putSerializable("user", currentUser);
                    b.putSerializable("company", company);

                    fragment.setArguments(b);
                    loadFragment(fragment);
                    return true;

            }
                else if(item.getItemId() == R.id.nav_settings){
                    SettingsFragment fragment = new SettingsFragment();
                    Bundle b = new Bundle();
                    b.putString("type", type);
                    b.putSerializable("user", currentUser);
                    b.putSerializable("company", company);
                    fragment.setArguments(b);
                    loadFragment(fragment);
                    return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}

