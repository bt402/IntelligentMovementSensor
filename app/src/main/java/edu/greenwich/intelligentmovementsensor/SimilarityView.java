package edu.greenwich.intelligentmovementsensor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.dfki.mycbr.core.Project;

public class SimilarityView extends Fragment {

    Project project;
    TableLayout tableLayout;
    double[][] changedSimTbl;
    Recommender recommender;
    ArrayList<String> nameList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.sim_table, container, false);
        tableLayout = (TableLayout) rootView.findViewById(R.id.similarityTableView);
        recommender = new Recommender();

        recommender.loadengine();

        project = recommender.rec;

        double[][] simTable =
                recommender.similarityTable("MovementName", "NameSim");

        nameList = recommender.getListOfNames("MovementName");
        int counter = 0;
        int row = 0;
        int column = 0;

        for (int i = 0; i < nameList.size() + 1; i++) {
            TableRow tableRow = new TableRow(getActivity());
            tableRow.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT));

            for (int j = 0; j < nameList.size() + 1; j++) {
                // Add empty space top left corner
               if (i == 0 && j == 0){
                   TextView nameTxt = new TextView(getActivity());
                   nameTxt.setText("       ");
                   tableRow.addView(nameTxt);
               }
               // headers(names) horizontally
               if (i == 0 && j > 0){
                   TextView nameTxt = new TextView(getActivity());
                   if (counter == nameList.size()) {
                       counter = 0;
                   }
                   nameTxt.setText(nameList.get(counter));
                   counter++;
                   tableRow.addView(nameTxt);
               }
               // headers(names) vertically
               if (i > 0 && j == 0){
                   TextView nameTxt = new TextView(getActivity());
                   if (counter == nameList.size()) {
                       counter = 0;
                   }
                   nameTxt.setText(nameList.get(counter));
                   counter++;
                   tableRow.addView(nameTxt);
               }
               // similarity
               if (i >= 1 && j >= 1){
                   EditText text = new EditText(getActivity());
                   row = i - 1;
                   column = j - 1;
                   text.setText(simTable[row][column] + "");
                   //text.setLayoutParams(tableRowParams);
                   text.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
                   text.setTextSize(10f);

                   tableRow.addView(text);
               }
            }
            tableLayout.addView(tableRow);
        }

        changedSimTbl = new double[simTable.length][simTable.length];


        Button saveBtn = (Button) rootView.findViewById(R.id.saveTableBtn);
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
                Toast.makeText(getActivity().getApplicationContext(),"Saved", Toast.LENGTH_LONG).show();
            }
        });

        return rootView;
    }
}