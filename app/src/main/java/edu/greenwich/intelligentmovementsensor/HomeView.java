package edu.greenwich.intelligentmovementsensor;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class HomeView extends Fragment {

    TextView statusColourTxt;
    private Spinner lookupFrequency;
    private Switch enableNotification;
    private  SharedPreferences.Editor editor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.home_view, container, false);
        final Button startStopBtn = (Button) rootView.findViewById(R.id.startstopBtn);
        statusColourTxt = (TextView) rootView.findViewById(R.id.statusColourTxt);

        TextView editNamesTxt = (TextView) rootView.findViewById(R.id.editNamesTxt);
        editNamesTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new EditNamesView();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.mainContent, fragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        editor = sharedPref.edit();


        // assign objects before setting the values from saved settings
        lookupFrequency = (Spinner) rootView.findViewById(R.id.lookupFrequencySpinner);
        enableNotification = (Switch) rootView.findViewById(R.id.enableNotificationSwitch);

        loadSettings();

        TextView noOfMovementstxt = (TextView) rootView.findViewById(R.id.noOfMovementsTxt);

        if (BackgroundDetector.noOfMovements != -1){
            noOfMovementstxt.setText("Number of Movements: " + BackgroundDetector.noOfMovements);
        }

        // save the settings using listeners
        lookupFrequency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor.putInt("lookupFrequencyPosition", lookupFrequency.getSelectedItemPosition());
                editor.putInt("lookupFrequencyInteger", Integer.parseInt(lookupFrequency.getSelectedItem().toString()));
                editor.commit();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        enableNotification.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                boolean checked = enableNotification.isChecked();
                editor.putBoolean("enableNotificationBool", checked);
                editor.commit();
            }
        });

        startStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startStopBtn.getText().equals("Stop")){
                    startStopBtn.setText("Start");
                    statusColourTxt.setText("Off");
                    statusColourTxt.setTextColor(Color.RED);
                    StatusView.text = "Off";
                    StatusView.colour = Color.RED;
                    BackgroundDetector.stopService();
                }
                else if (startStopBtn.getText().equals("Start")){
                    startStopBtn.setText("Stop");
                    statusColourTxt.setText("On");
                    statusColourTxt.setTextColor(Color.GREEN);
                    StatusView.text = "On";
                    StatusView.colour = Color.GREEN;
                    getActivity().startService(MainActivity.intent);
                }
            }
        });

        return rootView;
    }

    void loadSettings(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isChecked = sharedPref.getBoolean("enableNotificationBool", true);
        int lookupFrequencyInt = sharedPref.getInt("lookupFrequencyPosition", 0); // 2.5 seconds default delay

        enableNotification.setChecked(isChecked);
        lookupFrequency.setSelection(lookupFrequencyInt);
    }
}
