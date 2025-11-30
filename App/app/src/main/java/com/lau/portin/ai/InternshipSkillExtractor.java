package com.lau.portin.ai;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;
import com.lau.portin.BuildConfig;

public class InternshipSkillExtractor {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    public interface ExtractCallback {
        void onResult(String jsonResult);
    }

    /**
     * Extract ONLY skills from internship description.
     */
    public void extractSkills(String description, ExtractCallback callback) {

        try {
            JSONObject json = new JSONObject();
            json.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");

            systemMsg.put("content",
                    "You extract SKILLS from internship/job descriptions.\n" +
                            "Return ONLY a JSON object in EXACT this format:\n" +
                            "{\n" +
                            "  \"skills\": [\"skill1\", \"skill2\", \"skill3\"]\n" +
                            "}\n" +
                            "Rules:\n" +
                            "- Output ONLY the JSON, nothing else.\n" +
                            "- Skills MUST be short normalized terms.\n" +
                            "- Do NOT invent technologies that are not in the description.\n" +
                            "- Remove duplicates.\n" +
                            "- No explanations, no commentary."
            );

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", description);

            messages.put(systemMsg);
            messages.put(userMsg);

            json.put("messages", messages);

            RequestBody body = RequestBody.create(json.toString(), JSON);

            Request request = new Request.Builder()
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
}
