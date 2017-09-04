package edu.greenwich.intelligentmovementsensor;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class EditNamesView extends Fragment {

    private String oldName;
    private String newName;
    private Spinner dropDown;
    private EditText newNameTxt;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.edit_names, container, false);
        dropDown = (Spinner) rootView.findViewById(R.id.currentNameSpinner);
        newNameTxt = (EditText) rootView.findViewById(R.id.newNameTxt);
        Button saveButton = (Button) rootView.findViewById(R.id.saveNameBtn);

        AddNew addNew = new AddNew();
        String[] names = addNew.getExistingMovementNames();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(), R.layout.support_simple_spinner_dropdown_item, names);

        dropDown.setAdapter(adapter);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // changeNames(oldName, newName);
                if (!isEmpty()) {
                    oldName = dropDown.getSelectedItem().toString();
                    newName = newNameTxt.getText().toString();

                    Recommender recommender = new Recommender();
                    recommender.loadengine();
                    boolean saved = recommender.changeNames(oldName, newName);
                    if (saved) {
                        Toast.makeText(getContext(), "Save Successful!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(), "Oops.. Something went wrong!", Toast.LENGTH_LONG).show();
                    }
                }
                else {
                    Toast.makeText(getContext(), "No name found!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    boolean isEmpty(){
        if (!dropDown.getSelectedItem().toString().equals("") && !newNameTxt.getText().toString().equals("")){
            return false;
        }
        return true;
    }
}
