package edu.greenwich.intelligentmovementsensor;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusView extends Fragment {

    static String text = "On";
    static int colour = Color.GREEN;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View statusView = inflater.inflate(R.layout.status_fragment, container, false);
        TextView statusColourTxtMain = (TextView) statusView.findViewById(R.id.statusColourTxtMain);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        boolean active = sharedPref.getBoolean("active", true);

        if (active){
            colour = Color.GREEN;
            text = "On";
        }
        else if (!active){
            colour = Color.RED;
            text = "Off";
        }

        statusColourTxtMain.setText(text);
        statusColourTxtMain.setTextColor(colour);

        // Inflate the layout for this fragment
        return statusView;
    }

}
