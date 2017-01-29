package com.giorgosmih.flightitineraries;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

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
    public static final String DOLLAR = "$";
    public static final String POUND = "£";
    public static final String YEN = "¥";
    public static final String RUBLE = "\u20BD";

    public static final String FLIGHTS_PARAM = "flights";
    public static final String DEPARTS_AT_PARAM = "departs_at";
    public static final String ARRIVES_AT_PARAM = "arrives_at";

    public static final String ORIGIN_PARAM = "origin";
    public static final String DESTINATION_PARAM = "destination";
    public static final String AIRPORT_PARAM = "airport";

    public static final String AIRLINE_PARAM = "operating_airline";
    public static final String FLIGHT_NO_PARAM = "flight_number";

    public static final String BOOKING_INFO_PARAM = "booking_info";
    public static final String CLASS_PARAM = "travel_class";
    public static final String SEATS_PARAM = "seats_remaining";

    private ArrayList<FlightData> outboundFlights = new ArrayList<FlightData>();
    private ArrayList<FlightData>  inboundFlights = new ArrayList<FlightData>();
    private String price;
    private boolean refundable;
    private boolean changePenalties;

    private static Context context;
    private static SharedPreferences prefs;

    public Flight(JSONObject outbound, String pr, boolean ref, boolean penalties) throws JSONException {
        this.price = pr;
        this.changePenalties = penalties;
        this.refundable = ref;
        JSONArray array = outbound.getJSONArray(FLIGHTS_PARAM);
        for(int i=0;i<array.length();i++){
            JSONObject obj = array.getJSONObject(i);
            outboundFlights.add(new FlightData(
                            obj.getString(DEPARTS_AT_PARAM),
                            obj.getString(ARRIVES_AT_PARAM),
                            obj.getJSONObject(ORIGIN_PARAM).getString(AIRPORT_PARAM),
                            obj.getJSONObject(DESTINATION_PARAM).getString(AIRPORT_PARAM),
                            obj.getString(AIRLINE_PARAM),
                            obj.getString(FLIGHT_NO_PARAM),
                            obj.getJSONObject(BOOKING_INFO_PARAM).getString(CLASS_PARAM),
                            obj.getJSONObject(BOOKING_INFO_PARAM).getString(SEATS_PARAM)
                    )
            );
        }
    }

    public Flight(JSONObject outbound, JSONObject inbound, String pr,boolean ref, boolean penalties) throws JSONException {
        this.price = pr;
        this.changePenalties = penalties;
        this.refundable = ref;

        JSONArray array = outbound.getJSONArray(FLIGHTS_PARAM);
        for(int i=0;i<array.length();i++){
            JSONObject obj = array.getJSONObject(i);
            outboundFlights.add(new FlightData(
                            obj.getString(DEPARTS_AT_PARAM),
                            obj.getString(ARRIVES_AT_PARAM),
                            obj.getJSONObject(ORIGIN_PARAM).getString(AIRPORT_PARAM),
                            obj.getJSONObject(DESTINATION_PARAM).getString(AIRPORT_PARAM),
                            obj.getString(AIRLINE_PARAM),
                            obj.getString(FLIGHT_NO_PARAM),
                            obj.getJSONObject(BOOKING_INFO_PARAM).getString(CLASS_PARAM),
                            obj.getJSONObject(BOOKING_INFO_PARAM).getString(SEATS_PARAM)
                    )
            );
        }

        array = inbound.getJSONArray(FLIGHTS_PARAM);
        for(int i=0;i<array.length();i++){
            JSONObject obj = array.getJSONObject(i);
            inboundFlights.add(new FlightData(
                            obj.getString(DEPARTS_AT_PARAM),
                            obj.getString(ARRIVES_AT_PARAM),
                            obj.getJSONObject(ORIGIN_PARAM).getString(AIRPORT_PARAM),
                            obj.getJSONObject(DESTINATION_PARAM).getString(AIRPORT_PARAM),
                            obj.getString(AIRLINE_PARAM),
                            obj.getString(FLIGHT_NO_PARAM),
                            obj.getJSONObject(BOOKING_INFO_PARAM).getString(CLASS_PARAM),
                            obj.getJSONObject(BOOKING_INFO_PARAM).getString(SEATS_PARAM)
                    )
            );
        }
    }

    public static void setPreferences(SharedPreferences p,Context c){prefs = p;context = c;}

    public boolean hasInbounds(){
        return inboundFlights.size() > 0;
    }

    public String getOriginCode(){
        return outboundFlights.get(0).origin;
    }

    public String getDestinationCode(){
        return outboundFlights.get(outboundFlights.size()-1).destination;
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

    public String getPrice(){
        switch (prefs.getString(context.getString(R.string.pref_currency_key),context.getString(R.string.pref_currency_default))){
            case "USD":case "CAD":
                return "Τιμή: " + price +DOLLAR;
            case "EUR":
                return "Τιμή: " + price +EURO;
            case "GBP":
                return "Τιμή: " + price +POUND;
            case "JPY":
                return "Τιμή: " + price +YEN;
            case "RUB":
                return "Τιμή: " + price +RUBLE;
        }
        return "";
    }
    public String getPriceNum(){
        switch (prefs.getString(context.getString(R.string.pref_currency_key),context.getString(R.string.pref_currency_default))){
            case "USD":case "CAD":
                return price +DOLLAR;
            case "EUR":
                return price +EURO;
            case "GBP":
                return price +POUND;
            case "JPY":
                return price +YEN;
            case "RUB":
                return price +RUBLE;
        }
        return "";
    }
    public String getRefundableString(){
        return (refundable) ? "Με επιστροφή χρημάτων":"Χωρίς επιστροφή χρημάτων";
    }
    public String getPenaltiesString(){
        return (changePenalties)?"Με χρέωση αλλαγής":"Χωρίς χρέωση αλλαγής";
    }

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
        private String flightNO;
        private String travel_class;
        private String seats;


        public FlightData(String depart,String arrive,String or,String dest,String oper,String flightNum,String tr_class,String seat){
            departAt = depart;
            arriveAt = arrive;
            origin = or;
            destination = dest;
            operation_airline = oper;
            flightNO = flightNum;
            travel_class = tr_class;
            seats = seat;
        }

        public String getDepartAt(){return departAt;}
        public String getArriveAt(){return arriveAt;}
        public String getOrigin(){return origin;}
        public String getDestination(){return destination;}
        public String getOperation_airline(){return operation_airline;}
        public String getFlightNumber(){return flightNO;}
        public String getTravel_class(){return travel_class;}
        public String getSeats(){return seats;}
    }
}