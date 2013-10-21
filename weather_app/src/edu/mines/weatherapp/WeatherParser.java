package edu.mines.weatherapp;

//import java.lang.String;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSReader;
import java.lang.Exception;

class WeatherParser {

    public static RSSFeed getRSSFeed() {
        RSSReader reader = new RSSReader();
        String uri = "http://weather.yahooapis.com/forecastrss?w=2411762";
        try {
            RSSFeed retfeed = reader.load(uri);
            return retfeed;
        } catch (Exception e) {
            System.out.println(e.getStackTrace());
            return null;
        }
    }
}
