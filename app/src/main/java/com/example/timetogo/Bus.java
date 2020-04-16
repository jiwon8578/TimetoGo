package com.example.timetogo;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.net.URL;
import java.util.ArrayList;

public class Bus {
    public ArrayList<String> busRouteList; //노선Id들의 리스트
    public ArrayList<String> stationList; //정류소Id들의 리스트
    public ArrayList<String> stationNmList; //정류소 이름들의 리스트
    public ArrayList<String> stationNoList; //정류소 번호들의 리스트
    public ArrayList<String> seqList; //정류소 순번들의 리스트
    public ArrayList<String> xList; //gpsX의 리스트
    public ArrayList<String> yList; //gpsY의 리스트
    public String text = "";

    public String destStn;
    public ArrayList<String> travelTimeList; //시간

    public int count = 0;
    public int totalTime = 0;
    static public int averageSpd = 0;

    public String lat1 = "";
    public String lat2 = "";
    public String lng1 = "";
    public String lng2 = "";

    public String showBusList(String bus, String station, String destStn) {
        busRouteList = new ArrayList<String>();
        stationList = new ArrayList<String>();
        stationNmList = new ArrayList<String>();
        stationNoList = new ArrayList<String>();
        seqList = new ArrayList<String>();
        text = "";
        travelTimeList = new ArrayList<String>();
        xList = new ArrayList<String>();
        yList = new ArrayList<String>();

        this.destStn = destStn;
        getBusRouteList(bus);
        getStationsByRouteList(busRouteList.get(0));
        for (int i = 0; i < stationNoList.size(); i++) {
            if (stationNoList.get(i).equals(station)) {
                getBusAPI(stationList.get(i), busRouteList.get(0), seqList.get(i));
            }
        }

        getTravelTime(station,destStn);
        return text;
    }

    //버스 속도 알아내는 함수
    public void getTravelTime(String station, String destStn) {
        boolean isStart = false;
        String time = null;
        int intTime = 0;

        count = 0;
        totalTime = 0;

        for(int i = 0; i < travelTimeList.size(); i++) {
            if(stationNoList.get(i).equals(station)) {
                isStart = true;
                lat1 = yList.get(i);
                lng1 = xList.get(i);
            }
            if(isStart) {
                time = travelTimeList.get(i);
                intTime = Integer.parseInt(time);
                totalTime += intTime;
                count++;
            }
            if(stationNoList.get(i).equals(destStn)) {
                isStart = false;
                lat2 = yList.get(i);
                lng2 = xList.get(i);
            }
        }

        if(count != 0) {
            averageSpd = totalTime / count;
        }
    }

    //getLowArrInfoByStIdList
    public void getBusAPI(String stId, String busRouteId, String ord) {
        boolean in_rtNm = false, in_arrmsg1 = false, in_arrmsg2 = false;
        String rtNm = null, arrmsg1 = null, arrmsg2 = null;

        try {
            URL url = new URL("http://ws.bus.go.kr/api/rest/arrive/getLowArrInfoByRoute?serviceKey=4Sc5MPbhBeWyQLVQ1JM9AqGWarV75Qk9hG%2FLuWVNl7%2B4hyHn8nP0mGrsxqDJHcgRdrSYT7sjblUpHpqjDcXdbw%3D%3D&stId=" + stId + "&busRouteId=" + busRouteId + "&ord=" + ord);
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();

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
                            text = "버스번호: " + rtNm + "\n" + "첫번째 전: " + arrmsg1 + "\n" + "두번째 전: " + arrmsg2;
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //busRouteId를 넣으면 stationId를 리턴한다.
    public void getStationsByRouteList(String busRouteId) {
        boolean in_station = false, in_stationNm = false, in_stationNo = false, in_seq = false, in_sectSpd = false, in_gpsX = false, in_gpsY = false;
        String station = null, stationNm = null, stationNo = null, seq = null, sectSpd = null, gpsX = null, gpsY = null;

        try {
            URL url = new URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getStaionByRoute?serviceKey=4Sc5MPbhBeWyQLVQ1JM9AqGWarV75Qk9hG%2FLuWVNl7%2B4hyHn8nP0mGrsxqDJHcgRdrSYT7sjblUpHpqjDcXdbw%3D%3D&busRouteId="+busRouteId);
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();

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
                        if(parser.getName().equals("sectSpd")) {
                            in_sectSpd = true;
                        }
                        if(parser.getName().equals("gpsX")) {
                            in_gpsX = true;
                        }
                        if(parser.getName().equals("gpsY")) {
                            in_gpsY = true;
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
                        if(in_sectSpd) {
                            sectSpd = parser.getText();
                            in_sectSpd = false;
                        }
                        if(in_gpsX) {
                            gpsX = parser.getText();
                            in_gpsX = false;
                        }
                        if(in_gpsY) {
                            gpsY = parser.getText();
                            in_gpsY = false;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if(parser.getName().equals("itemList")) {
                            stationList.add(station);
                            stationNmList.add(stationNm);
                            stationNoList.add(stationNo);
                            seqList.add(seq);
                            travelTimeList.add(sectSpd);
                            xList.add(gpsX);
                            yList.add(gpsY);
                        }
                        break;
                }
                parserEvent = parser.next();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //버스 번호를 입력하면 busRouteId를 리턴한다
    public void getBusRouteList(String busRouteNm) {
        boolean in_busRouteId = false;
        String busRouteId = null;

        try {
            URL url = new URL("http://ws.bus.go.kr/api/rest/busRouteInfo/getBusRouteList?serviceKey=4Sc5MPbhBeWyQLVQ1JM9AqGWarV75Qk9hG%2FLuWVNl7%2B4hyHn8nP0mGrsxqDJHcgRdrSYT7sjblUpHpqjDcXdbw%3D%3D&strSrch="+busRouteNm);
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();
            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();

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
}
