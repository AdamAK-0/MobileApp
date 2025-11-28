package com.lau.portin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lau.portin.ai.SkillExtractor;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import org.json.JSONArray;

import java.io.InputStream;
import java.util.ArrayList;

public class SkillsFragment extends Fragment {

    Button btnUpload, btnExtract;
    TableLayout table;
    ArrayList<String> pdfTexts = new ArrayList<>();
    ArrayList<JSONArray> extractedArray = new ArrayList<>();
    SkillExtractor extractor = new SkillExtractor();

    private static final int PICK_PDF = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_skill, container, false);

        btnUpload = view.findViewById(R.id.btnUpload);
        btnExtract = view.findViewById(R.id.btnExtract);
        table = view.findViewById(R.id.tableSkills);

        btnUpload.setOnClickListener(v -> pickPdf());
        btnExtract.setOnClickListener(v -> extractSkillsPerPDF());

        PDFBoxResourceLoader.init(requireContext());
        btnExtract.setEnabled(false);

        return view;
    }

    private void pickPdf() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/pdf");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(i, PICK_PDF);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF && resultCode == getActivity().RESULT_OK && data != null) {
            pdfTexts.clear();

            try {
                if (data.getClipData() != null) {
                    // Multiple PDFs
                    for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                        Uri uri = data.getClipData().getItemAt(i).getUri();
                        pdfTexts.add(readPdf(uri));
                    }
                } else if (data.getData() != null) {
                    // Single PDF
                    Uri uri = data.getData();
                    pdfTexts.add(readPdf(uri));
                }

                Toast.makeText(getContext(), "PDFs loaded!", Toast.LENGTH_SHORT).show();
                btnExtract.setEnabled(true);

            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to read PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String readPdf(Uri uri) throws Exception {
        InputStream is = requireContext().getContentResolver().openInputStream(uri);
        com.tom_roush.pdfbox.pdmodel.PDDocument doc =
                com.tom_roush.pdfbox.pdmodel.PDDocument.load(is);
        String text = new com.tom_roush.pdfbox.text.PDFTextStripper().getText(doc);
        doc.close();
        return text;
    }

    private void extractSkillsPerPDF() {
        Toast.makeText(getContext(), "Extracting skills…", Toast.LENGTH_LONG).show();
        extractedArray.clear();

        for (String t : pdfTexts) {
            extractor.extractSkills(t, result -> requireActivity().runOnUiThread(() -> {
                try {
                    JSONArray arr = new JSONArray(result);
                    extractedArray.add(arr);

                    JSONArray combined = new JSONArray();
                    for (int i = 0; i < extractedArray.size(); i++)
                        for (int j = 0; j < extractedArray.get(i).length(); j++)
                            combined.put(extractedArray.get(i).optString(j));

                    showSkills(combined);

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Invalid AI format!", Toast.LENGTH_LONG).show();
                }
            }));
        }
    }

    private void showSkills(JSONArray skills) {
        table.removeAllViews();

        for (int i = 0; i < skills.length(); i++) {
            TableRow row = new TableRow(getContext());

            TextView tv = new TextView(getContext());
            tv.setText("• " + skills.optString(i));
            tv.setPadding(10, 10, 10, 10);
            tv.setTextSize(18);

            row.addView(tv);
            table.addView(row);
        }
    }
}
