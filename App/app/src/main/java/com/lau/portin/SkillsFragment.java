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
import android.util.Pair;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lau.portin.ai.SkillExtractor;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
interface SkillExtractionCallback {
    void onSkillsExtracted(List<JSONArray> extractedSkills);
}
// Callback interface
interface InternshipSkillsCallback {
    void onInternshipsFetched(List<JSONObject> internships);
}

public class SkillsFragment extends Fragment {
    public static final String BASE_URL = "http://10.0.2.2/portin/";

    Button btnUpload, btnExtract, btnTranscript, btntop5;
    TableLayout table;
    ArrayList<String> pdfTexts = new ArrayList<>();
    ArrayList<JSONArray> extractedArray = new ArrayList<>();
    SkillExtractor extractor = new SkillExtractor();

    private static final int PICK_PDF = 1001;
    private static final int PICK_TRANSCRIPT = 2001;
    String transcriptRaw = null;
    boolean isTranscriptLoaded = false;
    boolean isPdfLoaded = false;
    // Inside your fragment or a separate file

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_skill, container, false);

        btnUpload = view.findViewById(R.id.btnUpload);
        btnExtract = view.findViewById(R.id.btnExtract);
        btnTranscript = view.findViewById(R.id.btnTranscript);
        btntop5 = view.findViewById(R.id.btnTop5);

        table = view.findViewById(R.id.tableSkills);

        btnUpload.setOnClickListener(v -> pickPdf());
        btnExtract.setOnClickListener(v -> {
            extractSkillsPerPDF(new SkillExtractionCallback() {
                @Override
                public void onSkillsExtracted(List<JSONArray> extractedSkills) {
                    if (getActivity() instanceof BaseFragmentActivity) {
                        Log.d("SkillsFragment", "onSkillsExtracted called");
                        BaseFragmentActivity main = (BaseFragmentActivity) getActivity();
                        for (JSONArray skillsArr : extractedSkills) {
                            saveExtractedSkillsToServer(main.currentUser.getUser_id(), skillsArr);
                        }
                    }
                }
            });
        });
        btntop5.setOnClickListener(v -> {
            if (getActivity() instanceof BaseFragmentActivity) {
                BaseFragmentActivity main = (BaseFragmentActivity) getActivity();
                fetchUserSkills(main.currentUser.getUser_id(), userSkills -> {
                    fetchInternshipsWithSkills(internships -> {
                        List<JSONObject> top5 = getTopInternshipsBySkills(userSkills, internships, 5);

                        try {
                            // Convert top5 to JSONArray string
                            JSONArray top5Json = new JSONArray();
                            for (JSONObject obj : top5) top5Json.put(obj);

                            Intent intent = new Intent(getContext(), TopInternshipsActivity.class);
                            intent.putExtra("top_internships", top5Json.toString());
                            intent.putExtra("user", main.currentUser);
                            startActivity(intent);

                        } catch (Exception e) {
                            Log.e("TopInternship", "Error passing top internships", e);
                        }
                    });
                });
            }
        });


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

    private void extractSkillsPerPDF(SkillExtractionCallback callback) {
        Toast.makeText(getContext(), "Extracting skills…", Toast.LENGTH_LONG).show();
        extractedArray.clear();

        List<CourseGrade> trans = parseTranscript(transcriptRaw);
        Toast.makeText(getContext(), trans.size() + " courses found!", Toast.LENGTH_LONG).show();
        Log.d("SkillsFragment", "First Course: " + (trans.isEmpty() ? "None" : trans.get(0).code));

        if (pdfTexts.isEmpty()) {
            callback.onSkillsExtracted(new ArrayList<>());
            return;
        }

        final int totalPDFs = pdfTexts.size();
        final int[] processedCount = {0};

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

                    JSONObject obj = new JSONObject(cleanResult);
                    JSONArray skillsArr = obj.getJSONArray("skills");
                    String title = obj.getString("course_name");
                    String code = obj.getString("course_code");
                    Log.d("SkillsFragment", "course_name: " + title + ", course_code: " + code);

                    for (CourseGrade c : trans) {
                        if (c.code.equalsIgnoreCase(code.replace(" ", "")) || c.code.equalsIgnoreCase(code) || c.title.equalsIgnoreCase(title)) {
                            for (int i = 0; i < skillsArr.length(); i++) {
                                skillsArr.put(i, skillsArr.optString(i) + " (" + gradeToPercent(c.grade) + "%)");
                                Log.d("SkillsFragment", "Grade: " + c.grade);
                            }
                            break;
                        }
                    }

                    extractedArray.add(skillsArr);

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Invalid AI format!", Toast.LENGTH_LONG).show();
                    Log.e("SkillsFragment", "Invalid AI format!", e);
                } finally {
                    // Increment processed count and check if all PDFs are done
                    processedCount[0]++;
                    if (processedCount[0] == totalPDFs) {
                        // Combine all skills into one list for display and callback
                        JSONArray combined = new JSONArray();
                        for (int i = 0; i < extractedArray.size(); i++)
                            for (int j = 0; j < extractedArray.get(i).length(); j++)
                                combined.put(extractedArray.get(i).optString(j));

                        showSkills(combined);
                        // Call the callback with a copy of extractedArray
                        callback.onSkillsExtracted(new ArrayList<>(extractedArray));
                    }
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
    private void saveExtractedSkillsToServer(int userId, JSONArray combinedSkills) {
        String url = BASE_URL + "add_user_skill.php";

        for (int i = 0; i < combinedSkills.length(); i++) {
            try {
                String skillWithScore = combinedSkills.getString(i); // e.g., "Java (95%)"

                // Extract skill name and score
                String skillName = skillWithScore;
                double score = 0;
                int idx = skillWithScore.lastIndexOf("(");
                if (idx != -1 && skillWithScore.endsWith("%)")) {
                    skillName = skillWithScore.substring(0, idx).trim();
                    try {
                        String percentStr = skillWithScore.substring(idx + 1, skillWithScore.length() - 2);
                        score = Double.parseDouble(percentStr);
                    } catch (Exception e) {
                        score = 0;
                    }
                }

                double finalScore = score;
                String finalSkillName = skillName;

                StringRequest req = new StringRequest(Request.Method.POST, url,
                        response -> {
                            if (!response.contains("success")) {
                                Log.e("SaveSkill", "Failed to save skill: " + finalSkillName);
                            } else {
                                Log.d("SaveSkill", "Saved skill: " + finalSkillName + " (" + finalScore + "%)");
                            }
                        },
                        error -> Log.e("SaveSkill", "Network error while saving skill: " + finalSkillName)
                ) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> map = new HashMap<>();
                        map.put("user_id", String.valueOf(userId));
                        map.put("skill_name", finalSkillName);
                        map.put("score", String.valueOf(finalScore));
                        return map;
                    }
                };

                Volley.newRequestQueue(requireContext()).add(req);

            } catch (Exception e) {
                Log.e("SaveSkill", "Error reading skill from JSON", e);
            }
        }
    }

    private void fetchUserSkills(int userId, SkillExtractionCallback callback) {
        String url = BASE_URL + "get_user_skills.php?user_id=" + userId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if (obj.optString("status").equals("success")) {
                            JSONArray skillsArray = obj.getJSONArray("skills");
                            List<JSONArray> extractedSkills = new ArrayList<>();

                            for (int i = 0; i < skillsArray.length(); i++) {
                                JSONObject skillObj = skillsArray.getJSONObject(i);
                                String skillName = skillObj.getString("skill_name");
                                double score = skillObj.optDouble("score", 0);

                                // Wrap as JSONArray with "SkillName (score%)"
                                JSONArray skillJsonArray = new JSONArray();
                                skillJsonArray.put(skillName + " (" + score + "%)");
                                extractedSkills.add(skillJsonArray);
                            }
                            Log.d("FetchSkills", "Extracted skills: " + extractedSkills);
                            callback.onSkillsExtracted(extractedSkills);

                        } else {
                            Log.e("FetchSkills", "Failed: " + obj.optString("error"));
                        }
                    } catch (Exception e) {
                        Log.e("FetchSkills", "Parsing error", e);
                    }
                },
                error -> Log.e("FetchSkills", "Network error", error)
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void fetchInternshipsWithSkills(InternshipSkillsCallback callback) {
        String url = BASE_URL + "get_internships_with_skills.php";

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        if(obj.optString("status").equals("success")) {
                            JSONArray internshipsArray = obj.getJSONArray("internships");
                            List<JSONObject> internships = new ArrayList<>();

                            for (int i = 0; i < internshipsArray.length(); i++) {
                                internships.add(internshipsArray.getJSONObject(i));
                            }
                            Log.d("FetchInternships", "Fetched " + internships);
                            callback.onInternshipsFetched(internships);
                        } else {
                            Log.e("FetchInternships", "Failed to fetch internships");
                        }
                    } catch (Exception e) {
                        Log.e("FetchInternships", "Parsing error", e);
                    }
                },
                error -> Log.e("FetchInternships", "Network error", error)
        );

        Volley.newRequestQueue(requireContext()).add(request);
    }
    private List<JSONObject> getTopInternshipsBySkills(
            List<JSONArray> userSkills,
            List<JSONObject> internships,
            int topN
    ) {
        // Map user skills to their scores
        Map<String, Double> userSkillMap = new HashMap<>();
        for (JSONArray skillArr : userSkills) {
            for (int i = 0; i < skillArr.length(); i++) {
                String skillWithScore = skillArr.optString(i); // e.g., "Java (95%)"
                if (skillWithScore.contains("(") && skillWithScore.endsWith("%)")) {
                    int idx = skillWithScore.lastIndexOf("(");
                    String skillName = skillWithScore.substring(0, idx).trim();
                    String scoreStr = skillWithScore.substring(idx + 1, skillWithScore.length() - 2).trim();
                    try {
                        double score = Double.parseDouble(scoreStr);
                        userSkillMap.put(skillName.toLowerCase(), score);
                    } catch (NumberFormatException e) {
                        // ignore invalid score
                    }
                } else {
                    // no score? set default 0
                    userSkillMap.put(skillWithScore.toLowerCase(), 0.0);
                }
            }
        }

        // Compute score for each internship
        List<Pair<JSONObject, Double>> scoredInternships = new ArrayList<>();

        for (JSONObject internship : internships) {
            JSONArray requiredSkills = internship.optJSONArray("skills");
            if (requiredSkills == null || requiredSkills.length() == 0) continue;

            double totalScore = 0;
            int matchedCount = 0;

            for (int i = 0; i < requiredSkills.length(); i++) {
                String skillName = requiredSkills.optString(i).toLowerCase();
                if (userSkillMap.containsKey(skillName)) {
                    totalScore += userSkillMap.get(skillName);
                    matchedCount++;
                }
            }

            if (matchedCount > 0) {
                double avgScore = totalScore / matchedCount;
                scoredInternships.add(new Pair<>(internship, avgScore));
            }
        }

        // Sort internships by descending avgScore
        scoredInternships.sort((a, b) -> Double.compare(b.second, a.second));

        // Take top N
        List<JSONObject> topInternships = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, scoredInternships.size()); i++) {
            topInternships.add(scoredInternships.get(i).first);
        }

        return topInternships;
    }

}
