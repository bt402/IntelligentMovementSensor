package edu.greenwich.intelligentmovementsensor;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.*;
import android.support.v4.content.ContextCompat;

public class StartupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
        else {
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {
                    Intent mainIntent = new Intent(StartupActivity.this, TabsActivity.class);
                    StartupActivity.this.startActivity(mainIntent);
                    StartupActivity.this.finish();
                }
            }, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run() {
                            Intent mainIntent = new Intent(StartupActivity.this, TabsActivity.class);
                            StartupActivity.this.startActivity(mainIntent);
                            StartupActivity.this.finish();
                        }
                    }, 0);
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    System.exit(0);
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
