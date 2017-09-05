package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class AddView extends Fragment implements SensorEventListener {

    LinearLayout layout = null;
    Spinner dropDown;
    EditText newMovementNameText;
    SensorManager mSensorManager;

    static int editTextID = -1;
    static int spinnerID = -1;

    // Define Sensors
    Sensor mAccelerometer;
    Sensor mGyroscope;
    Sensor mGravitometer;

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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.record_movement, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final RadioButton newRadioButton = (RadioButton) rootView.findViewById(R.id.newRadioBtn);
        final RadioButton existingRadioButton = (RadioButton) rootView.findViewById(R.id.existinRadioBtn);
        RadioGroup radioGroup = (RadioGroup) rootView.findViewById(R.id.radioButtonGroup);

        final Button recordBtn = (Button) rootView.findViewById(R.id.recordBtn);
        layout = (LinearLayout) rootView.findViewById(R.id.recScreenLinearLayout);

        final AddNew addNew = new AddNew();
        final AddExisting addExisting = new AddExisting();
        final String[] names = addNew.getExistingMovementNames();


        // Listen for change on the group rather than individual radio buttons
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (newRadioButton.isChecked()){
                    // Add editext
                    removeItem();
                    newMovementNameText = new EditText(getActivity());
                    LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    newMovementNameText.setLayoutParams(mRparams);
                    newMovementNameText.setHint("Enter name here");
                    newMovementNameText.setId(R.id.editTextID);
                    editTextID = R.id.editTextID;
                    layout.addView(newMovementNameText);
                }
                else if (existingRadioButton.isChecked()){
                    // Add dropdown
                    removeItem();
                    dropDown = new Spinner(getActivity());
                    LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dropDown.setLayoutParams(mRparams);

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            getContext(), R.layout.support_simple_spinner_dropdown_item, names);

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    dropDown.setAdapter(adapter);
                    dropDown.setId(R.id.spinnerID);
                    spinnerID = R.id.spinnerID;
                    layout.addView(dropDown);
                }
            }
        });

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check all info exists and start record
                String buttonText = recordBtn.getText().toString();

                if (newRadioButton.isChecked() || existingRadioButton.isChecked()){
                    if (!isEmpty()){
                        if (buttonText.equals("Start Recording")){
                            recordBtn.setText("Stop Recording");

                            mSensorManager = (SensorManager) getActivity().getSystemService(Activity.SENSOR_SERVICE);

                            mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                            mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
                            mGravitometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

                            mSensorManager.registerListener(AddView.this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
                            mSensorManager.registerListener(AddView.this, mGyroscope, mSensorManager.SENSOR_DELAY_NORMAL);
                            mSensorManager.registerListener(AddView.this, mGravitometer, mSensorManager.SENSOR_DELAY_NORMAL);

                        }
                        else if (buttonText.equals("Stop Recording")){
                            mSensorManager.unregisterListener(AddView.this);
                            recordBtn.setText("Start Recording");

                            String accInputPeak = "" + absoluteSum(accXAxisValues, accYAxisValues, accZAxisValues);
                            String gravInputPeak = "" + absoluteSum(gravXAxisValues, gravYAxisValues, gravZAxisValues);
                            String gyroInputPeak = "" + absoluteSum(gyroXAxisValues, gyroYAxisValues, gyroZAxisValues);
                            String name;

                            if (newMovementNameText != null){
                                if (!newMovementNameText.getText().equals("")){
                                    name = newMovementNameText.getText().toString();
                                    try {
                                        addNew.addCase(name);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    addExisting.addCase(name, Float.parseFloat(accInputPeak), Float.parseFloat(gravInputPeak), Float.parseFloat(gyroInputPeak));
                                }
                            }
                            else {
                                name = dropDown.getSelectedItem().toString();
                                addExisting.addCase(name, Float.parseFloat(accInputPeak), Float.parseFloat(gravInputPeak), Float.parseFloat(gyroInputPeak));
                            }

                            Toast toast = Toast.makeText(getContext(), "Movement added!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), "No name found", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast toast = Toast.makeText(getContext(), "Please make a selection", Toast.LENGTH_LONG);
                    toast.show();
                }

            }
        });

        return rootView;
    }

    void removeItem(){
        if(editTextID != -1){
            layout.removeView(newMovementNameText);
        }
        if (spinnerID != -1){
            layout.removeView(dropDown);
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

    boolean isEmpty(){
        if (newMovementNameText != null){
            if (!newMovementNameText.getText().toString().equals("")){
                return false;
            }
        }
        else if (dropDown != null){
            if (!dropDown.getSelectedItem().toString().equals("")){
                return false;
            }
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
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
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
