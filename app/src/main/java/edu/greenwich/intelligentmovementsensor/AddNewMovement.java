package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class AddNewMovement extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_movement);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Button saveMovement = (Button) findViewById(R.id.saveMovementBtn);
        final EditText movementName = (EditText) findViewById(R.id.movementNameTxt);

        saveMovement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNew addNew = new AddNew();
                try {
                    addNew.addCase(movementName.getText().toString());
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}