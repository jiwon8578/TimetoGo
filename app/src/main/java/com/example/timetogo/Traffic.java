package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;

public class Traffic extends AppCompatActivity {
    public int speed = 0;
    public boolean green = false;
    public boolean yellow = false;
    public boolean red = false;

    //교통상황 카테고리로 나누기
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
