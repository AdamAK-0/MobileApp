package com.lau.portin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    EditText etName, etMiddle, etLast, etEmailSignup, etPasswordSignup, etConfirmSignup, etBirth;
    EditText etEmailLogin, etPasswordLogin;
    Spinner spinnerSignup, spinnerLogin;
    CheckBox rememberSignup, rememberLogin;
    Button btnSubmit;
    TextView tvToggle;

    boolean isSignup = true;
    SharedPreferences prefs;
    public static final String BASE_URL = "http://10.0.2.2/portin/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("login", MODE_PRIVATE);

        initViews();
        setupSpinner();
        loadSavedLogin();
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
                    etName.setHint("First Name");
                } else {
                    etLast.setVisibility(View.GONE);
                    etMiddle.setVisibility(View.GONE);
                    etBirth.setVisibility(View.GONE);
                    etName.setHint("Company Name");
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
        etMiddle = findViewById(R.id.etMiddle);
        etLast = findViewById(R.id.etLast);
        etBirth = findViewById(R.id.etBirth);
        etEmailSignup = findViewById(R.id.etEmailSignup);
        etPasswordSignup = findViewById(R.id.etPasswordSignup);
        etConfirmSignup = findViewById(R.id.etConfirmSignup);

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
    }

    void saveLogin(String email, String password) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email", email);
        editor.putString("password", password);
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
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
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

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
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

                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                intent.putExtra("company", company);
                                startActivity(intent);
                                finish();
                            }

                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Signup failed: " + obj.getString("error"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
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
        btnSubmit.setEnabled(true);
        tvToggle.setEnabled(true);
    }

        void authenticateUser() {
        String urlUser = BASE_URL + "authenticate_user.php";
        String urlCompany = BASE_URL + "authenticate_company.php";

        String selected = spinnerLogin.getSelectedItem().toString();
        btnSubmit.setEnabled(false);
        tvToggle.setEnabled(false);
        StringRequest req = new StringRequest(Request.Method.POST,
                selected.equals("User") ? urlUser : urlCompany,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.getString("status");

                        if (status.equals("success")) {
                            int userId;
                            String firstName;
                            if(selected.equals("Company")) {
                                firstName = obj.getString("company_id");
                                userId = obj.getInt("company_id");
                            }
                            else {
                                firstName = obj.getString("first_name");
                                userId = obj.getInt("user_id");
                            }

                            if (rememberLogin.isChecked()) {
                                saveLogin(etEmailLogin.getText().toString(), etPasswordLogin.getText().toString());
                            }

                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            intent.putExtra("user_id", userId);
                            intent.putExtra("first_name", firstName);
                            startActivity(intent);
                            finish();
                        } else if (status.equals("wrong_password")) {
                            Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                        } else if (status.equals("not_found")) {
                            Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("email", etEmailLogin.getText().toString());
                map.put("password", etPasswordLogin.getText().toString());
                return map;
            }
        };
        btnSubmit.setEnabled(true);
        tvToggle.setEnabled(true);
        Volley.newRequestQueue(this).add(req);
    }
}
