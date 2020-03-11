package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;

import static com.example.timetogo.MainActivity.weatherValue;

public class Weather extends AppCompatActivity {

    public void getWeatherAPI(double curLat, double curLng) {
        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat="+curLat+"&lon="+curLng+"&appid=214ca5ff55f7bc7e5e085dafb21c0789&mode=xml&lang=kr&units=metric");

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            System.out.println("파싱 시작합니다");

            while(parserEvent != XmlPullParser.END_DOCUMENT) {
                switch(parserEvent) {
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("weather")) {
                            weatherValue = parser.getAttributeValue(null, "value");
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
