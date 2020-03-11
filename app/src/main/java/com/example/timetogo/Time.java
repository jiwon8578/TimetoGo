package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;

public class Time extends AppCompatActivity {

    double curLat;
    double curLng;

    boolean rainSnow;
    boolean sunny;
    boolean green;
    boolean yellow;
    boolean red;

    public int early = 0;

    public Time(double curLat, double curLng) {
        this.curLat = curLat;
        this.curLng = curLng;
    }

    public void determineTime() {
        Weather weather = new Weather();
        weather.getWeatherAPI(this.curLat, this.curLng);
        weather.categorize();

        Traffic traffic = new Traffic();
        traffic.getTrafficAPI();
        traffic.categorize();

        this.rainSnow = weather.rainSnow;
        this.sunny = weather.sunny;
        this.green = traffic.green;
        this.yellow = traffic.yellow;
        this.red = traffic.red;

        if (sunny && green) {
            early = 0;
        } else if (sunny && yellow) {
            early = 1;
        } else if (sunny && red) {
            early = 2;
        } else if (rainSnow && green) {
            early = 1;
        } else if (rainSnow && yellow) {
            early = 2;
        } else if (rainSnow && red) {
            early = 3;
        }
    }
}
