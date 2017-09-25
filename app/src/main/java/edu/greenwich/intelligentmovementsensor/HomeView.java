package edu.greenwich.intelligentmovementsensor;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class HomeView extends Fragment {

    TextView statusColourTxt;
    private Spinner lookupFrequency;
    private Switch enableNotification;
    private SharedPreferences.Editor editor;
    private Button saveAlertButton;
    private EditText phoneNumber;
    private EditText emailAddress;
    Button startStopBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.home_view, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        startStopBtn = (Button) rootView.findViewById(R.id.startstopBtn);
        statusColourTxt = (TextView) rootView.findViewById(R.id.statusColourTxt);

        saveAlertButton = (Button) rootView.findViewById(R.id.saveAlertButton);

        phoneNumber = (EditText) rootView.findViewById(R.id.phone_number);
        emailAddress = (EditText) rootView.findViewById(R.id.email_address);

        phoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveAlertButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                saveAlertButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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
                    editor.putBoolean("active", false);
                    editor.commit();
                }
                else if (startStopBtn.getText().equals("Start")){
                    startStopBtn.setText("Stop");
                    statusColourTxt.setText("On");
                    statusColourTxt.setTextColor(Color.GREEN);
                    StatusView.text = "On";
                    StatusView.colour = Color.GREEN;
                    editor.putBoolean("active", true);
                    editor.commit();
                }
            }
        });

        saveAlertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailAddress.getText().toString();
                String phone = phoneNumber.getText().toString();

                boolean validNumber = false;

                if (phoneNumber.length() < 6 || phoneNumber.length() > 13) {
                    validNumber = false;
                }
                else {
                    validNumber = android.util.Patterns.PHONE.matcher(phone).matches();
                }

                if (!validNumber){
                    Toast.makeText(getContext(), "Invalid phone number!", Toast.LENGTH_LONG).show();
                }
                else if (!email.matches("^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3}$")){
                    Toast.makeText(getContext(), "Invalid email address!", Toast.LENGTH_LONG).show();
                }
                else {
                    editor.putString("phone_number", phone);
                    editor.putString("email_address", email);
                    editor.commit();
                    Toast.makeText(getContext(), "Contacts Updated!", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    void loadSettings(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean isChecked = sharedPref.getBoolean("enableNotificationBool", true);
        int lookupFrequencyInt = sharedPref.getInt("lookupFrequencyPosition", 0); // 2.5 seconds default delay
        String phone = sharedPref.getString("phone_number", "000");
        String email = sharedPref.getString("email_address", "example.com");
        boolean active = sharedPref.getBoolean("active", true);

        enableNotification.setChecked(isChecked);
        lookupFrequency.setSelection(lookupFrequencyInt);
        phoneNumber.setText(phone);
        emailAddress.setText(email);
        if (active){
            statusColourTxt.setText("On");
            statusColourTxt.setTextColor(Color.GREEN);
            startStopBtn.setText("Stop");
        }
        else if(!active){
            statusColourTxt.setText("Off");
            statusColourTxt.setTextColor(Color.RED);
            startStopBtn.setText("Start");
        }
    }
}
