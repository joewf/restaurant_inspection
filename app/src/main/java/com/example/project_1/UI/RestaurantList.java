package com.example.project_1.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.project_1.Model.Restaurant;
import com.example.project_1.Model.RestaurantManager;
import com.example.project_1.R;

import java.util.ArrayList;
import java.util.List;

public class RestaurantList extends AppCompatActivity {

    private RestaurantManager restaurantManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        restaurantManager.getInstance();

        readRestaurantData();
    }

    private void readRestaurantData() {
        Restaurant sample;
    }
}