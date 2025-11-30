package com.lau.portin.ai;

import static com.lau.portin.MainActivity.BASE_URL;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lau.portin.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class InternshipSkillExtractor {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = BuildConfig.API_KEY;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private ArrayList<String> masterSkills = new ArrayList<>();


    /**
     * Extract Callback for AI Skill Extraction
     */
    public interface ExtractCallback {
        void onResult(String jsonResult);
    }

    /**
     * Callback for Database Skill Fetch
     */
    public interface SkillFetchCallback {
        void onSuccess(ArrayList<String> skills);
        void onFailure(Exception e);
    }

    /**
     * Extract ONLY skills from internship description using Groq API.
     */
    public void extractSkills(String description, ExtractCallback callback) {

        try {
            JSONObject json = new JSONObject();
            json.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "You are an assistant that EXTRACTS SKILLS from internship/job descriptions.\n\n" +
                    "You are given a MASTER_SKILLS list (canonical names) below. Your job:\n" +
                    " 1) Detect all skill mentions in the job description.\n" +
                    " 2) For each mention, if it matches (exact, abbreviation, or obvious synonym) any entry in MASTER_SKILLS, return the canonical name from MASTER_SKILLS.\n" +
                    " 3) If a detected skill does NOT match any MASTER_SKILLS entry, report it as NEW so the client can add it to the database.\n\n" +
                    "MASTER_SKILLS = " + masterSkills.toString() + "\n\n" +
                    "OUTPUT FORMAT (ONLY JSON, nothing else):\n" +
                    "{\n" +
                    "  \"normalized\": [\"Canonical Skill 1\", \"Canonical Skill 2\", ...],   // skills that map to DB entries\n" +
                    "  \"new_skills\": [\"New skill string1\", \"New skill string2\", ...]  // detected skill strings NOT in MASTER_SKILLS\n" +
                    "}\n\n" +
                    "RULES:\n" +
                    "- Use canonical forms from MASTER_SKILLS when applicable (e.g. if description says 'SQL', return 'Structured Query Language' if that is in MASTER_SKILLS).\n" +
                    "- new_skills must be raw detected phrases that do not map to MASTER_SKILLS.\n" +
                    "- Do NOT invent skills.\n" +
                    "- Remove duplicates in both arrays.\n" +
                    "- Output only the JSON object above and NOTHING ELSE.");


            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", description);

            messages.put(systemMsg);
            messages.put(userMsg);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + API_KEY)
                    .addHeader("User-Agent", "okhttp")
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onResult("{\"skills\":[]}");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    try {
                        JSONObject root = new JSONObject(res);
                        JSONArray choices = root.getJSONArray("choices");
                        JSONObject msg = choices.getJSONObject(0).getJSONObject("message");
                        String content = msg.getString("content");

                        Log.d("InternshipSkillExtractor", "AI Skills: " + content);
                        callback.onResult(content);

                    } catch (Exception ex) {
                        callback.onResult("{\"skills\":[]}");
                    }
                }
            });

        } catch (Exception e) {
            callback.onResult("{\"skills\":[]}");
        }
    }

    /**
     * Fetch all skills from the database using Volley.
     */
    public void fetchDatabaseSkills(Context context, final SkillFetchCallback callback) {

        String url = BASE_URL + "get_all_skills.php";  // FIX THIS URL

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(response);
                        JSONArray array = obj.getJSONArray("skills");
                        masterSkills.clear();
                        for (int i = 0; i < array.length(); i++) {
                            masterSkills.add(array.getString(i));
                        }


                        callback.onSuccess(masterSkills);

                    } catch (Exception e) {
                        callback.onFailure(e);
                    }
                },
                error -> callback.onFailure(new Exception(error.toString()))
        );

        queue.add(request);
    }

}
