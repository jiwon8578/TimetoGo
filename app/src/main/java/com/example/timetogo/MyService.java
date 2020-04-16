package com.example.timetogo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Locale;
import static com.example.timetogo.StepCount.average;
import static com.example.timetogo.StepCount.total;

public class MyService extends Service {
    public static final String START_SOCKET = "startsocket";
    public static final String STOP_SOCKET = "stopsocket";
    public Socket socket;
    public PrintWriter out;
    public BufferedReader in;

    Thread socketThread;
    Thread alarmThread;

    public final String NOTIFICATION_CHANNEL_ID = "10001";
    public int nWeek;
    public int hour;
    public int min;
    public int sec;
    public String strweek = null;
    public String data;

    public String[] data_split;
    public String bus1;
    public String station1;
    public String destStn;

    public static double curLat;
    public static double curLng;

    public Bus bus = new Bus();
    public String busText;
    public String timeText;
    public String stepMsg;

    public GpsTracker gpsTracker;

    public static TextToSpeech tts;

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        gpsTracker = new GpsTracker(getApplicationContext());
        curLat = gpsTracker.getLatitude();
        curLng = gpsTracker.getLongitude();

        String action = intent.getAction();
        if (action.equals(START_SOCKET)) {
            this.socketThread = new Thread(new SocketThread());
            this.socketThread.start();
            this.alarmThread = new Thread(new AlarmThread());
            this.alarmThread.start();
        }
        if (action.equals(STOP_SOCKET)) {
            try {
                socket.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        ttsInit();
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //소켓 생성 쓰레드
    class SocketThread implements Runnable {
        public void run() {
            try {
                if(socket == null && out == null && in == null) {
                    socket = new Socket("ec2-13-209-36-232.ap-northeast-2.compute.amazonaws.com", 7777);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //알람(푸시 메시지) 생성 쓰레드
    class AlarmThread implements Runnable {
        public void run() {
            while (true) {
                try {
                    final Calendar cal;
                    cal = Calendar.getInstance();
                    nWeek = cal.get(Calendar.DAY_OF_WEEK);

                    if (nWeek == 1) {
                        strweek = "일요일";
                    }
                    if (nWeek == 2) {
                        strweek = "월요일";
                        alarmTime(9, 20, 0); //판교역 -> 을지로2가
                        alarmTime(9, 50, 0); //을지로 2가 -> 숙대입구역
                        alarmTime(15, 20, 0); //숙대입구역 -> 서울역
                        alarmTime(15, 40, 0); //서울역 -> 판교역
                    }
                    if (nWeek == 3) {
                        strweek = "화요일";
                        alarmTime(9, 20, 0); //판교역 -> 을지로2가
                        alarmTime(9, 50, 0); //을지로 2가 -> 숙대입구역
                        alarmTime(12, 10, 0); //숙대입구역 -> 서울역
                        alarmTime(12, 30, 0); //서울역 -> 판교역
                    }
                    if (nWeek == 4) {
                        strweek = "수요일";
                        alarmTime(9, 20, 0); //판교역 -> 을지로2가
                        alarmTime(9, 50, 0); //을지로 2가 -> 숙대입구역
                        alarmTime(15, 20, 0); //숙대입구역 -> 서울역
                        alarmTime(15, 40, 0); //서울역 -> 판교역
                    }
                    if (nWeek == 5) {
                        strweek = "목요일";
                        alarmTime(9, 20, 0); //판교역 -> 을지로2가
                        alarmTime(9, 50, 0); //을지로 2가 -> 숙대입구역
                        alarmTime(12, 10, 0); //숙대입구역 -> 서울역
                        alarmTime(12, 30, 0); //서울역 -> 판교역
                    }
                    if (nWeek == 6) {
                        strweek = "금요일";
                        alarmTime(10, 50, 0); //판교역 -> 을지로2가
                        alarmTime(11, 30, 0); //을지로 2가 -> 숙대입구역
                        alarmTime(18, 5, 0); //숙대입구역 -> 서울역
                        alarmTime(18, 15, 0); //서울역 -> 판교역
                    }
                    if (nWeek == 7) {
                        strweek = "토요일";
                    }
                    Thread.sleep(1000); //1초에 한번씩 실행하도록
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //해당 알람 시간에 socketdo 함수를 실행
    private void alarmTime(int hour1,int min1,int alarm) {
        final Calendar cal;
        cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);
        sec = cal.get(Calendar.SECOND);
        nWeek = cal.get(Calendar.DAY_OF_WEEK);

        if(hour == hour1 && min == min1 && sec == alarm) {
            socketdo();
        }
    }

    //서버에 현재 시간과 날짜를 보내고, 서버에서 보내오는 데이터를 받고 푸시메시지에 띄우는 함수
    private void socketdo(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    sendToServer();
                    getFromServer();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    //서버로 현재 시간과 날짜를 보내는 함수
    private void sendToServer() {
        final Calendar cal;
        cal = Calendar.getInstance();
        String send = "";
        hour = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);
        nWeek = cal.get(Calendar.DAY_OF_WEEK);
        send = nWeek + "," + hour + "," + min + "\n";
        out.print(send);
        out.flush();
    }

    //서버에서 보내오는 데이터를 받는 함수
    private void getFromServer() {
        while(true) {
            try {
                data = in.readLine();
                splitData();

                //이 코드 없으면 Can't create handler inside thread that has not called Looper.prepare() 에러 발생
                Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        busText = bus.showBusList(bus1, station1, destStn); //버스 도착정보 메시지

                        curLat = gpsTracker.getLatitude();
                        curLng = gpsTracker.getLongitude();
                        Log.i("curLat", curLat+"");
                        Log.i("curLng", curLng+"");
                        Time time = new Time(curLat, curLng);
                        timeText = time.determineTime(); //몇번째 전 버스를 사용해야 하는지(날씨, 교통상황)

                        stepMsg = getStepMessage(); //걸음 수 메시지

                        NotificationSomethings(busText);
                    }
                }, 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //서버에서 받아온 데이터를 나누는 함수
    private void splitData() {
        data = data.replace("[", "");
        data = data.replace("'", "");
        data = data.replace(" ", "");
        data = data.replace("]", "");

        data_split = data.split(",");
        bus1 = data_split[1];
        if(data_split[0].contains("-")) {
            String[] temp = data_split[0].split("-");
            station1 = temp[0] + temp[1];
        } else {
            station1 = data_split[0];
        }

        if(data_split[2].contains("-")) {
            String[] temp = data_split[2].split("-");
            destStn = temp[0] + temp[1];
        } else {
            destStn = data_split[2];
        }
    }

    //걸음 수 메시지 생성 함수
    private String getStepMessage() {
        double lat1 = Double.parseDouble(bus.lat1);
        double lat2 = Double.parseDouble(bus.lat2);
        double lng1 = Double.parseDouble(bus.lng1);
        double lng2 = Double.parseDouble(bus.lng2);
        double distance = distance(lat1, lat2, lng1, lng2);
        String stepMsg = "";

        if(distance <= 3000) {
            if(total > average) {
                stepMsg = "이동 거리가 3km 이하입니다.\n" +
                        "현재 걸음 수는 " + String.valueOf(total) +"입니다.\n" +
                        "평균 걸음수는 " + String.valueOf(average) + "입니다.\n" +
                        "평소보다 많이 걸으셨으니 대중교통을 이용하셔도 좋습니다.";
            } else {
                stepMsg = "이동 거리가 3km 이하입니다.\n" +
                        "현재 걸음 수는 " + String.valueOf(total) +"입니다.\n" +
                        "평균 걸음수는 " + String.valueOf(average) + "입니다.\n" +
                        "평소보다 걸음수가 적습니다. 목적지까지 도보로 이동하시는 것은 어떠신가요?";
            }
        } else {
            stepMsg = "이동 거리가 3km 이상입니다.\n" +
                    "현재 걸음 수는 " + String.valueOf(total) +"입니다.\n" +
                    "평균 걸음수는 " + String.valueOf(average) + "입니다.\n" +
                    "목적지까지 대중교통을 이용해주세요.";
        }
        return stepMsg;
    }

    //푸시메시지 생성 함수
    private void NotificationSomethings(String contentText) {
        NotificationManager notificationManager = (NotificationManager)getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        notificationIntent.putExtra("notificationId", busText+"@"+timeText+"@"+stepMsg); //전달할 값
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle("Time to go!!!").setWhen(System.currentTimeMillis())
                .setContentText(contentText).setVibrate(new long[]{40,300})
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) //사용자가 노티피케이션을 클릭 시 MainActivity로 이동하도록 설정
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setSmallIcon(R.drawable.ic_launcher_foreground);
            CharSequence channelName  = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        } else builder.setSmallIcon(R.mipmap.ic_launcher);

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build());
    }

    //거리 계산 함수(정류장 간 이동거리가 3키로 이상인지, 이하인지) -> 위도와 경도로 계산
    private double distance(double lat1, double lat2, double lon1, double lon2) {
        final int R = 6371; //지구의 반지름
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; //미터로 변환
        distance = Math.pow(distance, 2);
        return Math.sqrt(distance);
    }

    //tts 설정
    private void ttsInit() {
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.KOREA);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    } else {
                        tts.setPitch(0.7f);
                        tts.setSpeechRate(1.2f);
                    }
                }
            }
        });
    }
}
