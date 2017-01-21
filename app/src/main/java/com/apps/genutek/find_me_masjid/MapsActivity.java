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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TextView;

import com.apps.genutek.find_me_masjid.utils.IabHelper;
import com.apps.genutek.find_me_masjid.utils.IabResult;
import com.apps.genutek.find_me_masjid.utils.Inventory;
import com.apps.genutek.find_me_masjid.utils.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
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

import static com.apps.genutek.find_me_masjid.R.id.adView;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private double latitude = 33.6;
    private double longitude = 73.1;
    private RewardedVideoAd mRewardedVideoAd;

    private static int support_point = 0;

    private double marker_Latitude;
    private double marker_Longitude;

    private TextView remove_ads_score;
    private TextView support_me_score;



    private TextView text_support_20;
    private TextView text_support_50;
    private TextView text_support_100;
    private TextView text_support_200;

    //in-app items
    String support_20_price,support_50_price,support_100_price,support_200_price;
    static final String support_20 = "support_20";
    static final String support_50 = "support_50";
    static final String support_100 = "support_100";
    static final String support_200 = "support_200";
    Inventory inventory;

    //in-app purchase related variables
    IabHelper mHelper;
    Boolean is_in_app_purchase_available = false;

    //in-app purchase related listeners
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener =
            new IabHelper.QueryInventoryFinishedListener() {
                public void onQueryInventoryFinished(IabResult result, Inventory inv) {
                    if (result.isFailure()) {
                        // handle error here
                    }
                    else {
                        inventory = inv;
                        //if any item purchased then consume it...
                        try {
                            if(inv.getPurchase(support_20) != null) mHelper.consumeAsync(inv.getPurchase(support_20), mConsumeFinishedListener);
                            if(inv.getPurchase(support_50) != null) mHelper.consumeAsync(inv.getPurchase(support_50), mConsumeFinishedListener);
                            if(inv.getPurchase(support_100) != null) mHelper.consumeAsync(inv.getPurchase(support_100), mConsumeFinishedListener);
                            if(inv.getPurchase(support_200) != null) mHelper.consumeAsync(inv.getPurchase(support_200), mConsumeFinishedListener);

                        } catch (IabHelper.IabAsyncInProgressException e) {
                            e.printStackTrace();
                        }
                    }

                }
            };

    // Called when consumption is complete
    private IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {

            if (mHelper == null) return;

            if (result.isSuccess()) {

            }

        }
    };

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (!result.isFailure()) {
                int amount = 0;
                try {
                    if (purchase.getSku().equals(support_20)) {
                        amount = 20;
                        mHelper.consumeAsync(inventory.getPurchase(support_20), mConsumeFinishedListener);
                    }
                    if (purchase.getSku().equals(support_50)) {
                        amount = 50;
                        mHelper.consumeAsync(inventory.getPurchase(support_50), mConsumeFinishedListener);
                    }
                    if (purchase.getSku().equals(support_100)) {
                        amount = 100;
                        mHelper.consumeAsync(inventory.getPurchase(support_100), mConsumeFinishedListener);
                    }
                    if (purchase.getSku().equals(support_200)) {
                        amount = 200;
                        mHelper.consumeAsync(inventory.getPurchase(support_200), mConsumeFinishedListener);
                    }
                } catch (Exception e) {

                }
                showDialog(getResources().getString(R.string.thanks_for_purchase));
                support_point = support_point + amount;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("support_point", support_point);
                editor.apply();
                support_me_score.setText(new Integer(support_point).toString());
                //hide banner if greater than 100
                if(support_point >= 100){
                    findViewById(R.id.layout_ads).setVisibility(View.GONE);
                }
            }

        }
    };



    // check from where the request is coming from... remove ads page or support me page.
    String request_originated_from;

    private long timeout = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //show intro_dialog
        AppExecutionCounter();

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
        // create listener for search icon (search icon of the right side)
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
                                removeAds();
                                return true;
                            case R.id.item_feedback:
                                feedBackPage();
                                return true;
                            case R.id.item_share:
                                sharePage();
                                return true;
                            case R.id.item_support_me:
                                support();
                                return true;
                            case R.id.how_to_use:
                                introScreen();
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

        //init in app purchase
        mHelper = new IabHelper(this, "2DZIyW2ciS77LHd9otNqLI8kht6gm3HxNeLr9pH0AHRUln2GLD57xxSRxUGD1Wi52DZIyW2ciS77L8kht6gm3HxNeL" +
                "r9pH0AHRUln2GLD57xxHd9otNqLISRxUGD1Wi52DZIyW2ciS77LHd9otNqLISRxUGD1Wi5");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    is_in_app_purchase_available =false;
                }
                else{
                    try {
                        mHelper.queryInventoryAsync(mGotInventoryListener);
                    } catch (IabHelper.IabAsyncInProgressException e) {

                    }
                    is_in_app_purchase_available = true;
                }
            }
        });



        // init advert
        MobileAds.initialize(getApplicationContext(), getResources().getString(R.string.app_ads_id));
        AdView mAdView = (AdView) findViewById(adView);
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {

            }

            @Override
            public void onRewardedVideoAdOpened() {

            }

            @Override
            public void onRewardedVideoStarted() {

            }

            @Override
            public void onRewardedVideoAdClosed() {
                loadRewardedVideoAd();
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                int amount = rewardItem.getAmount();
                support_point = support_point + amount;
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("support_point", support_point);
                editor.apply();
                if(request_originated_from.equals("removeAds"))
                    remove_ads_score.setText(support_point + " / 100");
                if(request_originated_from.equals("support"))
                    support_me_score.setText(new Integer(support_point).toString());
                if(support_point >= 100){
                    findViewById(R.id.layout_ads).setVisibility(View.GONE);
                }

            }

            @Override
            public void onRewardedVideoAdLeftApplication() {

            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {

            }
        });
        loadRewardedVideoAd();
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        getEarnedSupportPoints();




    }

    @Override
    public void onPause(){
        super.onPause();
        mRewardedVideoAd.pause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRewardedVideoAd.resume(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null)
            try {
                mHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        mHelper = null;
    }

    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));
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
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                updateMap();
                PlacesTask placesTask = new PlacesTask();
                placesTask.execute(sbMethod());
            }
        });

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng latLng =  marker.getPosition();
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                updateMap();
                PlacesTask placesTask = new PlacesTask();
                placesTask.execute(sbMethod());
            }
        });


    }

    public void getCountryLongLat() {
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String iso = tm.getNetworkCountryIso();
        Locale locale = new Locale("", iso);
        String country = locale.getDisplayCountry();
        // Get Current Location
        getLongLat(country, false);
    }

    public void getLongLat(String location, Boolean isLocation) {
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
            if (isLocation) {
                showDialog(getResources().getString(R.string.location_found_error));
                e.printStackTrace();
            }
        }
    }

    public void searchArea() {
        if(isNetworkAvailable()) {
            EditText mEdit = (EditText) findViewById(R.id.EditText_Search);
            mEdit.clearFocus();
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            in.hideSoftInputFromWindow(mEdit.getWindowToken(), 0);
            String cityText = mEdit.getText().toString();
            getLongLat(cityText, true);
            updateMap();
            PlacesTask placesTask = new PlacesTask();
            placesTask.execute(sbMethod());
        }
        else{
            showDialog(getResources().getString(R.string.no_internet_connection));
        }
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
        if(isNetworkAvailable()) {

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
        }else{
            showDialog(getResources().getString(R.string.no_internet_connection));
        }

    }

    public void buildAlertMessageNoGps() {
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

    public void updateMap() {
        mMap.clear();
        LatLng location = new LatLng(latitude, longitude);
        mMap.addMarker(new MarkerOptions().position(location).title(getResources().getString(R.string.requested_location)).draggable(true));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14));
    }

    public AlertDialog showDialog(String s) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setCancelable(true);
        builder.setMessage(s);
        builder.setPositiveButton(
                getResources().getString(R.string.okay),
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

    public class PlacesTask extends AsyncTask<String, Integer, String> {

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

    public String downloadUrl(String strUrl) throws IOException {
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

    public class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

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

        public List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
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

        public HashMap<String, String> getPlace(JSONObject jPlace) {

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
    public void showAboutDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setMessage(getResources().getString(R.string.about_us_message));

        builder.setCancelable(true);

        AlertDialog about = builder.create();
        about.show();
        TextView messageView = (TextView) about.findViewById(android.R.id.message);
        messageView.setLinkTextColor(Color.YELLOW);
        messageView.setGravity(Gravity.CENTER);
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
        } catch (Exception e) {

        }
    }

    public void feedBackPage() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        builder.setMessage(getResources().getString(R.string.feedback_message));
        builder.setCancelable(true);
        AlertDialog about = builder.create();
        about.show();
        TextView messageView = (TextView) about.findViewById(android.R.id.message);
        messageView.setLinkTextColor(Color.YELLOW);
        messageView.setGravity(Gravity.CENTER);
    }

    public void AppExecutionCounter() {
        // create or load shared preferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        int count = preferences.getInt("run_count", -100);
        //application is running for first time....
        if (count == -100) {
            count = 1;
            editor.putInt("run_count", count);
            editor.apply();
        } else {
            count = count + 1;
            editor.putInt("run_count", count);
            editor.apply();
        }


        if (count == 1) {
            // ask for GPS permission if not allowed yet....
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                int isPermissionGranted = 0;
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        isPermissionGranted);
            }
            introScreen();
        }

        if (count %10 == 0) {
          support();
        }
    }

    public void onClick_zoom_in(View view) {
        float zoom = mMap.getCameraPosition().zoom;
        double longi = mMap.getCameraPosition().target.longitude;
        double lati = mMap.getCameraPosition().target.latitude;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati, longi), zoom + 1));
    }

    public void onClick_zoom_out(View view) {
        float zoom = mMap.getCameraPosition().zoom;
        double longi = mMap.getCameraPosition().target.longitude;
        double lati = mMap.getCameraPosition().target.latitude;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lati, longi), zoom - 1));

    }

    public void removeAds() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.remove_ads, null);
        builder.setView(dialogView);
        remove_ads_score = (TextView)dialogView.findViewById(R.id.text_support_pts_rem_ads);
        remove_ads_score.setText(support_point + " / 100");
        final AlertDialog removeAdsScreen = builder.create();

        Button show_ads = (Button)dialogView.findViewById(R.id.button_show_rewarded_ads);
        if(!mRewardedVideoAd.isLoaded()){
            show_ads.setEnabled(false);
            show_ads.setBackgroundColor(ContextCompat.getColor(this, R.color.LightGray));
            TextView no_ads = (TextView)dialogView.findViewById(R.id.text_no_ad);
            no_ads.setVisibility(View.VISIBLE);
        }
        show_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request_originated_from = "removeAds";
                show_rewarded_ads();
            }
        });
        Button gotoSupport = (Button)dialogView.findViewById(R.id.button_goto_support);
        gotoSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                support();
                removeAdsScreen.dismiss();

            }
        });
        removeAdsScreen.show();
    }

    public void getEarnedSupportPoints() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        support_point = preferences.getInt("support_point", -100);
        if(support_point == -100){
            support_point = 0;
            editor.putInt("support_point", support_point);
            editor.apply();
        }
    }

    public void show_rewarded_ads() {
        showRewardedVideo();
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void loadRewardedVideoAd() {
        if (!mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.loadAd(getResources().getString(R.string.video_ad_unit_id), new AdRequest.Builder().build());
        }
    }

    public void showRewardedVideo() {
        if (mRewardedVideoAd.isLoaded()) {
            mRewardedVideoAd.show();
        }
    }

    //support page.....
    public void support(){
        //fetch price list in local currency from google.
        getPriceList();


        // create and setup Dialog window
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.support, null);

        builder.setView(dialogView);

        support_me_score = (TextView)dialogView.findViewById(R.id.text_support_score);
        support_me_score.setText(new Integer(support_point).toString());

        text_support_20 = (TextView)dialogView.findViewById(R.id.text_price_20);
        text_support_50 = (TextView)dialogView.findViewById(R.id.text_price_50);
        text_support_100 = (TextView)dialogView.findViewById(R.id.text_price_100);
        text_support_200 = (TextView)dialogView.findViewById(R.id.text_price_200);

        //check if inapp purchase is available
        if(is_in_app_purchase_available){
            //change the button to be active.
            dialogView.findViewById(R.id.button_s_20).setEnabled(true);
            dialogView.findViewById(R.id.button_s_20).setBackgroundResource(R.drawable.support_10);

            dialogView.findViewById(R.id.button_s_50).setEnabled(true);
            dialogView.findViewById(R.id.button_s_50).setBackgroundResource(R.drawable.support_25);

            dialogView.findViewById(R.id.button_s_100).setEnabled(true);
            dialogView.findViewById(R.id.button_s_100).setBackgroundResource(R.drawable.support_50);

            dialogView.findViewById(R.id.button_s_200).setEnabled(true);
            dialogView.findViewById(R.id.button_s_200).setBackgroundResource(R.drawable.support_100);
        }



        // show rewarded Advert..
        Button show_ads = (Button)dialogView.findViewById(R.id.button_show_rewarded_ads);
        if(!mRewardedVideoAd.isLoaded()){
            show_ads.setEnabled(false);
            show_ads.setBackgroundColor(ContextCompat.getColor(this, R.color.LightGray));
            TextView no_ads = (TextView)dialogView.findViewById(R.id.text_no_ad);
            no_ads.setVisibility(View.VISIBLE);
        }
        show_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                request_originated_from = "support";
                show_rewarded_ads();

            }
        });

        AlertDialog b = builder.create();
        b.show();
    }

    public void introScreen(){
        //show the welcome dialog screen to user...

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.intro_dialog, null);
        builder.setView(dialogView);

        builder.setCancelable(true);
        final AlertDialog intro = builder.create();
        intro.show();
        //okay to dismiss...
        Button okay = (Button) dialogView.findViewById(R.id.button_okay);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intro.dismiss();
            }
        });

    }
    ///////////////////////////////////////////////////////////////
    // in-app purchase functions...
    ///////////////////////////////////////////////////////////////
    public void getPriceList() {
        List<String> purchaseItemList = new ArrayList<>();
        purchaseItemList.add(support_20);
        purchaseItemList.add(support_50);
        purchaseItemList.add(support_100);
        purchaseItemList.add(support_200);

        IabHelper.QueryInventoryFinishedListener mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
            public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
                if (result.isFailure()) {
                    // handle error
                    return;
                } else {
                    support_20_price = inventory.getSkuDetails(support_20).getPrice();
                    support_50_price = inventory.getSkuDetails(support_50).getPrice();
                    support_100_price = inventory.getSkuDetails(support_100).getPrice();
                    support_200_price = inventory.getSkuDetails(support_200).getPrice();

                    // update the UI
                    text_support_20.setText("20 Support \n" + support_20_price);
                    text_support_50.setText("50 Support \n" + support_50_price);
                    text_support_100.setText("100 Support \n" + support_100_price);
                    text_support_200.setText("200 Support \n" + support_200_price);
                }
            }
        };

        try {
            mHelper.queryInventoryAsync(true, purchaseItemList, null, mQueryFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }


    }

    public void onClick_support_20(View view) {
        if (is_in_app_purchase_available) {
            try {
                mHelper.launchPurchaseFlow(this, support_20, 777, mPurchaseFinishedListener, "");
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

    }

    public void onClick_support_50(View view) {
        if (is_in_app_purchase_available) {
            try {
                mHelper.launchPurchaseFlow(this, support_50, 777, mPurchaseFinishedListener, "");
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

    }

    public void onClick_support_100(View view) {
        if (is_in_app_purchase_available) {
            try {
                mHelper.launchPurchaseFlow(this, support_100, 777, mPurchaseFinishedListener, "");
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

    }

    public void onClick_support_200(View view) {
        if (is_in_app_purchase_available) {
            try {
                mHelper.launchPurchaseFlow(this, support_200, 777, mPurchaseFinishedListener, "");
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }

    }


}



