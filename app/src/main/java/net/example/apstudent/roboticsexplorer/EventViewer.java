/* Shows a summary of every team at a given event.  Links to TeamViewer when a team is selected.
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.example.apstudent.roboticsexplorer.tbarequest.TBAJsonArrayRequest;
import net.example.apstudent.roboticsexplorer.tbarequest.TBAJsonObjectRequest;
import net.example.apstudent.roboticsexplorer.util.TeamAdapter;
import net.example.apstudent.roboticsexplorer.util.TeamData;
import net.example.apstudent.roboticsexplorer.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
public class EventViewer extends AppCompatActivity implements TeamAdapter.ClickListener {

    private static final String TAG = "EventViewer";
    private String eventKey;

    private String apiKey;
    private String baseURL;

    private RecyclerView rv;
    private RequestQueue rq;

    private ArrayList<TeamData> eventData;
    /* TeamData is a Kotlin data class.  I chose to use this instead of something like a HashMap
    because it reduces the likeliness of error, because getting values is no longer prone to typos.
    It also makes it more clear what values are there, without having to write a reference somewhere */

    private volatile boolean requestsFailed;
    /* Variable is volatile because responses are handled in separate threads.  By making the variable
     volatile, each thread will refer back to the original instance rather than a reference from
     the specific thread.  In essence, the variable will be synchronized between threads, and therefore
     if the variable changes, it will be reflected in every reference.

     If this is completely wrong I apologize multitasking is confusing 😅
     https://youtu.be/IQSbIOKhC4g is a good lecture for more about thread safety in java */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_viewer);

        // "Constants"
        baseURL = getString(R.string.tba_url);
        apiKey = getString(R.string.tba_key);

        // Initialize views and objects
        rv = findViewById(R.id.recyclerview_teams);
        rq = Volley.newRequestQueue(this);
        rq.start();  // Allow the queue to start processing requests

        // Data objects
        eventData = new ArrayList<>();
        requestsFailed = false;

        // Get variables from intent
        Bundle intentVars = getIntent().getExtras();
        eventKey = intentVars.getString("EVENT_KEY");


        // Print variables to debug log
        Log.d(TAG, String.format("onCreate: Event Key; %s", eventKey));

        // Check for connectivity
        if (!Utils.hasConnection(this))
            Toast.makeText(this, getText(R.string.no_connection_disclaimer), Toast.LENGTH_LONG).show();

        // Get rankings
        Toast.makeText(this, getText(R.string.json_getting_rankings), Toast.LENGTH_SHORT).show();

        // sorry in advance for the spaghetti
        TBAJsonArrayRequest teamsRequest = new TBAJsonArrayRequest(
                baseURL + "/event/" + eventKey + "/teams",
                response -> {
                    Log.d(TAG, "onCreate: Got teamRequest");

                    try {
                        for (TeamData team : eventData) {
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject teamInfo = response.getJSONObject(i);
                                if (team.getTeamKey().equals(teamInfo.getString("key"))) {
                                    team.setTeamName(teamInfo.getString("nickname"));
                                    team.setTeamNumber(teamInfo.getString("team_number"));
                                    break;
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: Failed to parse json; could not get team names", e);
                        Toast.makeText(this, getString(R.string.json_request_failed_team), Toast.LENGTH_SHORT).show();
                        requestsFailed = true;
                    }
                    // configure recycler view now that we have data
                    Log.d(TAG, "onCreate: Setting TeamAdapter");
                    rv.setAdapter(new TeamAdapter(this, this.eventData, this::onTeamClick)); // Have to wait to set adapters until the list is populated
                },
                error -> {
                    Toast.makeText(this, getString(R.string.json_request_failed_team), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCreate: Request for teams failed with exception", error);
                    requestsFailed = true;
                },
                apiKey
        );

        TBAJsonObjectRequest rankingsRequest = new TBAJsonObjectRequest(
                baseURL + "/event/" + eventKey + "/rankings",

                response -> {
                    try {
                        JSONArray rankings = response.getJSONArray("rankings");
                        if (rankings.length() == 0)
                            throw new RuntimeException("Empty Rankings!");
                        /* This will be handled by a catch clause below.  It probably isn't the best design,
                           but because getJSONArray already needs exception handling, this allows for a singular
                           case where rankings don't exist.  TBA is inconsistent about how not having rankings appears. */

                        for (int i = 0; i < rankings.length(); i++) {
                            JSONObject team = rankings.getJSONObject(i);
                            TeamData teamInfo = new TeamData();
                            teamInfo.setTeamKey(team.getString("team_key"));
                            teamInfo.setRank(team.getString("rank"));
                            try {
                                teamInfo.setQualWins(team.getJSONObject("record").getString("wins"));
                                teamInfo.setQualLosses(team.getJSONObject("record").getString("losses"));
                                teamInfo.setQualTies(team.getJSONObject("record").getString("ties"));
                            } catch (JSONException e) {
                                // If event doesn't have a record
                                teamInfo.setQualWins(getString(R.string.label_nodata_number));
                                teamInfo.setQualLosses(getString(R.string.label_nodata_number));
                                teamInfo.setQualTies(getString(R.string.label_nodata_number));
                            }

                            eventData.add(teamInfo);
                        }
                    } catch (JSONException | RuntimeException e) {
                        TeamData errorData = new TeamData();
                        errorData.setTeamKey("frc0000");
                        errorData.setTeamName(getString(R.string.no_rankings));
                        errorData.setTeamNumber("0000");
                        errorData.setRank("1");
                        errorData.setQualWins(getString(R.string.label_nodata_number));
                        errorData.setQualLosses(getString(R.string.label_nodata_number));
                        errorData.setQualTies(getString(R.string.label_nodata_number));

                        this.eventData.add(errorData);
                        Toast.makeText(this, R.string.no_rankings, Toast.LENGTH_SHORT).show();
                        requestsFailed = true;
                    }

                    rq.add(teamsRequest); // daisy chaining requests is very hacky and probably bad practice
                },
                error -> {
                    Toast.makeText(this, getString(R.string.json_request_failed_team), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCreate: Request for rankings failed with exception", error);
                    requestsFailed = true;

                    TeamData errorData = new TeamData();
                    errorData.setTeamKey("frc0000");
                    errorData.setTeamName(getString(R.string.no_rankings));
                    errorData.setTeamNumber("0000");
                    errorData.setRank("1");
                    errorData.setQualWins(getString(R.string.label_nodata_number));
                    errorData.setQualLosses(getString(R.string.label_nodata_number));
                    errorData.setQualTies(getString(R.string.label_nodata_number));

                    this.eventData.add(errorData);
                    rq.add(teamsRequest);
                },
                apiKey
        );

        rq.add(rankingsRequest);

        rv.setLayoutManager(new LinearLayoutManager(this));
    }

    // Called when an entry is clicked
    @Override
    public void onTeamClick(int position) {
        Intent i = new Intent(this, TeamViewer.class);

        i.putExtra("TEAM_KEY", eventData.get(position).getTeamKey());
        i.putExtra("EVENT_KEY", eventKey);
        if (requestsFailed)
            Toast.makeText(this, getString(R.string.no_data), Toast.LENGTH_SHORT).show();
        else
            startActivity(i);
    }

}