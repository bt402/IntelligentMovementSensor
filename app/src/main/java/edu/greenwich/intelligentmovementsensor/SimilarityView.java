package edu.greenwich.intelligentmovementsensor;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.dfki.mycbr.core.Project;

public class SimilarityView extends Activity {

    Project project;
    TableLayout tableLayout;
    double[][] changedSimTbl;
    Recommender recommender;
    ArrayList<String> nameList;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sim_table);

        tableLayout = (TableLayout) findViewById(R.id.similarityTableView);
        recommender = new Recommender();

        recommender.loadengine();

        project = recommender.rec;

        double[][] simTable =
                recommender.similarityTable("MovementName", "NameSim");

        nameList = recommender.getListOfNames("MovementName", "NameSim");
        int counter = 0;

        for (int i = 0; i < simTable.length; i++) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < simTable[i].length; j++) {
                if (i == 0 || j == 0){
                   /* if (i == 0 && j == 0){
                        TextView nameTxt = new TextView(this);
                        nameTxt.setText("       ");
                        tableRow.addView(nameTxt);
                    }else {*/
                    TextView nameTxt = new TextView(this);
                    if (counter == nameList.size()) {
                        counter = 0;
                    }
                    nameTxt.setText(nameList.get(counter));
                    counter++;
                    tableRow.addView(nameTxt);
                    //}
                }
                else {
                    EditText text = new EditText(this);
                    text.setText(simTable[i][j] + "");
                    //text.setLayoutParams(tableRowParams);
                    text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                    text.setTextSize(10f);

                    tableRow.addView(text);
                }
            }
            tableLayout.addView(tableRow);
        }

        changedSimTbl = new double[simTable.length][simTable.length];


        Button saveBtn = (Button) findViewById(R.id.saveTableBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 0; i < tableLayout.getChildCount(); i++)
                {
                    TableRow row = (TableRow) tableLayout.getChildAt(i);
                    //This will iterate through the table row.
                    for(int j = 0; j < row.getChildCount(); j++)
                    {
                        if (i != 0) {
                            if (j != 0) {
                                EditText simTxt = (EditText) row.getChildAt(j);
                                double sim = Double.parseDouble(simTxt.getText().toString());
                                changedSimTbl[i][j] = sim;
                            }
                        }
                    }
                }
                recommender.saveTable("MovementName", "NameSim", changedSimTbl, nameList.toArray(new String[nameList.size()]));
                Toast.makeText(getApplicationContext(),"Saved, I think....", Toast.LENGTH_LONG).show();
            }
        });

    }
}

