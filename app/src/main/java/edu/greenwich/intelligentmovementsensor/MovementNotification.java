package edu.greenwich.intelligentmovementsensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;

public class MovementNotification {

    String name;
    float accelerometerPeak, gravitometerPeak, gyroPeak;

    public MovementNotification(Context context, String name, float accelerometerPeak, float gravitometerPeak, float gyroPeak){
        this.name = name;
        this.accelerometerPeak = accelerometerPeak;
        this.gravitometerPeak = gravitometerPeak;
        this.gyroPeak = gyroPeak;

        showNotification(context);
    }

    private void showNotification(Context context) {

        Intent positiveResponseIntent = new Intent(context, AddExistingMovement.class);
        positiveResponseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        positiveResponseIntent.putExtra("MovementName", name);
        positiveResponseIntent.putExtra("accelerometerPeak", accelerometerPeak);
        positiveResponseIntent.putExtra("gravitometerPeak", gravitometerPeak);
        positiveResponseIntent.putExtra("gyroPeak", gyroPeak);
        PendingIntent positivePendingIntent = PendingIntent.getActivity(context, 1, positiveResponseIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent negativeResponseIntent = new Intent(context, AddNewMovement.class);
        negativeResponseIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent negativePendingIntent = PendingIntent.getActivity(context, 1, negativeResponseIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.grelogo)
                        .setColor(Color.rgb(0,82,155))
                        .setContentTitle("Movement Detecion")
                        .setContentText("Was this a " + name)
                        .addAction(0, "Yes", positivePendingIntent)
                        .addAction(0, "No", negativePendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
