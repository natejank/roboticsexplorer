/* A menu to dynamically select an FRC event.  Each event is sorted by year and district to make
 * selection easier.
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

package net.example.apstudent.roboticsexplorer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.example.apstudent.roboticsexplorer.tbarequest.TBAJsonArrayRequest;
import net.example.apstudent.roboticsexplorer.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private String eventKey = "2020mabri"; // Event key as is used in TBA's api
    private String district = "New England";  // District; pretty name
    private String districtKey = "2020ne";

    private String apiKey;
    private String baseURL;

    // Components used in the activity
    private RequestQueue rq;
    private Spinner yearSpinner;
    private Spinner districtSpinner;
    private Spinner eventSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get "constants" from resource file
        baseURL = getString(R.string.tba_url);
        apiKey = getString(R.string.tba_key);

        // Initialize views
        yearSpinner = findViewById(R.id.yearSpinner);
        districtSpinner = findViewById(R.id.districtSpinner);
        eventSpinner = findViewById(R.id.eventSpinner);

        // Initialize the request handler and begin processing things in the queue
        rq = Volley.newRequestQueue(this);
        rq.start();

        // Check for connectivity
        if(!Utils.hasConnection(this))
            Toast.makeText(this, getText(R.string.no_connection_disclaimer), Toast.LENGTH_LONG).show();

        // Initialize the first spinner
        initYearSpinner();
    }

    private void initYearSpinner() {
        List<String> years = new ArrayList<>();
        IntStream.range(1992, Year.now().getValue() + 1) // Counting from 0 doesn't make sense here
                .forEach(value -> years.add(0, String.valueOf(value)));
                // By prepending instead of appending, the current year is default


        yearSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                years
        ));

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initDistrictSpinner(years.get(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        }
        );
    }

    private void initDistrictSpinner(String year) {
        List<String> districtKeys = new ArrayList<>();
        List<String> districts = new ArrayList<>();

        Log.d(TAG, String.format("initDistrictSpinner: Got year %s", year));

        Toast.makeText(this, getString(R.string.json_getting_events) + year, Toast.LENGTH_SHORT).show();

        // https://developer.android.com/training/volley/simple
        TBAJsonArrayRequest request = new TBAJsonArrayRequest(
                // url to request
                baseURL + "/events/" + year + "/simple",
                // Runs on response
                response -> {
                    // Parse the json object
                    Log.d(TAG, String.format("initDistrictSpinner: Got content for events in %s", year));

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            // Contains a JSONObject
                            String district;
                            String districtKey;
                            JSONObject o = response.getJSONObject(i);
                            if (o.get("district") == JSONObject.NULL) {
                                district = getString(R.string.label_no_district);
                                districtKey = "none";
                            } else {
                                district = o.getJSONObject("district").getString("display_name");
                                districtKey = o.getJSONObject("district").getString("key");
                            }


                            if (!districts.contains(district))
                                districts.add(district);
                            if (!districtKeys.contains(districtKey))
                                districtKeys.add(districtKey);
                        } catch (JSONException e) {
                            Toast.makeText(this, getText(R.string.json_request_failed), Toast.LENGTH_LONG).show();
                            Log.e(TAG, "initDistrictSpinner: Failed to iterate through response!", e);
                            break;
                        }
                    };
                    districtSpinner.setAdapter(new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_dropdown_item,
                            districts));
                    districtSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            district = districts.get(position);
                            districtKey = districtKeys.get(position);
                            initEventSpinner(response);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                },
                // Runs on error
                error -> {
                    Toast.makeText(this, getString(R.string.json_request_failed), Toast.LENGTH_SHORT).show();
                    Log.e("Request failed", "Request failed with exception.", error);
                },
                // Api key to be passed in as a part of the request header
                apiKey);

        // Add request to queue to be processed in the future
        rq.add(request);
    }

    public void initEventSpinner(JSONArray response) {
        List<String> events = new ArrayList<>();
        List<String> eventKeys = new ArrayList<>();

        Log.d(TAG, String.format("initEventSpinner: Got district %s", district));

        for (int i = 0; i < response.length(); i++) {
            try {
                String iterEvent;
                JSONObject o = response.getJSONObject(i);
                if (o.get("district") != JSONObject.NULL) {
                    iterEvent = o.getJSONObject("district").getString("display_name");
                } else {
                    iterEvent = getString(R.string.label_no_district);
                }
                if (district.equals(iterEvent)) {
                    events.add(o.getString("name"));
                    eventKeys.add(o.getString("key"));
                }
            } catch (JSONException e) {
                Log.e(TAG, "initEventSpinner: Failed to iterate through response!", e);
                Toast.makeText(this, getText(R.string.json_request_failed), Toast.LENGTH_LONG).show();
            }
        }

        eventSpinner.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                events));
        eventSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                eventKey = eventKeys.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        Log.d(TAG, "initEventSpinner: Read events successfully");
    }

    public void submit(View v) {
        Intent i = new Intent(this, EventViewer.class);
        i.putExtra("EVENT_KEY", eventKey);
        startActivity(i);
    }
}
