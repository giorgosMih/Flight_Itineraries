package com.giorgosmih.flightitineraries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Flight implements Serializable{

    public static final String EURO = "\u20ac";

    public static final String FLIGHTS_PARAM = "flights";
    public static final String DEPARTS_AT_PARAM = "departs_at";
    public static final String ARRIVES_AT_PARAM = "arrives_at";

    private ArrayList<FlightData> outboundFlights = new ArrayList<FlightData>();
    private ArrayList<FlightData>  inboundFlights = new ArrayList<FlightData>();
    private String price;

    public Flight(JSONObject outbound, String pr) throws JSONException {
        this.price = pr;
        JSONArray array = outbound.getJSONArray(FLIGHTS_PARAM);
        for(int i=0;i<array.length();i++){
            JSONObject obj = array.getJSONObject(i);
            outboundFlights.add(new FlightData(
                            obj.getString(DEPARTS_AT_PARAM),
                            obj.getString(ARRIVES_AT_PARAM),
                            obj.getJSONObject("origin").getString("airport"),
                            obj.getJSONObject("destination").getString("airport"),
                            obj.getString("operating_airline"),
                            obj.getJSONObject("booking_info").getString("travel_class"),
                            obj.getJSONObject("booking_info").getString("seats_remaining")
                    )
            );
        }
    }

    public Flight(JSONObject outbound, JSONObject inbound, String pr) throws JSONException {
        this.price = pr;

        JSONArray array = outbound.getJSONArray(FLIGHTS_PARAM);
        for(int i=0;i<array.length();i++){
            JSONObject obj = array.getJSONObject(i);
            outboundFlights.add(new FlightData(
                            obj.getString(DEPARTS_AT_PARAM),
                            obj.getString(ARRIVES_AT_PARAM),
                            obj.getJSONObject("origin").getString("airport"),
                            obj.getJSONObject("destination").getString("airport"),
                            obj.getString("operating_airline"),
                            obj.getJSONObject("booking_info").getString("travel_class"),
                            obj.getJSONObject("booking_info").getString("seats_remaining")
                    )
            );
        }

        array = inbound.getJSONArray(FLIGHTS_PARAM);
        for(int i=0;i<array.length();i++){
            JSONObject obj = array.getJSONObject(i);
            inboundFlights.add(new FlightData(
                            obj.getString(DEPARTS_AT_PARAM),
                            obj.getString(ARRIVES_AT_PARAM),
                            obj.getJSONObject("origin").getString("airport"),
                            obj.getJSONObject("destination").getString("airport"),
                            obj.getString("operating_airline"),
                            obj.getJSONObject("booking_info").getString("travel_class"),
                            obj.getJSONObject("booking_info").getString("seats_remaining")
                    )
            );
        }
    }

    public boolean hasInbounds(){
        return inboundFlights.size() > 0;
    }

    public String getDerartArrive(){
        String depart = outboundFlights.get(0).departAt;
        depart = depart.substring(depart.indexOf('T')+1);

        String arrive = outboundFlights.get(outboundFlights.size()-1).arriveAt;
        arrive = arrive.substring(arrive.indexOf('T')+1);

        return depart + "->" + arrive;
    }

    public String getDuration(){
        String depart = outboundFlights.get(0).departAt;
        String arrive = outboundFlights.get(outboundFlights.size()-1).arriveAt;

        DateFormat df = DateFormat.getDateInstance();
        ((SimpleDateFormat)df).applyPattern("yyyy-MM-dd'T'HH:mm");

        try {
            Date d1 = df.parse(depart);
            Date d2 = df.parse(arrive);

            long mills = d2.getTime() - d1.getTime();
            int hours = ((int)mills/(1000 * 60 * 60));
            int mins = ((int) (mills/(1000 * 60)) % 60);

            return hours + "ώρες " + mins + "λεπτά";
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "Σφάλμα: Μετατροπής Date";
    }

    public String getPrice(){return "Τιμή: " + price +EURO;}
    public String getPriceNum(){return price + EURO;}

    public String getDirect(){
        if(outboundFlights.size() <= 1 )
            return "Απευθείας";
        else
            return "";
    }

    public ArrayList<FlightData> getOutboundFlights(){return outboundFlights;}
    public ArrayList<FlightData> getInboundFlights(){return inboundFlights;}

    public String toString(){
        String s="";
        int size = outboundFlights.size();
        for(int i=0;i<size;i++){
            s += outboundFlights.get(i).toString()+"\n";
        }

        if(size <= 1){
            s+="Απευθείας\n";
        }
        else{
            s+="Με Αλλαγές\n";
        }

        return s + "\nPrice: " + price;
    }

    class FlightData implements Serializable{

        private String departAt;
        private String arriveAt;
        private String origin;
        private String destination;
        private String operation_airline;
        private String travel_class;
        private String seats;


        public FlightData(String depart,String arrive,String or,String dest,String oper,String tr_class,String seat){
            departAt = depart;
            arriveAt = arrive;
            origin = or;
            destination = dest;
            operation_airline = oper;
            travel_class = tr_class;
            seats = seat;
        }

        public String getDepartAt(){return departAt;}
        public String getArriveAt(){return arriveAt;}
        public String getOrigin(){return origin;}
        public String getDestination(){return destination;}
        public String getOperation_airline(){return operation_airline;}
        public String getTravel_class(){return travel_class;}
        public String getSeats(){return seats;}

        public String toString(){
            return    "Departs: " + departAt + "\n"
                    + "Arrives: " + arriveAt + "\n"
                    + origin + "->" + destination + "\n"
                    + "Airline: " + operation_airline + "\n"
                    + "Class: " + travel_class + ", seats: " + seats;
        }
    }
}