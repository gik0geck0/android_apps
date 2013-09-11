package org.buland.quidditch_scoring;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.util.Log;
import android.util.Pair;

import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.buland.quidditch_scoring.StatisticsDBHelper;

/**
 *  The summary screen shows the results of a previously ended game
 */
public class SummaryScreen extends Activity {

    // Date format to display on-screen dates with
    SimpleDateFormat date_formatter = new SimpleDateFormat("MMMM d, yyyy kk:mm", Locale.US);

    // Game ID to lookup in the database.
    int gameId;

    SQLiteDatabase statsDb;

    // Views that depict the game output
    TextView aScore;
    TextView bScore;

    TextView aSnitch;
    TextView bSnitch;

    TextView aWinner;
    TextView bWinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.summary_screen);

        // Date View bindings
        final TextView gameDateView = (TextView) findViewById(R.id.game_date);

        Intent startIntent = getIntent();
        this.gameId = startIntent.getIntExtra("GAMEID", -1);

        // Get the database, and lookup the date of the game
        this.statsDb = (new StatisticsDBHelper(this)).getWritableDatabase();
        Date gameDate = new Date(StatisticsDBHelper.getGameDate(this.statsDb, this.gameId));
        gameDateView.setText(this.date_formatter.format(gameDate));

        //final long gameDate = startIntent.getLongExtra("GAMEDATE", -1);
        if (gameId == -1) {
            // Yes, this is annoying, but it'll make an error VERY obvious
            Toast.makeText(this, "GameID not valid", 400).show();
            Log.e("ScoringScreen", "GAME ID IS NOT VALID!!!!!");
            Log.e("ScoringScreen", "GAME ID IS NOT VALID!!!!!");
            Log.e("ScoringScreen", "GAME ID IS NOT VALID!!!!!");
            Log.e("ScoringScreen", "GAME ID IS NOT VALID!!!!!");
            Log.e("ScoringScreen", "GAME ID IS NOT VALID!!!!!");
        }

        // Score View Bindings
        this.aScore = (TextView) findViewById(R.id.a_score);
        this.bScore = (TextView) findViewById(R.id.b_score);

        // Snitch caught Views
        this.aSnitch = (TextView) findViewById(R.id.a_snitch);
        this.bSnitch = (TextView) findViewById(R.id.b_snitch);

        // Winning Team Views
        this.aWinner = (TextView) findViewById(R.id.a_winner);
        this.bWinner = (TextView) findViewById(R.id.b_winner);

        // Fetch the current scores from the database.
        updateScores();
    }

    /**
     * Update the on-screen score views to show the same values as stored in the database
     */
    private void updateScores() {
        // Get the score from the database. x=a, y=b
        Pair<Integer, Integer> score = StatisticsDBHelper.check_score(this.statsDb, this.gameId);

        // Update the score values
        this.aScore.setText(""+score.first);
        this.bScore.setText(""+score.second);

        // Check who caught the snitch, and a bit of on-screen wtf-ness
        int snitchCaught = StatisticsDBHelper.checkSnitchCaught(this.statsDb, this.gameId);
        if (snitchCaught < 0) {
            // A caught the snitch
            this.aSnitch.setVisibility(View.VISIBLE);
            if (snitchCaught < -1) {
                this.aSnitch.setText("Caught the snitch! " + -snitchCaught + " times?");
            }
        } else if (snitchCaught > 0) {
            // B caught the snitch
            this.bSnitch.setVisibility(View.VISIBLE);
            if (snitchCaught > 1) {
                this.bSnitch.setText("Caught the snitch! " + snitchCaught + " times?");
            }
        }

        // Update the view to show the winner
        if (score.first > score.second) {
            // A wins
            this.aWinner.setVisibility(View.VISIBLE);
        } else if (score.first < score.second) {
            // B wins
            this.bWinner.setVisibility(View.VISIBLE);
        } else {
            // Tie. Settled by the snitch catcher
            if (snitchCaught < 0) {
                this.aWinner.setVisibility(View.VISIBLE);
            } else {
                this.bWinner.setVisibility(View.VISIBLE);
            }
        }
    }
}
