package edu.greenwich.intelligentmovementsensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BackgroundService extends Service implements SensorEventListener {
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

    // allow static access to filename so that the dialog can change it's name
    public static String fileName = "test";

    static boolean isRunning = false;

    @Override
    public void onCreate() {
       showNotification(text, "");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void showNotification(String smallText, String bigText){
        isRunning = true;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder( getApplicationContext() )
                        .setSmallIcon(R.drawable.grelogo)
                        .setColor(Color.rgb(0,82,155))
                        .setContentTitle("Sensor Data")
                        .setContentText(smallText)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))
                        .setAutoCancel(true);

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("Stop", true);
        Bundle stopBundle = new Bundle();
        stopBundle.putBoolean("Stop", true);//This is the value I want to pass

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_action_cancel_small, "Stop", pendingIntent).build();

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.addAction(action);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }


    public void recordData(String data) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + fileName + ".csv");
        file.createNewFile();
        if(file.exists())
        {
                OutputStream fo = new FileOutputStream(file, true);
                fo.write(data.getBytes());
                fo.close();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (isRunning) {
            // grab the values and timestamp -- off the main thread
            new SensorEventLoggerTask().execute(sensorEvent);
            StringBuilder text = new StringBuilder(accelData.toString()).append("\n");
            text.append(compassData).append("\n");
            text.append(gyroData).append("\n");
            text.append(gravData).append("\n");

            // compute rotation matrix
            float rotation[] = new float[9];
            float identity[] = new float[9];
            if (lastAccelerometer != null && lastCompass != null) {
                boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                        identity, lastAccelerometer, lastCompass);
                if (gotRotation) {
                    float cameraRotation[] = new float[9];
                    // remap such that the camera is pointing straight down the Y
                    // axis
                    SensorManager.remapCoordinateSystem(rotation,
                            SensorManager.AXIS_X, SensorManager.AXIS_Z,
                            cameraRotation);

                    // orientation vector
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(cameraRotation, orientation);

                    text.append(String.format("Orientation (%.3f, %.3f, %.3f)", Math.toDegrees(orientation[0]), Math.toDegrees(orientation[1]), Math.toDegrees(orientation[2]))).append("\n");

                }
            }

            StringBuilder msg = new StringBuilder(sensorEvent.sensor.getName())
                    .append(" ");
            for (float value : sensorEvent.values) {
                msg.append("[").append(String.format("%.3f", value)).append("]");
                try {
                    recordData(sensorEvent.sensor.getName() + ", " + String.format("%.3f", value) + ", ");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                recordData("\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    lastAccelerometer = sensorEvent.values.clone();
                    accelData = msg.toString();
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    gyroData = msg.toString();
                    break;
                case Sensor.TYPE_GRAVITY:
                    gravData = msg.toString();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    lastCompass = sensorEvent.values.clone();
                    compassData = msg.toString();
                    break;
            }
            final SensorEventListener sensorEventListener = this;
            showNotification(accelData, text.toString());
            final Intent intent = new Intent(this, MainActivity.class);
            // stop the service
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    isRunning = false;
                    mSensorManager.unregisterListener(sensorEventListener);
                    stopSelf();
                    stopService(intent);
                    System.exit(0);
                }
            }, 20000);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isRunning = false;
                    mSensorManager.unregisterListener(sensorEventListener);
                    stopSelf();
                    stopService(intent);
                    System.exit(0);
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public class LocalBinder extends Binder {
        BackgroundService getService() {
            return BackgroundService.this;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // initialize the sensor and location manager
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // set type to each sensor
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravitometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // get data from the sensors
        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravitometer, mSensorManager.SENSOR_STATUS_ACCURACY_MEDIUM); // MEDIUM is less power consumption
        mSensorManager.registerListener(this, mCompass, mSensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    private class SensorEventLoggerTask extends
            AsyncTask<SensorEvent, Void, Void> {
        @Override
        protected Void doInBackground(SensorEvent... events) {
            SensorEvent event = events[0];
            return null;
        }
    }
}