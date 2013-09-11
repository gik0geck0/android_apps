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
 * The scoring screen provides an active place to view and record scores for either team A, or team B.
 *
 *  @author: Matt Buland
 */
public class ScoringScreen extends Activity implements View.OnClickListener {

    SimpleDateFormat date_formatter = new SimpleDateFormat("MMMM d, yyyy kk:mm", Locale.US);

    // Game ID to update
    int gameId;

    SQLiteDatabase statsDb;

    // Live team's scores
    TextView aScore;
    TextView bScore;

    // Buttons for scoring actions
    Button aQuaff;
    Button bQuaff;
    Button aSnitch;
    Button bSnitch;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.score_screen);

        // Date View bindings
        final TextView gameDateView = (TextView) findViewById(R.id.game_date);

        Intent startIntent = getIntent();
        this.gameId = startIntent.getIntExtra("GAMEID", -1);

        // Get the database, and find the Date the game was created (this is sort-of a unique identifier for the game)
        this.statsDb = (new StatisticsDBHelper(this)).getWritableDatabase();
        Date gameDate = new Date(StatisticsDBHelper.getGameDate(this.statsDb, this.gameId));
        gameDateView.setText(this.date_formatter.format(gameDate));

        //final long gameDate = startIntent.getLongExtra("GAMEDATE", -1);
        if (gameId == -1) {
            // Yes, this is annoying, but it'll make an error VERY obvious. In testing, I have never seen this toast/error message. But ya never know
            Toast.makeText(this, "GameID not valid", 400).show();
            Log.e("QuidditchScoring:ScoringScreen", "GAME ID IS NOT VALID!!!!!");
        }

        // Score View Bindings
        this.aScore = (TextView) findViewById(R.id.a_score);
        this.bScore = (TextView) findViewById(R.id.b_score);

        // Button View Bindings
        this.aQuaff = (Button) findViewById(R.id.a_quaffle);
        this.bQuaff = (Button) findViewById(R.id.b_quaffle);

        this.aSnitch = (Button) findViewById(R.id.a_snitch);
        this.bSnitch = (Button) findViewById(R.id.b_snitch);

        // Bind buttons to this Activity's onClick method
        aQuaff.setOnClickListener(this);
        bQuaff.setOnClickListener(this);
        aSnitch.setOnClickListener(this);
        bSnitch.setOnClickListener(this);

        // Fetch the current scores from the database.
        updateScores();
    }

    /**
     * <p>
     * When one of the 4 scoring buttons are clicked on, add something to the score,
     * append it to the database, then update the score shown.
     * </p>
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a_quaffle:
                StatisticsDBHelper.insert_score(this.statsDb, new Date(), 0, 10, false, this.gameId);
                break;
            case R.id.b_quaffle:
                StatisticsDBHelper.insert_score(this.statsDb, new Date(), 1, 10, false, this.gameId);
                break;
            case R.id.a_snitch:
                StatisticsDBHelper.insert_score(this.statsDb, new Date(), 0, 150, true, this.gameId);
                break;
            case R.id.b_snitch:
                StatisticsDBHelper.insert_score(this.statsDb, new Date(), 1, 150, true, this.gameId);
                break;
        }

        // Update onscreen views
        updateScores();
    }

    /**
     * Update the on-screen score views to show the same values as stored in the database
     */
    private void updateScores() {
        // Get the score from the database. first=Team A, second=Team B
        Pair<Integer, Integer> score = StatisticsDBHelper.check_score(this.statsDb, this.gameId);

        this.aScore.setText(""+score.first);
        this.bScore.setText(""+score.second);

        // Since someone just scored, we should check to see if the game is over yet.
        checkEndGame();
    }

    /**
     * <p>
     * Check to see if the game is over. If it is, prevent more scoring from happening,
     * and start the statistics activity for game-review. This should be ran any time
     * the score is updated, or when starting a game incorrectly (going to scoring instead
     * of the statistics for an old game).
     * </p>
     */
    private void checkEndGame() {
        int snitchCaught = StatisticsDBHelper.checkSnitchCaught(this.statsDb, this.gameId);

        // Check if the snitch was caught at all
        if (snitchCaught != 0) {
            // Disable scoring buttons, since no scoring should take place once a game has finished
            this.aQuaff.setClickable(false);
            this.bQuaff.setClickable(false);
            this.aSnitch.setClickable(false);
            this.bSnitch.setClickable(false);

            /* TODO: Potential problem:
             *
             * If the next intent doesn't work properly, or the user
             * returns to this activity, those buttons will be unclickable,
             * which can be confusing, specifically confused with being broken.
             *
             */

            // Start the SummaryScreen for this ended game
            Intent scoringIntent = new Intent(this, SummaryScreen.class);
            scoringIntent.putExtra("GAMEID", this.gameId);
            this.startActivity(scoringIntent);
        }
    }
}
