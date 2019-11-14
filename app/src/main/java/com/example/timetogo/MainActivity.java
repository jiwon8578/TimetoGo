package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.widget.TextView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;
import java.util.ArrayList;

//흐름: 버스 번호를 입력 -> 해당 버스에 대한 노선 id가 나옴 -> 해당 노선에 대한 정류장들에 대한 정류장 정보가 나온다(정류장 id, 정류장 이름, 정류장 번호) -> 정류장 id, 노선 id, 순번을 입력하여 해당 정류장에 도착하는 특정 버스들에 대한 정보를 받아볼 수 있다.
//busRouteNm이 버스 번호, busRouteId가 노선Id, station이 정류소Id

public class MainActivity extends AppCompatActivity {

    public ArrayList<String> busRouteList = new ArrayList<String>(); //노선Id들의 리스트
    public ArrayList<String> stationList = new ArrayList<String>(); //정류소Id들의 리스트
    public ArrayList<String> stationNmList = new ArrayList<String>(); //정류소 이름들의 리스트
    public ArrayList<String> stationNoList = new ArrayList<String>(); //정류소 번호들의 리스트
    public ArrayList<String> seqList = new ArrayList<String>(); //정류소 순번들의 리스트

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.enableDefaults();

        getBusRouteList("152");
        getStationsByRouteList(busRouteList.get(0));
        for(int i = 0; i < stationNoList.size(); i++) {
            if(stationNoList.get(i).equals("02158")) {
                getBusAPI(stationList.get(i), busRouteList.get(0), seqList.get(i));
            }
        }
    }

    public void getBusAPI(String stId, String busRouteId, String ord) { //getLowArrInfoByStIdList
        TextView status1 = (TextView)findViewById(R.id.result); //파싱된 결과확인

        boolean in_rtNm = false, in_arrmsg1 = false, in_arrmsg2 = false;

        String rtNm = null, arrmsg1 = null, arrmsg2 = null;

        try {
            URL url = new URL("http://ws.bus.go.kr/api/rest/arrive/getLowArrInfoByRoute?serviceKey=4Sc5MPbhBeWyQLVQ1JM9AqGWarV75Qk9hG%2FLuWVNl7%2B4hyHn8nP0mGrsxqDJHcgRdrSYT7sjblUpHpqjDcXdbw%3D%3D&stId=" + stId + "&busRouteId=" + busRouteId + "&ord=" + ord);
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            System.out.println("파싱 시작합니다");

            while(parserEvent != XmlPullParser.END_DOCUMENT) {
                switch(parserEvent) {
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("rtNm")) {
                            in_rtNm = true;
                        }
                        if(parser.getName().equals("arrmsg1")) {
                            in_arrmsg1 = true;
                        }
                        if(parser.getName().equals("arrmsg2")) {
                            in_arrmsg2 = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(in_rtNm) {
                            rtNm = parser.getText();
                            in_rtNm = false;
                        }
                        if(in_arrmsg1) {
                            arrmsg1 = parser.getText();
                            in_arrmsg1 = false;
                        }
                        if(in_arrmsg2) {
                            arrmsg2 = parser.getText();
                            in_arrmsg2 = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("itemList")) {
                            status1.setText(status1.getText() + "버스번호: " + rtNm + "첫번째 전: " + arrmsg1 + "두번째 전" + arrmsg2+"\n");
                        }
                        break;
                }
                parserEvent = parser.next();
            }

        } catch(Exception e) {
            status1.setText("에러발생");
        }
    }

    public void getBusRouteList(String busRouteNm) { //버스 번호를 입력하면 busRouteId를 리턴한다

        boolean in_busRouteId = false;

        String busRouteId = null;

        try {
            URL url = new URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList?serviceKey=4Sc5MPbhBeWyQLVQ1JM9AqGWarV75Qk9hG%2FLuWVNl7%2B4hyHn8nP0mGrsxqDJHcgRdrSYT7sjblUpHpqjDcXdbw%3D%3D&strSrch="+busRouteNm);
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            System.out.println("파싱 시작합니다");

            while(parserEvent != XmlPullParser.END_DOCUMENT) {
                switch(parserEvent) {
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("busRouteId")) {
                            in_busRouteId = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(in_busRouteId) {
                            busRouteId = parser.getText();
                            in_busRouteId = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("itemList")) {
                            busRouteList.add(busRouteId);
                        }
                        break;
                }
                parserEvent = parser.next();
            }

        } catch(Exception e) {
            //error
        }
    }

    public void getStationsByRouteList(String busRouteId) { //busRouteId를 넣으면 stationId를 리턴한다.

        boolean in_station = false, in_stationNm = false, in_stationNo = false, in_seq = false;

        String station = null, stationNm = null, stationNo = null, seq = null;

        try {
            URL url = new URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?serviceKey=4Sc5MPbhBeWyQLVQ1JM9AqGWarV75Qk9hG%2FLuWVNl7%2B4hyHn8nP0mGrsxqDJHcgRdrSYT7sjblUpHpqjDcXdbw%3D%3D&busRouteId="+busRouteId);
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            System.out.println("파싱 시작합니다");

            while(parserEvent != XmlPullParser.END_DOCUMENT) {
                switch(parserEvent) {
                    case XmlPullParser.START_TAG:
                        if(parser.getName().equals("station")) {
                            in_station = true;
                        }
                        if(parser.getName().equals("stationNm")) {
                            in_stationNm = true;
                        }
                        if(parser.getName().equals("stationNo")) {
                            in_stationNo = true;
                        }
                        if(parser.getName().equals("seq")) {
                            in_seq = true;
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if(in_station) {
                            station = parser.getText();
                            in_station = false;
                        }
                        if(in_stationNm) {
                            stationNm = parser.getText();
                            in_stationNm = false;
                        }
                        if(in_stationNo) {
                            stationNo = parser.getText();
                            in_stationNo = false;
                        }
                        if(in_seq) {
                            seq = parser.getText();
                            in_seq = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("itemList")) {
                            stationList.add(station);
                            stationNmList.add(stationNm);
                            stationNoList.add(stationNo);
                            seqList.add(seq);
                        }
                        break;
                }
                parserEvent = parser.next();
            }

        } catch(Exception e) {
            //error
        }
    }
}
