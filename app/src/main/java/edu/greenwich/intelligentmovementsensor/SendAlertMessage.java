package edu.greenwich.intelligentmovementsensor;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SendAlertMessage {

    static LocationManager mLocationManager;

    static double longitude;
    static double latitude;

    private static Timer myTimer;

    static String address;

    public SendAlertMessage(){}

    public static void startTimer(Context context){
        // 10000ms = 10s
        final int DELAY = 10000;

        mLocationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        String bestProvider = mLocationManager.getBestProvider(criteria, false);
        Location myLocation = null;

        try {
            myLocation = mLocationManager.getLastKnownLocation(bestProvider);
        }
        catch (SecurityException e){};

        longitude = myLocation.getLongitude();
        latitude = myLocation.getLatitude();

        // get the closest address
        try {
            Geocoder mGeocoder = new Geocoder(context);
            List<Address> l = mGeocoder.getFromLocation(latitude, longitude, 1);
            for (Address a: l) {
                address = a.getAddressLine(0) + " " + a.getAdminArea() + " " + a.getSubAdminArea();
            }
        } catch (IOException e) {
            Log.e("GEOCODER ERROR", "", e);
        }

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                // send message here
                sendAlert();
            }
        }, DELAY);
    }

    public static void stopTimer(){
        myTimer.cancel();
    }

    public static void sendAlert(){
        new AsyncTask<Void, Void, Void>() {
            @Override public Void doInBackground(Void... arg) {
                try {
                    GMailSender sender = new GMailSender("terrybrett94@gmail.com", "Presario1");
                    sender.sendMail("This is Subject",
                            "Location of the incident: latitude - " + latitude + ", longitude - " + longitude
                            + "\n Approximate address: " + address,
                            "terrybrett94@gmail.com",
                            "bt402@greenwich.ac.uk");
                } catch (Exception e) {
                    Log.e("SendMail", e.getMessage(), e);
                }
                return null;
            }
        }.execute();

    }
}
