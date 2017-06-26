package edu.greenwich.intelligentmovementsensor;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.RemoteViews;

public class MovementNotification {

    public MovementNotification(Context context){
        showNotification(context);
    }

    private void showNotification(Context context) {

        Intent cancelIntent = new Intent(context, ClosingActivity.class);
        cancelIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        cancelIntent.putExtra("Cancelled", true);
        PendingIntent cancelPendingIntent = PendingIntent.getActivity(context, 1, cancelIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.grelogo)
                        .setColor(Color.rgb(0,82,155))
                        .setContentTitle("Movement Detecion")
                        .setContentText("Was this a drop?")
                        .addAction(0, "Yes", cancelPendingIntent)
                        .addAction(0, "No", null);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
