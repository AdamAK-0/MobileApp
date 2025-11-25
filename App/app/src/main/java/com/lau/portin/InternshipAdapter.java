package com.lau.portin;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class InternshipAdapter extends RecyclerView.Adapter<InternshipAdapter.ViewHolder> {

    List<Internship> list;
    private ArrayList<Internship> originalList;

    public InternshipAdapter(ArrayList<Internship> list) {
        this.list = list;
        this.originalList = new ArrayList<>(list);
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


    public InternshipAdapter(List<Internship> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCompany, tvType;
        ImageView img, btnEdit, btnDelete;


        public ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvInternName);
            tvCompany = v.findViewById(R.id.tvInternCompany);
            tvType = v.findViewById(R.id.tvInternType);
            img = v.findViewById(R.id.imgInternship);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnDelete = v.findViewById(R.id.btnDelete);
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
            intent.putExtra("internship", i); // make Internship Parcelable
            h.itemView.getContext().startActivity(intent);
        });
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

    @Override
    public int getItemCount() {
        return list.size();
    }
}
