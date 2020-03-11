package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;

public class Weather extends AppCompatActivity {

    public String weatherNumber = null;
    public int intNumber = 0;
    public boolean rainSnow = false;
    public boolean sunny = false;

    public void getWeatherAPI(double curLat, double curLng) {
        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+curLat+"&lon="+curLng+"&appid=214ca5ff55f7bc7e5e085dafb21c0789&mode=xml&lang=kr&units=metric");

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();

            while(parserEvent != XmlPullParser.END_DOCUMENT) {
                switch(parserEvent) {
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("weather")) {
                            weatherNumber = parser.getAttributeValue(null, "number");
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void categorize() {
        intNumber = Integer.parseInt(weatherNumber);
        if (intNumber < 300) { //thunderstorm
            rainSnow = true;
        } else if (intNumber < 500) { //drizzle
            rainSnow = true;
        } else if (intNumber < 600) { //rain
            rainSnow = true;
        } else if (intNumber < 700) { //snow
            rainSnow = true;
        } else if (intNumber < 800) { //atmosphere
            sunny = true;
        } else if (intNumber < 900) { //clear, clouds
            sunny = true;
        }
    }
}
