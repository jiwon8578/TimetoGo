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
    public String text = "";

    public void showBusList(String bus, String station) {
        busRouteList = new ArrayList<String>();
        stationList = new ArrayList<String>();
        stationNmList = new ArrayList<String>();
        stationNoList = new ArrayList<String>();
        seqList = new ArrayList<String>();
        text = "";

        getBusRouteList(bus);
        getStationsByRouteList(busRouteList.get(0));
        for (int i = 0; i < stationNoList.size(); i++) {
            if (stationNoList.get(i).equals(station)) {
                getBusAPI(stationList.get(i), busRouteList.get(0), seqList.get(i));
            }
        }

        MainActivity.items.add(text);
        MainActivity.adapter.notifyDataSetChanged();
    }

    public void getBusAPI(String stId, String busRouteId, String ord) { //getLowArrInfoByStIdList

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
                            text = "버스번호: " + rtNm + "첫번째 전: " + arrmsg1 + "두번째 전" + arrmsg2+"\n";
                        }
                        break;
                }
                parserEvent = parser.next();
            }

        } catch(Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
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
