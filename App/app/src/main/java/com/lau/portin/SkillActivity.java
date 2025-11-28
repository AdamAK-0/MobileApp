package com.lau.portin;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lau.portin.ai.SkillExtractor;

import org.json.JSONArray;

import java.io.InputStream;
import java.util.ArrayList;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

public class SkillActivity extends BaseActivity {

    Button btnUpload, btnExtract;
    TableLayout table;
    ArrayList<String> pdfTexts = new ArrayList<>();

    SkillExtractor extractor = new SkillExtractor();
    ArrayList<JSONArray> extractedArray = new ArrayList<>();
    //static BottomNavigationView bottomNav;


    private static final int PICK_PDF = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_skill);
        setActivityLayout(R.layout.activity_skill);


        btnUpload = findViewById(R.id.btnUpload);
        btnExtract = findViewById(R.id.btnExtract);
        table = findViewById(R.id.tableSkills);

        btnUpload.setOnClickListener(v -> pickPdf());
        btnExtract.setOnClickListener(v -> extractSkillsPerPDF());
        PDFBoxResourceLoader.init(getApplicationContext());
        //bottomNav = findViewById(R.id.bottomNav);
        //bottomNav.getMenu().findItem(R.id.nav_skills).setChecked(true);
        /*bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                //Intent i = new Intent(SkillActivity.this, HomeActivity.class);
                //i.putExtra("type", "User");
                //i.putExtra("user", currentUser);
                //startActivity(i);
                finish();
                return true;
            }
            if (id == R.id.nav_skills) {
                //Already here
                return true;
            }

            return false;
        });*/
    }

    private void pickPdf() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("application/pdf");
        i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(i, PICK_PDF);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_PDF && res == RESULT_OK && data != null) {

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

                Toast.makeText(this, "PDFs loaded!", Toast.LENGTH_SHORT).show();
                btnExtract.setEnabled(true);

            } catch (Exception e) {
                Toast.makeText(this, "Failed to read PDF", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String readPdf(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        com.tom_roush.pdfbox.pdmodel.PDDocument doc =
                com.tom_roush.pdfbox.pdmodel.PDDocument.load(is);
        String text = new com.tom_roush.pdfbox.text.PDFTextStripper().getText(doc);
        doc.close();
        return text;
    }

    private void extractSkills() {
        String combined = "";
        for (String t : pdfTexts) combined += t + "\n\n";

        Toast.makeText(this, "Extracting skills…", Toast.LENGTH_LONG).show();

        extractor.extractSkills(combined, result -> runOnUiThread(() -> {
            try {
                JSONArray arr = new JSONArray(result);
                showSkills(arr);
            } catch (Exception e) {
                Toast.makeText(this, "Invalid AI format!", Toast.LENGTH_LONG).show();
            }
        }));
    }

    private void extractSkillsPerPDF() {

        Toast.makeText(this, "Extracting skills…", Toast.LENGTH_LONG).show();
        for (String t : pdfTexts) {
            extractor.extractSkills(t, result -> runOnUiThread(() -> {
                try {
                    JSONArray arr = new JSONArray(result);
                    extractedArray.add(arr);
                    JSONArray combined = new JSONArray();
                    for (int i = 0; i < extractedArray.size(); i++)
                        for (int j = 0; j < extractedArray.get(i).length(); j++)
                            combined.put(extractedArray.get(i).optString(j));
                    showSkills(combined);
                } catch (Exception e) {
                    Toast.makeText(this, "Invalid AI format!", Toast.LENGTH_LONG).show();
                }
            }));
        }
    }
    private void showSkills(JSONArray skills) {
        table.removeAllViews();

        for (int i = 0; i < skills.length(); i++) {
            TableRow row = new TableRow(this);

            TextView tv = new TextView(this);
            tv.setText("• " + skills.optString(i));
            tv.setPadding(10, 10, 10, 10);
            tv.setTextSize(18);

            row.addView(tv);
            table.addView(row);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNav.getMenu().findItem(R.id.nav_skills).setChecked(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //HomeActivity.bottomNav.getMenu().findItem(R.id.nav_home).setChecked(true);
    }
}
