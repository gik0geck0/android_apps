package edu.mines.weatherapp;

//import java.lang.String;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;

class WeatherParser {

    public static RSSFeed getRSSFeed(String url) {
        RSSReader reader = new RSSReader();
        String uri = "http://weather.yahooapis.com/forecastrss?w=2411762";
        return reader.load(uri);
    }
}
