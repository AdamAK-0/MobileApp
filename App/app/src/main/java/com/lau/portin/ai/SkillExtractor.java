package com.lau.portin.ai;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.*;
import com.lau.portin.BuildConfig;

public class SkillExtractor {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String API_KEY = BuildConfig.API_KEY;
    // Replace
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    public interface SkillCallback {
        void onResult(String result);
    }

    /**
     * Extracts skills from raw text using the LLaMA model.
     */
    public void extractSkills(String text, SkillCallback callback) {

        try {
            JSONObject json = new JSONObject();
            json.put("model", "llama-3.3-70b-versatile");

            JSONArray messages = new JSONArray();

            JSONObject systemMsg = new JSONObject();
            systemMsg.put("role", "system");
            systemMsg.put("content",
                    "You are an AI that extracts SKILLS ONLY from any text. "
                            + "Return the final answer as a JSON array of strings.\n"
                            + "Example: [\"Java\", \"OOP\", \"Machine Learning\"]\n"
                            + "Do NOT add explanations. ONLY return the array!"
            );

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", text);

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
                    callback.onResult("ERROR: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    try {
                        JSONObject root = new JSONObject(res);
                        JSONArray choices = root.getJSONArray("choices");
                        JSONObject msg = choices.getJSONObject(0).getJSONObject("message");
                        String content = msg.getString("content");

                        callback.onResult(content);

                    } catch (Exception ex) {
                        callback.onResult("FAILED TO PARSE SKILLS");
                    }
                }
            });

        } catch (Exception e) {
            callback.onResult("ERROR BUILDING REQUEST");
        }
    }
}
