package com.giorgosmih.flightitineraries;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Intent intent = getActivity().getIntent();
        Flight dataObj=null;
        if(intent != null && intent.hasExtra(Intent.EXTRA_TEXT)){
            dataObj = (Flight) intent.getSerializableExtra(Intent.EXTRA_TEXT);
        }

        View rootView = inflater.inflate(R.layout.fragment_detail_activity,container,false);
        ((TextView)rootView.findViewById(R.id.outboundCost)).setText(dataObj.getPriceNum());

        ListView outbounds = (ListView) rootView.findViewById(R.id.outboundList);

        ArrayList<Flight.FlightData> arrayList = dataObj.getOutboundFlights();
        OutboundsAdapter outboundsAdapter = new OutboundsAdapter(getContext(),R.layout.detail_item,arrayList);
        outbounds.setAdapter(outboundsAdapter);

        if(dataObj.hasInbounds()){
            ListView inbounds = (ListView) rootView.findViewById(R.id.inboundsList);
            ((LinearLayout)rootView.findViewById(R.id.inboundsLayout)).setVisibility(View.VISIBLE);

            arrayList = dataObj.getInboundFlights();
            OutboundsAdapter inboundAdapter = new OutboundsAdapter(getContext(),R.layout.detail_item,arrayList);
            inbounds.setAdapter(inboundAdapter);
        }
        else{
            LinearLayout layout = ((LinearLayout)rootView.findViewById(R.id.outboundsLayout));
            ((LinearLayout.LayoutParams)layout.getLayoutParams()).weight = 10;
        }

        return rootView;
    }

    class OutboundsAdapter extends ArrayAdapter{

        private Context context;
        private int layoutResourceID;
        private List list;

        public OutboundsAdapter(Context context,int layout,List data){
            super(context, layout, data);
            layoutResourceID = layout;
            this.context = context;
            list = data;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            View row = convertView;
            DataHolder holder = null;

            if(row == null){
                LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                row = inflater.inflate(layoutResourceID,parent,false);

                holder = new DataHolder();
                holder.departsAt = (TextView) row.findViewById(R.id.outboundDepartsAt);
                holder.arrivesAt = (TextView) row.findViewById(R.id.outboundArrivesAt);
                holder.origin = (TextView) row.findViewById(R.id.outboundOrigin);
                holder.destination = (TextView) row.findViewById(R.id.outboundDestination);
                holder.ailine = (TextView)row.findViewById(R.id.outboundAirline);
                holder.travel_class = (TextView)row.findViewById(R.id.outboundClass);
                holder.seats = (TextView)row.findViewById(R.id.outboundSeats);

                row.setTag(holder);
            }
            else{
                holder = (DataHolder)row.getTag();
            }

            Flight.FlightData data = (Flight.FlightData) list.get(position);
            holder.departsAt.setText(data.getDepartAt());
            holder.arrivesAt.setText(data.getArriveAt());
            holder.origin.setText(data.getOrigin());
            holder.destination.setText(data.getDestination());
            holder.ailine.setText(data.getOperation_airline());
            holder.travel_class.setText(data.getTravel_class());
            holder.seats.setText(data.getSeats());

            return row;
        }

        class DataHolder{
            TextView departsAt;
            TextView arrivesAt;
            TextView origin;
            TextView destination;
            TextView ailine;
            TextView travel_class;
            TextView seats;
        }
    }
}