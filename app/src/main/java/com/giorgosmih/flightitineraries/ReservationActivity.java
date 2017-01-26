package com.giorgosmih.flightitineraries;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Spinner;
import android.widget.TextView;

public class ReservationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Intent intent = getIntent();
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            Flight dataObj = (Flight) intent.getSerializableExtra(Intent.EXTRA_TEXT);

            ((TextView)findViewById(R.id.price)).setText(dataObj.getPriceNum());
            ((TextView)findViewById(R.id.refundable)).setText(dataObj.getRefundableString());
            ((TextView)findViewById(R.id.penalties)).setText(dataObj.getPenaltiesString());

            ((TextView)findViewById(R.id.from)).setText(dataObj.getOriginCode());
            ((TextView)findViewById(R.id.to)).setText(dataObj.getDestinationCode());
        }
    }
}
