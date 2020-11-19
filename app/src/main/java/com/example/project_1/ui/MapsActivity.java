package com.example.project_1.ui;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.project_1.R;
import com.example.project_1.model.CustomInfoWindowAdapter;
import com.example.project_1.model.HazardRating;
import com.example.project_1.model.InputStreamVolleyRequest;
import com.example.project_1.model.Inspection;
import com.example.project_1.model.InspectionType;
import com.example.project_1.model.Restaurant;
import com.example.project_1.model.RestaurantManager;
import com.example.project_1.model.Violation;
import com.example.project_1.model.ViolationSeverity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    public static final float DEFAULT_ZOOM = 15f;
    private static final String COORDINATE = "coordinate";

    private GoogleMap mMap;
    //private View restaurantClicker;
    private static final String TAG = "Map activity";
    private boolean mLocationPermissionGranted = false;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker mMarker;
    private TextView tvSnippet;
    private ImageView ivSnippet;

    private RestaurantManager mRestaurantManager;
    private List<Restaurant> mRestaurantList;
    private List<Inspection> mCurrentRestaurantInspectionList;
    private Restaurant mCurrentRestaurant;
    private Inspection mLastInspection;

    private HazardRating hazardRating;
    private String restaurantName;
    private String snippet;
    private double latitude;
    private double longitude;
    private GoogleMapOptions mGoogleMapOptions;

    ProgressDialog pDialog;
    private static boolean loadedFromSave = false;
    private int count;
    private long lastUpdatedTimeInMilliseconds = 0;
    private long lastModifiedTimeInMilliseconds = 0;
    private boolean exitAllActivities = false;
    private long currentLastModifiedTimeInMilliseconds = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mRestaurantManager = RestaurantManager.getInstance();

        load();
        checkUpdateOfFraserHealthRestaurantInspectionReports();

        //getLocationPermission();

    }

    private void addRestaurantMarker() {

        Log.d(TAG, "addRestaurantMarker: getting restaurant info");

        // Get restaurant lists
        mRestaurantList = mRestaurantManager.getRestaurants();
        LatLng currentRestaurantLatLng;
        MarkerOptions options;
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(MapsActivity.this));


        // Add a marker for for restaurant on the list
        for (int i = 0; i < mRestaurantList.size(); i++) {

            // Get current restaurant
            mCurrentRestaurant = mRestaurantList.get(i);
            // Get a list of inspections for the current restaurant
            mCurrentRestaurantInspectionList = mRestaurantManager.getInspectionsForRestaurant(i);

            // Current restaurant inspection list is not empty
            if (!mCurrentRestaurantInspectionList.isEmpty()) {
                // Get restaurant info
                restaurantName = mCurrentRestaurant.getName();
                Log.d(TAG, "name: " + restaurantName);
                latitude = mCurrentRestaurant.getLatitude();
                Log.d(TAG, "latitude: " + latitude);
                longitude = mCurrentRestaurant.getLongitude();
                Log.d(TAG, "longitude: " + longitude);
                currentRestaurantLatLng = new LatLng(latitude, longitude);

                // Get the last inspection hazard
                hazardRating = mCurrentRestaurantInspectionList.get(0).getHazardRating();
                Log.d(TAG, "Hazard rating: " + hazardRating);

                snippet = "Address: " + mCurrentRestaurant.getPhysicalAddress() + "\n"
                        + "Hazard level: " + hazardRating;

                switch (hazardRating) {
                    case LOW:
                        Log.d(TAG, "setting restaurant marker green");
                        // Set marker
                        options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_green_24));
                        mMap.addMarker(options);
                        break;

                    case MODERATE:
                        Log.d(TAG, "setting restaurant marker yellow");
                        // Set marker
                        options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_yellow_24));
                        mMap.addMarker(options);
                        break;

                    case HIGH:
                        Log.d(TAG, "setting restaurant marker red");
                        // Set marker
                        options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_red_24));
                        mMap.addMarker(options);
                        break;
                }


            } else {
                // Current restaurant inspection list is empty
                restaurantName = mCurrentRestaurant.getName();
                Log.d(TAG, "name: " + restaurantName);
                latitude = mCurrentRestaurant.getLatitude();
                Log.d(TAG, "latitude: " + latitude);
                longitude = mCurrentRestaurant.getLongitude();
                Log.d(TAG, "longitude: " + longitude);
                currentRestaurantLatLng = new LatLng(latitude, longitude);
                hazardRating = HazardRating.LOW;
                snippet = "Address: " + mCurrentRestaurant.getPhysicalAddress() + "\n"
                        + "Hazard level: " + hazardRating;

                Log.d(TAG, "setting restaurant marker green");
                options = new MarkerOptions()
                        .position(currentRestaurantLatLng)
                        .title(restaurantName)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_green_24));
                mMap.addMarker(options);
            }

        }

        // Get restaurant full info after clicking the info window
        setCallbackToStartFullRestaurantInfo();
    }

    private void setCallbackToStartFullRestaurantInfo() {

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                LatLng markerLatLng = marker.getPosition();

                Intent intent = RestaurantDetails.makeIntent(MapsActivity.this,
                        mRestaurantManager.getIndexFromLatLng(markerLatLng.latitude, markerLatLng.longitude));
                startActivity(intent);

            }
        });
    }

    private BitmapDescriptor BitmapDescriptorFromVector(Context context, int vectorResID) {

        // source: https://www.youtube.com/watch?v=26bl4r3VtGQ&t=355s

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResID);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void getLocationPermission() {
        Log.d(TAG, "Getting permission location");
        String[] permissions = {FINE_LOCATION, COARSE_LOCATION};
        int checkFineLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION);
        int checkCoarseLocation = ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION);

        if (checkFineLocation == PackageManager.PERMISSION_GRANTED) {
            if (checkCoarseLocation == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initiateMap();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions, LOCATION_PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionResult: called");
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult : permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionResult: permission granted");
                    mLocationPermissionGranted = true;
                    // Initiate our map
                    initiateMap();
                }
            }
        }
    }

    private void initiateMap() {

        Log.d(TAG, "Initiating map");

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        /*SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);*/

        mGoogleMapOptions = new GoogleMapOptions();
        mGoogleMapOptions.zoomControlsEnabled(true);

        Log.d(TAG, "Initiating map");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(mGoogleMapOptions);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.map, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(MapsActivity.this);
    }

    private void getDeviceLocation() {
        Log.d(TAG, "GetDeviceLocation: getting device current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: found location");
                            mCurrentLocation = (Location) task.getResult();

                            LatLng userLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
                            Log.d(TAG, "current location " + userLatLng);
                            moveCamera(userLatLng, DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapsActivity.this,
                                    "unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude
                + " long " + latLng.longitude);
        // Update map current location
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: creating google map");
        mMap = googleMap;

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            Log.d(TAG, "onMapReady: getting device location");

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            addRestaurantMarker();
        }
        /*// Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }


    // check update
    private void checkUpdateOfFraserHealthRestaurantInspectionReports() {

        String urlFraserHealthRestaurantInspectionReports = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

        //final JSONObject[] restaurantRequest = new JSONObject[1];
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlFraserHealthRestaurantInspectionReports,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject fraserHealthRestaurantInspectionReportsObj = new JSONObject(response);
                            JSONObject result = fraserHealthRestaurantInspectionReportsObj.getJSONObject("result");
                            JSONArray resources = result.getJSONArray("resources");
                            String inspectionReportsTimeStamp = resources.getJSONObject(0).getString("last_modified");
                            String inspectionReportsURL = resources.getJSONObject(0).getString("url");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.ENGLISH);
                            LocalDateTime lastModified = LocalDateTime.parse(inspectionReportsTimeStamp, formatter);
                            currentLastModifiedTimeInMilliseconds = lastModified.atOffset(ZoneOffset.ofHours(-7)).toInstant().toEpochMilli();
                            long currentTimeInMilliseconds = Instant.now().toEpochMilli();
                            Log.i("myResponse", response);
                            Log.i("myResources", resources.toString());
                            Log.i("myInspectionReportTimeStamp", inspectionReportsTimeStamp);
                            Log.i("myLastModifiedTime", "Date in milli :: FOR API >= 26 >>> " + currentLastModifiedTimeInMilliseconds);
                            Log.i("myCurrentTime", "current time: " + currentTimeInMilliseconds);
                            Log.i("myURL", inspectionReportsURL);

                            promptUserDownloadUpdateDialog(currentTimeInMilliseconds, inspectionReportsURL);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });


        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);

    }


    private void promptUserDownloadUpdateDialog(long currentTimeInMilliseconds, String inspectionReportsURL) {

        // 20 hours = 72000000 Milliseconds
        if ((!loadedFromSave || (currentTimeInMilliseconds - lastUpdatedTimeInMilliseconds > 72000000))
        && (currentLastModifiedTimeInMilliseconds != lastModifiedTimeInMilliseconds)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("Last update is 20 hours ago");
                    builder.setMessage("Do you want to download the update?");

                    builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Boolean updateAvailable = false;
                            File oldRestaurants = new File(getFilesDir(), "oldRestaurants");

                            setRestaurantData(updateAvailable, oldRestaurants);
                            File oldInspectionReports = new File(getFilesDir(), "oldRestaurants");
                            setInspectionData(updateAvailable, oldInspectionReports);
                            /*mRestaurantManager.sortRestaurantList();
                            mRestaurantManager.sortInspectionDate();
                            populateListView();
                            populateIcon();*/
                            //Intent home = new Intent(getApplicationContext(), OfficeActivity.class);
                            //startActivity(home);
                            //finish();
                            mRestaurantManager.sortRestaurantList();
                            mRestaurantManager.sortInspectionDate();

                            if (!loadedFromSave) {
                                save();
                            }

                            getLocationPermission();
                            Log.e(TAG, "onClick: " + mRestaurantManager);
                        }
                    });

                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            String urlRestaurants = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";

                            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRestaurants,
                                    new Response.Listener<String>() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject restaurants = new JSONObject(response);
                                                JSONObject result = restaurants.getJSONObject("result");
                                                JSONArray resources = result.getJSONArray("resources");
                                                String restaurantsURL = resources.getJSONObject(0).getString("url");
                                                Log.i("myURLRestaurant", restaurantsURL);

                                                downloadUpdate(restaurantsURL, inspectionReportsURL);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            });

                            // Add the request to the RequestQueue.
                            //RequestQueue requestQueue = Volley.newRequestQueue(this);
                            Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
                            //requestQueue.add(stringRequest);

                            //Intent home = new Intent(getApplicationContext(), SchoolActivity.class);
                            //startActivity(home);
                            //finish();
                        }
                    });
                    AlertDialog alertdialog = builder.create();
                    alertdialog.show();
                }
            }, 100);

        } else {
            getLocationPermission();
        }

    }


    private void downloadUpdate(String restaurantsURL, String inspectionReportsURL) {
        // cannot save download file to res https://stackoverflow.com/questions/3374061/write-to-res-drawable-on-the-fly/3374149#3374149
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        //pDialog.setCancelable(false);
        final boolean[] cancelDownloading = {false};
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                pDialog.dismiss();//dismiss dialog
                cancelDownloading[0] = true;
                //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).stop();
                //requestQueue.stop();
                Boolean updateAvailable = false;
                File oldRestaurants = new File(getFilesDir(), "oldRestaurants");
                setRestaurantData(updateAvailable, oldRestaurants);
                File oldInspectionReports = new File(getFilesDir(), "oldRestaurants");
                setInspectionData(updateAvailable, oldInspectionReports);
                /*mRestaurantManager.sortRestaurantList();
                mRestaurantManager.sortInspectionDate();*/
                mRestaurantManager.sortRestaurantList();
                mRestaurantManager.sortInspectionDate();
                save();
                getLocationPermission();
            }
        });
        pDialog.show();

        InputStreamVolleyRequest request;
        request = new InputStreamVolleyRequest(Request.Method.GET, restaurantsURL, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                try {
                    if (response != null) {

                        String filename = "newRestaurants";
                        //filename = filename.replace(":", ".");
                        Log.d("myRESUME FILE NAME", filename);

                        try {
                            long lenghtOfFile = response.length;

                            //covert reponse to input stream
                            InputStream input = new ByteArrayInputStream(response);
                            //File path = Environment.getExternalStorageDirectory();
                            File newRestaurants = new File(getFilesDir(), filename);
                            map.put("resume_path", newRestaurants.toString());
                            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(newRestaurants));
                            byte data[] = new byte[1024];

                            long total = 0;

                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }

                            output.flush();
                            output.close();
                            input.close();

                            downloadInspectionReports(newRestaurants, inspectionReportsURL, cancelDownloading);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE. ERROR:: " + error.getMessage());
            }
        }, null);
        request.setTag("tag");
        Request<byte[]> requestQueueByte = Volley.newRequestQueue(getApplicationContext(), new HurlStack()).add(request);
        //RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
        //requestQueue = requestQueue.add(request);
        if (cancelDownloading[0]) {
            //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).stop();
            //Log.i("myStop", "stop");
            requestQueueByte.cancel();
        }
        //requestQueue.add(request);
    }


    private void downloadInspectionReports(File newRestaurants, String inspectionReportsURL, boolean[] cancelDownloading) {
        InputStreamVolleyRequest request;
        request = new InputStreamVolleyRequest(Request.Method.GET, inspectionReportsURL, new Response.Listener<byte[]>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(byte[] response) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                try {
                    if (response != null) {

                        String filename = "newInspectionReports";
                        //filename = filename.replace(":", ".");
                        Log.d("myRESUME FILE NAME", filename);

                        try {
                            long lenghtOfFile = response.length;

                            //covert reponse to input stream
                            InputStream input = new ByteArrayInputStream(response);
                            //File path = Environment.getExternalStorageDirectory();
                            File newInspectionReports = new File(getFilesDir(), filename);
                            map.put("resume_path", newInspectionReports.toString());
                            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(newInspectionReports));
                            byte data[] = new byte[1024];

                            long total = 0;

                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }

                            output.flush();
                            output.close();
                            input.close();

                            int fileSizeNewRestaurants = Integer.parseInt(String.valueOf(newRestaurants.length() / 1024));
                            Log.d("myFileSize", ": " + fileSizeNewRestaurants + "KB");
                            int fileSizeNewInspectionReports = Integer.parseInt(String.valueOf(newInspectionReports.length() / 1024));
                            Log.d("myFileSize", ": " + fileSizeNewInspectionReports + "KB");

                            pDialog.dismiss();
                            Boolean updateAvailable = true;
                            if (cancelDownloading[0]) {
                                updateAvailable = false;
                            }
                            Log.i("myCancel", ": " + cancelDownloading[0]);
                            setRestaurantData(updateAvailable, newRestaurants);
                            setInspectionData(updateAvailable, newInspectionReports);

                            lastUpdatedTimeInMilliseconds = Instant.now().toEpochMilli();
                            lastModifiedTimeInMilliseconds = currentLastModifiedTimeInMilliseconds;

                            //change here to adapt large data
                            //sortRestaurantList();
                            //sortInspectionDate();
                            //populateListView();
                            //populateIcon();
                            mRestaurantManager.sortRestaurantList();
                            mRestaurantManager.sortInspectionDate();
                            save();
                            getLocationPermission();
                            Log.e(TAG, "onClick: " + mRestaurantManager);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE. ERROR:: " + error.getMessage());
            }
        }, null);
        //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).add(request);
        Request<byte[]> requestQueueByte = Volley.newRequestQueue(getApplicationContext(), new HurlStack()).add(request);
        if (cancelDownloading[0]) {
            //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).stop();
            //Log.i("myStop", "stop");
            requestQueueByte.cancel();
        }
        //requestQueue.add(request);
    }


    private void setInspectionData(Boolean updateAvailable, File newInspectionReports) {
        if (updateAvailable) {
            try (BufferedReader reader = new BufferedReader(new FileReader(newInspectionReports))) {
                mRestaurantManager.emptyInspections();
                String line;

                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 6) {
                        break;
                    }
                    Log.i("myLine", ": " + line.length());
                    String[] tokens = line.split(",");
                    Log.i("myToken0", tokens[0]);
                    //if (tokens[0] == "") {break;}
                    Inspection sampleInspection = new Inspection();
                    sampleInspection.setTrackingNumber(tokens[0]);
                    Log.i("myTrackingNumber", tokens[0]);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    Date date = simpleDateFormat.parse(tokens[1]);

                    sampleInspection.setDate(date);

                    // dry inspection type
                    InspectionType inspectionType;
                    if (tokens[2].equals("Routine")) {
                        inspectionType = InspectionType.ROUTINE;

                    } else {
                        inspectionType = InspectionType.FOLLOW_UP;
                    }
                    sampleInspection.setType(inspectionType);

                    // Set critical issues
                    if (tokens[3].length() > 0) {
                        sampleInspection.setNumCritical(Integer.parseInt(tokens[3]));
                    } else {
                        sampleInspection.setNumCritical(0);
                    }

                    // Set non critical issues
                    if (tokens[4].length() > 0) {
                        sampleInspection.setNumNonCritical(Integer.parseInt(tokens[4]));
                    } else {
                        sampleInspection.setNumNonCritical(0);
                    }

                    // Hazard rating
                    HazardRating hazardRating = null;
                    if (tokens[tokens.length - 1].equals("Low")) {
                        hazardRating = HazardRating.LOW;
                    } else if (tokens[tokens.length - 1].equals("Moderate")) {
                        hazardRating = HazardRating.MODERATE;
                    } else if (tokens[tokens.length - 1].equals("High")) {
                        hazardRating = HazardRating.HIGH;
                    } else {
                        hazardRating = HazardRating.LOW;
                    }
                    Log.i("myHazardRating", tokens[tokens.length - 1]);
                    sampleInspection.setHazardRating(hazardRating);


                    // Violations
                    if (tokens.length > 6) {
                        Log.i("myViolationsLength ", "setInspectionData: " + (tokens.length - 6));

                        // Get violations String "..."
                        StringBuilder violationsString = new StringBuilder();
                        for (int i = 5; i < tokens.length - 1; i++) {
                            //Log.e("token[i]", "loop: " + tokens[i]);
                            if (i == 5) {
                                violationsString.append(tokens[i]);
                            } else {
                                violationsString.append(",").append(tokens[i]);
                            }
                        }

                        List<Violation> violationList = getViolationsFromString(violationsString.toString());
                        //List<Violation> violationList = new ArrayList<>();
                        sampleInspection.setViolations(violationList);

                        //Log.i("myViolations", "setInspectionData: " + violationList);
                    }

                    mRestaurantManager.addInspection(sampleInspection);
                    Log.i("myInspectionLength", ": " + mRestaurantManager.getInspections().size());
                }
                Log.i("myInspectionLengthFinal", ": " + mRestaurantManager.getInspections().size());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }


        } /*else if (LOAD) {
            load();
            Log.e("TAG", "setInspectionData: " + mRestaurantManager);
        }*/ else if (!loadedFromSave) {
            InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );

            String line = "";
            try {
                // Step over headers
                reader.readLine();

                while ((line = reader.readLine()) != null) {

                    // Split by ','
                    String[] tokens = line.split(",");

                    // Read data from inspectionreports_itr1.csv
                    Inspection sampleInspection = new Inspection();
                    sampleInspection.setTrackingNumber(tokens[0]);
                    Log.i("myTrackingNumber", tokens[0]);

                    // Set date
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    Date date = simpleDateFormat.parse(tokens[1]);

                    sampleInspection.setDate(date);

                    // dry inspection type
                    InspectionType inspectionType;
                    if (tokens[2].equals("\"Routine\"")) {
                        inspectionType = InspectionType.ROUTINE;

                    } else {
                        inspectionType = InspectionType.FOLLOW_UP;
                    }
                    sampleInspection.setType(inspectionType);

                    // Set critical issues
                    if (tokens[3].length() > 0) {
                        sampleInspection.setNumCritical(Integer.parseInt(tokens[3]));
                    } else {
                        sampleInspection.setNumCritical(0);
                    }

                    // Set non critical issues
                    if (tokens[4].length() > 0) {
                        sampleInspection.setNumNonCritical(Integer.parseInt(tokens[4]));
                    } else {
                        sampleInspection.setNumNonCritical(0);
                    }

                    // Hazard rating
                    HazardRating hazardRating = null;
                    if (tokens[5].equals("\"Low\"")) {
                        hazardRating = HazardRating.LOW;
                    }
                    if (tokens[5].equals("\"Moderate\"")) {
                        hazardRating = HazardRating.MODERATE;
                    }
                    if (tokens[5].equals("\"High\"")) {
                        hazardRating = HazardRating.HIGH;
                    }
                    sampleInspection.setHazardRating(hazardRating);

                    // Violations
                    if (tokens.length > 6) {
                        //Log.e("violations length ", "setInspectionData: " + (tokens.length - 6));

                        // Get violations String "..."
                        StringBuilder violationsString = new StringBuilder();
                        for (int i = 6; i < tokens.length; i++) {
                            //Log.e("token[i]", "loop: " + tokens[i]);

                            if (i == 6) {
                                violationsString.append(tokens[i].substring(1));
                            } else {
                                violationsString.append(",").append(tokens[i]);
                            }
                        }

                        List<Violation> violationList = getViolationsFromString(violationsString.toString());

                        sampleInspection.setViolations(violationList);

                        //Log.e("violations", "setInspectionData: " + violationList);
                    }

                    mRestaurantManager.addInspection(sampleInspection);

                    Log.d("Inspection List", "Just created: " + sampleInspection);
                }
            } catch (IOException | ParseException e) {
                Log.wtf("Inspection List", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }
        }


    }

    private List<Violation> getViolationsFromString(String violationsString) {
        violationsString = violationsString.replaceAll("^\"|\"$", "");
        Log.i("myGetViolationsFromString", "getViolationsFromString: " + violationsString);
        ArrayList<Violation> list = new ArrayList<>();
        if (violationsString != "") {
            String[] violations = violationsString.split("\\|");

            String code;
            ViolationSeverity severity;
            String description;
            for (String s : violations) {
                Log.i("in loop", "getViolationsFromString: " + s);
                String[] tokens = s.split(",");

                code = tokens[0];

                if (tokens[1].equals("Critical")) {
                    severity = ViolationSeverity.CRITICAL;
                } else {
                    severity = ViolationSeverity.NON_CRITICAL;
                }

                description = tokens[2];

                Violation violation = new Violation(description, severity, code);
                //Log.e("Violation OBJECT", "getViolationsFromString: " + violation);

                list.add((violation));

            }
        }
        return list;
    }


    private void setRestaurantData(Boolean updateAvailable, File newRestaurants) {
        if (updateAvailable) {
            // Common pattern to process large file
            try (BufferedReader reader = new BufferedReader(new FileReader(newRestaurants))) {
                mRestaurantManager.emptyRestaurants();
                String line;

                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    Restaurant newRestaurant = new Restaurant();
                    newRestaurant.setTrackingNumber(tokens[0]);
                    Log.i("myRestaurantName", tokens[1]);

                    /*sampleRestaurant.setName(tokens[1]);
                    sampleRestaurant.setPhysicalAddress(tokens[2]);
                    sampleRestaurant.setPhysicalCity(tokens[3]);
                    sampleRestaurant.setFactType(tokens[4]);
                    if (tokens[5].length() > 0) {
                    sampleRestaurant.setLatitude(tokens[5]);
                    } else {
                        sampleRestaurant.setLatitude("0");
                    }
                    if (tokens[6].length() > 0) {
                        sampleRestaurant.setAltitude(tokens[6]);
                    } else {
                        sampleRestaurant.setAltitude("0");
                    }*/
                    if (tokens.length == 7) {
                        newRestaurant.setName(tokens[1]);
                        newRestaurant.setPhysicalAddress(tokens[2]);
                        newRestaurant.setPhysicalCity(tokens[3]);
                        newRestaurant.setFactType(tokens[4]);
                        newRestaurant.setLatitude(Double.parseDouble(tokens[5]));
                        newRestaurant.setLongitude(Double.parseDouble(tokens[6]));
                    } else {
                        tokens[1] = tokens[1] + ", " + tokens[2];
                        newRestaurant.setName(tokens[1].substring(1, tokens[1].length() - 1));
                        newRestaurant.setPhysicalAddress(tokens[3]);
                        newRestaurant.setPhysicalCity(tokens[4]);
                        newRestaurant.setFactType(tokens[5]);
                        newRestaurant.setLatitude(Double.parseDouble(tokens[6]));
                        newRestaurant.setLongitude(Double.parseDouble(tokens[7]));
                    }


                    mRestaurantManager.addRestaurant(newRestaurant);

                    //Log.d("RestaurantList", "Just created: " + sampleRestaurant);

                }
                Log.i("myRestaurantLength", ": " + mRestaurantManager.getRestaurants().size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } /*else if (LOAD) {
            //load();
        }*/ else if (!loadedFromSave) {
            InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, Charset.forName("UTF-8"))
            );


            String line = "";
            try {
                // Step over headers
                reader.readLine();

                while ((line = reader.readLine()) != null) {

                    // Split by ','
                    String[] tokens = line.split(",");

                    // Read the data
                    Restaurant sampleRestaurant = new Restaurant();
                    sampleRestaurant.setTrackingNumber(tokens[0]);
                    Log.i("myRestaurantName", tokens[1]);
                    sampleRestaurant.setName(tokens[1]);
                    sampleRestaurant.setPhysicalAddress(tokens[2]);
                    sampleRestaurant.setPhysicalCity(tokens[3]);
                    sampleRestaurant.setFactType(tokens[4]);
                    if (tokens[5].length() > 0) {
                        sampleRestaurant.setLatitude(Double.parseDouble(tokens[5]));
                    } else {
                        sampleRestaurant.setLatitude(0);
                    }
                    if (tokens[6].length() > 0) {
                        sampleRestaurant.setLongitude(Double.parseDouble(tokens[6]));
                    } else {
                        sampleRestaurant.setLongitude(0);
                    }
                    mRestaurantManager.addRestaurant(sampleRestaurant);

                    Log.d("RestaurantList", "Just created: " + sampleRestaurant);

                }
            } catch (IOException e) {
                Log.wtf("RestaurantList", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }
        }
    }

    public void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(this.openFileOutput("save", Context.MODE_PRIVATE));
            oos.writeObject(mRestaurantManager);
            oos.writeLong(lastUpdatedTimeInMilliseconds);
            oos.writeLong(lastModifiedTimeInMilliseconds);
            Log.e(TAG, "save: done");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean load() {
        try {
            ObjectInputStream ois = new ObjectInputStream(this.openFileInput("save"));

            Log.e(TAG, "load: loading...");

            RestaurantManager temp;
            if ((temp = (RestaurantManager) ois.readObject()) == null) {
                Log.e(TAG, "load: ois not available");

                return false;
            } else {
                mRestaurantManager = RestaurantManager.getInstance();
                mRestaurantManager.setInspections(temp.getInspections());
                mRestaurantManager.setRestaurants(temp.getRestaurants());
                lastUpdatedTimeInMilliseconds = ois.readLong();
                lastModifiedTimeInMilliseconds = ois.readLong();

                loadedFromSave = true;
                return true;
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to exit?")
                .setPositiveButton("Save and exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //save();
                        System.exit(0);
                    }
                })
                /*.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(MapsActivity.this, RestaurantList.class));
                    }
                })*/
                .setNegativeButton("Cancel", null)
                .show();

    }

    public void myOnClick(View view) {
        startActivity(RestaurantList.makeIntent(getApplicationContext(), lastUpdatedTimeInMilliseconds, lastModifiedTimeInMilliseconds));
    }

    public static Intent makeIntent(Context context, LatLng coordinate) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.putExtra(COORDINATE, coordinate);
        return intent;
    }
}