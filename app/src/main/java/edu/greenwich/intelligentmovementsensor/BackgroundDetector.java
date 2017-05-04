package edu.greenwich.intelligentmovementsensor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundDetector extends Service implements SensorEventListener{

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;

    float[] accSensorVals;

    ArrayList<Float> xAxisValues = new ArrayList<>();
    ArrayList<Float> yAxisValues = new ArrayList<>();
    ArrayList<Float> zAxisValues = new ArrayList<>();

    static final int DELAY = 5000;
    long lastUpdate;

    LocalBroadcastManager broadcaster;

    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        broadcaster = LocalBroadcastManager.getInstance(this);
        lastUpdate = System.currentTimeMillis();
    }

    static final public String RESULT = "edu.greenwich.intelligentmovementsensor.BackgroundDetector.REQUEST_PROCESSED";
    static final public String MESSAGE = "edu.greenwich.intelligentmovementsensor.BackgroundDetector.COPA_MSG";

    public void sendResult(String message) {
        Intent intent = new Intent(RESULT);
        if(message != null)
            intent.putExtra(MESSAGE, message);
        broadcaster.sendBroadcast(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return mBinder; }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accSensorVals = lowPass(sensorEvent.values.clone(), accSensorVals);
        }

        xAxisValues.add(accSensorVals[0]); // [0] - X axis
        yAxisValues.add(accSensorVals[1]); // [1] - Y axis
        zAxisValues.add(accSensorVals[2]); // [2] - Z axis

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 500){ // only reads data twice per second
            lastUpdate = curTime;
            //System.out.println("Hello every 5 seconds");
            checkSpikes(xAxisValues, yAxisValues, zAxisValues);
            xAxisValues = new ArrayList<>();
            yAxisValues = new ArrayList<>();
            zAxisValues = new ArrayList<>();
        }

        /*new Thread() {
            public void run() {
                try {
                    while(true){
                        Thread.sleep(DELAY); // delay for 500 ms
                        checkSpikes(xAxisValues, yAxisValues, zAxisValues);
                        xAxisValues = new ArrayList<>();
                        yAxisValues = new ArrayList<>();
                        zAxisValues = new ArrayList<>();
                    }
                } catch (Exception e) {}
            }
        }.start();*/
    }

    static final float ALPHA = 0.25f;

    private static float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    private void checkSpikes(ArrayList<Float> x, ArrayList<Float> y, ArrayList<Float> z){
        ArrayList<Float> xSpikes = new ArrayList<>();
        ArrayList<Float> ySpikes = new ArrayList<>();
        ArrayList<Float> zSpikes = new ArrayList<>();

        for (int i = 0; i < x.size() - 2; i++) {
            if ((x.get(i + 1) - x.get(i)) * (x.get(i + 2) - x.get(i + 1)) <= 0) {
                //extremeValues.add(accelerometerXVal[i + 1]);
                //message += x.get(i+1) + " ";
                xSpikes.add(x.get(i+1));
            }
            if ((y.get(i + 1) - y.get(i)) * (y.get(i + 2) - y.get(i + 1)) <= 0) {
                //extremeValues.add(accelerometerXVal[i + 1]);
                //message += y.get(i+1) + " ";
                ySpikes.add(y.get(i+1));
            }
            if ((z.get(i + 1) - z.get(i)) * (z.get(i + 2) - z.get(i + 1)) <= 0) {
                //extremeValues.add(accelerometerXVal[i + 1]);
                //message += z.get(i+1);
                zSpikes.add(z.get(i+1));
            }
        }

        Set<Float> singleSet = new HashSet<Float>();
        singleSet.addAll(xSpikes);
        xSpikes.clear();
        xSpikes.addAll(singleSet);

        singleSet.clear();
        singleSet.addAll(ySpikes);
        ySpikes.clear();
        ySpikes.addAll(singleSet);

        singleSet.clear();
        singleSet.addAll(zSpikes);
        zSpikes.clear();
        zSpikes.addAll(singleSet);

        singleSet.clear(); // save some memory

        sendResult("There was " + xSpikes.size() + " X spikes, " + ySpikes.size() + " Y spikes, " + zSpikes.size() + " Z spikes");
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    public class LocalBinder extends Binder {
        BackgroundDetector getService() {
            return BackgroundDetector.this;
        }
    }
}
