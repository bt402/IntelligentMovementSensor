package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

public class ClosingActivity extends Activity {
    @Override
    public void onCreate(Bundle savedBundleInstance){
        super.onCreate(savedBundleInstance);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //notificationManager.cancel(0);
        //System.exit(0);
        System.out.println("Yes");
    }
}
