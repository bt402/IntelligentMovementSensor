package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

public class AddExistingMovement extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ProgressBar spinner = (ProgressBar)findViewById(R.id.progressBar);

        spinner.setVisibility(View.VISIBLE);

        Intent notificationIntent = getIntent();

        String movementName = notificationIntent.getStringExtra("MovementName");
        float accelerometerPeak = notificationIntent.getFloatExtra("accelerometerPeak", 0f);
        float gravitometerPeak = notificationIntent.getFloatExtra("gravitometerPeak", 0f);
        float gyroPeak = notificationIntent.getFloatExtra("gyroPeak", 0f);

        String[] split = movementName.split("=");

        AddExisting addExisiting = new AddExisting();
        addExisiting.addCase(split[1], accelerometerPeak, gravitometerPeak, gyroPeak);
        spinner.setVisibility(View.GONE);
        finish();
    }
}
