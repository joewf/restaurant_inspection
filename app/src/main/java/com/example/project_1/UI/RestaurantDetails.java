package com.example.project_1.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.example.project_1.Model.Restaurant;
import com.example.project_1.Model.RestaurantManager;
import com.example.project_1.R;

public class RestaurantDetails extends AppCompatActivity {

    private static Restaurant restaurant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);
    }

    public static Intent makeIntent(Context context, int restaurantIndex) {
        Intent intent = new Intent(context, RestaurantDetails.class);
        intent.putExtra("Restaurant index", restaurantIndex);
        RestaurantManager restaurantManager = RestaurantManager.getInstance();
        restaurant = restaurantManager.get(restaurantIndex);
        return intent;
    }
}