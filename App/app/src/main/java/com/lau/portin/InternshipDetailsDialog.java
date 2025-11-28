package com.lau.portin;


import android.content.DialogInterface;
import android.os.Bundle;
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

import java.util.HashMap;
import java.util.Map;

public class InternshipDetailsDialog extends DialogFragment {

    private Internship internship;
    private User currentUser;
    private String type;
    private InternshipAdapter.ViewHolder h;
    private Runnable dismissCallback;


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

        Glide.with(getContext()).load(internship.getPhoto()).placeholder(R.drawable.ic_image).into(img);
        name.setText(internship.getName());
        company.setText(internship.getCompanyName());
        typeTv.setText(internship.getType());
        desc.setText(internship.getDescription());
        rating.setText("Rating: " + internship.getRating() + "/5");

        dates.setText("Duration: " + internship.getStartDate() + " → " + internship.getEndDate());

        slots.setText("Slots: " + internship.getSlots() + "/" + internship.getMaxSlots());

        created.setText("Posted: " + internship.getCreatedAt());

        apply.setEnabled(false);
        apply.setText("Checking...");
        loadApplicationStatus(internship.getId(), currentUser.getUser_id(), h, apply);

        if (!"User".equals(type)) {
            apply.setVisibility(View.GONE);
        } else {
            apply.setOnClickListener(view -> {
                if (currentUser != null) {
                    applyToInternship(internship.getId(), currentUser.getUser_id(), apply);
                }
            });
        }

        return v;
    }

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
                            loadApplicationStatus(internshipId, userId, h, applyBtn);
                        } else if ("full".equals(status)) {
                            Toast.makeText(getContext(), "No available slots for this internship", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(false);
                            applyBtn.setText("Full");
                        } else {
                            Toast.makeText(getContext(), "Failed to apply", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        applyBtn.setEnabled(true);
                    }
                },
                error -> Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(userId));
                map.put("internship_id", String.valueOf(internshipId));
                return map;
            }
        };
        Volley.newRequestQueue(getContext()).add(req);
    }
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
                            h.btnApply.setEnabled(false);
                            apply.setEnabled(false);
                            h.btnApply.setText(prettifyStatusShort(statusCode));
                            apply.setText(prettifyStatusShort(statusCode));

                            if (h.tvApplicationStatus != null) {
                                h.tvApplicationStatus.setVisibility(View.VISIBLE);
                                h.tvApplicationStatus.setText("Status: " + prettifyStatus(statusCode));
                            }
                        } else {
                            // Not applied yet -> check if slots are full
                            if (internship.getSlots() >= internship.getMaxSlots()) {
                                // No available slots
                                h.btnApply.setEnabled(false);
                                apply.setEnabled(false);
                                h.btnApply.setText("Full");
                                apply.setText("Full");

                                if (h.tvApplicationStatus != null) {
                                    h.tvApplicationStatus.setVisibility(View.VISIBLE);
                                    h.tvApplicationStatus.setText("Slots are full");
                                }
                            } else {
                                // Slots available -> normal apply state
                                h.btnApply.setEnabled(true);
                                apply.setEnabled(true);
                                h.btnApply.setText("Apply");
                                apply.setText("Apply");
                                if (h.tvApplicationStatus != null) {
                                    h.tvApplicationStatus.setVisibility(View.GONE);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        // Ignore parse errors in UI
                    }
                },
                error -> {
                    // In case of network error, leave current UI state
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

        Volley.newRequestQueue(h.itemView.getContext()).add(req);
    }
    /*private void applyToInternship(int internshipId, int userId, InternshipAdapter.ViewHolder h, Button btnApply) {
        String url = AddInternshipActivity.BASE_URL + "apply_to_internship.php";

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        String status = obj.optString("status", "");

                        if ("success".equals(status)) {
                            Toast.makeText(h.itemView.getContext(),
                                    "Application submitted",
                                    Toast.LENGTH_SHORT).show();
                            h.btnApply.setEnabled(false);
                            h.btnApply.setText(prettifyStatusShort("applied"));

                            if (h.tvApplicationStatus != null) {
                                h.tvApplicationStatus.setVisibility(View.VISIBLE);
                                h.tvApplicationStatus.setText("Status: " + prettifyStatus("applied"));
                            }
                        } else if ("already_applied".equals(status)) {
                            // User already applied earlier
                            String currentStatus = obj.optString("current_status", "applied");
                            Toast.makeText(h.itemView.getContext(),
                                    "You already applied. Current status: " + prettifyStatus(currentStatus),
                                    Toast.LENGTH_LONG).show();
                            h.btnApply.setEnabled(false);
                            h.btnApply.setText(prettifyStatusShort(currentStatus));
                            if (h.tvApplicationStatus != null) {
                                h.tvApplicationStatus.setVisibility(View.VISIBLE);
                                h.tvApplicationStatus.setText("Status: " + prettifyStatus(currentStatus));
                            }
                        } else {
                            Toast.makeText(h.itemView.getContext(),
                                    "Could not apply. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        // If response is not JSON, fallback to previous simple behaviour
                        if (response.contains("success")) {
                            Toast.makeText(h.itemView.getContext(),
                                    "Application submitted",
                                    Toast.LENGTH_SHORT).show();
                            h.btnApply.setEnabled(false);
                            h.btnApply.setText(prettifyStatusShort("applied"));
                        } else {
                            Toast.makeText(h.itemView.getContext(),
                                    "Could not apply. Try again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> Toast.makeText(h.itemView.getContext(),
                        "Network Error",
                        Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("user_id", String.valueOf(userId));
                map.put("internship_id", String.valueOf(internshipId));
                return map;
            }
        };

        Volley.newRequestQueue(h.itemView.getContext()).add(req);
    }*/
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

    private String prettifyStatusShort(String statusCode) {
        // Short label for the button text
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
