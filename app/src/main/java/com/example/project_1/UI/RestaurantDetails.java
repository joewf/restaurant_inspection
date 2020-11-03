package com.example.project_1.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.example.project_1.Model.Restaurant;
import com.example.project_1.Model.RestaurantManager;
import com.example.project_1.R;

import org.w3c.dom.Text;

public class RestaurantDetails extends AppCompatActivity {

    private static Restaurant restaurant;   // Store the index of the restaurant
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvGPS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        getRestaurantDetails();

    }

    private void getRestaurantDetails() {

        // Set name
        String name = restaurant.getName();
        tvName = (TextView) findViewById(R.id.text_restaurant_name);
        tvName.setText(name);
        tvName.setTextColor(Color.BLUE);

        // Set Address
        String address = restaurant.getPhysicalAddress();
        tvAddress = (TextView) findViewById(R.id.text_address);
        tvAddress.setText("Address: " + address);

        // Set GPS
        double latitude = restaurant.getLatitude();
        double altitude = restaurant.getAltitude();
        tvGPS = (TextView) findViewById(R.id.text_GPS);
        tvGPS.setText("GPS Coordinates: " + altitude + ", " + latitude);

    }

    public static Intent makeIntent(Context context, int restaurantIndex) {
        Intent intent = new Intent(context, RestaurantDetails.class);
        intent.putExtra("Restaurant index", restaurantIndex);
        RestaurantManager restaurantManager = RestaurantManager.getInstance();
        restaurant = restaurantManager.get(restaurantIndex);
        return intent;
    }
}