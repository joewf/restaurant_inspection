package com.example.project_1.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        restaurantManager = restaurantManager.getInstance();

        readRestaurantData();
    }

    private void readRestaurantData() {
        InputStream is = getResources().openRawResource(R.raw.restaurants_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8"))
        );

        String line = "";
        while(true) {
            try {
                // Step over headers
                reader.readLine();

                if (!((line = reader.readLine()) != null)) break;
                Log.d("RestaurantList" , "lines " + line);
                // Split by ','
                String[] tokens = line.split(",");

                // Read the data
                Restaurant sample = new Restaurant();
                sample.setTrackingNumber( tokens[0] );
                sample.setName( tokens[1] );
                sample.setPhysicalAddress( tokens[2] );
                sample.setPhysicalCity( tokens[3] );
                sample.setFactType( tokens[4] );
                if (tokens[5].length() > 0) {
                    sample.setLatitude(Double.parseDouble( tokens[5] ) );
                }
                else {
                    sample.setLatitude(0);
                }
                if (tokens[6].length() > 0) {
                    sample.setAltitude(Double.parseDouble( tokens[6] ));
                }
                else {
                    sample.setAltitude(0);
                }
                restaurantManager.add(sample);

                Log.d("RestaurantList", "Just created: " + sample);

            } catch (IOException e) {
                Log.wtf("RestaurantList", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }

        }

    }
}