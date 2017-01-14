package com.apps.genutek.find_me_masjid;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    private double latitude = 33.6;
    private double longitude = 73.1;

    private double marker_Latitude;
    private double marker_Longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // create action listener for text
        final EditText editText = (EditText) findViewById(R.id.EditText_Search);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchArea();
                    return true;
                }
                return false;
            }
        });
        // create listener for edit text icon (search icon of the right side)
        editText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (editText.getRight() - editText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        searchArea();
                        return true;
                    }
                }
                return false;
            }
        });
        //Add listener to drop down button
        findViewById(R.id.button_drop_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(MapsActivity.this, view);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.item_remove_ads:
                                return true;
                            case R.id.item_feedback:
                                feedBackPage();
                                return true;
                            case R.id.item_share:
                                sharePage();
                                return true;
                            case R.id.item_support_me:
                                return true;
                            case R.id.item_about:
                                showAboutDialog();
                                return true;
                        }
                        return false;
                    }
                });
                popupMenu.inflate(R.menu.popup_menu);
                popupMenu.show();
            }
        });

        // init advert
        MobileAds.initialize(getApplicationContext(),  getResources().getString(R.string.app_ads_id));
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


    }

    public void onMapReady(GoogleMap googleMap) {
        AppExecutionCounter();

        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle( this, R.raw.map_style));
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setInfoWindowAdapter(new MyInfoWindowAdapter());
        getCountryLongLat();
        LatLng country = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(country, 4));
        // marker click listener
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if (marker != null) {
                    marker.showInfoWindow();
                    marker_Latitude = marker.getPosition().latitude;
                    marker_Longitude = marker.getPosition().longitude;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker_Latitude, marker_Longitude), 14));
                }
                return true;
            }
        });
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                onClick_routeButton();
            }
        });


    }

    private void getCountryLongLat() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String iso = tm.getNetworkCountryIso();
        Locale locale = new Locale("", iso);
        String country = locale.getDisplayCountry();
        // Get Current Location
        getLongLat(country,false);
    }

    private void getLongLat(String location, Boolean isLocation) {
        Geocoder coder = new Geocoder(this);
        List<Address> address;
        try {
            address = coder.getFromLocationName(location, 2);
            if (address != null) {
                Address longLat = address.get(0);
                latitude = longLat.getLatitude();
                longitude = longLat.getLongitude();
            }
        } catch (Exception e) {
            if(isLocation) {
                showDialog(getResources().getString(R.string.location_found_error));
                e.printStackTrace();
            }
        }
    }

    public void searchArea() {
        EditText mEdit = (EditText) findViewById(R.id.EditText_Search);
        mEdit.clearFocus();
        InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        in.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
        String cityText = mEdit.getText().toString();
        getLongLat(cityText,true);
        updateMap();
        PlacesTask placesTask = new PlacesTask();
        placesTask.execute(sbMethod());
    }

    @SuppressWarnings("MissingPermission")
    public class FetchCoordinates extends AsyncTask<String, Integer, String> {
        ProgressDialog progress_dialog = null;
        double lati = 0;
        double longi = 0;

        LocationManager mLocationManager;
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    longi = longitude;
                    lati = latitude;


                } catch (Exception e) {

                }

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        @Override
        protected void onPreExecute() {
            mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            progress_dialog = new ProgressDialog(MapsActivity.this, R.style.CustomLoadTheme);

            progress_dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    FetchCoordinates.this.cancel(true);
                }
            });
            progress_dialog.setMessage(getResources().getString(R.string.fetching_coordinates));
            progress_dialog.setIndeterminate(true);
            progress_dialog.setCancelable(true);
            progress_dialog.show();

        }

        @Override
        protected void onCancelled() {
            progress_dialog.dismiss();
            mLocationManager.removeUpdates(locationListener);
        }

        @Override
        protected void onPostExecute(String result) {
            progress_dialog.dismiss();
            updateMap();
            PlacesTask placesTask = new PlacesTask();
            placesTask.execute(sbMethod());
            mLocationManager.removeUpdates(locationListener);
        }

        @Override
        protected String doInBackground(String... params) {

            while (this.lati == 0.0) {

            }
            return null;
        }


    }

    public void onClick_GPSSearch(View view) {

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                FetchCoordinates f = new FetchCoordinates();
                f.execute();
            } else {
                int isPermissionGranted = 0;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        isPermissionGranted);


            }
        }


    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setMessage(getResources().getString(R.string.enable_gps_message))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
        Button button = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(Color.WHITE);
        button = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        button.setTextColor(Color.WHITE);
    }

    private void updateMap() {
        mMap.clear();
        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(getResources().getString(R.string.requested_location)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
    }

    private AlertDialog showDialog(String s) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setMessage(s);
        builder.setCancelable(true);

        builder.setPositiveButton(
                getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
        Button button = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        button.setTextColor(Color.WHITE);
        return alert;
    }


    //***************************************************************************
    // CODE  FOR FINDING MASJID USING GOOGLE
    //***************************************************************************
    public String sbMethod() {

        String sb = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + latitude + "," + longitude +
                "&radius=3000" +
                "&types=" + "mosque" +
                "&sensor=true" +
                "&key=AIzaSyAxvTaGa2xOp3x4pX3xHOb0VFA-iiTwbEg";
        return sb;
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String data = null;

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
            }
            return data;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            ParserTask parserTask = new ParserTask();

            // Start parsing the Google places in JSON format
            // Invokes the "doInBackground()" method of the class ParserTask
            parserTask.execute(result);
        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {

        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = null;
            Place_JSON placeJson = new Place_JSON();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {

            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {
            for (int i = 0; i < list.size(); i++) {
                MarkerOptions markerOptions = new MarkerOptions();
                HashMap<String, String> hmPlace = list.get(i);
                double lat = Double.parseDouble(hmPlace.get("lat"));
                double lng = Double.parseDouble(hmPlace.get("lng"));
                String name = hmPlace.get("place_name");
                String vicinity = hmPlace.get("vicinity");
                LatLng latLng = new LatLng(lat, lng);
                markerOptions.position(latLng);
                markerOptions.title(name);
                markerOptions.snippet(vicinity);
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                mMap.addMarker(markerOptions);
            }
        }
    }

    public class Place_JSON {

        public List<HashMap<String, String>> parse(JSONObject jObject) {
            JSONArray jPlaces = null;
            try {
                jPlaces = jObject.getJSONArray("results");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<>();
            HashMap<String, String> place;

            for (int i = 0; i < placesCount; i++) {
                try {
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return placesList;
        }
        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<String, String>();
            String placeName = "-NA-";
            String vicinity = "-NA-";
            String latitude = "";
            String longitude = "";
            String reference = "";

            try {
                // Extracting Place name, if available
                if (!jPlace.isNull("name")) {
                    placeName = jPlace.getString("name");
                }

                // Extracting Place Vicinity, if available
                if (!jPlace.isNull("vicinity")) {
                    vicinity = jPlace.getString("vicinity");
                }

                latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getString("lng");
                reference = jPlace.getString("reference");

                place.put("place_name", placeName);
                place.put("vicinity", vicinity);
                place.put("lat", latitude);
                place.put("lng", longitude);
                place.put("reference", reference);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return place;
        }
    }

    //  method for Custom Info Box
    class MyInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        private final View myContentsView;

        MyInfoWindowAdapter() {
            myContentsView = getLayoutInflater().inflate(R.layout.map_info_content, null);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {

            TextView tvTitle = ((TextView) myContentsView.findViewById(R.id.title));
            tvTitle.setText(marker.getTitle());
            TextView tvSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
            tvSnippet.setText(marker.getSnippet());

            return myContentsView;
        }
    }

    // redirect to google web for route finding...
    public void onClick_routeButton() {

        String sb = "https://www.google.com/maps/dir/" + latitude + "," + longitude + "/" + marker_Latitude + "," + marker_Longitude + "/";
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse(sb));
        startActivity(intent);

    }

    //Drop Down Popup page
    public AlertDialog showAboutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setMessage(getResources().getString(R.string.about_me_message) );

        builder.setCancelable(true);

        AlertDialog about = builder.create();
        about.show();
        TextView messageView = (TextView) about.findViewById(android.R.id.message);
        messageView.setLinkTextColor(Color.YELLOW);
        messageView.setGravity(Gravity.CENTER);
        return about;
    }

    //Drop Down Share page

    public void sharePage() {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            String sAux = getResources().getString(R.string.app_short_desc);
            sAux = sAux + getResources().getString(R.string.app_url);
            i.putExtra(Intent.EXTRA_TEXT, sAux);
            startActivity(Intent.createChooser(i, getResources().getString(R.string.share_popup_title)));
        } catch(Exception e) {

        }
    }


    public AlertDialog feedBackPage(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setMessage(getResources().getString(R.string.feedback_message) );
        builder.setCancelable(true);
        AlertDialog about = builder.create();
        about.show();
        TextView messageView = (TextView) about.findViewById(android.R.id.message);
        messageView.setLinkTextColor(Color.YELLOW);
        messageView.setGravity(Gravity.CENTER);
        return about;
    }


    public AlertDialog AppExecutionCounter(){
        // create or load shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        int count = preferences.getInt("run_count",-100);
        //application is running for first time....
        if(count == -100){
            count = 1;
            editor.putInt("run_count",count);
            editor.apply();
        }
        else{
            count = count +1;
            editor.putInt("run_count",count);
            editor.apply();
        }



        if(count == 1){
            final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            builder.setPositiveButton(getResources().getString(R.string.okay),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.setMessage(getResources().getString(R.string.intro_message));

            builder.setCancelable(true);


            AlertDialog about = builder.create();
            about.show();
            Button button = about.getButton(DialogInterface.BUTTON_POSITIVE);
            button.setTextColor(Color.WHITE);
            button.setGravity(Gravity.CENTER);
            button.setTextSize(20);
            TextView messageView = (TextView) about.findViewById(android.R.id.message);
            messageView.setGravity(Gravity.CENTER);
            return about;
        }
        return null;
    }

    public void onClick_zoom_in(View view){
            float zoom = mMap.getCameraPosition().zoom;
            double longi = mMap.getCameraPosition().target.longitude;
            double lati = mMap.getCameraPosition().target.latitude;
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati,longi), zoom + 1 ));
    }

    public void onClick_zoom_out(View view){
        float zoom = mMap.getCameraPosition().zoom;
        double longi = mMap.getCameraPosition().target.longitude;
        double lati = mMap.getCameraPosition().target.latitude;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati,longi), zoom - 1 ));

    }

}



