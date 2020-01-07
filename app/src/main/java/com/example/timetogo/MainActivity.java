package com.example.timetogo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

//흐름: 버스 번호를 입력 -> 해당 버스에 대한 노선 id가 나옴 -> 해당 노선에 대한 정류장들에 대한 정류장 정보가 나온다(정류장 id, 정류장 이름, 정류장 번호) -> 정류장 id, 노선 id, 순번을 입력하여 해당 정류장에 도착하는 특정 버스들에 대한 정보를 받아볼 수 있다.
//busRouteNm이 버스 번호, busRouteId가 노선Id, station이 정류소Id

public class MainActivity extends AppCompatActivity {
    static int num = 0;

    static String addr = null, chargeTp = null, city = null;
    Button mRefreshBtn;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private int count = 0;
    public Socket socket;
    public BufferedReader in;
    public PrintWriter out;
    static int nWeek;
    static int hour;
    static int min;
    static String strweek = null;
    String data;
    static public ArrayList<String> weatherList;
   static public ArrayList<String> dayList;
    public ArrayList<String> busRouteList; //노선Id들의 리스트
    public ArrayList<String> stationList; //정류소Id들의 리스트
    public ArrayList<String> stationNmList; //정류소 이름들의 리스트
    public ArrayList<String> stationNoList; //정류소 번호들의 리스트
    public ArrayList<String> seqList; //정류소 순번들의 리스트
    TextView result;
    static public String text = "";

    String[] data_split;
    String bus;
    String station;
    static String bus1;
    static String station1;

    ArrayList<String> items;
    ArrayAdapter<String> adapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.enableDefaults();

