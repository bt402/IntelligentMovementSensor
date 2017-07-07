package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class AddNewMovement extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_movement);

        Button saveMovement = (Button) findViewById(R.id.saveMovementBtn);
        final EditText movementName = (EditText) findViewById(R.id.movementNameTxt);
        Intent notificationIntent = getIntent();
        final float accelerometerPeak = notificationIntent.getFloatExtra("accelerometerPeak", 0f);
        final float gravitometerPeak = notificationIntent.getFloatExtra("gravitometerPeak", 0f);
        final float gyroPeak = notificationIntent.getFloatExtra("gyroPeak", 0f);

        saveMovement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNew addNew = new AddNew();
                try {
                    addNew.addCase(movementName.getText().toString());
                    AddExisting addExisiting = new AddExisting();
                    addExisiting.addCase(movementName.toString(), accelerometerPeak, gravitometerPeak, gyroPeak);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
