package edu.greenwich.intelligentmovementsensor;


import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;

public class TabsActivity  extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_main);

        TabHost mTabHost = getTabHost();

        mTabHost.addTab(mTabHost.newTabSpec("first").setIndicator("Record Data").setContent(new Intent(this  ,MainActivity.class )));
        mTabHost.addTab(mTabHost.newTabSpec("second").setIndicator("Graph View").setContent(new Intent(this , GraphView.class )));
        mTabHost.setCurrentTab(0);


    }
}