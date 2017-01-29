package com.giorgosmih.flightitineraries;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {
    public static int tasksDone = 0;

    private static int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "flightsDB";

    private static final String TABLE_COUNTRIES = "countries";
    private static final String COUNTRY_ID = "ID";
    private static final String COUNTRY_NAME = "NAME";

    private static final String TABLE_AIRPORTS = "airports";
    private static final String AIRPORT_ID = "ID";
    private static final String AIRPORT_CODE = "CODE";
    private static final String AIRPORT_NAME = "NAME";
    private static final String AIRPORT_FR_COUNTRYID = "COUNTRYID";

    private static final String TABLE_AIRLINES = "airlines";
    private static final String AIRLINES_ID = "ID";
    private static final String AIRLINES_CODE = "CODE";
    private static final String AIRLINES_NAME = "NAME";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_COUNTRIES_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_COUNTRIES + "("
                        + COUNTRY_ID + " INTEGER PRIMARY KEY,"
                        + COUNTRY_NAME + " TEXT" + ")";

        String CREATE_AIRPORTS_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_AIRPORTS + "("
                        + AIRPORT_ID + " INTEGER PRIMARY KEY,"
                        + AIRPORT_NAME + " TEXT,"
                        + AIRPORT_CODE + " TEXT,"
                        + AIRPORT_FR_COUNTRYID + " INTEGER REFERENCES "+ TABLE_COUNTRIES +"("+COUNTRY_ID+"))";

        String CREATE_CONTACTS_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TABLE_AIRLINES + "("
                + AIRLINES_ID + " INTEGER PRIMARY KEY,"
                + AIRLINES_CODE + " TEXT,"
                + AIRLINES_NAME + " TEXT" + ")";

        db.execSQL(CREATE_COUNTRIES_TABLE);
        db.execSQL(CREATE_AIRPORTS_TABLE);
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AIRPORTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COUNTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_AIRLINES);
        onCreate(db);
    }

    public void deleteAllData(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_AIRPORTS,null,null);
        db.delete(TABLE_COUNTRIES,null,null);
        db.delete(TABLE_AIRLINES,null,null);
        db.close();
    }

    public boolean isEmpty(){
        boolean b = true;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_COUNTRIES, new String[]{"(SELECT COUNT(*) FROM "+TABLE_COUNTRIES+")"}, null, null, null, null, null, null);
        cursor.moveToFirst();

        int n1 = 0;
        if(cursor.getCount() > 0)
            n1 = cursor.getInt(0);
        cursor.close();

        cursor = db.query(TABLE_AIRPORTS, new String[]{"(SELECT COUNT(*) FROM "+TABLE_AIRPORTS+")"}, null, null, null, null, null, null);
        cursor.moveToFirst();

        int n2 = 0;
        if(cursor.getCount() > 0)
            n2 = cursor.getInt(0);
        cursor.close();

        cursor = db.query(TABLE_AIRLINES, new String[]{"(SELECT COUNT(*) FROM "+TABLE_AIRLINES+")"}, null, null, null, null, null, null);
        cursor.moveToFirst();

        int n3 = 0;
        if(cursor.getCount() > 0)
            n3 = cursor.getInt(0);
        cursor.close();

        if(n1 > 0 && n2 >0 && n3 >0)
            b = false;

        return b;
    }

    public void addCountry(String data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COUNTRY_NAME, data);

        long id = db.insert(TABLE_COUNTRIES, null, values);

        new FetchAirportsData(id).execute(data);

        if(db.isOpen()){
            db.close();
        }
    }

    public ArrayList<String> getAllCountries(){
        ArrayList<String> data = new ArrayList<String>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_COUNTRIES, new String[]{COUNTRY_NAME}, null, null, null, null, null, null);

        if(cursor != null){
            cursor.moveToFirst();

            do{
                data.add(cursor.getString(0));
            }while(cursor.moveToNext());
        }

        return data;
    }

    public ArrayList<String> getAirportsOfCountry(String country){
        ArrayList<String> data = new ArrayList<String>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT "+AIRPORT_NAME+","+AIRPORT_CODE+" FROM "+TABLE_AIRPORTS+" WHERE "+AIRPORT_FR_COUNTRYID+" = (SELECT "+COUNTRY_ID+" FROM "+TABLE_COUNTRIES+" WHERE "+COUNTRY_NAME+" = ?)",new String[]{country});

        if(c != null){
            c.moveToFirst();

            if(c.getCount() <= 0){
                return data;
            }

            do{
                data.add(c.getString(1) + ", " + c.getString(0));
            }while(c.moveToNext());
        }

        return data;
    }

    public ArrayList<String> getAllAirports(){
        ArrayList<String> data = new ArrayList<String>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_AIRPORTS, new String[]{AIRPORT_NAME,AIRPORT_CODE}, null, null, null, null, null, null);

        if(cursor != null){
            cursor.moveToFirst();

            do{
                data.add(cursor.getString(1)+", "+cursor.getString(0));
            }while(cursor.moveToNext());
        }

        return data;
    }

    public void addAirline(String[] data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AIRLINES_CODE, data[0]);
        values.put(AIRLINES_NAME, data[1]);

        db.insert(TABLE_AIRLINES, null, values);
        db.close();
    }

    public String getAirlineByCode(String code) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_AIRLINES, new String[]{AIRLINES_NAME}, AIRLINES_CODE + "=?",new String[]{code}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        String val = cursor.getString(0);
        cursor.close();
        return val;
    }

    public class FetchAirportsData extends AsyncTask<String,Void,String[]> {

        long id;

        public FetchAirportsData(long n){
            id = n;
        }

        protected String[] doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            try {
                final String baseUrl = "https://iatacodes.org/api/v6/autocomplete?";
                final String apiKeyParam = "api_key";
                final String queryParam = "query";

                Uri builtUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam, BuildConfig.IATA_API_KEY)
                        .appendQueryParameter(queryParam, params[0])
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("URL", "Error closing stream", e);
                    }
                }
            }
            try {
                return JSONparser.getCitiesAndAirportsDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e("URL", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {

                SQLiteDatabase db = getWritableDatabase();

                ContentValues values;
                for(String s: result){

                    if(s == null){
                        int n = db.delete(TABLE_COUNTRIES,COUNTRY_ID+" = ?",new String[]{String.valueOf(id)});
                        Log.v("DBug",String.valueOf(n));
                        break;
                    }

                    String[] data = s.split(",");
                    values = new ContentValues();
                    values.put(AIRPORT_CODE, data[1].trim());
                    values.put(AIRPORT_NAME, data[0].trim());
                    values.put(AIRPORT_FR_COUNTRYID,id);

                    db.insert(TABLE_AIRPORTS, null, values);
                }

                db.close();

                tasksDone++;
                Log.v("DBug","Tasks Done: " +tasksDone);
                if(FlightSearchFragment.countries <= tasksDone){
                    FlightSearchFragment.initAdapters();
                    FlightSearchFragment.dialog.dismiss();
                }
            }
        }
    }
}