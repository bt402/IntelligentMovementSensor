package edu.greenwich.intelligentmovementsensor;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.dfki.mycbr.core.similarity.AmalgamationFct;

public class BackgroundDetector extends Service implements SensorEventListener, SharedPreferences.OnSharedPreferenceChangeListener{

    private SensorManager mSensorManager = null;
    private Sensor mAccelerometer = null;
    private Sensor mGyroscope = null;
    private Sensor mGravitometer = null;

    float[] accSensorVals, gravSensorVals;

    ArrayList<Float> accXAxisValues = new ArrayList<>();
    ArrayList<Float> accYAxisValues = new ArrayList<>();
    ArrayList<Float> accZAxisValues = new ArrayList<>();

    ArrayList<Float> gravXAxisValues = new ArrayList<>();
    ArrayList<Float> gravYAxisValues = new ArrayList<>();
    ArrayList<Float> gravZAxisValues = new ArrayList<>();

    ArrayList<Float> gyroXAxisValues = new ArrayList<>();
    ArrayList<Float> gyroYAxisValues = new ArrayList<>();
    ArrayList<Float> gyroZAxisValues = new ArrayList<>();

    ArrayList<Float> accXAxisSecondBuffer = new ArrayList<>();
    ArrayList<Float> accYAxisSecondBuffer = new ArrayList<>();
    ArrayList<Float> accZAxisSecondBuffer = new ArrayList<>();

    ArrayList<Float> gravXAxisSecondBuffer = new ArrayList<>();
    ArrayList<Float> gravYAxisSecondBuffer = new ArrayList<>();
    ArrayList<Float> gravZAxisSecondBuffer = new ArrayList<>();

    ArrayList<Float> gyroXAxisSecondBuffer = new ArrayList<>();
    ArrayList<Float> gyroYAxisSecondBuffer = new ArrayList<>();
    ArrayList<Float> gyroZAxisSecondBuffer = new ArrayList<>();

    ArrayList<Float> accXAxisCopy = new ArrayList<>();
    ArrayList<Float> accYAxisCopy = new ArrayList<>();
    ArrayList<Float> accZAxisCopy = new ArrayList<>();

    ArrayList<Float> gravXAxisCopy = new ArrayList<>();
    ArrayList<Float> gravYAxisCopy = new ArrayList<>();
    ArrayList<Float> gravZAxisCopy = new ArrayList<>();

    ArrayList<Float> gyroXAxisCopy = new ArrayList<>();
    ArrayList<Float> gyroYAxisCopy = new ArrayList<>();
    ArrayList<Float> gyroZAxisCopy = new ArrayList<>();

    ArrayList<Float> accSecondBufferAverage, gravSecondBufferAverage, gyrosecondBufferAverage = new ArrayList<>();

    static int DELAY;
    long lastUpdate;
    long lastUpdateTwo;

    LocalBroadcastManager broadcaster;

    private final IBinder mBinder = new LocalBinder();

    static Recommender recommender;
    static String inputAmalgamation;
    String inputMovement;
    String numberOfCases;
    long startTime = 0;
    long endTime = 0;

    static Service service;

    private static AlertNotification alertNotification;
    public static int noOfMovements = -1;
    boolean enabledNotifications;

    @Override
    public void onCreate() {
        service = this;
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravitometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravitometer, mSensorManager.SENSOR_STATUS_ACCURACY_MEDIUM); // MEDIUM is less power consumption
        broadcaster = LocalBroadcastManager.getInstance(this);
        lastUpdate = System.currentTimeMillis();
        lastUpdateTwo = System.currentTimeMillis();
        startTime = System.currentTimeMillis();

        recommender = new Recommender();
        recommender.loadengine();
        inputAmalgamation = recommender.myConcept.getActiveAmalgamFct().getName();

        inputMovement = "_unknown_"; // Freefall, Bunce, Impact, Still, Flat, Walk
        numberOfCases = "5";

