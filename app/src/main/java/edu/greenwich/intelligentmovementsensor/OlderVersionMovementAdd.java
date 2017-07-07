package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class OlderVersionMovementAdd extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_movement_layout);

        Intent notificationIntent = getIntent();

        final String movementName = notificationIntent.getStringExtra("MovementName");
        final float accelerometerPeak = notificationIntent.getFloatExtra("accelerometerPeak", 0f);
        final float gravitometerPeak = notificationIntent.getFloatExtra("gravitometerPeak", 0f);
        final float gyroPeak = notificationIntent.getFloatExtra("gyroPeak", 0f);


        final AddNew addNew = new AddNew();
        final String[] names = addNew.getExistingMovementNames();

        Spinner movementTxt = (Spinner) findViewById(R.id.movementTxt);
        //movementTxt.setText(movementName);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);

        int position = adapter.getPosition(movementName);

        movementTxt.setAdapter(adapter);
        movementTxt.setSelection(position);


        TextView accPeakTxt = (TextView) findViewById(R.id.accValue);
        accPeakTxt.setText(accelerometerPeak + "");

        TextView gravPeakTxt = (TextView) findViewById(R.id.gravValue);
        gravPeakTxt.setText(gravitometerPeak + "");

        TextView gyroPeakTxt = (TextView) findViewById(R.id.gyroValue);
        gyroPeakTxt.setText(gyroPeak + "");

        Button addBtn = (Button) findViewById(R.id.addBtn);
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
                        if (names[i].equals(movementName)){
                            exists = true;
                        }
                    }
                    if (!exists) {
                        addNew.addCase(movementName);
                        AddExisting addExisiting = new AddExisting();
                        addExisiting.addCase(movementName, accelerometerPeak, gravitometerPeak, gyroPeak);
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
        });
    }
}