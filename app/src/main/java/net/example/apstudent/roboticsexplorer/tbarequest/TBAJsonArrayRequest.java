/* Child class of JsonArrayRequest to make it easier to make requests to TBA for JSON Arrays.
 *     Copyright (C) 2021  Nathan Jankowski
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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
