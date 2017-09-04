package edu.greenwich.intelligentmovementsensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager = null;

    private Sensor mAccelerometer = null;
    private Sensor mGyroscope = null;
    private Sensor mGravitometer = null;
    private Sensor mCompass = null;

    float[] lastAccelerometer;
    float[] lastCompass;

    BroadcastReceiver receiver;

    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";
    String gravData = "Gravity Data";

    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    ArrayList<NavigationItem> mNavItems = new ArrayList<>();
    ArrayList<String> tableContent = new ArrayList<>();

    public static Intent intent;

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(BackgroundDetector.RESULT)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

//            copyAssets();

        setContentView(R.layout.activity_main);
        intent = new Intent(getApplicationContext(), BackgroundDetector.class);
        startService(intent);
        //startService(new Intent(MainActivity.this,BackgroundDetector.class));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setTitle("Status");

        mNavItems.add(new NavigationItem("Home", "Enable/Disable Options", R.drawable.ic_action_home));
        mNavItems.add(new NavigationItem("Table", "Movement Similarity Table", R.drawable.ic_action_list));
        mNavItems.add(new NavigationItem("Add", "Add New Movements Manually", R.drawable.ic_action_add));
        mNavItems.add(new NavigationItem("About", "Legal Disclaimer", R.drawable.ic_action_info));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);

        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        Fragment fragment = new StatusView();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.mainContent, fragment);
        ft.addToBackStack(null);
        ft.commit();

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
            /*
            * Called when a particular item from the navigation drawer
            * is selected.
            * */
            private void selectItemFromDrawer(int position) {
                //Fragment fragment = new PreferencesFragement();
                Fragment fragment = null;

                //FragmentManager fragmentManager = getFragmentManager();
                //fragmentManager.beginTransaction()
                //        .replace(R.id.mainContent, fragment)
                 //       .commit();

                mDrawerList.setItemChecked(position, true);
                setTitle(mNavItems.get(position).mTitle);

                if(position == 0){
                    fragment = new HomeView();
                }
                else if (position == 1){
                    fragment = new SimilarityView();
                }
                else if (position == 2){
                    fragment = new AddView();
                }
                else if (position == 3){
                    fragment = new AboutView();
                }
                //replacing the fragment
                if (fragment != null) {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.mainContent, fragment);
                    ft.addToBackStack(null);
                    ft.commit();
                }
                // Close the drawer
                mDrawerLayout.closeDrawer(mDrawerPane);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s = intent.getStringExtra(BackgroundDetector.MESSAGE);

                tableContent.add(s);

                if (tableContent.size() <= 5){
                    for (int i = 1; i <= tableContent.size(); i++){
                        TextView row = (TextView) findViewById(getResources().getIdentifier("row" + i, "id", getPackageName())); // use string to indentify the textview, which will allow to loop through
                        if (row != null) {
                            row.setText(s);
                        }
                    }
                }
                else {
                    tableContent = new ArrayList<>();
                }
            }
        };

        // initialize the sensor and location manager
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);

        // set type to each sensor
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mGravitometer = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mCompass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // get data from the sensors
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravitometer, SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM); // MEDIUM is less power consumption
        mSensorManager.registerListener(this, mCompass, SensorManager.SENSOR_DELAY_NORMAL);


        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        TextView textBox = (TextView) findViewById(R.id.dataTxt);

        StringBuilder text = new StringBuilder(accelData.toString()).append("\n");
        text.append(compassData).append("\n");
        text.append(gyroData).append("\n");
        text.append(gravData).append("\n");


        // compute rotation matrix
        float rotation[] = new float[9];
        float identity[] = new float[9];
        if (lastAccelerometer != null && lastCompass != null) {
            boolean gotRotation = SensorManager.getRotationMatrix(rotation,
                    identity, lastAccelerometer, lastCompass);
            if (gotRotation) {
                float cameraRotation[] = new float[9];
                // remap such that the camera is pointing straight down the Y
                // axis
                SensorManager.remapCoordinateSystem(rotation,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                        cameraRotation);

                // orientation vector
                float orientation[] = new float[3];
                SensorManager.getOrientation(cameraRotation, orientation);

                text.append(String.format("Orientation (%.3f, %.3f, %.3f)", Math.toDegrees(orientation[0]), Math.toDegrees(orientation[1]), Math.toDegrees(orientation[2]))).append("\n");

            }
        }

        if (textBox != null) {
            textBox.setText(text);
        }

        StringBuilder msg = new StringBuilder(sensorEvent.sensor.getName())
                .append(" ");
        for (float value : sensorEvent.values) {
            msg.append("[").append(String.format("%.3f", value)).append("]");
        }

        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                lastAccelerometer = sensorEvent.values.clone();
                accelData = msg.toString();
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroData = msg.toString();
                break;
            case Sensor.TYPE_GRAVITY:
                gravData = msg.toString();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                lastCompass = sensorEvent.values.clone();
                compassData = msg.toString();
                break;
        }
    }

    private void copyAssets() {
        AssetManager assetManager = getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        for(String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);

                String outDir = Environment.getExternalStorageDirectory() + "";

                File outFile = new File(outDir, filename);

                out = new FileOutputStream(outFile);
                copyFile(in, out);
                in.close();
                in = null;
                out.flush();
                out.close();
                out = null;
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        //startService(new Intent(MainActivity.this,BackgroundService.class));
        //mSensorManager.unregisterListener(this);
    }

    class NavigationItem {
        // Add new items to the slide menu
        String mTitle;
        String mSubtitle;
        int mIcon;

        public NavigationItem(String title, String subtitle, int icon) {
            mTitle = title;
            mSubtitle = subtitle;
            mIcon = icon;
        }
    }

    class DrawerListAdapter extends BaseAdapter {
        /**
         * Bind with ListView in the menu
         **/
        Context mContext;
        ArrayList<NavigationItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavigationItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);
            ImageView iconView = (ImageView) view.findViewById(R.id.icon);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );
            iconView.setImageResource(mNavItems.get(position).mIcon);

            return view;
        }
    }
}
