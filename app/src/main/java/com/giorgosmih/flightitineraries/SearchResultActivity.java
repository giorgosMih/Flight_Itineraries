package com.giorgosmih.flightitineraries;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
public class SearchResultActivity extends AppCompatActivity {

    SearchResultActivityFragment fragment;
    static final String fragmentTag = "searchResultFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        if(savedInstanceState == null){
            fragment = new SearchResultActivityFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.activity_search_result, fragment,fragmentTag)
                    .commit();
        }
    }

}