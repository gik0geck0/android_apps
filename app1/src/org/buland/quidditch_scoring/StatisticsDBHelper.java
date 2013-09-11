package org.buland.quidditch_scoring;

import android.content.Context;
import android.content.ContentValues;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

import android.util.Log;
import android.util.Pair;
import android.widget.ListView;

import java.util.Date;
import java.util.ArrayList;

/**
 *  <h1>SQLite Database Adapter (helper as Google/Android calls it)</h1>
 *
 *  Offers many static functions that can be used to update or view game-statistics in the database
 *  The API follows the general rule of first aquiring the database via 'getWritable/ReadableDatabase',
 *  then using the static functions defined in this class to interact with the database.
 *
 *  @author: Matt Buland
 */
public class StatisticsDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "QuidditchStatistics";
    private static final int DB_VERSION = 1;

    public StatisticsDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * For an SQLiteOpenHelper, the onCreate method is called if and only if
     * the database-name in question does not already exist. Theoretically,
     * this should only happen once ever, and after the one time, updates
     * will be applied for schema updates.
     */
    @Override
    public void onCreate(SQLiteDatabase database) {
        //Log.i("StatisticsDBHelper", "Creating the game database!");

        /*
         * Create the game table. Stores a unique game ID, and the date the game
         * was started (stored as a 64-bit long representing ms since EPOCH)
         */
        database.execSQL("CREATE TABLE IF NOT EXISTS game ( " +
                "game_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "creation_datetime INTEGER )");

        /*
         * Create the scoring table. Each score record has the amount of points 
         * the scoring event added to the scoring team, which game the score was
         * for, the team that made the score (could be expanded to a table including
         * team names, and etc), whether or not the snitch was caught in the score,
         * and the datetime the score happened at (could be used later for
         * scoring-analytics). The datetime is a 64-bit integer representing the
         * number of ms since EPOCH.
         */
        database.execSQL("CREATE TABLE IF NOT EXISTS score ( " +
                "score_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "score_datetime INTEGER, " +
                "team_id INTEGER, " +   // team_id is a number identifying the team. In this first revision, it will be 0 or 1 for left and right
                "amount INTEGER, " +
                "snitch INTEGER, " +
                "game_id INTEGER, " +
                "FOREIGN KEY(game_id) REFERENCES game(game_id) )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        // Do nothing.
    }

    /**
     *  Record a scoring even in the database.
     *
     *  @param database         Database to record the scoring event in
     *  @param score_datetime   Date/Time that the event happened at
     *  @param scoring_team     0 = Team A; 1 = Team B.
     *  @param points           Number of points the score was worth
     *  @param snitch_caught    0 = snitch was not caught in this event, 1 = snitch WAS caught in this event
     *  @param game_id          Game ID that the event happened for
     *
     *  @return ROWID for the new row
     */
    public static long insert_score(SQLiteDatabase database, Date score_datetime, int scoring_team, int points, boolean snitch_caught, int game_id) {
        // Make a values list
        ContentValues vals = new ContentValues();

        // Bind the parts of the list to their values
        vals.put("score_datetime", score_datetime.getTime());
        vals.put("team_id", scoring_team);
        vals.put("amount", points);
        vals.put("snitch", snitch_caught);
        vals.put("game_id", game_id);

        // Insert the operation into the database, and return the ROWID of the affected row
        return database.insert("score", null, vals);
    }

    /**
     *  Return a pair of integers showing the final score of a game
     *
     *  @param database Database to look in
     *  @param game_id  Game ID to lookup
     *
     *  @return Pair of integers showing the score for Team A and Team B, in that order.
     */
    public static Pair<Integer, Integer> check_score(SQLiteDatabase database, int game_id) {
        // Do the query, and get a result set (accessible via a cursor)
        Cursor c = database.rawQuery("SELECT SUM(CASE score.team_id WHEN 0 THEN score.amount ELSE 0 END) AS team_a, SUM(CASE score.team_id WHEN 1 THEN score.amount ELSE 0 END) AS team_b FROM score WHERE score.game_id = ?", new String[] {""+game_id});

        // Select the first one. (Expecting 1)
        c.moveToFirst();
        int a_score = c.getInt(c.getColumnIndex("team_a"));
        int b_score = c.getInt(c.getColumnIndex("team_b"));
        c.close();

        // Make a pair for the scores
        return new Pair<Integer, Integer>(a_score, b_score);
    }

    /**
     *  Returns a list of games that have been played
     *  
     *  @param database Database to examine
     *
     *  @return List of Integer/Long pairs that represent game IDs and game start dates. The Long represents the number of ms since EPOCH
     */
    public static ArrayList<Pair<Integer, Long>> get_games(SQLiteDatabase database) {
        // do the query
        Cursor c = database.rawQuery("SELECT game.creation_datetime, game.game_id FROM game ORDER BY game.creation_datetime", new String[] {});

        // Create a list containing all the ID-Datetime pairs
        ArrayList<Pair<Integer, Long>> games = new ArrayList<Pair<Integer, Long>>();
        int game_id_column = c.getColumnIndex("game_id");
        int date_column = c.getColumnIndex("creation_datetime");
        if (c.moveToFirst()) {
            while ( !c.isAfterLast() ) {
                games.add(new Pair<Integer, Long>(c.getInt(game_id_column), c.getLong(date_column)));
                c.moveToNext();
            }
        } else {
            Log.e("QuidditchScoring:StatisticsDBHelper", "Move to first will fail. Cursor count: " + c.getCount());
        }

        return games;
    }

    /**
     *  Make a new game in the database, then return a Pair representing the game
     *
     *  @param database Database to add a game to
     *
     *  @return Integer/Long pair representing the new Game ID and Start datetime of the game
     */
    public static Pair<Integer, Long> newGame(SQLiteDatabase database) {
        ContentValues gamevals = new ContentValues();
        gamevals.put("creation_datetime", (new Date()).getTime());

        // Grab the ROWID of the newly added row
        long rowid = database.insert("game", null, gamevals);

        // Get the extended information around that new row (ID and Date)
        Cursor c = database.rawQuery("SELECT game.creation_datetime, game.game_id FROM game WHERE game.ROWID = ?", new String[] { ""+rowid });
        c.moveToFirst();

        // Make a new pair representing the game
        return new Pair<Integer, Long>(c.getInt(c.getColumnIndex("game_id")), c.getLong(c.getColumnIndex("creation_datetime")));
    }

    /**
     *  Check to see if any team has caught the snitch within the supplied game
     *
     *  @param database Target DB
     *  @param game_id  ID for the game in question
     *
     *  @return -1 if Team A has caught the snitch. 1 if Team B has caught the snitch.
     *      Returns 0 if no team has caught the snitch yet. If outside the range [-1,1],
     *      that means one team has caught the snitch more than one times. Under the
     *      game rules, this is disallowed, but the database has no triggers against it.
     */
    public static int checkSnitchCaught(SQLiteDatabase database, int game_id) {
        Cursor c = database.rawQuery("SELECT SUM(CASE score.team_id WHEN 0 THEN score.snitch ELSE 0 END) AS team_a, SUM(CASE score.team_id WHEN 1 THEN score.snitch ELSE 0 END) AS team_b FROM score WHERE score.game_id = ?", new String[] {""+game_id});
        c.moveToFirst();
        int a_snitch = c.getInt(c.getColumnIndex("team_a"));
        int b_snitch = c.getInt(c.getColumnIndex("team_b"));
        c.close();

        if (a_snitch + b_snitch > 1) {
            Log.e("StatisticsDBHelper:checkSnitchCaught", "Team A had " + a_snitch + " snitches, and Team B had " + b_snitch + ". There should only be 1 total.");
        }

        // -1: A caught snitch
        //  0: Snitch not caught
        //  1: B caught snitch
        // Hack/precaution: If the snitch was incorrectly caught, return the number of times the "catcher" caught the snitch. This will be 1+ times, either by A or B, but not both
        int winner = 0;
        if (a_snitch > 0) {
            winner = -a_snitch;
        } else if (b_snitch > 0) {
            winner = b_snitch;
        }

        return winner;
    }

    /**
     *  Lookup only the date the game in question was started
     *
     *  @param database
     *  @param game_id
     *
     *  @return Datetime the game was started in ms since EPOCH
     */
    public static long getGameDate(SQLiteDatabase database, int game_id) {
        Cursor c = database.rawQuery("SELECT game.creation_datetime, game.game_id FROM game WHERE game_id = ?", new String[] {""+game_id});
        c.moveToFirst();
        long gameDate = c.getLong(c.getColumnIndex("creation_datetime"));
        c.close();
        return gameDate;
    }
}
