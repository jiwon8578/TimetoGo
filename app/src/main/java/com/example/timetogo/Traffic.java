package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;

public class Traffic extends AppCompatActivity {

    public String speed = null;
    public int intSpeed = 0;
    public boolean green = false;
    public boolean yellow = false;
    public boolean red = false;

    public void getTrafficAPI() {
        boolean in_prcs_spd = false;
        String prcs_spd = null;

        try {
            URL url = new URL("http://openapi.seoul.go.kr:8088/737a6d6e6968796f34375474525268/xml/TrafficInfo/1/3/1220003800");
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();

            while(parserEvent != XmlPullParser.END_DOCUMENT) {
                switch(parserEvent) {
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("prcs_spd")) {
                            in_prcs_spd = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(in_prcs_spd) {
                            prcs_spd = parser.getText();
                            in_prcs_spd = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("row")) {
                            speed = prcs_spd;
                            break;
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void categorize() {
        intSpeed = Integer.parseInt(speed);
        if (intSpeed < 40) {
            red = true;
        } else if (intSpeed <= 80) {
            yellow = true;
        } else {
            green = true;
        }
    }
}
