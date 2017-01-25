package com.giorgosmih.flightitineraries;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    FlightSearchFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(savedInstanceState == null){
            fragment = new FlightSearchFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.activity_main, fragment).commit();
        }
    }

    public void ButtonPersonsAdd(View v){
        EditText et = ((EditText)v.getRootView().findViewById(R.id.editTextPersons));
        int n = Integer.parseInt(et.getText().toString()) + 1;
        et.setText(String.valueOf(n));
    }

    public void ButtonPersonsMinus(View v){
        EditText et = ((EditText)v.getRootView().findViewById(R.id.editTextPersons));
        int n = Integer.parseInt(et.getText().toString()) - 1;
        if(n < 0)
            n = 0;
        et.setText(String.valueOf(n));
    }
}
