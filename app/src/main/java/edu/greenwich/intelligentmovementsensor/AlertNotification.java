package edu.greenwich.intelligentmovementsensor;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

public class AlertNotification {

    public static NotificationCompat.Builder mBuilder;
    public static NotificationManager mNotificationManager;

    AlertNotification(Context context){
        showAlertNotification(context);
    }

    public void showAlertNotification(Context context){

        Intent cancelAlert = new Intent(context, CancelAlert.class);
        cancelAlert.putExtra("NotiClick",true);
        cancelAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, cancelAlert,
                PendingIntent.FLAG_CANCEL_CURRENT);


        mBuilder = new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_warning)
                        .setColor(Color.rgb(0,82,155))
                        .setLights(Color.BLUE, 2000, 1000)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                        .setContentTitle("Alert! The Application has detected an impact!")
                        .setContentText("Countdown to sent an alert has started(10). Tap to cancel")
                        .setLights(Color.BLUE, 2000, 1000)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
        mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(631, mBuilder.build());
        int DELAY = 10;
        while (DELAY != 0){
            try {
                Thread.sleep(1000);
                DELAY--;
                mBuilder.setContentText("Countdown to sent an alert has started(" + DELAY + "). Tap to cancel");
                mBuilder.setOnlyAlertOnce(true);
                mNotificationManager.notify(631, mBuilder.build());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        SendAlertMessage.startTimer(context);
    }
}