        items = new ArrayList<String>();
        items.add("test");
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, items);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);

        Thread worker = new Thread() {
            public void run() {
                try {
                    socket = new Socket("ec2-13-209-36-232.ap-northeast-2.compute.amazonaws.com", 7777);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(
                            socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        worker.start();

        getWeatherAPI();
        getTrafficAPI();

        result = (TextView) findViewById(R.id.result);

        (new Thread(new Runnable() {

            @Override
            public void run() {
                while (true)
                {
                    try {
                        final Calendar cal;

                        cal = Calendar.getInstance();
                        hour = cal.get(Calendar.HOUR_OF_DAY);
                        min = cal.get(Calendar.MINUTE);
                        nWeek = cal.get(Calendar.DAY_OF_WEEK);

                        long time = System.currentTimeMillis();

                        if(nWeek==1){
                            strweek="일요일";
                        }
                        if(nWeek==2){
                            strweek="월요일";
                        }
                        if(nWeek==3){
                            strweek="화요일";
                        }
                        if(nWeek==4){
                            strweek="수요일";

                        }
                        if(nWeek==5){
                            strweek="목요일";
                            alarmTime(23,2,1);
                            alarmTime(23,3,2);
                            alarmTime(23,4,3);
                            alarmTime(23,5,4);
                            alarmTime(23,6,5);
                            alarmTime(23,7,6);
                            alarmTime(23,8, 7);
                        }
                        if(nWeek==6){
                            strweek="금요일";
                        }
                        if(nWeek==7){
                            strweek="토요일";
                        }
                        Thread.sleep(1000);

                        /*Thread.sleep(1000);
                        runOnUiThread(new Runnable() // start actions in UI thread
                        {

                            @Override
                            public void run() {


                            }
                        });*/
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        })).start();

        //socketdo();

    }
    public void alarmTime(int hour1,int min1,int alarm){

        //final Calendar cal;
        //cal = Calendar.getInstance();

        final Calendar cal;

        cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);
        nWeek = cal.get(Calendar.DAY_OF_WEEK);

        if(hour==hour1){
            if(min==min1) {
                while (num < alarm) {
                    socketdo();
                    //NotificationSomethings();
                    num++;
                }
            }
        }

    }

    public void socketdo(){

        Thread worker = new Thread() {
            public void run() {
                try {
                    final Calendar cal;

                    cal = Calendar.getInstance();
                    hour = cal.get(Calendar.HOUR_OF_DAY);
                    min = cal.get(Calendar.MINUTE);
                    nWeek = cal.get(Calendar.DAY_OF_WEEK);
                    //socket = new Socket("ec2-13-209-36-232.ap-northeast-2.compute.amazonaws.com", 7777);
                    //out = new PrintWriter(socket.getOutputStream(), true);
                    //in = new BufferedReader(new InputStreamReader(
                      //      socket.getInputStream()));
                    out.print(nWeek+","+hour+","+min+"\n");
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                while (true) {

                    try {
                        data = in.readLine();
                        count++;

                         result.post(new Runnable() {

                            @Override
                             public void run() {
                                data = data.replace("[", "");
                                data = data.replace("'", "");
                                data = data.replace(" ", "");
                                data = data.replace("]", "");
                                Log.d(data,data);

                                data_split = data.split(",");
                                bus1 = data_split[1];
                                if(data_split[0].contains("-")) {
                                    String[] temp = data_split[0].split("-");
                                    station1 = temp[0] + temp[1];
                                } else {
                                    station1 = data_split[0];
                                }

                                showBusList(bus1, station1);
                                NotificationSomethings();

                            }
                         });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        worker.start();
    }

    public void showBusList(String bus, String station) {
        busRouteList = new ArrayList<String>();
        stationList = new ArrayList<String>();
        stationNmList = new ArrayList<String>();
        stationNoList = new ArrayList<String>();
        seqList = new ArrayList<String>();
        text = "";

        //result.append("\nbus no: " + bus);
        //result.append("\nstation: " + station);

        getBusRouteList(bus);
        getStationsByRouteList(busRouteList.get(0));
        for (int i = 0; i < stationNoList.size(); i++) {
            if (stationNoList.get(i).equals(station)) {
                getBusAPI(stationList.get(i), busRouteList.get(0), seqList.get(i));
            }
        }
       // result.append("\n"+text);

        items.add(text);
        adapter.notifyDataSetChanged();
    }

    public void getWeatherAPI() {
        weatherList=new ArrayList<String>();
        dayList=new ArrayList<String>();

        boolean initem = false, inAddr = false, inChargeTp = false, inCity = false;
        TextView status1 = (TextView) findViewById(R.id.result); //파싱된 결과확인!
        String lat = null, longi = null, statUpdateDatetime = null;

        try {
            URL url = new URL("https://www.weather.go.kr/weather/forecast/mid-term-rss3.jsp?stnId=108"
            ); //검색 URL부분

            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);
            // mNow = System.currentTimeMillis();
            //mDate = new Date(mNow);
            Calendar cal = new GregorianCalendar(Locale.KOREA);
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, 3); // 하루를 더한다.

            SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd 00:00");
            String strDate = fm.format(cal.getTime());


            int parserEvent = parser.getEventType();
            System.out.println("파싱시작합니다.");

            while (parserEvent != XmlPullParser.END_DOCUMENT) {
                switch (parserEvent) {
                    case XmlPullParser.START_TAG://parser가 시작 태그를 만나면 실행
                        if (parser.getName().equals("city")) { //title 만나면 내용을 받을수 있게 하자

                            inCity = true;
                        }
                        if (parser.getName().equals("tmEf")) { //title 만나면 내용을 받을수 있게 하자
                            inAddr = true;
                        }

                        if (parser.getName().equals("wf")) { //address 만나면 내용을 받을수 있게 하자
                            inChargeTp = true;
                        }

                        break;

                    case XmlPullParser.TEXT://parser가 내용에 접근했을때

                        if (inAddr) {
                            //if(parser.getText()==mFormat.format(mDate))
                            addr = parser.getText();
                            dayList.add(addr);
                            inAddr = false;
                        }
                        if (inChargeTp) {
                            chargeTp = parser.getText();
                            weatherList.add(chargeTp);
                            inChargeTp = false;
                        }
                        if (inCity) { //isAddress이 true일 때 태그의 내용을 저장.

                            city = parser.getText();
                            inCity = false;


                        }

                        break;
                    case XmlPullParser.END_TAG:
                        if (parser.getName().equals("data")) {
                            if (city.equals("서울")) {
                                if (addr.equals(strDate)) {
                                    status1.setText(status1.getText() + "도시:" + city + "\n 날짜 : " + addr + "\n 날씨: " + chargeTp + "\n");
                                    initem = false;
                                }
                            }
                        }
                        break;
                }
                parserEvent = parser.next();

            }
        } catch (Exception e) {
            status1.setText("에러가..났습니다...");
            e.printStackTrace();
        }
    }

    public void getTrafficAPI() {

        boolean in_prcs_spd = false;

        String prcs_spd = null;

        try {
            URL url = new URL("http://openapi.seoul.go.kr:8088/737a6d6e6968796f34375474525268/xml/TrafficInfo/1/3/1220003800");
            XmlPullParserFactory parserCreator = XmlPullParserFactory.newInstance();
            XmlPullParser parser = parserCreator.newPullParser();

            parser.setInput(url.openStream(), null);

            int parserEvent = parser.getEventType();
            System.out.println("파싱 시작합니다");

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
                            Toast.makeText(this, prcs_spd, Toast.LENGTH_LONG).show();
                            break;
                        }
                        break;
                }
                parserEvent = parser.next();
            }

        } catch(Exception e) {
            //error
        }

    }

    public void NotificationSomethings() {


        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, ResultActivity.class);
        notificationIntent.putExtra("notificationId", count); //전달할 값
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle("Time to go!!!").setWhen(System.currentTimeMillis())

                //.setContentText("상태바 드래그시 보이는 서브타이틀").setVibrate(new long[]{40,300})
                .setContentText(text).setVibrate(new long[]{40,300})

                //출처: https://androphil.tistory.com/368?category=423967 [소림사의 홍반장!]
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            /*PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK  |
                    PowerManager.ACQUIRE_CAUSES_WAKEUP |
                    PowerManager.ON_AFTER_RELEASE, "My:Tag");
            wakeLock.acquire(10000);*/
            notificationManager.createNotificationChannel(channel);

        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴

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
                            //status1.setText(status1.getText() + "버스번호: " + rtNm + "첫번째 전: " + arrmsg1 + "두번째 전" + arrmsg2+"\n");
                            text = "버스번호: " + rtNm + "첫번째 전: " + arrmsg1 + "두번째 전" + arrmsg2+"\n";
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
                            break;
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

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
