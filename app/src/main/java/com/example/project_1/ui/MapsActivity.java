package com.example.project_1.ui;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.project_1.R;
import com.example.project_1.model.HazardRating;
import com.example.project_1.model.Inspection;
import com.example.project_1.model.Restaurant;
import com.example.project_1.model.RestaurantManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST = 101;
    public static final float DEFAULT_ZOOM = 15f;

    private GoogleMap mMap;
    private RestaurantManager mRestaurantManager;
    private static final String TAG = "Map activity";
    private boolean mLocationPermissionGranted = false;
    private Location mCurrentLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private List<Restaurant> mRestaurantList;
    private List<Inspection> mRestaurantInspectionList;
    private Restaurant mCurrentRestaurant;
    private Inspection mLastInspection;

    private HazardRating hazardRating;
    private String restaurantName;
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

        Log.d(TAG, "addRestaurantMarker: getting restaurant info");

        // Get restaurant lists
        mRestaurantList = mRestaurantManager.getRestaurants();
        LatLng currentRestaurantLatLng;
        MarkerOptions options;

        // Add a marker for for restaurant on the list
        for (int i = 0; i < mRestaurantList.size(); i++) {
            // Get current restaurant
            mCurrentRestaurant = mRestaurantList.get(i);

            // Get a list of inspections for the current restaurant
            mRestaurantInspectionList = mRestaurantManager.getInspectionsForRestaurant(i);

            // Current restaurant inspection list is empty
            if (mRestaurantInspectionList.isEmpty()) {
                // Get restaurant info
                restaurantName = mCurrentRestaurant.getName();
                Log.d(TAG, "name: " + restaurantName);
                latitude = mCurrentRestaurant.getLatitude();
                Log.d(TAG, "latitude: " + latitude);
                longitude = mCurrentRestaurant.getLongitude();
                Log.d(TAG, "longitude: " + longitude);
                currentRestaurantLatLng = new LatLng(latitude, longitude);

                Log.d(TAG, "setting restaurant marker low");
                options = new MarkerOptions()
                        .position(currentRestaurantLatLng)
                        .title(restaurantName)
                        .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_green_24));
                mMap.addMarker(options);
                break;

            }

                // Get restaurant info
                restaurantName = mCurrentRestaurant.getName();
                Log.d(TAG, "name: " + restaurantName);
                latitude = mCurrentRestaurant.getLatitude();
                Log.d(TAG, "latitude: " + latitude);
                longitude = mCurrentRestaurant.getLongitude();
                Log.d(TAG, "longitude: " + longitude);
                currentRestaurantLatLng = new LatLng(latitude, longitude);

                // Get the last inspection hazard
                hazardRating = mRestaurantInspectionList.get(0).getHazardRating();
                Log.d(TAG, "Hazard rating: " + hazardRating);

                switch (hazardRating) {
                    case LOW:
                        Log.d(TAG, "setting restaurant marker low");
                        // Set marker for each restaurant
                        options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_green_24));
                        mMap.addMarker(options);
                        break;

                    case MODERATE:
                        Log.d(TAG, "setting restaurant marker moderate");
                        // Set marker for each restaurant
                        options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_yellow_24));
                        mMap.addMarker(options);
                        break;

                    case HIGH:
                        Log.d(TAG, "setting restaurant marker high");
                        // Set marker for each restaurant
                        options = new MarkerOptions()
                                .position(currentRestaurantLatLng)
                                .title(restaurantName)
                                .icon(BitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_map_red_24));
                        mMap.addMarker(options);
                        break;
                }
            }
        }

    private BitmapDescriptor BitmapDescriptorFromVector (Context context, int vectorResID) {

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

        Log.d(TAG, "Initiating map");
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
}