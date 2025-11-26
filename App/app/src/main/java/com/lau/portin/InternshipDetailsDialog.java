package com.lau.portin;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    public InternshipDetailsDialog(Internship internship, User currentUser, String type) {
        this.internship = internship;
        this.currentUser = currentUser;
        this.type = type;
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

        Glide.with(getContext()).load(internship.getPhoto()).placeholder(R.drawable.ic_image).into(img);
        name.setText(internship.getName());
        company.setText(internship.getCompanyName());
        typeTv.setText(internship.getType());
        desc.setText(internship.getDescription());

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
        StringRequest req = new StringRequest(Request.Method.POST, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if ("success".equals(obj.optString("status"))) {
                            Toast.makeText(getContext(), "Application submitted", Toast.LENGTH_SHORT).show();
                            applyBtn.setEnabled(false);
                            applyBtn.setText("Applied");
                        } else {
                            Toast.makeText(getContext(), "Failed to apply", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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
    /*private void applyToInternship(int internshipId, int userId, InternshipAdapter.ViewHolder h) {
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
}
