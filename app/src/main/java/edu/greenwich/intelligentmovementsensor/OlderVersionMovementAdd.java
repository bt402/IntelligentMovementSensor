package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OlderVersionMovementAdd extends AppCompatActivity{

    LinearLayout layout = null;
    Spinner dropDown;
    EditText newMovementNameText;

    static int editTextID = -1;
    static int spinnerID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_movement_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        final Activity activity = this;
        activity.setTitle("Add Recorded Movement");

        Intent notificationIntent = getIntent();
        final String movementName = notificationIntent.getStringExtra("MovementName");
        final float accelerometerPeak = notificationIntent.getFloatExtra("accelerometerPeak", 0f);
        final float gravitometerPeak = notificationIntent.getFloatExtra("gravitometerPeak", 0f);
        final float gyroPeak = notificationIntent.getFloatExtra("gyroPeak", 0f);
        layout = (LinearLayout) findViewById(R.id.recScreenLinearLayout2);

        final AddNew addNew = new AddNew();
        final String[] names = addNew.getExistingMovementNames();

        //movementTxt.setText(movementName);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);

        final int position = adapter.getPosition(movementName);

        TextView accPeakTxt = (TextView) findViewById(R.id.accValue);
        accPeakTxt.setText("Accelerometer Peak: [" + accelerometerPeak + "]");

        TextView gravPeakTxt = (TextView) findViewById(R.id.gravValue);
        gravPeakTxt.setText("Gravitometer Peak: [" + gravitometerPeak + "]");

        TextView gyroPeakTxt = (TextView) findViewById(R.id.gyroValue);
        gyroPeakTxt.setText("Gyroscope Peak: [" + gyroPeak + "]");

        final RadioButton newRadioButton = (RadioButton) findViewById(R.id.newRadioBtn2);
        final RadioButton existingRadioButton = (RadioButton) findViewById(R.id.existinRadioBtn2);
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioButtonGroup2);


        // Listen for change on the group rather than individual radio buttons
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (newRadioButton.isChecked()){
                    // Add editext
                    removeItem();
                    newMovementNameText = new EditText(OlderVersionMovementAdd.this);
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
                    dropDown = new Spinner(OlderVersionMovementAdd.this);
                    LinearLayout.LayoutParams mRparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dropDown.setLayoutParams(mRparams);

                    dropDown.setAdapter(adapter);
                    dropDown.setSelection(position);
                    dropDown.setId(R.id.spinnerID);
                    spinnerID = R.id.spinnerID;
                    layout.addView(dropDown);
                }
            }
        });

        Button addBtn = (Button) findViewById(R.id.addBtn);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newRadioButton.isChecked()){
                    try {
                        boolean exists = false;

                        for (int i = 0; i < names.length; i ++){
                            if (names[i].equals(newMovementNameText.getText().toString())){
                                exists = true;
                            }
                        }
                        if (!exists) {
                            addNew.addCase(newMovementNameText.getText().toString());
                            AddExisting addExisiting = new AddExisting();
                            addExisiting.addCase(newMovementNameText.getText().toString(), accelerometerPeak, gravitometerPeak, gyroPeak);
                            Toast.makeText(getApplicationContext(), "Data added!",
                                    Toast.LENGTH_LONG).show();
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "This movement already exist! Use new name!",
                                    Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                else if (existingRadioButton.isChecked()){
                    AddExisting addExisiting = new AddExisting();
                    addExisiting.addCase(movementName, accelerometerPeak, gravitometerPeak, gyroPeak);
                    Toast.makeText(getApplicationContext(), "Data added!",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        /*final EditText movementEditText = (EditText) findViewById(R.id.newmovementName);

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddExisting addExisiting = new AddExisting();
                addExisiting.addCase(movementName, accelerometerPeak, gravitometerPeak, gyroPeak);
                Toast.makeText(getApplicationContext(), "Data added!",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        });

        Button addNewBtn = (Button) findViewById(R.id.addNewBtn);
        addNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    boolean exists = false;

                    for (int i = 0; i < names.length; i ++){
                        if (names[i].equals(movementEditText.getText().toString())){
                            exists = true;
                        }
                    }
                    if (!exists) {
                        addNew.addCase(movementEditText.getText().toString());
                        AddExisting addExisiting = new AddExisting();
                        addExisiting.addCase(movementEditText.getText().toString(), accelerometerPeak, gravitometerPeak, gyroPeak);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "This movement already exist! Use new name!",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

    void removeItem(){
        if(editTextID != -1){
            layout.removeView(newMovementNameText);
        }
        if (spinnerID != -1){
            layout.removeView(dropDown);
        }
    }
}