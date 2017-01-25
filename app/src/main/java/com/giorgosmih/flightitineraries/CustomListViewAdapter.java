package com.giorgosmih.flightitineraries;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomListViewAdapter extends ArrayAdapter{

    private Context context;
    private int layoutResourceID;
    private List list;

    public CustomListViewAdapter(Context context,int layout,List data){
        super(context, layout, data);

        layoutResourceID = layout;
        this.context = context;
        list = data;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        View row = convertView;
        DataHolder holder = null;

        if(row == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceID,parent,false);

            holder = new DataHolder();
            holder.departArrive = (TextView) row.findViewById(R.id.departArrive);
            holder.duration = (TextView) row.findViewById(R.id.duration);
            holder.price = (TextView) row.findViewById(R.id.price);
            holder.direct = (TextView) row.findViewById(R.id.direct);

            row.setTag(holder);
        }
        else{
            holder = (DataHolder)row.getTag();
        }

        Flight data = (Flight) list.get(position);
        holder.departArrive.setText(data.getDerartArrive());
        holder.duration.setText(data.getDuration());
        holder.price.setText(data.getPrice());
        holder.direct.setText(data.getDirect());

        return row;
    }

    public Flight getObjectAt(int index){
        if(index < list.size() && !list.isEmpty())
            return (Flight) list.get(index);
        else
            return null;
    }

    class DataHolder{
        TextView departArrive;
        TextView duration;
        TextView price;
        TextView direct;
    }
}
