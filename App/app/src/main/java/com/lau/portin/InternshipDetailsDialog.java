package com.lau.portin;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class InternshipDetailsDialog extends DialogFragment {

    private Internship internship;
    private User currentUser;
    private String type;
    private InternshipAdapter.ViewHolder h;
    private Runnable dismissCallback;

    // CV upload state
    private static final int REQUEST_CV_PICK = 1001;
    private String cvBase64 = null;
    private String cvFileName = null;

    private TextView tvCvFileName;
    private Button btnPickCv;

    public InternshipDetailsDialog(Internship internship, User currentUser, String type, InternshipAdapter.ViewHolder h) {
        this.internship = internship;
        this.currentUser = currentUser;
        this.type = type;
        this.h = h;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_internship_details, container, false);

        ImageView img = v.findViewById(R.id.imgInternshipDialog);
        TextView name = v.findViewById(R.id.tvDialogName);
        TextView company = v.findViewById(R.id.tvDialogCompany);
        TextView typeTv = v.findViewById(R.id.tvDialogType);
        TextView desc = v.findViewById(R.id.tvDialogDescription);
        Button apply = v.findViewById(R.id.btnDialogApply);
        TextView rating = v.findViewById(R.id.tvDialogRating);
        TextView dates = v.findViewById(R.id.tvDialogDates);
        TextView slots = v.findViewById(R.id.tvDialogSlots);
        TextView created = v.findViewById(R.id.tvDialogCreated);

        // CV layout views (for the card)
        View cvLayout = v.findViewById(R.id.layoutCvUpload);
        tvCvFileName = v.findViewById(R.id.tvCvFileName);
        btnPickCv = v.findViewById(R.id.btnPickCv);

        Glide.with(getContext())
                .load(internship.getPhoto())
                .placeholder(R.drawable.ic_image)
                .into(img);

        name.setText(internship.getName());
        company.setText(internship.getCompanyName());
        typeTv.setText(internship.getType());
        desc.setText(internship.getDescription());
        rating.setText("Rating: " + internship.getRating() + "/5");

        dates.setText("Duration: " + internship.getStartDate() + " \u2192 " + internship.getEndDate());
        slots.setText("Slots: " + internship.getSlots() + "/" + internship.getMaxSlots());
        created.setText("Posted: " + internship.getCreatedAt());

        // -------- STATUS CHECK (bug fix here) --------
        // Initially show "Checking...", then load status from server.
        // If anything goes wrong (JSON error / network error) we now FALL BACK to "Apply"
        apply.setEnabled(false);
        apply.setText("Checking...");
        btnPickCv.setEnabled(false);
        if (currentUser != null && "User".equals(type)) {
            loadApplicationStatus(internship.getId(), currentUser.getUser_id(), h, apply);
        } else {
            apply.setVisibility(View.GONE);
            if (cvLayout != null) cvLayout.setVisibility(View.GONE);
        }

        if (!"User".equals(type)) {
            // Company / other -> hide apply and CV section
            apply.setVisibility(View.GONE);
            if (cvLayout != null) cvLayout.setVisibility(View.GONE);
        } else {
            // User: show CV upload card and apply button
            if (cvLayout != null) {
                cvLayout.setVisibility(View.VISIBLE);
            }
            if (btnPickCv != null) {
                btnPickCv.setOnClickListener(view1 -> openCvPicker());
            }

            apply.setOnClickListener(view -> {
                if (currentUser != null) {
                    if (cvBase64 == null || cvBase64.isEmpty()) {
                        Toast.makeText(getContext(), "Please upload your CV (PDF) before applying.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    applyToInternship(internship.getId(), currentUser.getUser_id(), apply);
                }
            });
        }

        return v;
    }

    // ===== APPLY with CV =====
    private void applyToInternship(int internshipId, int userId, Button applyBtn) {
        String url = AddInternshipActivity.BASE_URL + "apply_to_internship.php";
        applyBtn.setEnabled(false);

        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.optString("status");

                        if ("success".equals(status)) {
                            Toast.makeText(getContext(), "Application submitted", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(false);
                            applyBtn.setText("Applied");
                            // Refresh UI on card + dialog
                            loadApplicationStatus(internshipId, userId, h, applyBtn);
                        } else if ("full".equals(status)) {
                            Toast.makeText(getContext(), "No available slots for this internship", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(false);
                            applyBtn.setText("Full");
                        } else if ("already_applied".equals(status)) {
                            String currentStatus = obj.optString("current_status", "applied");
                            Toast.makeText(getContext(),
                                    "You already applied. Current status: " + prettifyStatus(currentStatus),
                                    Toast.LENGTH_LONG).show();
                            applyBtn.setEnabled(false);
                            applyBtn.setText(prettifyStatusShort(currentStatus));
                        } else {
                            Toast.makeText(getContext(), "Failed to apply", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(true);
                            applyBtn.setText("Apply");
                        }
                    } catch (JSONException e) {
                        // Fallback: simple string-based response
                        if (response.contains("success")) {
                            Toast.makeText(getContext(), "Application submitted", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(false);
                            applyBtn.setText("Applied");
                        } else {
                            Toast.makeText(getContext(), "Could not apply. Try again.", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(true);
                            applyBtn.setText("Apply");
                        }
                    }
                },
                error -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                    applyBtn.setEnabled(true);
                    applyBtn.setText("Apply");
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(userId));
                map.put("internship_id", String.valueOf(internshipId));
                map.put("cv_base64", cvBase64 != null ? cvBase64 : "");
                return map;
            }
        };

        Volley.newRequestQueue(getContext()).add(req);
    }

    // ===== STATUS CHECK (fixed "Checking..." bug) =====
    private void loadApplicationStatus(int internshipId, int userId, InternshipAdapter.ViewHolder h, Button apply) {
        String url = AddInternshipActivity.BASE_URL + "check_application_status.php";

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        boolean applied = obj.optBoolean("applied", false);

                        if (applied) {
                            String statusCode = obj.optString("status", "applied");
                            btnPickCv.setText("Uploaded");
                            if (h != null && h.btnApply != null) {
                                h.btnApply.setEnabled(false);
                                h.btnApply.setText(prettifyStatusShort(statusCode));
                            }
                            if (apply != null) {
                                apply.setEnabled(false);
                                apply.setText(prettifyStatusShort(statusCode));
                            }

                            if (h != null && h.tvApplicationStatus != null) {
                                h.tvApplicationStatus.setVisibility(View.VISIBLE);
                                h.tvApplicationStatus.setText("Status: " + prettifyStatus(statusCode));
                            }
                        } else {
                            // Not applied yet -> check if slots are full
                            if (internship.getSlots() >= internship.getMaxSlots()) {
                                btnPickCv.setText("Full");
                                // FULL
                                if (h != null && h.btnApply != null) {
                                    h.btnApply.setEnabled(false);
                                    h.btnApply.setText("Full");
                                }
                                if (apply != null) {
                                    apply.setEnabled(false);
                                    apply.setText("Full");
                                }

                                if (h != null && h.tvApplicationStatus != null) {
                                    h.tvApplicationStatus.setVisibility(View.VISIBLE);
                                    h.tvApplicationStatus.setText("Slots are full");
                                }
                            } else {
                                // AVAILABLE -> should show Apply (this is the normal case)
                                btnPickCv.setEnabled(true);
                                btnPickCv.setText("Upload");
                                if (h != null && h.btnApply != null) {
                                    h.btnApply.setEnabled(true);
                                    h.btnApply.setText("Apply");
                                }
                                if (apply != null) {
                                    apply.setEnabled(true);
                                    apply.setText("Apply");
                                }
                                if (h != null && h.tvApplicationStatus != null) {
                                    h.tvApplicationStatus.setVisibility(View.GONE);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // If parsing fails, DO NOT leave "Checking..." forever.
                        // Fall back to "Apply".
                        if ("User".equals(type) && apply != null) {
                            apply.setEnabled(true);
                            apply.setText("Apply");
                        }
                        if (h != null && h.btnApply != null) {
                            h.btnApply.setEnabled(true);
                            h.btnApply.setText("Apply");
                            if (h.tvApplicationStatus != null) {
                                h.tvApplicationStatus.setVisibility(View.GONE);
                            }
                        }
                    }
                },
                error -> {
                    // On network error, same: show Apply so user can still try.
                    if ("User".equals(type) && apply != null) {
                        apply.setEnabled(true);
                        apply.setText("Apply");
                    }
                    if (h != null && h.btnApply != null) {
                        h.btnApply.setEnabled(true);
                        h.btnApply.setText("Apply");
                        if (h.tvApplicationStatus != null) {
                            h.tvApplicationStatus.setVisibility(View.GONE);
                        }
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(userId));
                map.put("internship_id", String.valueOf(internshipId));
                return map;
            }
        };

        // If dialog created from a ViewHolder, use its context. Otherwise use dialog context.
        if (h != null && h.itemView != null) {
            Volley.newRequestQueue(h.itemView.getContext()).add(req);
        } else {
            Volley.newRequestQueue(requireContext()).add(req);
        }
    }

    // ===== CV picker helpers =====
    private void openCvPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select CV (PDF)"), REQUEST_CV_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CV_PICK && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                String displayName = getFileName(uri);
                InputStream is = requireContext().getContentResolver().openInputStream(uri);
                byte[] fileBytes = readAllBytes(is);
                if (fileBytes != null) {
                    cvBase64 = Base64.encodeToString(fileBytes, Base64.NO_WRAP);
                    cvFileName = displayName != null ? displayName : "cv.pdf";
                    if (tvCvFileName != null) {
                        tvCvFileName.setText(cvFileName);
                    }
                    Toast.makeText(getContext(), "CV attached successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Could not read the selected file", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(getContext(), "Error reading file", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = requireContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) {
                        result = cursor.getString(idx);
                    }
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    private byte[] readAllBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) return null;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    // ===== helpers to prettify status labels =====
    private String prettifyStatusShort(String statusCode) {
        if (statusCode == null) return "Applied";
        switch (statusCode) {
            case "in_review":
                return "In review";
            case "accepted":
                return "Accepted";
            case "rejected":
                return "Rejected";
            case "withdrawn":
                return "Withdrawn";
            case "applied":
            default:
                return "Applied";
        }
    }

    private String prettifyStatus(String statusCode) {
        if (statusCode == null) return "Applied";
        switch (statusCode) {
            case "in_review":
                return "In review";
            case "accepted":
                return "Accepted";
            case "rejected":
                return "Rejected";
            case "withdrawn":
                return "Withdrawn";
            case "applied":
            default:
                return "Applied";
        }
    }

    public void setOnDismissListener(Runnable callback) {
        this.dismissCallback = callback;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissCallback != null) dismissCallback.run();
    }
}
