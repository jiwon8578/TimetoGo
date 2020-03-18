package com.example.timetogo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Response;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

//흐름: 버스 번호를 입력 -> 해당 버스에 대한 노선 id가 나옴 -> 해당 노선에 대한 정류장들에 대한 정류장 정보가 나온다(정류장 id, 정류장 이름, 정류장 번호) -> 정류장 id, 노선 id, 순번을 입력하여 해당 정류장에 도착하는 특정 버스들에 대한 정보를 받아볼 수 있다.
//busRouteNm이 버스 번호, busRouteId가 노선Id, station이 정류소Id

public class MainActivity extends AppCompatActivity {
    static int num = 0;
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

    TextView result;

    String[] data_split;
    static String bus1;
    static String station1;
    static String destStn;

    static ArrayList<String> items;
    static ArrayAdapter<String> adapter;
    static ListView listView;

    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    static double curLat;
    static double curLng;

    public Bus bus = new Bus();

    int GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1;

    FitnessOptions fitnessOptions;

    long total;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.enableDefaults();

        //위치 퍼미션
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        } else {
            checkRunTimePermission();
        }

        GpsTracker gpsTracker = new GpsTracker(MainActivity.this);
        curLat = gpsTracker.getLatitude();
        curLng = gpsTracker.getLongitude();

        //Toast.makeText(getApplicationContext(),Double.toString(curLat),Toast.LENGTH_SHORT).show();
        //Toast.makeText(getApplicationContext(),Double.toString(curLng),Toast.LENGTH_SHORT).show();

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

        //Time time = new Time(curLat, curLng);
        //time.determineTime();
        //Toast.makeText(getApplicationContext(),Integer.toString(time.early),Toast.LENGTH_SHORT).show();

        result = (TextView) findViewById(R.id.result);

        (new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        final Calendar cal;

                        cal = Calendar.getInstance();
                        hour = cal.get(Calendar.HOUR_OF_DAY);
                        min = cal.get(Calendar.MINUTE);
                        nWeek = cal.get(Calendar.DAY_OF_WEEK);

                        long time = System.currentTimeMillis();

                        if (nWeek == 1) {
                            strweek = "일요일";
                        }
                        if (nWeek == 2) {
                            strweek = "월요일";
                        }
                        if (nWeek == 3) {
                            strweek = "화요일";
                            alarmTime(22, 11, 1);
                            alarmTime(22, 12, 2);
                            alarmTime(22, 13, 3);
                            alarmTime(22, 14, 4);
                            alarmTime(22, 6, 5);
                            alarmTime(22, 7, 6);
                        }
                        if (nWeek == 4) {
                            strweek = "수요일";
                            alarmTime(21, 18, 1);
                            alarmTime(21, 19, 2);
                            alarmTime(21, 20, 3);
                            alarmTime(21, 21, 4);
                            alarmTime(21, 22, 5);
                            alarmTime(21, 23, 6);

                        }
                        if (nWeek == 5) {
                            strweek = "목요일";
                            alarmTime(21, 41, 1);
                            alarmTime(21, 42, 2);
                            alarmTime(21, 43, 3);
                            alarmTime(21, 44, 4);
                            alarmTime(21, 45, 5);
                            alarmTime(21, 7, 6);
                            alarmTime(21, 8, 7);
                        }
                        if (nWeek == 6) {
                            strweek = "금요일";
                            alarmTime(21, 43, 1);
                            alarmTime(21, 44, 2);
                            alarmTime(21, 45, 3);
                            alarmTime(21, 46, 4);
                            alarmTime(21, 47, 5);
                            alarmTime(21, 48, 6);
                        }
                        if (nWeek == 7) {
                            strweek = "토요일";
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
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                historyAPI();
            }
        });


        fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                .build();
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(this), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this,
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
                    GoogleSignIn.getLastSignedInAccount(this),
                    fitnessOptions);
        } else {
            subscribe();
        }
        /*GoogleSignInAccount account = GoogleSignIn.getAccountForExtension(this, fitnessOptions);
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                    this, // your activity
                    GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                    account,
                    fitnessOptions);
        } else {
            subscribe();
        }*/
        //readData();



        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                readData();
            }
        }, 0, 1000);


    }

    public void historyAPI() {

        // get the start and end date of the urrent mobile
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1); //어제 데이터
        long startTime = cal.getTimeInMillis();

        GoogleSignInOptionsExtension fitnessOptions =
                FitnessOptions.builder()
                        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
                        .build();

        GoogleSignInAccount googleSignInAccount =
                GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        Task<DataReadResponse> response = Fitness.getHistoryClient(this, googleSignInAccount)
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build());

        try {
            //code that throws the exception
            DataReadResponse readDataResult = Tasks.await(response);
            DataSet dataSet = readDataResult.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
            showDataSet(dataSet);
        } catch (Exception e) {
            //what to do when it throws the exception
            Log.i("History", "Not working");
        }

    }

    private void showDataSet(DataSet dataSet) {
        Log.i("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH");

        int totalsteps = 0;

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.i("History", "Data point:");
            Log.i("History", "\tType: " + dp.getDataType().getName());
            Log.i("History", "\tStart: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.i("History", "\tEnd: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                Log.i("History", "\tField: " + field.getName() +
                        " Value: " + dp.getValue(field));
                totalsteps += dp.getValue(field).asInt();

                String hour = format.format(dp.getStartTime(TimeUnit.MILLISECONDS));

                Calendar cal = Calendar.getInstance();
                Date now = new Date();
                cal.setTime(now);
                long curTime = cal.getTimeInMillis();

                String curHour = format.format(curTime);

                Log.i("History", "Hour:"+hour);
                Log.i("History", "curHour:"+curHour);

                if(hour == curHour) {
                    break;
                }
            }
        }
        Log.i("History", "Totalsteps:"+totalsteps);
    }

    /** Records step data by requesting a subscription to background step data. */
    public void subscribe() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.getRecordingClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .subscribe(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.i("Stepcount", "Successfully subscribed!");
                                } else {
                                    Log.w("Stepcount", "There was a problem subscribing.", task.getException());
                                }
                            }
                        });
    }

    /**
     * Reads the current daily step total, computed from midnight of the current day on the device's
     * current timezone.
     */
    private void readData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                .addOnSuccessListener(
                        new OnSuccessListener<DataSet>() {
                            @Override
                            public void onSuccess(DataSet dataSet) {
                                total = dataSet.isEmpty() ? 0 : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                Log.i("Stepcount", "Total steps: " + total);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("Stepcount", "There was a problem getting the step count.", e);
                            }
                        });
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
                    String send = "";
                    hour = cal.get(Calendar.HOUR_OF_DAY);
                    min = cal.get(Calendar.MINUTE);
                    nWeek = cal.get(Calendar.DAY_OF_WEEK);
                    send = nWeek + "," + hour + "," + min + "\n";

                    out.print(send);
                    out.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                while (true) {
                    //readData();

                    try {
                        data = in.readLine();
                        count++;

                         result.post(new Runnable() {

                            @Override
                             public void run() {
                                //readData();

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

                                if(data_split[2].contains("-")) {
                                    String[] temp = data_split[2].split("-");
                                    destStn = temp[0] + temp[1];
                                } else {
                                    destStn = data_split[2];
                                }

                                bus.showBusList(bus1, station1, destStn);
                                //showBusList(bus1, station1);
                                NotificationSomethings(bus.text);

                                Time time = new Time(curLat, curLng);
                                time.determineTime();
                                items.add(time.text);

                                //readData();
                                items.add(String.valueOf(total));
                                //NotificationSomethings(Integer.toString(time.early));
                                // Toast.makeText(getApplicationContext(),Integer.toString(bus.averageSpd),Toast.LENGTH_SHORT).show();
                                // Toast.makeText(getApplicationContext(),Integer.toString(time.early),Toast.LENGTH_SHORT).show();

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

    public void NotificationSomethings(String contentText) {
        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, ResultActivity.class);
        notificationIntent.putExtra("notificationId", count); //전달할 값
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle("Time to go!!!").setWhen(System.currentTimeMillis())

                //.setContentText("상태바 드래그시 보이는 서브타이틀").setVibrate(new long[]{40,300})
                .setContentText(contentText).setVibrate(new long[]{40,300})

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

    @Override
    protected void onStop() {
        super.onStop();
        try {
            socket.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    //참고한 사이트: https://webnautes.tistory.com/1315
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == REQUIRED_PERMISSIONS.length) { //요청 코드가 PERMISSONS_REQUEST_CODE이고, 요청한 퍼시면 개수만큼 수신되었다면
            boolean check_result = true;
            for(int result : grantResults) {
                if(result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if(check_result) {
            } else {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0]) || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, "퍼미션이 거부되었습니다. 설정에서 퍼미션을 허용해야 합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    void checkRunTimePermission() {
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, REQUIRED_PERMISSIONS[0])) {
                Toast.makeText(MainActivity.this, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                subscribe();
            }
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
