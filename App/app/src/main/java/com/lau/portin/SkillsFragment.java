package com.lau.portin;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SkillsFragment extends Fragment {

    Button btnUpload, btnExtract, btnTranscript;
    TableLayout table;
    ArrayList<String> pdfTexts = new ArrayList<>();
    ArrayList<JSONArray> extractedArray = new ArrayList<>();
    SkillExtractor extractor = new SkillExtractor();

    private static final int PICK_PDF = 1001;
    private static final int PICK_TRANSCRIPT = 2001;
    String transcriptRaw = null;
    boolean isTranscriptLoaded = false;
    boolean isPdfLoaded = false;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_skill, container, false);

        btnUpload = view.findViewById(R.id.btnUpload);
        btnExtract = view.findViewById(R.id.btnExtract);
        btnTranscript = view.findViewById(R.id.btnTranscript);

        table = view.findViewById(R.id.tableSkills);

        btnUpload.setOnClickListener(v -> pickPdf());
        btnExtract.setOnClickListener(v -> extractSkillsPerPDF());
        btnTranscript.setOnClickListener(v -> pickTranscript());

        PDFBoxResourceLoader.init(requireContext());
        btnExtract.setEnabled(false);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_TRANSCRIPT && resultCode == getActivity().RESULT_OK && data != null) {
            try {
                Uri uri = data.getData();
                transcriptRaw = readTxt(uri);
                Toast.makeText(getContext(), "Transcript loaded!", Toast.LENGTH_SHORT).show();
                isTranscriptLoaded = true;
                if (isPdfLoaded)
                    btnExtract.setEnabled(true);
            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to read transcript!", Toast.LENGTH_SHORT).show();
            }
        }
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
                isPdfLoaded = true;
                if (isTranscriptLoaded)
                    btnExtract.setEnabled(true);

            } catch (Exception e) {
                Toast.makeText(getContext(), "Failed to read PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void pickTranscript() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("text/plain");
        startActivityForResult(i, PICK_TRANSCRIPT);
    }


    private String readTxt(Uri uri) throws Exception {
        InputStream is = requireContext().getContentResolver().openInputStream(uri);
        if (is == null) return "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder builder = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line).append("\n");
        }

        reader.close();
        is.close();

        return builder.toString();
    }



    private void pickPdf() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/pdf");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(i, PICK_PDF);
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
        List<CourseGrade> trans = parseTranscript(transcriptRaw);
        Toast.makeText(getContext(), trans.size() + " courses found!", Toast.LENGTH_LONG).show();
        Log.d("SkillsFragment", "First Course: " + trans.get(0).code);
        for (String t : pdfTexts) {
            String processedText = t;
            int index = processedText.toLowerCase().indexOf("student code of conduct");
            if (index != -1) {
                processedText = processedText.substring(0, index).trim();
                Log.d("SkillsFragment", "Processed: " + processedText);
            }
            extractor.extractSkills(processedText, result -> requireActivity().runOnUiThread(() -> {
                try {
                    String cleanResult = result.trim();
                    Log.d("SkillsFragment", "Extracted: " + cleanResult);

                    // Remove triple backticks if present
                    if (cleanResult.startsWith("```") && cleanResult.endsWith("```")) {
                        cleanResult = cleanResult.substring(3, cleanResult.length() - 3).trim();
                    }

                    // Remove surrounding quotes if present
                    if (cleanResult.startsWith("\"") && cleanResult.endsWith("\"")) {
                        cleanResult = cleanResult.substring(1, cleanResult.length() - 1).replace("\\\"", "\"");
                    }
                    Log.d("SkillsFragment", "JSON: " + cleanResult);
                    JSONObject obj = new JSONObject(cleanResult);


                    //JSONObject obj = new JSONObject(result);
                    JSONArray skillsArr = obj.getJSONArray("skills");
                    String title = obj.getString("course_name");
                    String code = obj.getString("course_code");
                    Log.d("SkillsFragment", "course_name: " + title + ", course_code: " + code);
                    for (CourseGrade c : trans) {
                        if (c.code.toLowerCase().equals(code.toLowerCase().replace(" ","")) || c.code.toLowerCase().equals(code.toLowerCase()) || c.title.toLowerCase().equals(title.toLowerCase())) {
                            for(int i = 0; i < skillsArr.length(); i++) {
                                skillsArr.put(i, skillsArr.optString(i) + " (" + gradeToPercent(c.grade) + "%)");
                                Log.d("SkillsFragment", "Grade: " + c.grade);
                            }
                            break;
                        }
                    }
                    extractedArray.add(skillsArr);

                    JSONArray combined = new JSONArray();
                    for (int i = 0; i < extractedArray.size(); i++)
                        for (int j = 0; j < extractedArray.get(i).length(); j++)
                            combined.put(extractedArray.get(i).optString(j));

                    showSkills(combined);

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Invalid AI format!", Toast.LENGTH_LONG).show();
                    Log.e("SkillsFragment", "Invalid AI format!", e);
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
    class CourseGrade {
        String code;     // CSC 243
        String title;    // Algorithms & Data Structures
        String grade;    // A-, B+, C...
        double percent;  // 0–100 mapped
    }
    private ArrayList<CourseGrade> parseTranscript(String raw) {
        ArrayList<CourseGrade> list = new ArrayList<>();

        String[] lines = raw.split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.matches("^[A-Z]{2,4}\\s*\\d{3}[A-Z]?(\\s|\\t).*")) continue;

            CourseGrade c = new CourseGrade();

            String[] parts = line.split("\\s+");

            // Detect course code
            if (parts.length >= 2 && parts[1].matches("\\d{3}[A-Z]?")) {
                c.code = parts[0] + parts[1];

                int titleIndex = 2;
                if (parts.length > 2 && parts[2].equalsIgnoreCase("UG")) {
                    titleIndex = 3;
                }

                // Try to detect grade at end
                String last = parts[parts.length - 1];
                if (last.matches("^(A|B|C|D|F|T)[+-]?$")) {
                    c.grade = last;
                    // title excludes the grade
                    StringBuilder title = new StringBuilder();
                    for (int j = titleIndex; j < parts.length - 1; j++) {
                        title.append(parts[j]).append(" ");
                    }
                    c.title = title.toString().trim();
                } else {
                    // No grade found, fallback to T
                    c.grade = "T";
                    StringBuilder title = new StringBuilder();
                    for (int j = titleIndex; j < parts.length; j++) {
                        title.append(parts[j]).append(" ");
                    }
                    c.title = title.toString().trim();
                }

            } else {
                // Code + title fallback
                c.code = parts[0];
                c.title = parts.length > 1 ? parts[1] : "";
                c.grade = "T";
            }
        Log.d("SkillsFragment", "Course: " + c.code + ", " + c.title + ", " + c.grade);
            c.percent = gradeToPercent(c.grade);
            list.add(c);
        }

        return list;
    }

    private double gradeToPercent(String g) {
        g = g.trim().toUpperCase();
        switch (g) {
            case "A+":
            case "A":
                return 100;
            case "A-": return 95;
            case "B+": return 88;
            case "B": return 82;
            case "B-": return 78;
            case "C+": return 75;
            case "C": return 70;
            case "C-": return 65;
            case "D+": return 60;
            case "D": return 55;
            default: return 20; // F or unknown
        }
    }

}
