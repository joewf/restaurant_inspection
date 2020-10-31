package com.example.project_1.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.project_1.Model.Restaurant;
import com.example.project_1.Model.RestaurantManager;
import com.example.project_1.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class RestaurantList extends AppCompatActivity {

    private RestaurantManager restaurantManager;
    private int[] restaurantIcon = new int[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        restaurantManager = restaurantManager.getInstance();

        setRestaurantData();
        populateIcon();
        populateListView();

    }

    private void populateIcon() {
        restaurantIcon[0] = R.drawable.icon_hamburgers;
        restaurantIcon[1] = R.drawable.icon_chinese_food;
        restaurantIcon[2] = R.drawable.icon_beer;
        restaurantIcon[3] = R.drawable.icon_chinese_food;
        restaurantIcon[4] = R.drawable.icon_pizza;
        restaurantIcon[5] = R.drawable.icon_tuna;
        restaurantIcon[6] = R.drawable.icon_pizza;
        restaurantIcon[7] = R.drawable.icon_chicken;
    }

    private void setRestaurantData() {
        InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
            try {
                // Step over headers
                reader.readLine();

                while ( (line = reader.readLine()) != null) {

                    // Split by ','
                    String[] tokens = line.split(",");

                    // Read the data
                    Restaurant sample = new Restaurant();
                    sample.setTrackingNumber(tokens[0]);
                    sample.setName(tokens[1]);
                    sample.setPhysicalAddress(tokens[2]);
                    sample.setPhysicalCity(tokens[3]);
                    sample.setFactType(tokens[4]);
                    if (tokens[5].length() > 0) {
                        sample.setLatitude(Double.parseDouble(tokens[5]));
                    } else {
                        sample.setLatitude(0);
                    }
                    if (tokens[6].length() > 0) {
                        sample.setAltitude(Double.parseDouble(tokens[6]));
                    } else {
                        sample.setAltitude(0);
                    }
                    restaurantManager.add(sample);

                    Log.d("RestaurantList", "Just created: " + sample);

                }
            } catch (IOException e) {
                Log.wtf("RestaurantList", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }

        }

    private void populateListView() {
        ArrayAdapter<Restaurant> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.restaurantListView);
        list.setAdapter(adapter);

        // Start RestaurantDetails.java with restaurant's index
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = RestaurantDetails.makeIntent(RestaurantList.this, position);
                startActivity(intent);
            }
        });
    }

    private class MyListAdapter extends ArrayAdapter<Restaurant> {
        public MyListAdapter() {
            super(RestaurantList.this, R.layout.restaurant_view, restaurantManager.getRestaurants());
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Make sure we have a view to work with
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.restaurant_view, parent,false);
            }

            // Find the restaurant to work with
            Restaurant currentRestaurant = restaurantManager.get(position);

            // Fill restaurant icon
            ImageView restaurantView = (ImageView) itemView.findViewById(R.id.restaurant_icon);
            restaurantView.setImageResource(restaurantIcon[position]);

            // Fill restaurant name
            TextView restaurantName = itemView.findViewById(R.id.text_restaurant_name);
            restaurantName.setText( currentRestaurant.getName() );
            restaurantName.setTextColor(Color.BLUE);

            // Fill issues

            // Fill hazard icon

            // Fill hazard text

            // Fill inspection date


            return itemView;

        }
    }
}