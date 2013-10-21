package edu.mines.weatherapp;

import android.app.Activity;
import android.os.Bundle;
import org.mcsoxford.rss.RSSFeed;
import edu.mines.weatherapp.WeatherParser;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        RSSFeed feed = WeatherParser.getRSSFeed();
    }
}
