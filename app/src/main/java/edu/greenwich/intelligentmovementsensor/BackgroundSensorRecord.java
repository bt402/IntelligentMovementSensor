package edu.greenwich.intelligentmovementsensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;


public class BackgroundSensorRecord extends Service implements SensorEventListener {

    private final IBinder mBinder = new LocalBinder();
    private String text = "Loading...";

    private SensorManager mSensorManager = null;

    private Sensor mAccelerometer = null;
    private Sensor mGyroscope = null;
    private Sensor mGravitometer = null;
    private Sensor mCompass = null;

    float[] lastAccelerometer;
    float[] lastCompass;

    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";
    String gravData = "Gravity Data";
    public static Context context;


    // allow static access to filename so that the dialog can change it's name
    public static String fileName = "test";

    public static ArrayList<String> dataList = new ArrayList<>();

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);

        showNotification();
    }

    private void showNotification() {
        Intent cancelIntent = new Intent(this, ClosingActivity.class);
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        cancelIntent.putExtra("Cancelled", true);

        PendingIntent cancelPendingIntent = PendingIntent.getActivity(this, 1, cancelIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_action_cancel);

        RemoteViews cancelOnLock = new RemoteViews(getApplicationContext().getPackageName(), R.layout.notification_layout);
        cancelOnLock.setImageViewBitmap(R.id.notification_icon, icon);
        cancelOnLock.setTextViewText(R.id.notification_text, "Cancel Recording Data");
        cancelOnLock.setOnClickPendingIntent(R.drawable.ic_action_cancel, cancelPendingIntent);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.grelogo)
                .setColor(Color.rgb(0,82,155))
                .setContentTitle("Recording Data in Progress")
                .setProgress(0,0,true)
                .addAction(R.drawable.ic_action_cancel_small, "Cancel", cancelPendingIntent)
                .setContent(cancelOnLock)
                .setAutoCancel(false);


        // Create Notification Manager
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationManager.notify(0, mBuilder.build());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //System.out.println(event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        dataList.add(event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public class LocalBinder extends Binder {
        BackgroundSensorRecord getService() {
            return BackgroundSensorRecord.this;
        }
    }
}
