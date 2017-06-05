package edu.greenwich.intelligentmovementsensor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.dfki.mycbr.core.similarity.AmalgamationFct;

public class BackgroundDetector extends Service implements SensorEventListener{

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mGyroscope = null;
    private Sensor mGravitometer = null;

    float[] accSensorVals;

    ArrayList<Float> accXAxisValues = new ArrayList<>();
    ArrayList<Float> accYAxisValues = new ArrayList<>();
    ArrayList<Float> accZAxisValues = new ArrayList<>();

    ArrayList<Float> gravXAxisValues = new ArrayList<>();
    ArrayList<Float> gravYAxisValues = new ArrayList<>();
    ArrayList<Float> gravZAxisValues = new ArrayList<>();

    ArrayList<Float> gyroXAxisValues = new ArrayList<>();
    ArrayList<Float> gyroYAxisValues = new ArrayList<>();
    ArrayList<Float> gyroZAxisValues = new ArrayList<>();



    static final int DELAY = 500;
    long lastUpdate;

    LocalBroadcastManager broadcaster;

    private final IBinder mBinder = new LocalBinder();

    static Recommender recommender;
    static String inputAmalgamation;
    String inputMovement;
    String numberOfCases;

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravitometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravitometer, mSensorManager.SENSOR_STATUS_ACCURACY_MEDIUM); // MEDIUM is less power consumption
        broadcaster = LocalBroadcastManager.getInstance(this);
        lastUpdate = System.currentTimeMillis();

        recommender = new Recommender();
        recommender.loadengine();
        inputAmalgamation = recommender.myConcept.getActiveAmalgamFct().getName();

        inputMovement = "_unknown_"; // Freefall, Bunce, Impact, Still, Flat, Walk
        numberOfCases = "5";

        CheckforAmalgamSelection();
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
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accSensorVals = lowPass(event.values.clone(), accSensorVals);
        }


        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                accXAxisValues.add(event.values[0]);
                accYAxisValues.add(event.values[1]);
                accZAxisValues.add(event.values[2]);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroXAxisValues.add(event.values[0]);
                gyroYAxisValues.add(event.values[1]);
                gyroZAxisValues.add(event.values[2]);
                break;
            case Sensor.TYPE_GRAVITY:
                gravXAxisValues.add(event.values[0]);
                gravYAxisValues.add(event.values[1]);
                gravZAxisValues.add(event.values[2]);
                break;
           /* case Sensor.TYPE_ROTATION_VECTOR:
                lastRotation = event.values.clone();
                rotvData = msg.toString();
                rotationVectorList.add(rotvData);
                break;*/
        }

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > DELAY){ // only reads data twice per second
            lastUpdate = curTime;
            //System.out.println("Hello every 5 seconds");
            checkSpikes(accXAxisValues, accYAxisValues, accZAxisValues,
                    gravXAxisValues, gravYAxisValues, gravZAxisValues,
                    gyroXAxisValues, gyroYAxisValues, gyroZAxisValues);
            accXAxisValues = new ArrayList<>();
            accYAxisValues = new ArrayList<>();
            accZAxisValues = new ArrayList<>();

            gravXAxisValues = new ArrayList<>();
            gravYAxisValues = new ArrayList<>();
            gravZAxisValues = new ArrayList<>();

            gyroXAxisValues = new ArrayList<>();
            gyroYAxisValues = new ArrayList<>();
            gyroZAxisValues = new ArrayList<>();
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

    private void checkSpikes(ArrayList<Float> accXAxisValues, ArrayList<Float> accYAxisValues, ArrayList<Float> accZzAxisValues,
                             ArrayList<Float> gravXAxisValues, ArrayList<Float> gravYAxisValues, ArrayList<Float> gravZAxisValues,
                             ArrayList<Float> gyroXAxisValues, ArrayList<Float> gyroYAxisValues, ArrayList<Float> gyroZAxisValues){
        long lastUpdate = System.currentTimeMillis();
        double sum = 0.0;
        double average;
        int count = 0;
            for (int i = 0; i < accXAxisValues.size(); i++){
                float absoluteSum = Math.abs(accXAxisValues.get(i) + accYAxisValues.get(i) + accZzAxisValues.get(i));
                sum += absoluteSum;
                count ++;
            }
            average = sum / count;
        System.out.println("Absolute sum accelerometer average: " + average);
        String accInputPeak = "" + average;

        double sum2 = 0.0;
        double average2 = 0.0;
        int count2 = 0;
        for (int i = 0; i < gravXAxisValues.size(); i++){
            float absoluteSum = Math.abs(gravXAxisValues.get(i) + gravYAxisValues.get(i) + gravZAxisValues.get(i));
            sum2 += absoluteSum;
            count2 ++;
        }
        average2 = sum2 / count2;
        System.out.println("Absolute sum gravitometer average: " + average2);
        String gravInputPeak = "" + average2;

        double sum3 = 0.0;
        double average3 = 0.0;
        int count3 = 0;
        for (int i = 0; i < gyroXAxisValues.size(); i++){
            float absoluteSum = Math.abs(gyroXAxisValues.get(i) + gyroYAxisValues.get(i) + gyroZAxisValues.get(i));
            sum3 += absoluteSum;
            count3 ++;
        }
        average3 = sum3 / count3;
        System.out.println("Absolute sum gyroscope average: " + average3);
        String gyroInputPeak = "" + average3;

        String[] split = recommender.solveOuery(inputMovement,Float.valueOf(accInputPeak), Float.valueOf(gravInputPeak), Float.valueOf(gyroInputPeak), Integer.valueOf(numberOfCases)).split(",");
        //recommender.solveOuery(inputMovement,Float.valueOf(inputPeak), Integer.valueOf(numberOfCases));
        sendResult(split[2]);
    }

    public void recordData(String data) throws IOException {
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "walking" + ".csv");
        file.createNewFile();
        if(file.exists())
        {
            OutputStream fo = new FileOutputStream(file, true);
            fo.write(data.getBytes());
            fo.close();
        }
    }

    private void checkSpikes(ArrayList<Float> x, ArrayList<Float> y, ArrayList<Float> z, int a){
        ArrayList<Float> xSpikes = new ArrayList<>();
        ArrayList<Float> ySpikes = new ArrayList<>();
        ArrayList<Float> zSpikes = new ArrayList<>();

        /*for (int i = 0; i < x.size(); i++){
            System.out.println(x.get(i));
        }
        System.out.println("------------------");
        for (int i = 0; i < y.size(); i++){
            System.out.println(y.get(i));
        }
        System.out.println("------------------");
        for (int i = 0; i < z.size(); i++){
            System.out.println(z.get(i));
        }*/

        for (int i = 0; i < x.size() - 2; i++) {
            float numX = (x.get(i + 1) - x.get(i)) * (x.get(i + 2) - x.get(i + 1));
            float numY = (y.get(i + 1) - y.get(i)) * (y.get(i + 2) - y.get(i + 1));
            float numZ = (z.get(i + 1) - z.get(i)) * (z.get(i + 2) - z.get(i + 1));

            if (numX <= 0) {
                //extremeValues.add(accelerometerXVal[i + 1]);
                //message += x.get(i+1) + " ";
                xSpikes.add(x.get(i+1));
            }
            if (numY <= 0) {
                //extremeValues.add(accelerometerXVal[i + 1]);
                //message += y.get(i+1) + " ";
                ySpikes.add(y.get(i+1));
            }
            if (numZ <= 0) {
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

    public static void CheckforAmalgamSelection() {

        List<AmalgamationFct> liste = recommender.myConcept.getAvailableAmalgamFcts();

        for (int i = 0; i < liste.size(); i++) {

            if ((liste.get(i).getName()).equals(inputAmalgamation)) {

                recommender.myConcept.setActiveAmalgamFct(liste.get(i));
            }
        }
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
