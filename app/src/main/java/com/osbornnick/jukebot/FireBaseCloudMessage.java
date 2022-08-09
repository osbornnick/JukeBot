package com.osbornnick.jukebot;

import android.content.Context;
import android.os.StrictMode;
import android.util.Log;

import androidx.browser.trusted.sharing.ShareTarget;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FireBaseCloudMessage {

    private static String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    private static String SERVER_KEY = "key=AAAAJ8BVt1w:APA91bFe54nc8Q52RBOVUn6Wjw5ZesWd4DNTfC5nY-FuPcn8Wg88u7UeOdMnVCZyLV30_YwXDMuUxjlWb3i3Y0cloX1pltGnmNjrQ8LpWQYYDE1phsmw7iAi-1ZI0E5oYIZnPUS0FvrB";
    private static final String TAG = "FireBaseCloudMessage";

    public static void pushNotification(Context context, String token, String title, String message){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);

        try {
            JSONObject json = new JSONObject();
            json.put("to",token);
            JSONObject notification = new JSONObject();
            notification.put("title",title);
            notification.put("body", message);
            json.put("notification",notification);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(TAG, "onResponse: " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }

             }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("Content-Type","application/json");
                    params.put("Authorization", SERVER_KEY);
                    return params;
                }
            };

            queue.add(jsonObjectRequest);
        } catch (JSONException e){
        e.printStackTrace();
        }
    }
}
