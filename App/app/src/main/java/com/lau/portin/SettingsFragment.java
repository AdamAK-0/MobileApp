package com.lau.portin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    Switch switchDarkMode;
    Button btnDeleteAccount;
    boolean isUser = true;
    User user;
    Company company;

    SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);

        switchDarkMode = v.findViewById(R.id.switchDarkMode);
        btnDeleteAccount = v.findViewById(R.id.btnDeleteAccount);

        prefs = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE);

        // Load dark mode preference
        boolean darkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(darkMode);
        AppCompatDelegate.setDefaultNightMode(darkMode ?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        Bundle b = getArguments();
        if (b != null) {
            isUser = "User".equals(b.getString("type"));
            if (isUser) user = (User) b.getSerializable("user");
            else company = (Company) b.getSerializable("company");
        }

        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppCompatDelegate.setDefaultNightMode(isChecked ?
                    AppCompatDelegate.MODE_NIGHT_YES :
                    AppCompatDelegate.MODE_NIGHT_NO);
            prefs.edit().putBoolean("dark_mode", isChecked).apply();
            //set the navigation bar to the nav_home
            requireActivity().findViewById(R.id.nav_home).performClick();
        });

        // Delete account
        btnDeleteAccount.setOnClickListener(v1 -> deleteAccount());

        return v;
    }

    private void deleteAccount() {
        String url = isUser ?
                "http://10.0.2.2/portin/delete_user.php" :
                "http://10.0.2.2/portin/delete_company.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    if ("success".equals(response)) {
                        Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show();

                        // Clear stored login/session info if you have any
                        // Redirect to MainActivity
                        Intent intent = new Intent(requireContext(), MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete account", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                if (isUser) map.put("user_id", String.valueOf(user.getUser_id()));
                else map.put("company_id", String.valueOf(company.getCompany_id()));
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(req);
    }
}
