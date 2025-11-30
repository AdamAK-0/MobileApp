package com.lau.portin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.content.ActivityNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationAdapter extends RecyclerView.Adapter<ApplicationAdapter.ViewHolder> {

    private List<Application> list;
    private Context context;

    public ApplicationAdapter(Context context, ArrayList<Application> list) {
        this.context = context;
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvApplicantName, tvApplicantEmail, tvAppliedAt, tvStatus;
        Button btnInReview, btnAccepted, btnRejected, btnViewCv, btnViewProfile;

        public ViewHolder(View v) {
            super(v);
            tvApplicantName = v.findViewById(R.id.tvApplicantName);
            tvApplicantEmail = v.findViewById(R.id.tvApplicantEmail);
            tvAppliedAt = v.findViewById(R.id.tvAppliedAt);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnInReview = v.findViewById(R.id.btnMarkInReview);
            btnAccepted = v.findViewById(R.id.btnMarkAccepted);
            btnRejected = v.findViewById(R.id.btnMarkRejected);
            btnViewCv = v.findViewById(R.id.btnViewCv);
            btnViewProfile = v.findViewById(R.id.btnViewProfile);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_application, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Application a = list.get(pos);

        h.tvApplicantName.setText(a.getUserName());
        h.tvApplicantEmail.setText(a.getUserEmail());
        h.tvAppliedAt.setText("Applied at: " + a.getAppliedAt());
        h.tvStatus.setText("Status: " + a.getStatus());

        // View applicant profile
        if (h.btnViewProfile != null) {
            h.btnViewProfile.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("type", "User");
                intent.putExtra("user_id", a.getUserId());
                context.startActivity(intent);
            });
        }

        // Allow company to tap the applicant name to view full profile
        h.tvApplicantName.setOnClickListener(v -> {
            if (a.getUserId() <= 0) {
                Toast.makeText(context, "User profile not available", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(context, ProfileActivity.class);
            intent.putExtra("type", "User");
            intent.putExtra("user_id", a.getUserId());
            context.startActivity(intent);
        });

        View.OnClickListener listener = v -> {
            String newStatus;
            if (v == h.btnInReview) newStatus = "in_review";
            else if (v == h.btnAccepted) newStatus = "accepted";
            else newStatus = "rejected";

            updateStatus(a, newStatus, h);
        };

        h.btnInReview.setOnClickListener(listener);
        h.btnAccepted.setOnClickListener(listener);
        h.btnRejected.setOnClickListener(listener);

        if (a.getCvUrl() != null && !a.getCvUrl().isEmpty()) {
            h.btnViewCv.setVisibility(View.VISIBLE);
            h.btnViewCv.setOnClickListener(v -> {
                String fullUrl = HomeActivity.BASE_URL + a.getCvUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(fullUrl), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                try {
                    context.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(context, "No app found to open PDF files", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            h.btnViewCv.setVisibility(View.GONE);
        }
    }

    private void updateStatus(Application a, String newStatus, ViewHolder h) {
        String url = HomeActivity.BASE_URL + "update_application_status.php";

        StringRequest req = new StringRequest(
                Request.Method.POST,
                url,
                response -> {
                    if (response.contains("success")) {
                        a.setStatus(newStatus);
                        h.tvStatus.setText("Status: " + newStatus);
                        Toast.makeText(context, "Status updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Could not update status", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Network error", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<>();
                map.put("application_id", String.valueOf(a.getApplicationId()));
                map.put("status", newStatus);
                return map;
            }
        };

        Volley.newRequestQueue(context).add(req);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
