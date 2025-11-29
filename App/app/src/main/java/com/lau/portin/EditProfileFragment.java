package com.lau.portin;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EditProfileFragment extends Fragment {

    EditText etFirst, etMiddle, etLast, etBirth, etCompanyName, etDesc, etPass, etPass2;
    EditText etEmail, etConfirm;
    ImageView imgPhoto;
    Button btnSave;

    String BASE_URL = "http://10.0.2.2/portin/uploads/";
    Uri selectedImageUri = null;

    boolean isUser = true;
    User user;
    Company company;
    String photo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        etFirst = v.findViewById(R.id.etFirst);
        etMiddle = v.findViewById(R.id.etMiddle);
        etLast = v.findViewById(R.id.etLast);
        etBirth = v.findViewById(R.id.etBirth);
        etPass = v.findViewById(R.id.passv);
        etPass2 = v.findViewById(R.id.passv2);

        etCompanyName = v.findViewById(R.id.etCompanyName);
        etDesc = v.findViewById(R.id.etDesc);

        imgPhoto = v.findViewById(R.id.imgNewPhoto);
        btnSave = v.findViewById(R.id.btnSave);

        Bundle b = getArguments();
        if (b == null) return v;

        if ("User".equals(b.getString("type"))) {
            isUser = true;
            user = (User) b.getSerializable("user");
            loadUser();
        } else {
            isUser = false;
            company = (Company) b.getSerializable("company");
            loadCompany();
        }

        imgPhoto.setOnClickListener(v1 -> openGallery());

        btnSave.setOnClickListener(v12 -> saveProfile());
        View groupUser = v.findViewById(R.id.group_user);
        View groupCompany = v.findViewById(R.id.group_company);

        if (isUser) {
            groupUser.setVisibility(View.VISIBLE);
            groupCompany.setVisibility(View.GONE);
        } else {
            groupUser.setVisibility(View.GONE);
            groupCompany.setVisibility(View.VISIBLE);
        }


        return v;
    }

    private void loadUser() {
        etFirst.setText(user.getFirst_name());
        etMiddle.setText(user.getMiddle_name());
        etLast.setText(user.getLast_name());
        etBirth.setText(String.valueOf(user.getBirth_year()));

        Glide.with(requireContext())
                .load(BASE_URL + user.getPhoto())
                .placeholder(R.drawable.ic_user)   // <<< ADD THIS
                .error(R.drawable.ic_user)         // optional but useful
                .into(imgPhoto);

    }

    private void loadCompany() {
        etCompanyName.setText(company.getName());
        etDesc.setText(company.getDescription());

        Glide.with(requireContext())
                .load(BASE_URL + company.getPhoto())
                .placeholder(R.drawable.ic_user)
                .error(R.drawable.ic_user)
                .into(imgPhoto);


    }

    private void openGallery() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 100);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            // Display selected image using Glide for better compatibility
            Glide.with(requireContext())
                    .load(selectedImageUri)
                    .placeholder(R.drawable.ic_user)
                    .into(imgPhoto);
        }
    }

    private void saveProfile() {
        String passv;
        if(isUser) {
            passv = etPass.getText().toString().trim();
        }
        else {
            passv = etPass2.getText().toString().trim();

        }
        if(!passv.isEmpty()) {
            if (passv.length() < 8) {
                Toast.makeText(getContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passv.matches(".*[A-Z].*")) {
                Toast.makeText(getContext(), "Password must contain at least one uppercase letter", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passv.matches(".*[0-9].*")) {
                Toast.makeText(getContext(), "Password must contain at least one number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!passv.matches(".*[!@#$%^&*+=?-].*")) {
                Toast.makeText(getContext(), "Password must contain at least one special character (!@#$%^&*+=?-)", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        else {
            if(isUser) {
                etPass.setError("Enter your password to confirm or type a new password");
                return;
            }
            else {
                etPass2.setError("Enter your old password to confirm or type a new password");
                return;
            }
        }

        if (isUser) {
            if(etFirst.getText().toString().isEmpty() || etLast.getText().toString().isEmpty() || etBirth.getText().toString().isEmpty()) {
                etFirst.setError("Required");
                etLast.setError("Required");
                etBirth.setError("Required");
                return;
            }
            String birth = etBirth.getText().toString().trim();
            if(!birth.isEmpty()) {
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
            if (selectedImageUri != null) {

                uploadImage(
                        isUser ? user.getUser_id() : company.getCompany_id(),
                        isUser ? "user" : "company",
                        filename -> {
                            photo = filename;

                            if (isUser) updateUser();
                            else updateCompany();
                        }
                );

            } else {
                // no new image
                photo = isUser ? user.getPhoto() : company.getPhoto();

                if (isUser) updateUser();
                else updateCompany();
            }
        }
        else {
            if (selectedImageUri != null) {

                uploadImage(
                        isUser ? user.getUser_id() : company.getCompany_id(),
                        isUser ? "user" : "company",
                        filename -> {
                            photo = filename;

                            if (isUser) updateUser();
                            else updateCompany();
                        }
                );

            } else {
                // no new image
                photo = isUser ? user.getPhoto() : company.getPhoto();

                if (isUser) updateUser();
                else updateCompany();
            }
        }
    }

    private void updateUser() {

        String url = "http://10.0.2.2/portin/update_user.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
                        Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show();
                        if (isUser) {
                            user.setFirst_name(etFirst.getText().toString());
                            user.setMiddle_name(etMiddle.getText().toString());
                            user.setLast_name(etLast.getText().toString());
                            user.setBirth_year(Integer.parseInt(etBirth.getText().toString()));
                            user.setPhoto(photo);
                            loadUser(); // reload UI
                        } else {
                            company.setName(etCompanyName.getText().toString());
                            company.setDescription(etDesc.getText().toString());
                            company.setPhoto(photo);
                            loadCompany(); // reload UI
                        }

                        requireActivity().onBackPressed();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error updating", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(user.getUser_id()));
                map.put("first_name", etFirst.getText().toString());
                map.put("middle_name", etMiddle.getText().toString());
                map.put("last_name", etLast.getText().toString());
                map.put("birth_year", etBirth.getText().toString());
                map.put("password", etPass.getText().toString());
                if (selectedImageUri != null)
                    map.put("photo", photo);
                else map.put("photo", user.getPhoto());
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(req);
    }

    private void updateCompany() {

        String url = "http://10.0.2.2/portin/update_company.php";

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    if (response.equals("success")) {
                        Toast.makeText(requireContext(), "Updated!", Toast.LENGTH_SHORT).show();
                        if (isUser) {
                            user.setFirst_name(etFirst.getText().toString());
                            user.setMiddle_name(etMiddle.getText().toString());
                            user.setLast_name(etLast.getText().toString());
                            user.setBirth_year(Integer.parseInt(etBirth.getText().toString()));
                            user.setPhoto(photo);
                            loadUser(); // reload UI
                        } else {
                            company.setName(etCompanyName.getText().toString());
                            company.setDescription(etDesc.getText().toString());
                            company.setPhoto(photo);
                            loadCompany(); // reload UI
                        }
                        requireActivity().onBackPressed();
                    }
                },
                error -> Toast.makeText(requireContext(), "Error updating", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("company_id", String.valueOf(company.getCompany_id()));
                map.put("name", etCompanyName.getText().toString());
                map.put("password", etPass2.getText().toString());
                map.put("description", etDesc.getText().toString());
                if (selectedImageUri != null)
                    map.put("photo", photo);
                else map.put("photo", company.getPhoto());
                return map;
            }
        };

        Volley.newRequestQueue(requireContext()).add(req);
    }

    private void uploadImage(int id, String type) {
        String url = "http://10.0.2.2/portin/upload_profile_image.php";

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(),
                    selectedImageUri
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            String imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            StringRequest req = new StringRequest(Request.Method.POST, url,
                    response -> {
                        // response contains the filename, example: "user_15_98238723.jpg"
                        if (!response.equals("fail")) {
                           photo = response;
                        } else {
                            Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(requireContext(), "Upload error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<>();
                    map.put("image", imageBase64);
                    map.put("type", type);
                    map.put("id", String.valueOf(id));
                    return map;
                }
            };

            Volley.newRequestQueue(requireContext()).add(req);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Image Error!", Toast.LENGTH_SHORT).show();
        }
    }
    interface UploadCallback {
        void onUploaded(String filename);
    }
    private void uploadImage(int id, String type, UploadCallback callback) {
        String url = "http://10.0.2.2/portin/upload_profile_image.php";

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    requireContext().getContentResolver(),
                    selectedImageUri
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            String imageBase64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

            StringRequest req = new StringRequest(Request.Method.POST, url,
                    response -> {
                        if (!response.equals("fail")) {
                            callback.onUploaded(response); // return filename
                        } else {
                            Toast.makeText(requireContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> Toast.makeText(requireContext(), "Upload error", Toast.LENGTH_SHORT).show()
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> map = new HashMap<>();
                    map.put("image", imageBase64);
                    map.put("type", type);
                    map.put("id", String.valueOf(id));
                    return map;
                }
            };

            Volley.newRequestQueue(requireContext()).add(req);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Image Error!", Toast.LENGTH_SHORT).show();
        }
    }

}
