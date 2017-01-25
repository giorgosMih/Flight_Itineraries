package com.giorgosmih.flightitineraries;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class DetailActivity extends AppCompatActivity {

    DetailActivityFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if(savedInstanceState == null){
            fragment = new DetailActivityFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.activity_detail, fragment).commit();
        }
    }
}