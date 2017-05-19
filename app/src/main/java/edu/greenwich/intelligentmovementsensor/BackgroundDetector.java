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

    float[] accSensorVals;

    ArrayList<Float> xAxisValues = new ArrayList<>();
    ArrayList<Float> yAxisValues = new ArrayList<>();
    ArrayList<Float> zAxisValues = new ArrayList<>();

    static final int DELAY = 500;
    long lastUpdate;

    LocalBroadcastManager broadcaster;

    private final IBinder mBinder = new LocalBinder();

    static Recommender recommender;
    static String inputAmalgamation;
    String inputMovement;
    String numberOfCases;
    String walking = null;

    @Override
    public void onCreate() {
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        broadcaster = LocalBroadcastManager.getInstance(this);
        lastUpdate = System.currentTimeMillis();

        recommender = new Recommender();
        recommender.loadengine();
        inputAmalgamation = recommender.myConcept.getActiveAmalgamFct().getName();

        inputMovement = "Still"; // Freefall, Bunce, Impact, Still, Flat, Walk
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
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accSensorVals = lowPass(sensorEvent.values.clone(), accSensorVals);
        }

        xAxisValues.add(accSensorVals[0]); // [0] - X axis
        yAxisValues.add(accSensorVals[1]); // [1] - Y axis
        zAxisValues.add(accSensorVals[2]); // [2] - Z axis

        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > DELAY){ // only reads data twice per second
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
        long lastUpdate = System.currentTimeMillis();
            for (int i = 0; i < x.size(); i++){
                float absoluteSum = Math.abs(x.get(i)) + Math.abs(y.get(i)) + Math.abs(z.get(i));
                System.out.println("Absolute sum: " + absoluteSum);
                /*if (absoluteSum > 25) {
                    sendResult("Spike occured with value of " + absoluteSum);
                }*/

                String inputPeak = "" + absoluteSum;
                String[] split = recommender.solveOuery(inputMovement,Float.valueOf(inputPeak), Integer.valueOf(numberOfCases)).split(",");
                //recommender.solveOuery(inputMovement,Float.valueOf(inputPeak), Integer.valueOf(numberOfCases));
                sendResult(split[1]);
                /*if (split[1].equals(" MovementName=Walk")){
                    long curTime = System.currentTimeMillis();
                    long timeElapsed = curTime - lastUpdate;
                    try {
                        recordData("Walked for " + timeElapsed);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }*/
            }
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
