package com.example.timetogo;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptionsExtension;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class StepCount extends Service {
    public static long total;
    public static int average;

    public StepCount() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                readData();
            }
        }, 0, 1000);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        historyAPI();
                    }
                }, 0, 1000);
            }
        });
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void historyAPI() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1); //어제 데이터
        long startTime = cal.getTimeInMillis();

        GoogleSignInOptionsExtension fitnessOptions = FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ).build();
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getAccountForExtension(this, fitnessOptions);

        Task<DataReadResponse> response = Fitness.getHistoryClient(this, googleSignInAccount)
                .readData(new DataReadRequest.Builder()
                        .read(DataType.TYPE_STEP_COUNT_DELTA)
                        .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                        .build());

        try {
            DataReadResponse readDataResult = Tasks.await(response);
            DataSet dataSet = readDataResult.getDataSet(DataType.TYPE_STEP_COUNT_DELTA);
            showDataSet(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDataSet(DataSet dataSet) {
        Log.i("History", "Data returned for Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();
        SimpleDateFormat format = new SimpleDateFormat("HH");

        int totalsteps = 0;
        int num = 0;
        average = 0;

        for (DataPoint dp : dataSet.getDataPoints()) {
            for(Field field : dp.getDataType().getFields()) {
                totalsteps += dp.getValue(field).asInt();
                String hour = format.format(dp.getStartTime(TimeUnit.MILLISECONDS));
                Calendar cal = Calendar.getInstance();
                Date now = new Date();
                cal.setTime(now);
                long curTime = cal.getTimeInMillis();
                String curHour = format.format(curTime);
                num++;
                average = totalsteps / num;
                if(Integer.valueOf(hour) >= Integer.valueOf(curHour)) {
                    break;
                }
            }
        }
        Log.i("History", "Totalsteps:"+totalsteps);
        Log.i("History", "Num:"+num);
        Log.i("History", "Average:"+average);
    }

    private void readData() {
        Fitness.getHistoryClient(this, GoogleSignIn.getLastSignedInAccount(getApplicationContext()))
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
}