        noOfMovements = recommender.getNoOfMovements();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        enabledNotifications = sharedPref.getBoolean("enableNotificationBool", true);
        DELAY = sharedPref.getInt("lookupFrequencyInteger", 2500);

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
        /*if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accSensorVals = lowPass(event.values.clone(), accSensorVals);
        }*/
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean active = sharedPref.getBoolean("active", true);
        if (active){

            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    accSensorVals = lowPass(event.values.clone(), accSensorVals);
                    accXAxisValues.add(accSensorVals[0]);
                    accYAxisValues.add(accSensorVals[1]);
                    accZAxisValues.add(accSensorVals[2]);

                    accXAxisSecondBuffer.add(accSensorVals[0]);
                    accYAxisSecondBuffer.add(accSensorVals[1]);
                    accZAxisSecondBuffer.add(accSensorVals[2]);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    //gyroSensorVals = lowPass(event.values.clone(), gyroSensorVals);
                    gyroXAxisValues.add(event.values[0]);
                    gyroYAxisValues.add(event.values[1]);
                    gyroZAxisValues.add(event.values[2]);

                    gyroXAxisSecondBuffer.add(event.values[0]);
                    gyroYAxisSecondBuffer.add(event.values[1]);
                    gyroZAxisSecondBuffer.add(event.values[2]);
                    break;
                case Sensor.TYPE_GRAVITY:
                    gravSensorVals = lowPass(event.values.clone(), gravSensorVals);
                    gravXAxisValues.add(gravSensorVals[0]);
                    gravYAxisValues.add(gravSensorVals[1]);
                    gravZAxisValues.add(gravSensorVals[2]);

                    gravXAxisSecondBuffer.add(gravSensorVals[0]);
                    gravYAxisSecondBuffer.add(gravSensorVals[1]);
                    gravZAxisSecondBuffer.add(gravSensorVals[2]);
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
                startTime = curTime;
                //System.out.println("Hello every 5 seconds");
                checkSpikes(accXAxisValues, accYAxisValues, accZAxisValues,
                        gravXAxisValues, gravYAxisValues, gravZAxisValues,
                        gyroXAxisValues, gyroYAxisValues, gyroZAxisValues);

                accXAxisCopy = new ArrayList<>(accXAxisValues);
                accYAxisCopy = new ArrayList<>(accYAxisValues);
                accZAxisCopy = new ArrayList<>(accZAxisValues);

                gravXAxisCopy = new ArrayList<>(gravXAxisValues);
                gravYAxisCopy = new ArrayList<>(gravYAxisValues);
                gravZAxisCopy = new ArrayList<>(gravZAxisValues);

                gyroXAxisCopy = new ArrayList<>(gyroXAxisValues);
                gyroYAxisCopy = new ArrayList<>(gyroYAxisValues);
                gyroZAxisCopy = new ArrayList<>(gyroZAxisValues);

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

            if ((curTime - lastUpdateTwo) > 3000){ // only reads data twice per second
                lastUpdateTwo = curTime;
                //System.out.println("Hello every 5 seconds");
                checkSpikes(accXAxisSecondBuffer, accYAxisSecondBuffer, accZAxisSecondBuffer,
                        gravXAxisSecondBuffer, gravYAxisSecondBuffer, gravZAxisSecondBuffer,
                        gyroXAxisSecondBuffer, gyroYAxisSecondBuffer, gyroZAxisSecondBuffer);

                accXAxisValues = new ArrayList<>(compareLists(accXAxisCopy, accXAxisSecondBuffer));
                accYAxisValues = new ArrayList<>(compareLists(accYAxisCopy, accYAxisSecondBuffer));
                accZAxisValues = new ArrayList<>(compareLists(accZAxisCopy, accZAxisSecondBuffer));

                gravXAxisValues = new ArrayList<>(compareLists(gravXAxisCopy, gravXAxisSecondBuffer));
                gravYAxisValues = new ArrayList<>(compareLists(gravYAxisCopy, gravYAxisSecondBuffer));
                gravZAxisValues = new ArrayList<>(compareLists(gravZAxisCopy, gravZAxisSecondBuffer));

                gyroXAxisValues = new ArrayList<>(compareLists(gyroXAxisCopy, gyroXAxisSecondBuffer));
                gyroYAxisValues = new ArrayList<>(compareLists(gyroYAxisCopy, gyroYAxisSecondBuffer));
                gyroZAxisValues = new ArrayList<>(compareLists(gyroZAxisCopy, gyroZAxisSecondBuffer));

                accXAxisCopy = new ArrayList<>();
                accYAxisCopy = new ArrayList<>();
                accZAxisCopy = new ArrayList<>();

                gravXAxisCopy = new ArrayList<>();
                gravYAxisCopy = new ArrayList<>();
                gravZAxisCopy = new ArrayList<>();

                gyroXAxisCopy = new ArrayList<>();
                gyroYAxisCopy = new ArrayList<>();
                gyroZAxisCopy = new ArrayList<>();

                accXAxisSecondBuffer = new ArrayList<>();
                accYAxisSecondBuffer = new ArrayList<>();
                accZAxisSecondBuffer = new ArrayList<>();

                gravXAxisSecondBuffer = new ArrayList<>();
                gravYAxisSecondBuffer = new ArrayList<>();
                gravZAxisSecondBuffer = new ArrayList<>();

                gyroXAxisSecondBuffer = new ArrayList<>();
                gyroYAxisSecondBuffer = new ArrayList<>();
                gyroZAxisSecondBuffer = new ArrayList<>();
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

        System.out.println("Absolute sum accelerometer average: " + absoluteSum(accXAxisValues, accYAxisValues, accZzAxisValues));
        String accInputPeak = "" + absoluteSum(accXAxisValues, accYAxisValues, accZzAxisValues);

        System.out.println("Absolute sum gravitometer average: " + absoluteSum(gravXAxisValues, gravYAxisValues, gravZAxisValues));
        String gravInputPeak = "" + absoluteSum(gravXAxisValues, gravYAxisValues, gravZAxisValues);

        System.out.println("Absolute sum gyroscope average: " + absoluteSum(gyroXAxisValues, gyroYAxisValues, gyroZAxisValues));
        String gyroInputPeak = "" + absoluteSum(gyroXAxisValues, gyroYAxisValues, gyroZAxisValues);

        endTime = System.currentTimeMillis();
        long timePassed = endTime - startTime;
        double time = ((double)timePassed/1000);
        if (timePassed > 10L) {
            System.out.println("Time: " + time);
        }
        float timeFloat = Float.parseFloat("" + time);
        String[] split = recommender.solveOuery(inputMovement,Float.valueOf(accInputPeak), Float.valueOf(gravInputPeak), Float.valueOf(gyroInputPeak), Integer.valueOf(numberOfCases), Float.valueOf(timeFloat)).split(",");
        recommender.similarityTable("MovementName", "NameSim");
        //recommender.solveOuery(inputMovement,Float.valueOf(inputPeak), Integer.valueOf(numberOfCases));

        float accPeak = 0f;
        float gravPeak = 0f;
        float gyroPeak = 0f;
        float similarity = 0f;
        String name = "";

        for (int i = 0; i < split.length; i ++){
            if (split[i].contains("Acc")){
                String val[] = split[i].split("=");
                accPeak = Float.parseFloat(val[1].split("\\}")[0]);
            }
            else if (split[i].contains("Gyro")){
                String val[] = split[i].split("=");
                gyroPeak = Float.parseFloat(val[1].split("\\}")[0]);
            }
            else if (split[i].contains("Grav")){
                String val[] = split[i].split("=");
                gravPeak = Float.parseFloat(val[1].split("\\}")[0]);
            }
            else if (split[i].contains("Sim")){
                String val[] = split[i].split("=");
                similarity = Float.parseFloat(val[1].split("\\}")[0]);
            }
            else if (split[i].contains("Mov")){
                String val[] = split[i].split("=");
                name = val[1].split("\\}")[0];
            }
        }

        // create and show notification
        /*if (similarity < 0.90f){
            // less than 70% certainty
            if (enabledNotifications) {
                MovementNotification movementNotification = new MovementNotification(this, name, Float.valueOf(accPeak), Float.valueOf(gravPeak), Float.valueOf(gyroPeak));
            }
        }*/

        String test = "Impact";
        if (name.equals("Impact")){
            if (alertNotification == null) {
                alertNotification = new AlertNotification(this);
            }
        }


        if (split.length > 1)
            sendResult(name);
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

    public double absoluteSum(ArrayList<Float> x, ArrayList<Float> y, ArrayList<Float> z){
        double sum = 0.0;
        double average;
        int count = 0;
        for (int i = 0; i < x.size(); i++){
            float absoluteSum = Math.abs(x.get(i) + y.get(i) + z.get(i));
            sum += absoluteSum;
            count ++;
        }
        average = sum / count;
        return average;
    }

    public List<Float> compareLists(ArrayList<Float> listOne, ArrayList<Float> listTwo){
        List<Float> sourceList = new ArrayList<>(listOne);
        List<Float> destinationList = new ArrayList<>(listTwo);


        sourceList.removeAll( listTwo );
        destinationList.removeAll( listOne );

        return sourceList;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean active = sharedPref.getBoolean("active", true);
        if (!active){
            // unregister sensor listeners
            mSensorManager.unregisterListener(this);
        }
        else if (active){
            // check if listener is registered
            mSensorManager.registerListener(this, mGyroscope, mSensorManager.SENSOR_DELAY_NORMAL);
            mSensorManager.registerListener(this, mGravitometer, mSensorManager.SENSOR_STATUS_ACCURACY_MEDIUM);
        }
    }

    public class LocalBinder extends Binder {
        BackgroundDetector getService() {
            return BackgroundDetector.this;
        }
    }

}
