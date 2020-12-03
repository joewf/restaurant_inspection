package com.example.project_1.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.project_1.R;
import com.example.project_1.model.ClusterMarker;
import com.example.project_1.model.CustomClusterRenderer;
import com.example.project_1.model.CustomInfoViewAdapter;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

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
import java.util.concurrent.TimeUnit;

/**
 * MapsActivity class models the information about a MapsActivity activity.
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    public static final float DEFAULT_ZOOM = 15f;
    private static final String COORDINATE = "coordinate";
    public static final int GREEN = 1;
    public static final int YELLOW = 2;
    public static final int RED = 3;

    private GoogleMap mMap;
    private GoogleMapOptions mGoogleMapOptions;
    private ClusterManager<ClusterMarker> mClusterManager;
    private ClusterMarker mMarker = null;
    private static final String TAG = "Map activity";
    private boolean mLocationPermissionGranted = false;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private RestaurantManager mRestaurantManager;
    private List<Restaurant> mRestaurantList;
    private List<Inspection> mCurrentRestaurantInspectionList;
    private List<ClusterMarker> mClusterMarkersList;
    private Restaurant mCurrentRestaurant;
    private Inspection mCurrentInspection;

    private HazardRating hazardRating;
    private String restaurantName;
    private String snippet;
    private String spinnerHazardText;
    private String trackingNumber;
    private int spinnerIssuesNum;
    private double latitude;
    private double longitude;
    private boolean favoriteToggled = false;

    private EditText mSearchText;
    private ImageView mGPS;
    private Spinner mSpinnerHazard;
    private Spinner mSpinnerIssues;
    private Spinner mSpinnerFavorite;

    ProgressDialog pDialog;
    private static boolean loadedFromSave = false;
    private int count;
    private long lastUpdatedTimeInMilliseconds = 0;
    private long lastModifiedTimeInMilliseconds = 0;
    private long currentLastModifiedTimeInMilliseconds = 0;
    private static LatLng coordinateInRestaurantDetail;
    private static BackFrom backFrom = BackFrom.DEFAULT;
    private static String trackingNumberInRestaurantDetail;
    HashMap<String, ClusterMarker> markerMap = new HashMap<>();
    private CustomClusterRenderer renderer;
    private HashMap<String, Integer> favInspectionNumMap = new HashMap<>();
    private boolean spinnerInitialized = false;
    public static String searchStringFromMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        load();

        mRestaurantManager = RestaurantManager.getInstance();
        favInspectionNumMap = new HashMap<>(mRestaurantManager.getFavMap());

        createDropDownLists();
        mSearchText = (EditText) findViewById(R.id.input_search);
        mGPS = (ImageView) findViewById(R.id.ic_gps);

        checkUpdateOfFraserHealthRestaurantInspectionReports();
        initSearch();

    }

    private void createDropDownLists() {

        // Drop down list for hazard
        mSpinnerHazard = findViewById(R.id.spinner_hazard);
        ArrayAdapter<CharSequence> adapterHazard = ArrayAdapter.createFromResource(this, R.array.hazards,
                android.R.layout.simple_spinner_item);
        adapterHazard.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerHazard.setAdapter(adapterHazard);
        mSpinnerHazard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerInitialized)
                    searchRestaurants();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Drop down list for issues
        mSpinnerIssues = findViewById(R.id.spinner_issues);
        ArrayAdapter<CharSequence> adapterIssues = ArrayAdapter.createFromResource(this, R.array.issues,
                android.R.layout.simple_spinner_item);
        adapterIssues.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerIssues.setAdapter(adapterIssues);
        mSpinnerIssues.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerInitialized)
                    searchRestaurants();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Drop down list for favorite
        mSpinnerFavorite = findViewById(R.id.spinner_favorite);
        ArrayAdapter<CharSequence> adapterFavorite = ArrayAdapter.createFromResource(this, R.array.favorite,
                android.R.layout.simple_spinner_item);
        adapterFavorite.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerFavorite.setAdapter(adapterFavorite);

        mSpinnerFavorite.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (spinnerInitialized)
                    searchRestaurants();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Reset search button
        Button reset = (Button) findViewById(R.id.button_reset_markers);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addRestaurantMarker();
                Toast.makeText(MapsActivity.this, "Search reset", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart: ");
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (spinnerInitialized) {
            searchStringFromMap = RestaurantList.searchStringFromList;
            mSearchText.setText(searchStringFromMap);
        }
//        if (backFrom == BackFrom.RestaurantList) {
//            showNewInspectionOnFav();
//        }

        Log.e(TAG, "onResume: ");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (backFrom.equals(BackFrom.RestaurantDetails)) {
            Log.e(TAG, "onRestart: " + coordinateInRestaurantDetail);
            moveCamera(coordinateInRestaurantDetail, DEFAULT_ZOOM);
        }

        Log.e(TAG, "onRestart: ");
    }

    private void initSearch() {
        Log.d(TAG, "init: initiating search");

        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //addRestaurantMarker();
                searchRestaurants();
            }
        });

        /*mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                if(actionID == EditorInfo.IME_ACTION_SEARCH             // Search when clicking on search icon
                || actionID == EditorInfo.IME_ACTION_DONE               // Search when clicking done
                || keyEvent.getAction() == keyEvent.ACTION_DOWN         // Search when hiding keyboard
                || keyEvent.getAction() == keyEvent.KEYCODE_ENTER){     // Search when pressing enter

                    // Search restaurants based on name
                    searchRestaurants();
                }
                return false;
            }
        });*/

        hideKeyboard();
    }


    private void addRestaurantMarker() {

        // Clear markers when there are markers in the map
        if (mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }

        // Clear marker restaurants list
        mRestaurantManager.emptyMarkerRestaurants();


        // Settings for cluster manager
        mClusterManager = new ClusterManager<ClusterMarker>(this, mMap);
        renderer = new CustomClusterRenderer(this, mMap, mClusterManager);
        mClusterManager.setRenderer(renderer);
        mClusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoViewAdapter(LayoutInflater.from(this)));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnInfoWindowClickListener(mClusterManager);

        Log.d(TAG, "addRestaurantMarker: getting restaurant info");

        // Get restaurant lists
        mRestaurantList = mRestaurantManager.getRestaurants();
        mClusterMarkersList = new ArrayList<>();
        LatLng currentRestaurantLatLng;

        // Add a marker for each restaurant on the list
        for (int i = 0; i < mRestaurantList.size(); i++) {

            // Get current restaurant
            mCurrentRestaurant = mRestaurantList.get(i);
            trackingNumber = mCurrentRestaurant.getTrackingNumber();
            // Get a list of inspections for the current restaurant
            mCurrentRestaurantInspectionList = mRestaurantManager.getInspectionsForRestaurant(i);

            ClusterMarker clusterMarker = null;
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

                snippet = getString(R.string.address) + mCurrentRestaurant.getPhysicalAddress() + "\n"
                        + getString(R.string.hazard_level_) + hazardRating;

                switch (hazardRating) {
                    case LOW:
                        Log.d(TAG, "setting restaurant marker green");
                        // Set marker
                        clusterMarker = new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng,
                                hazardRating, GREEN, mCurrentRestaurantInspectionList, mCurrentRestaurant,
                                trackingNumber);
                        mClusterManager.addItem(clusterMarker);
                        mClusterMarkersList.add(clusterMarker);
                        mClusterManager.cluster();
                        mRestaurantManager.addMarkerRestaurant(mCurrentRestaurant);
                        break;

                    case MODERATE:
                        Log.d(TAG, "setting restaurant marker yellow");
                        // Set marker
                        clusterMarker = new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng,
                                hazardRating, YELLOW, mCurrentRestaurantInspectionList, mCurrentRestaurant,
                                trackingNumber);
                        mClusterManager.addItem(clusterMarker);
                        mClusterMarkersList.add(clusterMarker);
                        mClusterManager.cluster();
                        mRestaurantManager.addMarkerRestaurant(mCurrentRestaurant);
                        break;

                    case HIGH:
                        Log.d(TAG, "setting restaurant marker red");
                        // Set marker
                        clusterMarker = new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng,
                                hazardRating, RED, mCurrentRestaurantInspectionList, mCurrentRestaurant,
                                trackingNumber);
                        mClusterManager.addItem(clusterMarker);
                        mClusterMarkersList.add(clusterMarker);
                        mClusterManager.cluster();
                        mRestaurantManager.addMarkerRestaurant(mCurrentRestaurant);
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
                clusterMarker = new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng,
                        hazardRating, GREEN, mCurrentRestaurantInspectionList, mCurrentRestaurant,
                        trackingNumber);
                mClusterManager.addItem(clusterMarker);
                mClusterMarkersList.add(clusterMarker);
                mClusterManager.cluster();
                mRestaurantManager.addMarkerRestaurant(mCurrentRestaurant);
            }

            markerMap.put(mCurrentRestaurant.getTrackingNumber(), clusterMarker);

        }

        // Get restaurant full info after clicking the info window
        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>() {
            @Override
            public void onClusterItemInfoWindowClick(ClusterMarker item) {

                List<Restaurant> list = mRestaurantManager.getMarkerRestaurants();
                int index = -1;
                for (int i = 0; i < list.size(); i++) {
                    Restaurant current = list.get(i);
                    if (markerMap.get(current.getTrackingNumber()).equals(item)) {
                        index = i;
                        break;
                    }
                }

                Log.e(TAG, "onInfoWindowClick: " + index);

                if (index != -1) {
                    Intent intent = RestaurantDetails.makeIntent(MapsActivity.this,
                            index);
                    startActivity(intent);

                }

            }
        });
    }


    private void searchRestaurants() {
        Log.d(TAG, "searchRestaurants: searching restaurants");

        // Get the name from search
        searchStringFromMap = mSearchText.getText().toString().toLowerCase().replaceAll("\n", "");

        // Get hazard and Critical issues from drop down list
        getHazardFromDropDownList();
        getIssuesFromDropDownList();
        getFavoriteFromDropDownList();

        // Date format
        Date inspectionDate;
        Date currentDate = new Date();                      // Current date
        Log.d(TAG, "Current Date: " + currentDate);

        // If there are markers, delete them
        if (mClusterManager != null) {
            mClusterManager.clearItems();
            mClusterManager.cluster();
        }

        mRestaurantManager.emptyMarkerRestaurants();        // Remove all restaurants for marker


        for (int i = 0; i < mClusterMarkersList.size(); i++) {

            // Get current marker
            mMarker = mClusterMarkersList.get(i);

            // List of inspections of the restaurant
            List<Inspection> currentMarkerInspectionsList = mMarker.getInspectionList();    // Initiate marker inspection list
            int currentMarkerInspectionListSize = currentMarkerInspectionsList.size();      // Inspection list size
            int numCritical = 0;
            Log.d(TAG, "Setting critical to ZERO: " + numCritical);

            Restaurant currentRestaurant = mMarker.getRestaurant();

            // Check favorite filter
            boolean favoriteFilterPassed = true;
            if (!currentRestaurant.isFavorite() && favoriteToggled) {
                favoriteFilterPassed = false;
            }

            // Compare search with marker's name
            if (mMarker.getTitle().toLowerCase().contains(searchStringFromMap)
                    && favoriteFilterPassed) {

                // No hazard check search
                if (hazardRating.equals(HazardRating.NONE)) {

                    Log.d(TAG, "No hazard check");

                    /*
                     Loop through all the inspections of the marker to check
                     the total number of issues
                     */

                    // Add issues for non empty inspection lists
                    if (currentMarkerInspectionListSize != 0) {
                        for (int j = 0; j < currentMarkerInspectionListSize; j++) {

                            // Get current inspection date
                            mCurrentInspection = currentMarkerInspectionsList.get(j);
                            Log.d(TAG, "Current inspection: " + mCurrentInspection);

                            inspectionDate = mCurrentInspection.getDate();
                            Log.d(TAG, "Inspection date: " + inspectionDate);

                            // Calculate the inspection time for less than one year
                            long diffInMilliSe = Math.abs(currentDate.getTime() - inspectionDate.getTime());
                            long diff = TimeUnit.DAYS.convert(diffInMilliSe, TimeUnit.MILLISECONDS);
                            Log.d(TAG, "DAYS: " + diff);

                            // Inspection date is less than 1 year
                            if (diff < 365) {
                                numCritical += mCurrentInspection.getNumCritical();
                                Log.d(TAG, "CRITICAL: " + numCritical);
                            }
                        }
                    }

                    // Add the marker if it's less than the number of critical issues
                    if (spinnerIssuesNum == 888888) {
                        mClusterManager.addItem(mMarker);
                        mClusterManager.cluster();
                        mRestaurantManager.addMarkerRestaurant(currentRestaurant);
                    } else if (numCritical <= spinnerIssuesNum) {

                        // Check at least the latest inspection is less than 1 year
                        if (currentMarkerInspectionListSize != 0) {
                            Date lastInspectionDate = currentMarkerInspectionsList.get(0).getDate();
                            if (lastInspectionDate != null) {
                                long diffInMilliSeLastInspection = Math.abs(currentDate.getTime() - lastInspectionDate.getTime());
                                long diff = TimeUnit.DAYS.convert(diffInMilliSeLastInspection, TimeUnit.MILLISECONDS);
                                Log.d(TAG, "DAYS: " + diff);

                                if (diff < 365) {
                                    Log.d(TAG, "NUMBER OF CRITICAL: " + numCritical);
                                    Log.d(TAG, "Adding marker to the map: " + mMarker.getTitle());
                                    mClusterManager.addItem(mMarker);
                                    mClusterManager.cluster();
                                    mRestaurantManager.addMarkerRestaurant(currentRestaurant);
                                }

                            }

                        }

                    }

                }


                // Check hazard search
                if (hazardRating.equals(mMarker.getHazard())) {

                    Log.d(TAG, "Hazard check");

                    // Add issues for non empty inspection lists
                    if (currentMarkerInspectionListSize != 0) {
                        for (int j = 0; j < currentMarkerInspectionListSize; j++) {

                            // Get current inspection date
                            mCurrentInspection = currentMarkerInspectionsList.get(j);
                            Log.d(TAG, "Current inspection: " + mCurrentInspection);

                            inspectionDate = mCurrentInspection.getDate();
                            Log.d(TAG, "Inspection date: " + inspectionDate);

                            // Calculate the inspection time for less than one year
                            long diffInMilliSe = Math.abs(currentDate.getTime() - inspectionDate.getTime());
                            long diff = TimeUnit.DAYS.convert(diffInMilliSe, TimeUnit.MILLISECONDS);
                            Log.d(TAG, "DAYS: " + diff);

                            // Inspection date is less than 1 year
                            if (diff < 365) {
                                numCritical += mCurrentInspection.getNumCritical();
                                Log.d(TAG, "CRITICAL: " + numCritical);
                            }
                        }
                    }

                    // Add the marker if it's less than the number of critical issues
                    if (spinnerIssuesNum == 888888) {
                        mClusterManager.addItem(mMarker);
                        mClusterManager.cluster();
                        mRestaurantManager.addMarkerRestaurant(currentRestaurant);
                    } else if (numCritical <= spinnerIssuesNum) {

                        // Check at least the latest inspection is less than 1 year
                        if (currentMarkerInspectionListSize != 0) {
                            Date lastInspectionDate = currentMarkerInspectionsList.get(0).getDate();

                            if (lastInspectionDate != null) {
                                long diffInMilliSeLastInspection = Math.abs(currentDate.getTime() - lastInspectionDate.getTime());
                                long diff = TimeUnit.DAYS.convert(diffInMilliSeLastInspection, TimeUnit.MILLISECONDS);
                                Log.d(TAG, "DAYS: " + diff);

                                if (diff < 365) {
                                    Log.d(TAG, "NUMBER OF CRITICAL: " + numCritical);
                                    Log.d(TAG, "Adding marker to the map: " + mMarker.getTitle());
                                    mClusterManager.addItem(mMarker);
                                    mClusterManager.cluster();
                                    mRestaurantManager.addMarkerRestaurant(currentRestaurant);
                                }

                            }

                        }

                    }

                }

            }
        }

        hideKeyboard();
        Log.d(TAG, "MARKER RESTAURANT SIZE:" + mRestaurantManager.getMarkerRestaurants().size());

    }

    private void getIssuesFromDropDownList() {
        Log.e(TAG, "getIssuesFromDropDownList: " + mSpinnerIssues.getSelectedItem().toString());


        // Get user's number
        if (mSpinnerIssues.getSelectedItem().toString().equals("max CRIT-VIOs w/n last yr")) {
            spinnerIssuesNum = 888888;
        } else {
            spinnerIssuesNum = Integer.parseInt(mSpinnerIssues.getSelectedItem().toString());
        }

        Log.e(TAG, "getIssuesFromDropDownList: " + spinnerIssuesNum);

    }

    private void getFavoriteFromDropDownList() {

        favoriteToggled = mSpinnerFavorite.getSelectedItem().toString().equals("Fav Only") ?
                (favoriteToggled = true) : (favoriteToggled = false);

        //Toast.makeText(this, "" + favorite, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "getIssuesFromDropDownList: " + favoriteToggled);

    }

    private void getHazardFromDropDownList() {

        // Get hazard level from drop down list
        spinnerHazardText = mSpinnerHazard.getSelectedItem().toString().toUpperCase();
        Log.d(TAG, "LEVEL: " + spinnerHazardText);
        switch (spinnerHazardText) {
            case "HAZARD LVL":
                hazardRating = HazardRating.NONE;
                break;
            case "LOW":
                hazardRating = HazardRating.LOW;
                break;
            case "MODERATE":
                hazardRating = HazardRating.MODERATE;
                break;
            case "HIGH":
                hazardRating = HazardRating.HIGH;
                break;
            default:
                assert false;
        }
        Log.e(TAG, "Hazard rating: " + hazardRating);
    }

    // Hide keyboard after search
    private void hideKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void setCallbackToStartFullRestaurantInfo() {

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {

                List<Restaurant> list = mRestaurantManager.getMarkerRestaurants();
                int index = -1;
                for (int i = 0; i < list.size(); i++) {
                    Restaurant current = list.get(i);
                    if (markerMap.get(current.getTrackingNumber()).equals(marker)) {
                        index = i;
                        break;
                    }
                }

                Log.e(TAG, "onInfoWindowClick: " + index);

                if (index != -1) {
                    Intent intent = RestaurantDetails.makeIntent(MapsActivity.this,
                            index);
                    startActivity(intent);

                }

            }
        });
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

        mGoogleMapOptions = new GoogleMapOptions();
        mGoogleMapOptions.zoomControlsEnabled(true);

        Log.d(TAG, "Initiating map");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = SupportMapFragment.newInstance(mGoogleMapOptions);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction()
                .replace(R.id.map, mapFragment);
        ft.commit();
        mapFragment.getMapAsync(MapsActivity.this);

        mGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "clicked GPS icon");
                getDeviceLocation();
            }
        });
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
        hideKeyboard();
    }

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

            spinnerInitialized = true;
        }
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
                    builder.setTitle(R.string.last_update);
                    builder.setMessage(R.string.download_update);

                    builder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
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
                            //Log.e(TAG, "onClick: " + mRestaurantManager);
                        }
                    });

                    builder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
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
        pDialog.setMessage(getString(R.string.loading));
        //pDialog.setCancelable(false);
        final boolean[] cancelDownloading = {false};
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
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

                            showNewInspectionOnFav();

                            save();

                            getLocationPermission();
                            //Log.e(TAG, "onClick: " + mRestaurantManager);

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

    private void showNewInspectionOnFav() {
        mRestaurantManager.updateFavInspectionNumMap();

        Log.e(TAG, "showNewInspectionOnFav: favInspectionNumMap " + favInspectionNumMap);
        Log.e(TAG, "showNewInspectionOnFav: FavMap " + mRestaurantManager.getFavMap());

        ArrayList<Restaurant> favWithNewInspection = mRestaurantManager.getFavRestaurantWithNewInspection(favInspectionNumMap);

//        if (loadedFromSave) {
        //favWithNewInspection = mRestaurantManager.getFavRestaurants();
//        }

        Log.e(TAG, "showNewInspectionOnFav: fav " + mRestaurantManager.getFavRestaurants());
        Log.e(TAG, "showNewInspectionOnFav: fav with update " + favWithNewInspection);

        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(MapsActivity.this, android.R.layout.simple_list_item_1);

        for (Restaurant restaurant : favWithNewInspection) {
            Inspection mostRecentInspection = mRestaurantManager.getMostRecentInspection(restaurant);

            String strDate = "";
            String hazard = "";
            if (mostRecentInspection != null) {
                Date mostRecentDate = mostRecentInspection.getDate();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy");
                strDate = simpleDateFormat.format(mostRecentDate);

                hazard = mostRecentInspection.getHazardRating().toString();
            }

            String info = restaurant.getName()
                    + "\n\t\taddress: " + restaurant.getPhysicalAddress()
                    + "\n\t\tmost recent: " + strDate
                    + "\n\t\thazard level: " + hazard;
            adapter.add(info);
        }


        ArrayList<Restaurant> finalFavWithNewInspection = favWithNewInspection;
        if (finalFavWithNewInspection.isEmpty()) {
            builder.setMessage(R.string.no_inspection_found_fav_restaurants)
                    .setPositiveButton(R.string.ok, null);
        } else {
            builder.setTitle("New inspections found for your favourite restaurants:");
        }
        builder
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = RestaurantDetails.makeIntent(MapsActivity.this,
                                mRestaurantManager.getIndexFromTrackingNumber(finalFavWithNewInspection.get(which).getTrackingNumber().replaceAll("\"", "")));
                        startActivity(intent);
                    }
                })
                .show();

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
                Log.e(TAG, "load: FavTrackingNumList " + temp.getFavTrackingNumList());
                mRestaurantManager.setFavTrackingNumList(temp.getFavTrackingNumList());
                mRestaurantManager.setFavMap(temp.getFavMap());
                lastUpdatedTimeInMilliseconds = ois.readLong();
                lastModifiedTimeInMilliseconds = ois.readLong();

                mRestaurantManager.updateFavList();

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
                        //mRestaurantManager.updateFavTrackingNumList();

                        Log.e(TAG, "onBackPressed: " + mRestaurantManager.getFavTrackingNumList());

                        mRestaurantManager.updateFavInspectionNumMap();
                        save();
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
        backFrom = BackFrom.DEFAULT;

        Log.e(TAG, "myOnClick: " + mRestaurantManager.getFavRestaurants());

        startActivity(RestaurantList.makeIntent(getApplicationContext(), lastUpdatedTimeInMilliseconds, lastModifiedTimeInMilliseconds));
    }

    public static Intent makeIntent(Context context, double latitude, double longitude, String trackingNumber, BackFrom where) {
        Intent intent = new Intent(context, MapsActivity.class);
        coordinateInRestaurantDetail = new LatLng(latitude, longitude);
        backFrom = where;
        trackingNumberInRestaurantDetail = trackingNumber;
        return intent;
    }

    public static Intent makeIntent(Context context, BackFrom where) {
        Intent intent = new Intent(context, MapsActivity.class);
        backFrom = where;
        return intent;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {


    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}