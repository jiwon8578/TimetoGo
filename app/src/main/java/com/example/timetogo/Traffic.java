package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;

public class Traffic extends AppCompatActivity {

    public int speed = 0;
    public boolean green = false;
    public boolean yellow = false;
    public boolean red = false;

    public void categorize() {
        speed = Bus.averageSpd;

        if (speed < 40) {
            red = true;
        } else if (speed <= 80) {
            yellow = true;
        } else {
            green = true;
        }
    }
}
