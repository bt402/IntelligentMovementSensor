package edu.greenwich.intelligentmovementsensor;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity implements SensorEventListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent notificationIntent = getIntent();
        // check if cancelled button on notification has been pressed
        boolean cancelled = notificationIntent.getBooleanExtra("Cancelled", false);

        if (cancelled){
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(0);
        }

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


        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        final Button recordBtn = (Button) findViewById(R.id.button);
        final TextView recordLbl = (TextView) findViewById(R.id.recordLbl);

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (recordBtn.getText().equals("Record Data")){

                    // Create a dialog to ask for a movement name
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    // Add a editable text area
                    final EditText inputTxt = new EditText(MainActivity.this);
                    inputTxt.setHint("Enter the movement name here"); // set placeholder for text area

                    // Use layout to add the text area to the dialog
                    LinearLayout layout = new LinearLayout(MainActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    alertDialog.setTitle("Name the movement"); // Dialog Title
                    layout.addView(inputTxt); // add the text area to the layout
                    alertDialog.setView(layout); // now add the layout to the dialog box

                    alertDialog.setPositiveButton("Record", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // what to do when Save is clicked
                            recordBtn.setText("Stop");
                            recordLbl.setText("Recording...");
                            BackgroundService.fileName = inputTxt.getText().toString();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    startService(new Intent(MainActivity.this,BackgroundService.class));
                                }
                            }).start();
                            //sensorMgr.unregisterListener(this);
                        }
                    });

                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                        }
                    });

                    alertDialog.show();  //<-- See This!
                }
                else if (recordBtn.getText().equals("Stop")){
                    System.out.println("Stopped");
                    recordBtn.setText("Record Data");
                    recordLbl.setText("");
                }

            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        TextView textBox = (TextView) findViewById(R.id.dataTxt);

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

        textBox.setText(text);

        StringBuilder msg = new StringBuilder(sensorEvent.sensor.getName())
                .append(" ");
        for (float value : sensorEvent.values) {
            msg.append("[").append(String.format("%.3f", value)).append("]");
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        //startService(new Intent(MainActivity.this,BackgroundService.class));
        //mSensorManager.unregisterListener(this);
    }

}
