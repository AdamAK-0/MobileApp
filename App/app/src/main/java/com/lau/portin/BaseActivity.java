package com.lau.portin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    protected User currentUser;
    protected Company company;
    protected String type;

    protected BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        // Read intent extras
        Intent intent = getIntent();
        if (intent != null) {
            type = intent.getStringExtra("type");
            if ("Company".equals(type)) {
                company = (Company) intent.getSerializableExtra("company");
            } else if ("User".equals(type)) {
                currentUser = (User) intent.getSerializableExtra("user");
            }
        }

        bottomNav = findViewById(R.id.bottomNav);
        setupBottomNavigation();
    }

    protected void setActivityLayout(@LayoutRes int layoutResID) {
        FrameLayout container = findViewById(R.id.container);
        LayoutInflater.from(this).inflate(layoutResID, container, true);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            // Already on the selected activity
            if ((id == R.id.nav_home && this instanceof HomeActivity) ||
                    (id == R.id.nav_skills && this instanceof SkillActivity)) {
                return true;
            }

            Intent i = null;
            if (id == R.id.nav_home) {
                bottomNav.getMenu().findItem(R.id.nav_skills).setChecked(true);
                i = new Intent(this, HomeActivity.class);
            } else if (id == R.id.nav_skills) {
                bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
                i = new Intent(this, SkillActivity.class);
            }

            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                // Pass extras
                i.putExtra("type", type);
                if ("User".equals(type)) i.putExtra("user", currentUser);
                else if ("Company".equals(type)) i.putExtra("company", company);

                startActivity(i);
            }
            return true;
        });
    }
}
