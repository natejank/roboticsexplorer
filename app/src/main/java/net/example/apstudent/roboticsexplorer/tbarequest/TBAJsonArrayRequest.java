package net.example.apstudent.roboticsexplorer.tbarequest;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 *This is a basic child class of JsonArrayRequest.  The main function of it is to modify the
 * headers so that a request can be more easily made to TBA.
 */
public class TBAJsonArrayRequest extends JsonArrayRequest {
    String apiKey;
    public TBAJsonArrayRequest(String url, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener, String apiKey) {
        super(url, listener, errorListener);
        this.apiKey = apiKey;
    }

    public TBAJsonArrayRequest(int method, String url, @Nullable JSONArray jsonRequest, Response.Listener<JSONArray> listener, @Nullable Response.ErrorListener errorListener, String apiKey) {
        super(method, url, jsonRequest, listener, errorListener);
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
