package edu.greenwich.intelligentmovementsensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

public class MovementNotification {

    String name;
    float accelerometerPeak, gravitometerPeak, gyroPeak, time;

    public MovementNotification(Context context, String name, float accelerometerPeak, float gravitometerPeak, float gyroPeak, float time){
        this.name = name;
        this.accelerometerPeak = accelerometerPeak;
        this.gravitometerPeak = gravitometerPeak;
        this.gyroPeak = gyroPeak;
        this.time = time;

        showNotification(context);
    }

    private void showNotification(Context context) {

        Intent positiveResponseIntent = new Intent(context, AddExistingMovement.class);
        positiveResponseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        positiveResponseIntent.putExtra("MovementName", name);
        positiveResponseIntent.putExtra("accelerometerPeak", accelerometerPeak);
        positiveResponseIntent.putExtra("gravitometerPeak", gravitometerPeak);
        positiveResponseIntent.putExtra("gyroPeak", gyroPeak);
        positiveResponseIntent.putExtra("Time", time);
        PendingIntent positivePendingIntent = PendingIntent.getActivity(context, 1, positiveResponseIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent negativeResponseIntent = new Intent(context, OlderVersionMovementAdd.class);
        negativeResponseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        negativeResponseIntent.putExtra("MovementName", name);
        negativeResponseIntent.putExtra("accelerometerPeak", accelerometerPeak);
        negativeResponseIntent.putExtra("gravitometerPeak", gravitometerPeak);
        negativeResponseIntent.putExtra("gyroPeak", gyroPeak);
        negativeResponseIntent.putExtra("Time", time);

        PendingIntent negativePendingIntent = PendingIntent.getActivity(context, 1, negativeResponseIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            // only for jelly bean (4.1) and newer versions
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.grelogo)
                            .setColor(Color.rgb(0,82,155))
                            .setContentTitle("Movement Detecion")
                            .setContentText("Was this " + name)
                            .addAction(0, "Yes", positivePendingIntent)
                            .addAction(0, "No", negativePendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }
        else if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN){
            // older than jelly bean version (4.1)
            Intent nid = new Intent(context, OlderVersionMovementAdd.class);
            nid.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            nid.putExtra("MovementName", name);
            nid.putExtra("accelerometerPeak", accelerometerPeak);
            nid.putExtra("gravitometerPeak", gravitometerPeak);
            nid.putExtra("gyroPeak", gyroPeak);
            nid.putExtra("Time", time);

            PendingIntent ci = PendingIntent.getActivity(context, 2, nid,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.grelogo_small)
                            .setColor(Color.rgb(0,82,155))
                            .setContentTitle("Movement Detecion")
                            .setContentText("Was this " + name)
                            .setContentIntent(ci);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(0, mBuilder.build());
        }

    }
}