package com.example.project_1.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.project_1.R;
import com.example.project_1.model.ClusterMarker;
import com.example.project_1.model.CustomClusterRenderer;
import com.example.project_1.model.CustomInfoViewAdapter;
import com.example.project_1.model.HazardRating;
import com.example.project_1.model.Inspection;
import com.example.project_1.model.Restaurant;
import com.example.project_1.model.RestaurantManager;
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
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    public static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private GoogleMapOptions mGoogleMapOptions;
    private ClusterManager<ClusterMarker> mClusterManager;
    private Marker mMarker;
    private static final String TAG = "Map activity";
    private boolean mLocationPermissionGranted = false;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private RestaurantManager mRestaurantManager;
    private List<Restaurant> mRestaurantList;
    private List<Inspection> mCurrentRestaurantInspectionList;
    private List<ClusterMarker> mClusterMarkersList = new ArrayList<>();
    private Restaurant mCurrentRestaurant;

    private HazardRating hazardRating;
    private String restaurantName;
    private String snippet;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mRestaurantManager = RestaurantManager.getInstance();
        getLocationPermission();

    }

    private void addRestaurantMarker() {

        mClusterManager = new ClusterManager<ClusterMarker>(this, mMap);
        CustomClusterRenderer renderer = new CustomClusterRenderer(this, mMap, mClusterManager);
        mClusterManager.setRenderer(renderer);

        // Cluster click listener
        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<ClusterMarker>() {
            @Override
            public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
                Toast.makeText(MapsActivity.this, "Cluster item click",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Cluster item
        mClusterManager.setOnClusterItemClickListener( new ClusterManager.OnClusterItemClickListener<ClusterMarker>() {
            @Override
            public boolean onClusterItemClick(ClusterMarker item) {
                Toast.makeText(MapsActivity.this, "Cluster item click",
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        });


        mClusterManager.getMarkerCollection().setInfoWindowAdapter(new CustomInfoViewAdapter(LayoutInflater.from(this)));
        mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>() {
            @Override
            public void onClusterItemInfoWindowClick(ClusterMarker item) {
                Toast.makeText(MapsActivity.this, "Cluster item click",
                        Toast.LENGTH_SHORT).show();
            }
        });

        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnInfoWindowClickListener(mClusterManager);


        Log.d(TAG, "addRestaurantMarker: getting restaurant info");

        // Get restaurant lists
        mRestaurantList = mRestaurantManager.getRestaurants();
        LatLng currentRestaurantLatLng;
        MarkerOptions options;

        // Add a marker for each restaurant on the list
        for (int i = 0; i < mRestaurantList.size(); i++) {

            // Get current restaurant
            mCurrentRestaurant = mRestaurantList.get(i);
            // Get a list of inspections for the current restaurant
            mCurrentRestaurantInspectionList = mRestaurantManager.getInspectionsForRestaurant(i);

            // Current restaurant inspection list is not empty
            if ( !mCurrentRestaurantInspectionList.isEmpty() ) {
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
                        /*options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_green_24));*/
                        //mMarker = mMap.addMarker(options);

                        //mClusterMarkersList.add(new ClusterMarker(options));
                        //mClusterManager.addItems(mClusterMarkersList);
                        mClusterManager.addItem(new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng, 1));
                        mClusterManager.cluster();
                        break;

                    case MODERATE:
                        Log.d(TAG, "setting restaurant marker yellow");
                        // Set marker
                        /*options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_yellow_24));*/
                        //mMarker = mMap.addMarker(options);

                        // Cluster the markers
                        //mClusterMarkersList.add(new ClusterMarker(options));
                        //mClusterManager.addItems(mClusterMarkersList);
                        mClusterManager.addItem(new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng, 2));
                        mClusterManager.cluster();
                        break;

                    case HIGH:
                        Log.d(TAG, "setting restaurant marker red");
                        // Set marker
                        /*options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .snippet(snippet)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_red_24));*/
                        //mMarker = mMap.addMarker(options);

                        //mClusterMarkersList.add(new ClusterMarker(options));
                        //mClusterManager.addItems(mClusterMarkersList);
                        mClusterManager.addItem(new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng, 3));
                        mClusterManager.cluster();
                        break;
                }
                // Get restaurant full info after clicking the info window
                startFullRestaurantInfo(mRestaurantList.indexOf(mCurrentRestaurant));


            } else{
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
                /*options = new MarkerOptions()
                        .position(currentRestaurantLatLng)
                        .title(restaurantName)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_green_24));*/
                //mMarker = mMap.addMarker(options);

                //mClusterMarkersList.add(new ClusterMarker(options));
                //mClusterManager.addItems(mClusterMarkersList);
                mClusterManager.addItem(new ClusterMarker(restaurantName, snippet, currentRestaurantLatLng, 1));
                mClusterManager.cluster();
            }

        }
    }

    private void startFullRestaurantInfo(int restaurantIndex) {

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = RestaurantDetails.makeIntent(MapsActivity.this,
                        restaurantIndex);
                startActivity(intent);
            }
        });
    }

    public BitmapDescriptor BitmapDescriptorFromVector (Context context, int vectorResID) {

        // source: https://www.youtube.com/watch?v=26bl4r3VtGQ&t=355s

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResID);
        vectorDrawable.setBounds(0,0,vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap( vectorDrawable.getIntrinsicWidth(),
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
                        }
                        else {
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
    }
}