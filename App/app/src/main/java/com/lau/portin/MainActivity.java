package com.lau.portin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.view.View;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilMiddle, tilLast, tilBirth, tilEmail, tilPassword, tilConfirm;
    private TextInputEditText etName;
    EditText etMiddle, etLast, etEmailSignup, etPasswordSignup, etConfirmSignup, etBirth;
    EditText etEmailLogin, etPasswordLogin;
    Spinner spinnerSignup, spinnerLogin;
    CheckBox rememberSignup, rememberLogin;
    Button btnSubmit;
    TextView tvToggle;

    boolean isSignup = true;
    SharedPreferences prefs;
    SharedPreferences prefs2;

    public static final String BASE_URL = "http://10.0.2.2/portin/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("login", MODE_PRIVATE);

        initViews();
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) tilMiddle.getLayoutParams();
        params.setMargins(0, 0, 0, 12); // left, top, right, bottom
        tilName.setLayoutParams(params);
        tilMiddle.setLayoutParams(params);
        tilLast.setLayoutParams(params);
        tilBirth.setLayoutParams(params);
        tilEmail.setLayoutParams(params);
        tilPassword.setLayoutParams(params);
        tilConfirm.setLayoutParams(params);
        setupSpinner();
        loadSavedLogin();
//        prefs2 = getSharedPreferences("settings", Context.MODE_PRIVATE);
//
//        // Load dark mode preference
//        boolean darkMode = prefs2.getBoolean("dark_mode", false);
//        AppCompatDelegate.setDefaultNightMode(darkMode ?
//                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);

        if(!prefs.getString("email", "").isEmpty()) {
            isSignup = false;
            findViewById(R.id.signupLayout).setVisibility(View.GONE);
            findViewById(R.id.loginLayout).setVisibility(View.VISIBLE);
            btnSubmit.setText("Login");
            tvToggle.setText("Don't have an account? Sign Up");
        }
        toggleMode();
        submitListener();
        spinnerSignup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                Log.d("Spinner", "Selected: " + selected);
                if(selected.equals("User")) {
                    etLast.setVisibility(View.VISIBLE);
                    etMiddle.setVisibility(View.VISIBLE);
                    etBirth.setVisibility(View.VISIBLE);
                    tilName.setHint("First Name");
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams) tilMiddle.getLayoutParams();
                    params.setMargins(0, 0, 0, 12); // left, top, right, bottom
                    tilName.setLayoutParams(params);
                    tilMiddle.setLayoutParams(params);
                    tilLast.setLayoutParams(params);
                    tilBirth.setLayoutParams(params);
                    tilEmail.setLayoutParams(params);
                    tilPassword.setLayoutParams(params);
                    tilConfirm.setLayoutParams(params);
                    tilMiddle.setVisibility(View.VISIBLE);
                    tilLast.setVisibility(View.VISIBLE);
                    tilBirth.setVisibility(View.VISIBLE);
                } else {
                    tilName.setHint("Company Name");
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams) tilMiddle.getLayoutParams();

                    params.setMargins(0, 0, 0, 0); // left, top, right, bottom
                    tilName.setLayoutParams(params);
                    tilMiddle.setLayoutParams(params);
                    tilLast.setLayoutParams(params);
                    tilBirth.setLayoutParams(params);
                    params.setMargins(0, 0, 0, 12);
                    tilEmail.setLayoutParams(params);
                    tilPassword.setLayoutParams(params);
                    tilConfirm.setLayoutParams(params);
                    etLast.setVisibility(View.GONE);
                    etMiddle.setVisibility(View.GONE);
                    etBirth.setVisibility(View.GONE);
                    tilMiddle.setVisibility(View.GONE);
                    tilLast.setVisibility(View.GONE);
                    tilBirth.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    void initViews() {
        etName = findViewById(R.id.etName);
        tilName = findViewById(R.id.tilName);
        etMiddle = findViewById(R.id.etMiddle);
        tilMiddle = findViewById(R.id.tilMiddle);
        etLast = findViewById(R.id.etLast);
        tilLast = findViewById(R.id.tilLast);
        etBirth = findViewById(R.id.etBirth);
        tilBirth = findViewById(R.id.tilBirth);
        etEmailSignup = findViewById(R.id.etEmailSignup);
        tilEmail = findViewById(R.id.tilEmail);
        etPasswordSignup = findViewById(R.id.etPasswordSignup);
        tilPassword = findViewById(R.id.tilPassword);
        etConfirmSignup = findViewById(R.id.etConfirmSignup);
        tilConfirm = findViewById(R.id.tilConfirm);

        etEmailLogin = findViewById(R.id.etEmailLogin);
        etPasswordLogin = findViewById(R.id.etPasswordLogin);

        spinnerSignup = findViewById(R.id.spinnerSignup);
        spinnerLogin = findViewById(R.id.spinnerLogin);

        rememberSignup = findViewById(R.id.rememberSignup);
        rememberLogin = findViewById(R.id.rememberLogin);

        btnSubmit = findViewById(R.id.btnSubmit);
        tvToggle = findViewById(R.id.tvToggle);
    }

    void setupSpinner() {
        String[] types = {"User", "Company"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, types);

        spinnerSignup.setAdapter(adapter);
        spinnerLogin.setAdapter(adapter);
    }

    void toggleMode() {
        tvToggle.setOnClickListener(v -> {
            isSignup = !isSignup;
            if (isSignup) {
                findViewById(R.id.signupLayout).setVisibility(View.VISIBLE);
                findViewById(R.id.loginLayout).setVisibility(View.GONE);
                btnSubmit.setText("Sign Up");
                tvToggle.setText("Have an account? Login");
            } else {
                findViewById(R.id.signupLayout).setVisibility(View.GONE);
                findViewById(R.id.loginLayout).setVisibility(View.VISIBLE);
                btnSubmit.setText("Login");
                tvToggle.setText("Don't have an account? Sign Up");
            }
        });
    }

    void submitListener() {
        btnSubmit.setOnClickListener(v -> {
            if (isSignup) registerUser();
            else authenticateUser();
        });
    }

    void loadSavedLogin() {
        etEmailLogin.setText(prefs.getString("email", ""));
        etPasswordLogin.setText(prefs.getString("password", ""));
        spinnerLogin.setSelection(prefs.getString("type", "User").equals("User") ? 0 : 1);
    }

    void saveLogin(String email, String password, String type) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putString("type", type);
        editor.apply();
    }

    void registerUser() {
        String namev = etName.getText().toString().trim();
        String middle = etMiddle.getText().toString().trim();
        String last = etLast.getText().toString().trim();
        String birth = etBirth.getText().toString().trim();
        String emailv = etEmailSignup.getText().toString().trim();
        String passv = etPasswordSignup.getText().toString().trim();
        String confirmv = etConfirmSignup.getText().toString().trim();
        String typev = spinnerSignup.getSelectedItem().toString();


        // Validation
        if (namev.isEmpty() || emailv.isEmpty() || passv.isEmpty() || confirmv.isEmpty()) {
            if(namev.isEmpty()) {
                etName.setError("Name required");

            }
            if(emailv.isEmpty()) {
                etEmailSignup.setError("Email required");
            }
            if(passv.isEmpty()) {
                etPasswordSignup.setError("Password required");
            }
            if(confirmv.isEmpty()) {
                etConfirmSignup.setError("Confirm password required");
            }
            if(birth.isEmpty() && typev.equals("User")) {
                etBirth.setError("Birth year required");
            }
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        if(birth.isEmpty() && typev.equals("User")) {
            etBirth.setError("Birth year required");
            return;
        }
        if (!isValidEmail(emailv)) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return; // stop submit
        }
        if(!birth.isEmpty() && typev.equals("User")) {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);

            // LOWER BOUND (reasonable)
            if (Integer.parseInt(birth) < currentYear - 100) {
                etBirth.setError("Birth year too old");
                return;
            }

            // UPPER BOUND (not in the future)
            if (Integer.parseInt(birth) > currentYear) {
                etBirth.setError("Birth year cannot be in the future");
                return;
            }
        }
        if (!passv.equals(confirmv)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passv.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passv.matches(".*[A-Z].*")) {
            Toast.makeText(this, "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passv.matches(".*[0-9].*")) {
            Toast.makeText(this, "Password must contain at least one number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passv.matches(".*[!@#$%^&*+=?-].*")) {
            Toast.makeText(this, "Password must contain at least one special character (!@#$%^&*+=?-)", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = typev.equals("User") ? BASE_URL + "add_user.php" : BASE_URL + "add_company.php";

        btnSubmit.setEnabled(false);
        tvToggle.setEnabled(false);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.getString("status").equals("success")) {

                            if (typev.equals("User")) {

                                JSONObject u = obj.getJSONObject("user");

                                User user = new User(
                                        u.getInt("user_id"),
                                        u.getString("first_name"),
                                        u.optString("middle_name", ""),
                                        u.optString("last_name", ""),
                                        u.getInt("birth_year"),
                                        u.getString("email"),
                                        u.getString("password"),
                                        u.optString("photo", ""),
                                        u.optString("transcript", ""),
                                        u.getString("created_at")
                                );

                                Toast.makeText(MainActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                btnSubmit.setEnabled(true);
                                tvToggle.setEnabled(true);
                                if (rememberSignup.isChecked())
                                    saveLogin(emailv, passv, typev);
                                //Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                Intent intent = new Intent(MainActivity.this, BaseFragmentActivity.class);
                                intent.putExtra("type", "User");
                                intent.putExtra("user", user); // Serializable object
                                startActivity(intent);
                                finish();

                            } else {
                                // Company signup
                                JSONObject c = obj.getJSONObject("company");

                                Company company = new Company(
                                        c.getInt("company_id"),
                                        c.getString("name"),
                                        c.getString("email"),
                                        c.getString("password"),
                                        c.optString("photo", ""),
                                        c.optString("rating") != null ? (float) c.getDouble("rating") : 0f,
                                        c.optString("description", ""),
                                        c.getString("created_at")
                                );

                                // Clean photo if it contains http://10.0.2.2/portin/uploads/
                                if (company.getPhoto() != null && company.getPhoto().contains("http://10.0.2.2/portin/uploads/")) {
                                    company.setPhoto(company.getPhoto().replace("http://10.0.2.2/portin/uploads/", ""));
                                }

                                Toast.makeText(MainActivity.this, "Company signup successful", Toast.LENGTH_SHORT).show();
                                btnSubmit.setEnabled(true);
                                tvToggle.setEnabled(true);
                                if (rememberSignup.isChecked())
                                    saveLogin(emailv, passv, typev);
                                //Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                Intent intent = new Intent(MainActivity.this, BaseFragmentActivity.class);
                                intent.putExtra("type", "Company");
                                intent.putExtra("company", company);
                                startActivity(intent);
                                finish();
                            }
                        } else if (obj.getString("status").equals("email_exists")) {
                            Toast.makeText(MainActivity.this,
                                    "Email already registered. Try another one.",
                                    Toast.LENGTH_SHORT).show();
                            btnSubmit.setEnabled(true);
                            tvToggle.setEnabled(true);
                        }
                        else {
                            Toast.makeText(MainActivity.this,
                                    "Signup failed: " + obj.getString("error"),
                                    Toast.LENGTH_SHORT).show();
                            btnSubmit.setEnabled(true);
                            tvToggle.setEnabled(true);
                        }

                    } catch (Exception e) {
                        btnSubmit.setEnabled(true);
                        tvToggle.setEnabled(true);
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    btnSubmit.setEnabled(true);
                    tvToggle.setEnabled(true);
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("email", emailv);
                params.put("password", passv);

                if (typev.equals("User")) {
                    params.put("first_name", namev);
                    params.put("middle_name", middle);
                    params.put("last_name", last);
                    params.put("birth_year", birth);
                } else {
                    params.put("name", namev);
                    params.put("description", "None");
                }

                return params;
            }
        };

        Volley.newRequestQueue(this).add(request);
    }

        void authenticateUser() {
            String urlUser = BASE_URL + "authenticate_user.php";
            String urlCompany = BASE_URL + "authenticate_company.php";

            String selected = spinnerLogin.getSelectedItem().toString();
            String email = etEmailLogin.getText().toString().trim();

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
                return; // stop submit
            }

            btnSubmit.setEnabled(false);
            tvToggle.setEnabled(false);
            StringRequest req = new StringRequest(Request.Method.POST,
                    selected.equals("User") ? urlUser : urlCompany,
                    response -> {
                        try {
                            JSONObject obj = new JSONObject(response);
                            String status = obj.getString("status");

                            if (status.equals("success")) {

                                if (selected.equals("User")) {
                                    JSONObject u = obj.getJSONObject("user");

                                    User user = new User(
                                            u.getInt("user_id"),
                                            u.getString("first_name"),
                                            u.optString("middle_name", ""),
                                            u.optString("last_name", ""),
                                            u.getInt("birth_year"),
                                            u.getString("email"),
                                            u.getString("password"),
                                            u.optString("photo", ""),
                                            u.optString("transcript", ""),
                                            u.getString("created_at")
                                    );

                                    if (rememberLogin.isChecked()) {
                                        saveLogin(etEmailLogin.getText().toString(), etPasswordLogin.getText().toString(), "User");
                                    }

                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    //Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    Intent intent = new Intent(MainActivity.this, BaseFragmentActivity.class);
                                    intent.putExtra("type", "User");
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                    finish();

                                } else {   // COMPANY LOGIN

                                    JSONObject c = obj.getJSONObject("company");

                                    Company company = new Company(
                                            c.getInt("company_id"),
                                            c.getString("name"),
                                            c.getString("email"),
                                            c.getString("password"),
                                            c.optString("photo", ""),
                                            (float) c.optDouble("rating", 0.0),
                                            c.optString("description", ""),
                                            c.optString("created_at", "")
                                    );

                                    if (rememberLogin.isChecked()) {
                                        saveLogin(etEmailLogin.getText().toString(), etPasswordLogin.getText().toString(), "Company");
                                    }

                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    //Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                    Intent intent = new Intent(MainActivity.this, BaseFragmentActivity.class);
                                    intent.putExtra("type", "Company");
                                    intent.putExtra("company", company);
                                    startActivity(intent);
                                    finish();
                                }
                                btnSubmit.setEnabled(true);
                                tvToggle.setEnabled(true);

                            } else if (status.equals("wrong_password")) {
                                btnSubmit.setEnabled(true);
                                tvToggle.setEnabled(true);
                                Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();

                            } else if (status.equals("not_found")) {
                                btnSubmit.setEnabled(true);
                                tvToggle.setEnabled(true);
                                Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            btnSubmit.setEnabled(true);
                            tvToggle.setEnabled(true);
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        tvToggle.setEnabled(true);
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<>();
                    map.put("email", etEmailLogin.getText().toString());
                    map.put("password", etPasswordLogin.getText().toString());
                    return map;
                }
            };

            Volley.newRequestQueue(this).add(req);
        }
    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
