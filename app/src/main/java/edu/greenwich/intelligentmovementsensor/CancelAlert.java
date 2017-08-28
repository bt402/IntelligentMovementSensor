package edu.greenwich.intelligentmovementsensor;


import android.app.Activity;
import android.os.Bundle;

public class CancelAlert extends Activity{

    @Override
    public void onCreate(Bundle savedInstanceBundle){
        super.onCreate(savedInstanceBundle);
        Bundle extras = getIntent().getExtras();
        if (extras.getBoolean("NotiClick"))
        {
            SendAlertMessage.stopTimer();
            super.finish();
        }
    }
}
