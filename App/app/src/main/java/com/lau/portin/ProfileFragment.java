package com.lau.portin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;

public class ProfileFragment extends Fragment {

    ImageView imgPhoto;
    TextView tvTitle, tvName, tvEmail, tvCreated, tvExtra1, tvExtra2, tvExtra3;

    String BASE_URL = "http://10.0.2.2/portin/uploads/";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_profile, container, false);

        imgPhoto = view.findViewById(R.id.imgProfile);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvCreated = view.findViewById(R.id.tvCreated);
        tvExtra1 = view.findViewById(R.id.tvExtra1);
        tvExtra2 = view.findViewById(R.id.tvExtra2);
        tvExtra3 = view.findViewById(R.id.tvExtra3);

        Bundle bundle = getArguments();
        if (bundle == null) return view;

        String type = bundle.getString("type");

        if ("User".equals(type)) {
            User u = (User) bundle.getSerializable("user");
            loadUser(u);
        } else {
            Company c = (Company) bundle.getSerializable("company");
            loadCompany(c);
        }
        ImageView btnEdit = view.findViewById(R.id.btnEdit);
        btnEdit.setVisibility(View.VISIBLE);

        btnEdit.setOnClickListener(v -> {
            EditProfileFragment fragment = new EditProfileFragment();
            fragment.setArguments(getArguments()); // pass user/company data

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

    private void loadUser(User u) {
        tvTitle.setText("User Profile");

        String fullName = u.getFirst_name()
                + " "
                + (u.getMiddle_name() == null ? "" : u.getMiddle_name() + " ")
                + u.getLast_name();

        tvName.setText(fullName.trim());

        tvEmail.setText("Email: " + u.getEmail());
        tvCreated.setText("Joined: " + u.getCreated_at());

        tvExtra1.setText("Birth Year: " + u.getBirth_year());
        String transcript = u.getTranscript();
        if (transcript == null || transcript.isEmpty() || transcript.equals("null")) transcript = "Not Available";
        tvExtra2.setText("");
        tvExtra3.setText("");

        loadImage(u.getPhoto());
    }

    private void loadCompany(Company c) {
        tvTitle.setText("Company Profile");

        tvName.setText(c.getName());
        tvEmail.setText("Email: " + c.getEmail());
        tvCreated.setText("Created: " + c.getCreated_at());

        tvExtra1.setText("Rating: " + c.getRating());
        tvExtra2.setText("Description: " + c.getDescription());
        tvExtra3.setText("");

        loadImage(c.getPhoto());
    }

    private void loadImage(String photo) {
        if (photo == null || photo.isEmpty()) {
            imgPhoto.setImageResource(R.drawable.ic_user);
            return;
        }

        // Clean if using full URL
        if (photo.contains("http://10.0.2.2/portin/uploads/")) {
            photo = photo.replace("http://10.0.2.2/portin/uploads/", "");
        }

        Glide.with(requireContext())
                .load(BASE_URL + photo)
                .placeholder(R.drawable.ic_user)
                .into(imgPhoto);
    }
}
