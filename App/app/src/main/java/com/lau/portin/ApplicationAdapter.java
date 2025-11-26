package com.lau.portin;

import android.content.Context;
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
        Button btnInReview, btnAccepted, btnRejected;

        public ViewHolder(View v) {
            super(v);
            tvApplicantName = v.findViewById(R.id.tvApplicantName);
            tvApplicantEmail = v.findViewById(R.id.tvApplicantEmail);
            tvAppliedAt = v.findViewById(R.id.tvAppliedAt);
            tvStatus = v.findViewById(R.id.tvStatus);
            btnInReview = v.findViewById(R.id.btnMarkInReview);
            btnAccepted = v.findViewById(R.id.btnMarkAccepted);
            btnRejected = v.findViewById(R.id.btnMarkRejected);
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
