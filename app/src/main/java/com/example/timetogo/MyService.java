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
import static com.example.timetogo.MainActivity.NOTIFICATION_CHANNEL_ID;
import static com.example.timetogo.MainActivity.average;
import static com.example.timetogo.MainActivity.bus;
import static com.example.timetogo.MainActivity.bus1;
import static com.example.timetogo.MainActivity.count;
import static com.example.timetogo.MainActivity.curLat;
import static com.example.timetogo.MainActivity.curLng;
import static com.example.timetogo.MainActivity.data;
import static com.example.timetogo.MainActivity.data_split;
import static com.example.timetogo.MainActivity.destStn;
import static com.example.timetogo.MainActivity.hour;
import static com.example.timetogo.MainActivity.sec;
import static com.example.timetogo.MainActivity.min;
import static com.example.timetogo.MainActivity.nWeek;
import static com.example.timetogo.MainActivity.station1;
import static com.example.timetogo.MainActivity.strweek;
import static com.example.timetogo.MainActivity.total;
import static com.example.timetogo.MainActivity.tts;

public class MyService extends Service {
    public static final String START_SOCKET = "startsocket";
    public static final String STOP_SOCKET = "stopsocket";
    public static Socket socket;
    public static PrintWriter out;
    public static BufferedReader in;

    Thread socketThread;
    Thread alarmThread;

    public String busText;
    public String timeText;
    public String stepMsg;

    public MyService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action.equals(START_SOCKET)) {
            //start your server thread from here
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
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

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
                    }
                    if (nWeek == 3) {
                        strweek = "화요일";
                    }
                    if (nWeek == 4) {
                        strweek = "수요일";
                    }
                    if (nWeek == 5) {
                        strweek = "목요일";
                        alarmTime(12, 39, 59);
                        alarmTime(12, 40, 0);
                        alarmTime(12, 41, 0);
                        alarmTime(12, 42, 0);
                        alarmTime(12, 43, 0);
                        alarmTime(12, 44, 0);
                    }
                    if (nWeek == 6) {
                        strweek = "금요일";
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

    public void alarmTime(int hour1,int min1,int alarm) {
        final Calendar cal;
        cal = Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        min = cal.get(Calendar.MINUTE);
        sec = cal.get(Calendar.SECOND);
        nWeek = cal.get(Calendar.DAY_OF_WEEK);

        if(hour == hour1 && min == min1 && sec == alarm){
            socketdo();
        }
    }

    public void socketdo(){
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    final Calendar cal;
                    cal = Calendar.getInstance();
                    String send = "";
                    hour = cal.get(Calendar.HOUR_OF_DAY);
                    min = cal.get(Calendar.MINUTE);
                    nWeek = cal.get(Calendar.DAY_OF_WEEK);
                    send = nWeek + "," + hour + "," + min + "\n";
                    Log.i("send", send);
                    out.print(send);
                    out.flush();

                    while(true) {
                        try {
                            data = in.readLine();
                            count++;

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

                            busText = bus.showBusList(bus1, station1, destStn);

                            Handler mHandler = new Handler(Looper.getMainLooper());
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Time time = new Time(curLat, curLng);
                                    timeText = time.determineTime();
                                    Log.i("timeText", timeText);

                                    double lat1 = Double.parseDouble(bus.lat1);
                                    double lat2 = Double.parseDouble(bus.lat2);
                                    double lng1 = Double.parseDouble(bus.lng1);
                                    double lng2 = Double.parseDouble(bus.lng2);

                                    double distance = distance(lat1, lat2, lng1, lng2);
                                    Log.i("distance", distance+"");

                                    stepMsg = "";
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
                                    Log.i("stepMsg", stepMsg);
                                    NotificationSomethings(busText);
                                }
                            }, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

    public void NotificationSomethings(String contentText) {
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

    public static double distance(double lat1, double lat2, double lon1, double lon2) {
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

    private void Speech(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            // API 20
        else
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }
}
