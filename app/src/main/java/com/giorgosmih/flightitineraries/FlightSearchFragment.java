package com.giorgosmih.flightitineraries;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Locale;

public class FlightSearchFragment extends Fragment {

    public static final String SELECT_PROMPT = "[Επιλέξτε Χώρα]";

    ArrayAdapter<String> arrayAdapterCountries;
    ArrayAdapter<String> arrayAdapterCitiesFrom;
    ArrayAdapter<String> arrayAdapterCitiesTo;

    MyProgressDialog dialog;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final WifiManager wifi = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        final ConnectivityManager Cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

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

        View rootView = inflater.inflate(R.layout.fragment_flight_search, container, false);

        arrayAdapterCitiesFrom = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_cities, R.id.spinner_item_flight_textview);
        arrayAdapterCitiesTo = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_cities, R.id.spinner_item_flight_textview);
        arrayAdapterCountries = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item_cities, R.id.spinner_item_flight_textview);

        Spinner from = ((Spinner)rootView.findViewById(R.id.spinnerFlightFrom));
        from.setAdapter(arrayAdapterCountries);
        from.setPrompt(getContext().getString(R.string.fr_main_spinnerFrom));
        from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0 || !wifi.isWifiEnabled())
                    return;
                String country = parent.getItemAtPosition(position).toString();
                new FetchCitiesAndAirportsData().execute("1",country);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Spinner to = ((Spinner)rootView.findViewById(R.id.spinnerFlightTo));
        to.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 0 || !wifi.isWifiEnabled())
                    return;
                String country = parent.getItemAtPosition(position).toString();
                new FetchCitiesAndAirportsData().execute("2",country);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        to.setPrompt(getContext().getString(R.string.fr_main_spinnerTo));
        to.setAdapter(arrayAdapterCountries);

        ((Spinner)rootView.findViewById(R.id.spinnerCitiesFrom)).setAdapter(arrayAdapterCitiesFrom);
        ((Spinner)rootView.findViewById(R.id.spinnerCitiesTo)).setAdapter(arrayAdapterCitiesTo);

        ((Button)rootView.findViewById(R.id.buttonSearch)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!wifi.isWifiEnabled())
                    return;

                Spinner sFrom = (Spinner) v.getRootView().findViewById(R.id.spinnerCitiesFrom);
                String from = "";
                if(sFrom.getSelectedItemPosition() >= 0) {
                    from = sFrom.getSelectedItem().toString();
                    from = from.split(",")[1].trim();
                }

                Spinner sTo = (Spinner) v.getRootView().findViewById(R.id.spinnerCitiesTo);
                String to = "";
                if(sTo.getSelectedItemPosition() >= 0) {
                    to = sTo.getSelectedItem().toString();
                    to = to.split(",")[1].trim();
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
                    builder.setMessage("Τα πεδία: Προέλευσης, Προορισμού και Ημερομηνίας Αναχώρισης\nείναι υποχρεωτικά. Παρακαλώ Συμπληρώστε τα.");
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

        return  rootView;
    }

    public void fetchCountryData(){
        dialog.setData(
                getString(R.string.dialog_countries_fetch),
                getString(R.string.dialog_wait_msg)
        );

        dialog.show(
                getFragmentManager(),
                getString(R.string.dialog_title));

        new FetchCountryData().execute();
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

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                //MODIFIED FOR CITY OF THESSALONIKI, GREECE
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

                Uri.Builder builder  = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam,BuildConfig.AMADEUS_API_KEY)
                        .appendQueryParameter(originParam,params[0])
                        .appendQueryParameter(destinationParam, params[1])
                        .appendQueryParameter(departDateParam, params[2])
                        .appendQueryParameter("currency","EUR");

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

                Uri builtUri = builder.build();

                URL url = new URL(builtUri.toString());

                Log.v("URL", "Built URI: "+builtUri.toString());

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
                    Log.v("URL", "empty inputStream");
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
                Log.v("URL","JSON String: "+jsonStr);
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
                Log.e("URL", e.getMessage(), e);
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

    public class FetchCountryData extends AsyncTask<Void,Void,String[]> {

        protected String[] doInBackground(Void... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String jsonStr = null;

            try {
                final String baseUrl = "https://iatacodes.org/api/v6/countries?";
                final String apiKeyParam = "api_key";

                Uri builtUri = Uri.parse(baseUrl).buildUpon()
                        .appendQueryParameter(apiKeyParam,BuildConfig.IATA_API_KEY)
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
                return JSONparser.getCountryDataFromJson(jsonStr);
            } catch (JSONException e) {
                Log.e("URL", e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                arrayAdapterCountries.clear();

                arrayAdapterCountries.add(SELECT_PROMPT);
                for(String country : result) {
                    arrayAdapterCountries.add(country);
                }

                arrayAdapterCountries.sort(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if(o1.equals(SELECT_PROMPT) || o2.equals(SELECT_PROMPT))
                            return 1;
                        return o1.compareTo(o2);
                    }
                });

                dialog.dismiss();
            }
        }
    }

    public class FetchCitiesAndAirportsData extends AsyncTask<String,Void,String[]> {
        private String type;

        protected String[] doInBackground(String... params) {
            type = params[0];

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
                        .appendQueryParameter(queryParam, params[1])
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

                if(type.equals("1")){
                    arrayAdapterCitiesFrom.clear();
                    for(String country : result) {
                        arrayAdapterCitiesFrom.add(country);
                    }
                }
                else{
                    arrayAdapterCitiesTo.clear();
                    for(String country : result) {
                        arrayAdapterCitiesTo.add(country);
                    }
                }

            }
        }
    }

}