package org.buland.quidditch_scoring;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.util.Log;
import android.util.Pair;

import android.view.View.OnClickListener;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ListView;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import org.buland.quidditch_scoring.ScoringScreen;

/**
 *  Quidditch Scoring Application
 *  Used to monitor an ongoing quidditch game.
 *
 *  @author Matt Buland
 *  @version 1
 */
public class StartScreen extends Activity {

    StatisticsDBHelper db_helper;
    SimpleDateFormat date_formatter = new SimpleDateFormat("MMMM d, yyyy kk:mm", Locale.US);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        // Bind on views
        final Button newGame = (Button) findViewById(R.id.new_game);
        final ListView gamesList = (ListView) findViewById(R.id.view_games);

        // Get the database helper
        this.db_helper = new StatisticsDBHelper(this);
        //Log.i("StartScreen:CurrentTime", "The Current time in ms = " + (new Date()).getTime());

        // Lookup all the games, and fill the adapter
        ArrayList<Pair<Integer, Long>> games_list = StatisticsDBHelper.get_games(db_helper.getReadableDatabase());
        ArrayList<String> games_display = new ArrayList<String>();

        for (Pair<Integer, Long> p : games_list) {
            games_display.add(p.first + ":" + this.date_formatter.format(new Date(p.second)));
            Log.i("QuidditchScoring:StartScreen:GamesList", "Game ID=" + p.first + ", Game Date=" + p.second);
        }

        // Show the games list in the List View
        final ArrayAdapter<String> gamesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, games_display);
        gamesList.setAdapter(gamesAdapter);

        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new game in the database, and start the scoring activity
                Pair<Integer, Long> created_game = StatisticsDBHelper.newGame(StartScreen.this.db_helper.getWritableDatabase());

                // Use the Pair of the newly created game, and update the games List on screen.
                gamesAdapter.add(created_game.first + ":" + StartScreen.this.date_formatter.format(new Date(created_game.second)));
                Log.i("QuidditchScoring:StartScreen", "Created the game: " + created_game.toString());
                gamesList.setAdapter(gamesAdapter);

                // Trigger the scoring activity to start, and give it the Game ID
                Context actContext = StartScreen.this;
                Intent scoringIntent = new Intent(actContext, ScoringScreen.class);
                scoringIntent.putExtra("GAMEID", created_game.first);
                StartScreen.this.startActivity(scoringIntent);
            }
        });

        gamesList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parentAdapter, View view, int position, long view_id) {
                // A game in the listview was clicked on. Lookup which game it was
                ArrayAdapter<String> viewGamesAdapter = (ArrayAdapter) parentAdapter.getAdapter();
                String item = viewGamesAdapter.getItem(position);

                // Use the string value of item to get the game ID for the respective game
                String game_id = item.split(":")[0];

                // Trigger the Summary Screen to start, giving it the Game ID to show the game summary for
                Context actContext = StartScreen.this;
                Intent summaryIntent = new Intent(actContext, SummaryScreen.class);
                summaryIntent.putExtra("GAMEID", Integer.parseInt(game_id));
                StartScreen.this.startActivity(summaryIntent);
            }
        });
    }
}
