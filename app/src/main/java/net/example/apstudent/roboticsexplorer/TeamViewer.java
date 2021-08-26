package net.example.apstudent.roboticsexplorer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import net.example.apstudent.roboticsexplorer.tbarequest.TBAJsonArrayRequest;
import net.example.apstudent.roboticsexplorer.tbarequest.TBAJsonObjectRequest;
import net.example.apstudent.roboticsexplorer.util.TeamData;
import net.example.apstudent.roboticsexplorer.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TeamViewer extends AppCompatActivity {
    private static final String TAG = "TeamViewer";
    private String teamKey;
    private String eventKey;

    private List<String> awardList;
    private TeamData info;

    private String baseURL;
    private String apiKey;

    private RequestQueue rq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_viewer);

        // Initializing data storage
        awardList = new ArrayList<>();
        info = new TeamData();

        // Getting "constants"
        baseURL = getString(R.string.tba_url);
        apiKey = getString(R.string.tba_key);

        // Creating & starting queue
        rq = Volley.newRequestQueue(this);
        rq.start();

        // Getting variables from intent
        Bundle intentVars = getIntent().getExtras();
        teamKey = intentVars.getString("TEAM_KEY");
        eventKey = intentVars.getString("EVENT_KEY");

        // Logging variables
        Log.d(TAG, String.format("onCreate:\nteamKey: %s\neventKey: %s", teamKey, eventKey));

        // Check for connectivity
        if(!Utils.hasConnection(this))
            Toast.makeText(this, getText(R.string.no_connection_disclaimer), Toast.LENGTH_LONG).show();

        // Getting values
        TBAJsonArrayRequest awardsRequest = new TBAJsonArrayRequest(
                baseURL + "/team/" + teamKey + "/event/" + eventKey + "/awards",

                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject award = response.getJSONObject(i);
                            awardList.add(award.getString("name"));
                        } catch (JSONException e) {
                            Log.e(TAG, "onCreate: Failed to parse json; error parsing json", e);
                            Toast.makeText(this, getString(R.string.json_request_failed), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                error -> {
                    Toast.makeText(this, getString(R.string.json_request_failed), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCreate: Failed to fetch resource awards", error);
                },

                apiKey
        );

        TBAJsonObjectRequest statusRequest = new TBAJsonObjectRequest(
                baseURL + "/team/" + teamKey + "/event/" + eventKey + "/status",
                response -> {
                    try {
                        TextView status = findViewById(R.id.label_status);
                        TextView qualWins = findViewById(R.id.label_qual_wins);
                        TextView qualLosses = findViewById(R.id.label_qual_losses);
                        TextView qualTies = findViewById(R.id.label_qual_ties);
                        TextView rank = findViewById(R.id.label_rank);
                        TextView dq = findViewById(R.id.label_dq);


                        info.setStatus(cleanStatus(response.getString("overall_status_str")));
                        status.setText(info.getStatus());

                        info.setElimStatus(response.getString("playoff_status_str"));

                        try {
                            JSONObject playoffData = response.getJSONObject("playoff");

                            info.setElimWins(playoffData.getJSONObject("record").getString("wins"));
                            info.setElimLosses(playoffData.getJSONObject("record").getString("losses"));
                            info.setElimTies(playoffData.getJSONObject("record").getString("ties"));
                        } catch (JSONException e) {
                            // This will fail if the team didn't make the playoffs
                            info.setElimWins("0");
                            info.setElimLosses("0");
                            info.setElimTies("0");
                        }

                        JSONObject qualData = response.getJSONObject("qual").getJSONObject("ranking");
                        // Data doesn't depend on other data, so the gui is populated as requests complete
                        info.setRank(qualData.getString("rank"));
                        rank.setText(info.getRank());
                        info.setQualDq(qualData.getString("dq"));
                        dq.setText(info.getQualDq());

                        try {
                            JSONObject qualRecord = qualData.getJSONObject("record");
                            info.setQualWins(qualRecord.getString("wins"));
                            info.setQualLosses(qualRecord.getString("losses"));
                            info.setQualTies(qualRecord.getString("ties"));
                        } catch (JSONException e) {
                            // I don't know why this would fail but it might
                            info.setQualWins(getString(R.string.label_nodata_number));
                            info.setQualLosses(getString(R.string.label_nodata_number));
                            info.setQualTies(getString(R.string.label_nodata_number));
                        }
                        qualWins.setText(info.getQualWins());
                        qualLosses.setText(info.getQualLosses());
                        qualTies.setText(info.getQualTies());
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: Failed to parse json; error parsing json", e);
                        Toast.makeText(this, getString(R.string.json_request_failed), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, getString(R.string.json_request_failed), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCreate: Failed to fetch resource event status", error);
                },
                apiKey
        );

        TBAJsonObjectRequest generalInfoRequest = new TBAJsonObjectRequest(
                baseURL + "/team/" + teamKey,
                response -> {
                    try {
                        TextView teamNumber = findViewById(R.id.label_team_number);
                        info.setTeamName(response.getString("nickname"));
                        info.setTeamNumber(response.getString("team_number"));

                        teamNumber.setText(info.getTeamNumber());
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: Failed to parse json; error parsing json", e);
                        Toast.makeText(this, getString(R.string.json_request_failed), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(this, getString(R.string.json_request_failed), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "onCreate: Failed to fetch resource team info", error);
                },
                apiKey
            );

        Toast.makeText(this, getString(R.string.json_getting_teaminfo), Toast.LENGTH_SHORT).show();

        // Requests don't depend on the data from other requests in this view, so each request can be sent at once because the order they come back in doesn't matter
        rq.add(generalInfoRequest);
        rq.add(statusRequest);
        rq.add(awardsRequest);
    }

    /** I still refuse to learn regex
     * (This class exists because the TBA status comes back with html formatting for whatever reason) **/
    private String cleanStatus(String status) {
        return status.replace("<b>", "").replace("</b>", "");
    }

    // Called when the "view on tba" button is clicked
    public void viewOnTBA(View v) {
        Uri url = Uri.parse(getString(R.string.tba_consumer_url) + "/event/" + eventKey);
        Intent i = new Intent(Intent.ACTION_VIEW, url);

    // ensure that a web browser exists by checking if anything can handle the intent
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.browser_not_found), Toast.LENGTH_LONG).show();
            // Copies the value to the clipboard if not
            ClipboardManager clippy = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clippy.setPrimaryClip(ClipData.newPlainText(getString(R.string.label_view_on_tba), url.toString()));
        }
    }

    // Sends the event key back because EventViewer needs it to fetch data again
    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: back pressed");
        Intent i = new Intent();
        i.putExtra("EVENT_KEY", eventKey);
        setResult(Activity.RESULT_OK, i);
        super.onBackPressed();
    }
}