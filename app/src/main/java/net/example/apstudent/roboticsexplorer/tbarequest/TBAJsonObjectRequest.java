package net.example.apstudent.roboticsexplorer.tbarequest;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *This is a basic child class of JsonArrayRequest.  The main function of it is to modify the
 * headers so that a request can be more easily made to TBA.
 */
public class TBAJsonObjectRequest extends JsonObjectRequest {
    String apiKey;


    public TBAJsonObjectRequest(String url, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, String apiKey) {
        this(url, null, listener, errorListener, apiKey);
    }
    public TBAJsonObjectRequest(int method, String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, String apiKey) {
        super(method, url, jsonRequest, listener, errorListener);
        this.apiKey = apiKey;
    }

    public TBAJsonObjectRequest(String url, @Nullable JSONObject jsonRequest, Response.Listener<JSONObject> listener, @Nullable Response.ErrorListener errorListener, String apiKey) {
        super(url, jsonRequest, listener, errorListener);
        this.apiKey = apiKey;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        super.getHeaders();

        Map<String, String> params = new HashMap<String, String>();
        params.put("User-Agent", "Robotics Explorer");
        params.put("X-TBA-Auth-Key", apiKey);
        return params;
    }
}
