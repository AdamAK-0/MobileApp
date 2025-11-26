package com.lau.portin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class InternshipAdapter extends RecyclerView.Adapter<InternshipAdapter.ViewHolder> {

    List<Internship> list;
    private ArrayList<Internship> originalList;
    static String type;
    private User currentUser;

    public InternshipAdapter(ArrayList<Internship> list, String type, User currentUser) {
        this.list = list;
        this.originalList = new ArrayList<>(list);
        InternshipAdapter.type = type;
        this.currentUser = currentUser;
    }

    public void filter(String text) {
        list.clear();
        if (text.isEmpty()) {
            list.addAll(originalList);
        } else {
            text = text.toLowerCase();
            for (Internship i : originalList) {
                if (i.getCompanyName().toLowerCase().contains(text) ||
                        i.getName().toLowerCase().contains(text) ||
                        i.getType().toLowerCase().contains(text)) {
                    list.add(i);
                }
            }
        }
        notifyDataSetChanged();
    }

    // Legacy constructor (not used in HomeActivity, but kept for compatibility)
    public InternshipAdapter(List<Internship> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCompany, tvType, tvApplicationStatus;
        ImageView img, btnEdit, btnDelete;
        Button btnApply;

        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvInternName);
            tvCompany = v.findViewById(R.id.tvInternCompany);
            tvType = v.findViewById(R.id.tvInternType);
            tvApplicationStatus = v.findViewById(R.id.tvApplicationStatus);
            img = v.findViewById(R.id.imgInternship);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
            btnApply = v.findViewById(R.id.btnApply);

            if(type.equals("Company")) {
                // Company can edit/delete, no Apply
                btnEdit.setVisibility(View.VISIBLE);
                btnDelete.setVisibility(View.VISIBLE);
                btnApply.setVisibility(View.GONE);
            }
            else {
                // User can Apply, no edit/delete
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnApply.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_internship, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int pos) {
        Internship i = list.get(pos);
        h.tvName.setText(i.name);
        h.tvCompany.setText(i.companyName);
        h.tvType.setText(i.type);
        Glide.with(h.itemView.getContext())
                .load(i.getPhoto())
                .placeholder(R.drawable.ic_image)
                .into(h.img);

        // Delete click
        h.btnDelete.setOnClickListener(v -> {
            deleteInternship(i.getId(), pos, h.itemView.getContext());
        });

        // Edit click
        h.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(h.itemView.getContext(), AddInternshipActivity.class);
            intent.putExtra("edit_mode", true);
            intent.putExtra("internship", i); // Internship implements Serializable
            h.itemView.getContext().startActivity(intent);
        });

        // Apply click (for User only)
        if ("User".equals(type) && h.btnApply.getVisibility() == View.VISIBLE) {
            h.btnApply.setEnabled(true);
            h.btnApply.setText("Apply");

            h.btnApply.setOnClickListener(v -> {
                if (currentUser == null) {
                    Toast.makeText(h.itemView.getContext(),
                            "User not loaded. Please log in again.",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                applyToInternship(i.getId(), currentUser.getUser_id(), h);
            });
        }

        // Always refresh current application status for this user (if logged in)
        if ("User".equals(type) && currentUser != null) {
            loadApplicationStatus(i.getId(), currentUser.getUser_id(), h);
        }

        if ("Company".equals(type)) {
            h.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(h.itemView.getContext(), CompanyApplicationsActivity.class);
                intent.putExtra("internship", i);
                h.itemView.getContext().startActivity(intent);
            });
        } else {
            // For regular users, show the internship details dialog
            h.itemView.setOnClickListener(v -> {
                InternshipDetailsDialog dialog = new InternshipDetailsDialog(i, currentUser, type);
                dialog.show(((FragmentActivity) h.itemView.getContext()).getSupportFragmentManager(), "internship_dialog");
            });
        }
    }


    private void loadApplicationStatus(int internshipId, int userId, ViewHolder h) {
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
                            h.btnApply.setText(prettifyStatusShort(statusCode));

                            if (h.tvApplicationStatus != null) {
                                h.tvApplicationStatus.setVisibility(View.VISIBLE);
                                h.tvApplicationStatus.setText("Status: " + prettifyStatus(statusCode));
                            }
                        } else {
                            // Not applied yet
                            h.btnApply.setEnabled(true);
                            h.btnApply.setText("Apply");
                            if (h.tvApplicationStatus != null) {
                                h.tvApplicationStatus.setVisibility(View.GONE);
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

    private void deleteInternship(int internshipId, int position, Context context) {
        String url = AddInternshipActivity.BASE_URL + "delete_internship.php?internship_id=" + internshipId;

        StringRequest req = new StringRequest(Request.Method.GET, url,
                response -> {
                    if (response.contains("success")) {
                        list.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
        );

        Volley.newRequestQueue(context).add(req);
    }

    private void applyToInternship(int internshipId, int userId, ViewHolder h) {
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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
