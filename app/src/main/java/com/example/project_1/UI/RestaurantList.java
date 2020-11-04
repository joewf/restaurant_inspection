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

import com.example.project_1.Model.HazardRating;
import com.example.project_1.Model.Inspection;
import com.example.project_1.Model.InspectionType;
import com.example.project_1.Model.Restaurant;
import com.example.project_1.Model.RestaurantManager;
import com.example.project_1.R;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestaurantList extends AppCompatActivity {

    private RestaurantManager restaurantManager;
    private int[] restaurantIcon = new int[8];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        restaurantManager = restaurantManager.getInstance();

        setRestaurantData();
        setInspectionData();
        sortRestaurantList();
        sortInspectionDate();
        populateListView();
        populateIcon();

    }


    private void setInspectionData() {
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

                sampleInspection.setViolations(null);
                restaurantManager.addInspection(sampleInspection);

                Log.d("Inspection List", "Just created: " + sampleInspection);
            }
        } catch (IOException | ParseException e) {
            Log.wtf("Inspection List", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
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

            while ((line = reader.readLine()) != null) {

                // Split by ','
                String[] tokens = line.split(",");

                // Read the data
                Restaurant sampleRestaurant = new Restaurant();
                sampleRestaurant.setTrackingNumber(tokens[0]);
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
                    sampleRestaurant.setAltitude(Double.parseDouble(tokens[6]));
                } else {
                    sampleRestaurant.setAltitude(0);
                }
                restaurantManager.addRestaurant(sampleRestaurant);

                Log.d("RestaurantList", "Just created: " + sampleRestaurant);

            }
        } catch (IOException e) {
            Log.wtf("RestaurantList", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }

    }

    private void populateIcon() {
        restaurantIcon[0] = R.drawable.icon_tuna;
        restaurantIcon[1] = R.drawable.icon_chinese_food;
        restaurantIcon[2] = R.drawable.icon_chinese_food;
        restaurantIcon[3] = R.drawable.icon_hamburgers;
        restaurantIcon[4] = R.drawable.icon_beer;
        restaurantIcon[5] = R.drawable.icon_pizza;
        restaurantIcon[6] = R.drawable.icon_pizza;
        restaurantIcon[7] = R.drawable.icon_chicken;
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
                itemView = getLayoutInflater().inflate(R.layout.restaurant_view, parent, false);
            }

            // Find the restaurant to work with
            Restaurant currentRestaurant = restaurantManager.get(position);
            List<Inspection> listInspections = restaurantManager.getInspections();

            // Fill restaurant icon
            ImageView restaurantView = (ImageView) itemView.findViewById(R.id.restaurant_icon);
            restaurantView.setImageResource(restaurantIcon[position]);

            // Fill restaurant name
            TextView restaurantName = (TextView) itemView.findViewById(R.id.text_restaurant_name);
            restaurantName.setText(currentRestaurant.getName());
            restaurantName.setTextColor(Color.BLUE);

            // Fill issues
            TextView restaurantCriticalIssues = (TextView) itemView.findViewById(R.id.text_issues_found);
            int criticalIssues;
            for (int i = 0; i < listInspections.size(); i++) {
                if (currentRestaurant.getTrackingNumber().equals(listInspections.get(i).getTrackingNumber())) {
                    criticalIssues = listInspections.get(i).getNumCritical();
                    restaurantCriticalIssues.setText("Critical issues: " + criticalIssues);
                    break;
                }
                else {
                    restaurantCriticalIssues.setText("Critical issues: 0");
                }
            }

            // Fill hazard icon and text
            ImageView RestaurantHazard = (ImageView) itemView.findViewById(R.id.hazard_icon);
            TextView txtRestaurantHazard = (TextView) itemView.findViewById(R.id.text_hazard_level);
            for (int i = 0; i < listInspections.size(); i++) {
                if (currentRestaurant.getTrackingNumber().equals(listInspections.get(i).getTrackingNumber())) {
                    HazardRating hazard = listInspections.get(i).getHazardRating();
                    switch (hazard) {
                        case LOW:
                            RestaurantHazard.setImageResource(R.drawable.green_hazard);
                            txtRestaurantHazard.setText("" + hazard);
                            txtRestaurantHazard.setTextColor(Color.GREEN);
                            break;

                        case MODERATE:
                            RestaurantHazard.setImageResource(R.drawable.yellow_hazard);
                            txtRestaurantHazard.setText("" + hazard);
                            txtRestaurantHazard.setTextColor(Color.YELLOW);
                            break;

                        case HIGH:
                            RestaurantHazard.setImageResource(R.drawable.red_hazard);
                            txtRestaurantHazard.setText("" + hazard);
                            txtRestaurantHazard.setTextColor(Color.RED);
                            break;
                        }
                    break;
                }
                else {
                    RestaurantHazard.setImageResource(R.drawable.green_hazard);
                    txtRestaurantHazard.setText("" + HazardRating.LOW);
                    txtRestaurantHazard.setTextColor(Color.GREEN);
                }
            }

            // Fill inspection date
            TextView restaurantDate = (TextView) itemView.findViewById(R.id.text_inspection_date);
            for (int i = 0; i < listInspections.size(); i++) {
                if (currentRestaurant.getTrackingNumber().equals(listInspections.get(i).getTrackingNumber())) {

                    Date inspectionDate = listInspections.get(i).getDate();   // Inspection date
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd"); // Set date format
                    Date currentDate = new Date();
                    simpleDateFormat.format(currentDate);   // Current date
                    // Subtract days
                    long diffInMillies = Math.abs(currentDate.getTime() - inspectionDate.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    // Less than 30 days
                    if(diff < 30) {
                        restaurantDate.setText( diff + " days ago");
                    }
                    // Less than one year
                    else if(diff < 365) {
                        simpleDateFormat = new SimpleDateFormat("MMMM dd");
                        String strDate = simpleDateFormat.format(inspectionDate);
                        restaurantDate.setText(strDate);
                    }
                    // More than one year
                    else {
                        simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
                        String strDate = simpleDateFormat.format(inspectionDate);
                        restaurantDate.setText(strDate);
                    }

                    break;
                }
                else {
                    restaurantDate.setText("No inspections found");
                }
            }
            return itemView;
        }
    }

    private void sortRestaurantList() {
        Collections.sort(RestaurantManager.restaurants, new Comparator<Restaurant>() {
            @Override
            // Sort ascendant order
            public int compare(Restaurant R1, Restaurant R2) {
                return R1.getName().compareTo(R2.getName());
            }
        });
    }

    private void sortInspectionDate() {
        Collections.sort(RestaurantManager.inspections, new Comparator<Inspection>() {
            @Override
            // Sort descendant order
            public int compare(Inspection I1, Inspection I2) {
                return I2.getDate().compareTo(I1.getDate());
            }
        });
    }
}