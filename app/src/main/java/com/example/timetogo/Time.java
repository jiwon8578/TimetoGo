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
    public String text = "";

    public Time(double curLat, double curLng) {
        this.curLat = curLat;
        this.curLng = curLng;
    }

    //날씨와 교통상황에 따라 출발 시간 결정하는 함수
    public String determineTime() {
        Weather weather = new Weather();
        weather.getWeatherAPI(this.curLat, this.curLng);
        weather.categorize();

        Traffic traffic = new Traffic();
        traffic.categorize();

        this.rainSnow = weather.rainSnow;
        this.sunny = weather.sunny;
        this.green = traffic.green;
        this.yellow = traffic.yellow;
        this.red = traffic.red;

        if (sunny && green) {
            early = 0;
            text = "날씨: 맑음\n" +
                    "교통상황: 원활\n" +
                    "오늘은 평소와 같은 시간에 출발해주세요.";
        } else if (sunny && yellow) {
            early = 1;
            text = "날씨: 맑음\n" +
                    "교통상황: 서행\n" +
                    "교통상황이 다소 좋지 않으니 첫번째 전 버스를 사용해주세요.";
        } else if (sunny && red) {
            early = 2;
            text = "날씨: 맑음\n" +
                    "교통상황: 정체\n" +
                    "교통상황이 많이 좋지 않으니 두번째 전 버스를 사용해주세요.";
        } else if (rainSnow && green) {
            early = 1;
            text = "날씨: 비/눈\n" +
                    "교통상황: 원활\n" +
                    "날씨로 인해 밀릴 수 있으니 첫번째 전 버스를 사용해주세요.";
        } else if (rainSnow && yellow) {
            early = 2;
            text = "날씨: 비/눈\n" +
                    "교통상황: 서행\n" +
                    "날씨와 교통상황으로 인해 밀릴 수 있으니 두번째 전 버스를 사용해주세요.";
        } else if (rainSnow && red) {
            early = 3;
            text = "날씨: 비/눈\n" +
                    "교통상황: 정체\n" +
                    "날씨와 교통상황으로 인해 많이 밀릴 수 있으니 세번째 버스를 사용해주세요.";
        }
        return text;
    }
}
