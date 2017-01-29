package com.giorgosmih.flightitineraries;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

public class FlightSearchFragment extends Fragment {

    public static int countries = 0;

    static ArrayAdapter<String> arrayAdapterCountries;
    static ArrayAdapter<String> arrayAdapterCitiesFrom;
    static ArrayAdapter<String> arrayAdapterCitiesTo;

    public static MyProgressDialog dialog;
    public static DBHandler dbHandler;

    final Calendar myCalendar;
    final DatePickerDialog.OnDateSetListener dateDepart;
    final DatePickerDialog.OnDateSetListener dateArrive;
    EditText departDateEditText;
    EditText arriveDataEditText;

    private int responseCode;

    public FlightSearchFragment(){
        myCalendar = Calendar.getInstance();
        dateDepart = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(0);
            }
        };

        dateArrive = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(1);
            }
        };

        dialog = new MyProgressDialog();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_updateDB){
            dbHandler.deleteAllData();
            updateDatabase();
            return true;
        }
        else if(id == R.id.action_settings){
            Intent intent = new Intent(getActivity(),SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        dbHandler = new DBHandler(this.getActivity());
        Flight.setPreferences(PreferenceManager.getDefaultSharedPreferences(getActivity()),getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final WifiManager wifi = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        final ConnectivityManager Cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        View rootView = inflater.inflate(R.layout.fragment_flight_search, container, false);
        arrayAdapterCitiesFrom = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_cities, R.id.spinner_item_flight_textview);
        arrayAdapterCitiesTo = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_cities, R.id.spinner_item_flight_textview);
        arrayAdapterCountries = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_cities, R.id.spinner_item_flight_textview);

        Thread t = new Thread(){
            NetworkInfo networkInfo =  Cm.getActiveNetworkInfo();

            public void run(){
                while(networkInfo == null){
                    try {
                        networkInfo = Cm.getActiveNetworkInfo();
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if(networkInfo.isConnected())
                    fetchCountryData();
            }
        };

        if(!wifi.isWifiEnabled()){
            wifi.setWifiEnabled(true);
            NetworkInfo networkInfo = Cm.getActiveNetworkInfo();

            if(networkInfo != null){
                if(networkInfo.isConnected())
                    fetchCountryData();
                else
                    t.start();
            }
            else{
                t.start();
            }
        }
        else{
            NetworkInfo networkInfo = Cm.getActiveNetworkInfo();
            if(networkInfo != null){
                if(networkInfo.isConnected())
                    fetchCountryData();
                else
                    t.start();
            }
            else{
                t.start();
            }
        }

        final AutoCompleteTextView from = ((AutoCompleteTextView)rootView.findViewById(R.id.spinnerFlightFrom));
        from.setAdapter(arrayAdapterCountries);

        from.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    from.dismissDropDown();
            }
        });

        from.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_UP){
                    from.showDropDown();
                }
                return false;
            }
        });

        from.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);

                String s = parent.getItemAtPosition(position).toString();
                arrayAdapterCitiesFrom.clear();
                ArrayList<String> data = dbHandler.getAirportsOfCountry(s);
                if(data.size() > 0)
                    arrayAdapterCitiesFrom.addAll(data);
                else{
                    arrayAdapterCitiesFrom.addAll(dbHandler.getAllAirports());
                }
                from.clearFocus();
            }
        });

        final AutoCompleteTextView to = ((AutoCompleteTextView)rootView.findViewById(R.id.spinnerFlightTo));
        to.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    to.dismissDropDown();
            }
        });

        to.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_UP){
                    to.showDropDown();
                }
                return false;
            }
        });

        to.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);

                String s = parent.getItemAtPosition(position).toString();
                arrayAdapterCitiesTo.clear();
                arrayAdapterCitiesTo.addAll(dbHandler.getAirportsOfCountry(s));
                to.clearFocus();
            }
        });

        to.setAdapter(arrayAdapterCountries);

        final AutoCompleteTextView sFrom = ((AutoCompleteTextView)rootView.findViewById(R.id.spinnerCitiesFrom));
        sFrom.setAdapter(arrayAdapterCitiesFrom);
        final AutoCompleteTextView sTo = ((AutoCompleteTextView)rootView.findViewById(R.id.spinnerCitiesTo));
        sTo.setAdapter(arrayAdapterCitiesTo);

        sFrom.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    sFrom.dismissDropDown();
            }
        });

        sFrom.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_UP){
                    sFrom.showDropDown();
                }
                return false;
            }
        });

        sFrom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);
                sFrom.clearFocus();
            }
        });

        sTo.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    sTo.dismissDropDown();
            }
        });

        sTo.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if(action == MotionEvent.ACTION_UP){
                    sTo.showDropDown();
                }
                return false;
            }
        });

        sTo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);
                sTo.clearFocus();
            }
        });

        ((Button)rootView.findViewById(R.id.buttonSearch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wifi.isWifiEnabled())
                    return;

                String from = "";
                if(sFrom.getText() != null) {
                    from = sFrom.getText().toString();
                    from = from.split(",")[0].trim();
                }

                String to = "";
                if(sTo.getText() != null) {
                    to = sTo.getText().toString();
                    to = to.split(",")[0].trim();
                }

                String depDate = "";
                if(departDateEditText.getText().length() > 0)
                    depDate = departDateEditText.getText().toString();
                String arrDate = arriveDataEditText.getText().toString();

                String persons = ((EditText)v.getRootView().findViewById(R.id.editTextPersons)).getText().toString();
                String children = ((EditText)v.getRootView().findViewById(R.id.editTextChildren)).getText().toString();
                String infants = ((EditText)v.getRootView().findViewById(R.id.editTextInfants)).getText().toString();

                //optional
                String nonStop = String.valueOf(((CheckBox)v.getRootView().findViewById(R.id.checkboxNonStop)).isChecked());
                String maxPrice = ((EditText)v.getRootView().findViewById(R.id.editTextMaxCost)).getText().toString();

                if(from.isEmpty() || to.isEmpty() || depDate.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Τα πεδία: \nΠροέλευσης\nΠροορισμού\nΗμερομηνίας Αναχώρισης\nείναι υποχρεωτικά. Παρακαλώ Συμπληρώστε τα.");
                    builder.setTitle("Ελλιπή Κριτήρια Αναζήτησης");

                    builder.create().show();
                    return;
                }

                new FetchFlightsDataTask().execute(from,to,depDate,arrDate,persons,children,infants,nonStop,maxPrice);
            }
        });

        //calendar stuff
        departDateEditText = ((EditText) rootView.findViewById(R.id.editTextDepartDate));
        departDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    new DatePickerDialog(getActivity(), dateDepart, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    if(!departDateEditText.getText().toString().isEmpty())
                        departDateEditText.getText().clear();
                    departDateEditText.clearFocus();
                }
            }
        });

        arriveDataEditText = (EditText) rootView.findViewById(R.id.editTextArriveDate);
        arriveDataEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    new DatePickerDialog(getActivity(), dateArrive, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),myCalendar.get(Calendar.DAY_OF_MONTH)).show();
                    if(!arriveDataEditText.getText().toString().isEmpty())
                        arriveDataEditText.getText().clear();
                    arriveDataEditText.clearFocus();
                }
            }
        });

        rootView.requestFocus();
        return  rootView;
    }

    public static void initAdapters(){
        arrayAdapterCountries.clear();
        arrayAdapterCountries.addAll(dbHandler.getAllCountries());
        arrayAdapterCountries.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });


        arrayAdapterCitiesFrom.addAll(dbHandler.getAllAirports());
        arrayAdapterCitiesTo.addAll(dbHandler.getAllAirports());
    }

    private void updateDatabase(){
        dialog.setData(
                getString(R.string.dialog_data_fetch),
                getString(R.string.dialog_wait_msg)
        );

        dialog.show(
                getFragmentManager(),
                getString(R.string.dialog_title));

        new FetchData().execute();
    }

    public void fetchCountryData(){
        if(dbHandler.isEmpty()) {
            updateDatabase();
        }
        else
            initAdapters();
    }

    private void updateLabel(int id) {

        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        if(id == 0)
            departDateEditText.setText(sdf.format(myCalendar.getTime()));
        else
            arriveDataEditText.setText(sdf.format(myCalendar.getTime()));
    }

    public class FetchFlightsDataTask extends AsyncTask<String,Void,Flight[]>{

        @Override
        protected Flight[] doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String currency = prefs.getString(getString(R.string.pref_currency_key), getString(R.string.pref_currency_default));
            int results = 0;
            try {
                results = Integer.parseInt(prefs.getString(getString(R.string.pref_max_results_key), getString(R.string.pref_max_results_default)));
            }catch(Exception e){}

            try {
                final String baseUrl = "https://api.sandbox.amadeus.com/v1.2/flights/low-fare-search?";
                final String apiKeyParam = "apikey";
                final String originParam= "origin";//required
                final String destinationParam= "destination";//required
                final String adultsParam = "adults";
                final String childrenParam = "children";
                final String infantsParam = "infants";
                final String departDateParam = "departure_date";//required
                final String arriveDateParam = "return_date";
                final String nonstopParam = "nonstop";
                final String maxPriceParam = "max_price";
                final String numResultsParam = "number_of_results";

                Uri.Builder builder  = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam,BuildConfig.AMADEUS_API_KEY)
                        .appendQueryParameter(originParam,params[0])
                        .appendQueryParameter(destinationParam, params[1])
                        .appendQueryParameter(departDateParam, params[2])
                        .appendQueryParameter("currency",currency);

                if(params[3] != null && !params[3].isEmpty()){
                    builder.appendQueryParameter(arriveDateParam,params[3]);
                }
                if(params[4] != null && !params[4].isEmpty()){
                    builder.appendQueryParameter(adultsParam,params[4]);
                }
                if(params[5] != null && !params[5].isEmpty()){
                    builder.appendQueryParameter(childrenParam,params[5]);
                }
                if(params[6] != null && !params[6].isEmpty()){
                    builder.appendQueryParameter(infantsParam,params[6]);
                }
                if(params[7] != null && !params[7].isEmpty()){
                    builder.appendQueryParameter(nonstopParam,params[7]);
                }
                if(params[8] != null && !params[8].isEmpty()){
                    builder.appendQueryParameter(maxPriceParam,params[8]);
                }
                if(results > 0){
                    builder.appendQueryParameter(numResultsParam,String.valueOf(results));
                }

                Uri builtUri = builder.build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                dialog.setData(
                        getString(R.string.dialog_flights_fetch),
                        getString(R.string.dialog_wait_msg)
                );

                dialog.show(
                        getFragmentManager(),
                        getString(R.string.dialog_title)
                );

                urlConnection.connect();

                responseCode = urlConnection.getResponseCode();
                if(responseCode != 200){
                    return null;
                }

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    //Log.v("URL", "empty inputStream");
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
                    Log.v("URL", "empty buffer");
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
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

                Flight[] array = JSONparser.getFlightsDataFromJson(jsonStr);
                System.out.println();
                return array;
            } catch (JSONException e) {
                Log.e(getClass().getName(), e.getMessage(), e);
                e.printStackTrace();
            }

            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

        @Override
        protected void onPostExecute(Flight[] result) {
            dialog.dismiss();

            if(result == null){
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getActivity());
                alertBuilder.setTitle("Σφάλμα Αναζήτησης");
                alertBuilder.setMessage("Δεν βρέθηκαν αποτελέσματα με τα επιλεγμένα κριτήρια.");
                alertBuilder.setNeutralButton("Εντάξη",null);

                alertBuilder.create().show();
                return;
            }

            Intent intent = new Intent(getActivity(),SearchResultActivity.class);

            intent.putExtra(Intent.EXTRA_TEXT, result);
            startActivity(intent);
        }
    }

    public class FetchData extends AsyncTask<Void,Void,Void> {

        protected Void doInBackground(Void... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            //get country data
            try {
                final String baseUrl = "https://iatacodes.org/api/v6/countries?";
                final String apiKeyParam = "api_key";

                Uri builtUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam, BuildConfig.IATA_API_KEY)
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
                JSONparser.getCountryDataFromJson(jsonStr,dbHandler);
            } catch (JSONException e) {
                Log.e("URL", e.getMessage(), e);
                e.printStackTrace();
            }

            //get airline data
            try {
                final String baseUrl = "https://iatacodes.org/api/v6/airlines?";
                final String apiKeyParam = "api_key";

                Uri builtUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam, BuildConfig.IATA_API_KEY)
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
                JSONparser.getAirlinesDataFromJson(jsonStr,dbHandler);
            } catch (JSONException e) {
                Log.e("URL", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }
    }
}