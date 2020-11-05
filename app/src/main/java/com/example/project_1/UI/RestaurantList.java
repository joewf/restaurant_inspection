package com.example.project_1.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
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
import com.example.project_1.Model.Violation;
import com.example.project_1.Model.ViolationSeverity;
import com.example.project_1.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

        restaurantManager = RestaurantManager.getInstance();

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
                Inspection newInspection = new Inspection();
                newInspection.setTrackingNumber(tokens[0]);

                // Set date
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                Date date = simpleDateFormat.parse(tokens[1]);

                newInspection.setDate(date);

                // dry inspection type
                InspectionType inspectionType;
                if (tokens[2].equals("\"Routine\"")) {
                    inspectionType = InspectionType.ROUTINE;

                } else {
                    inspectionType = InspectionType.FOLLOW_UP;
                }
                newInspection.setType(inspectionType);

                // Set critical issues
                if (tokens[3].length() > 0) {
                    newInspection.setNumCritical(Integer.parseInt(tokens[3]));
                } else {
                    newInspection.setNumCritical(0);
                }

                // Set non critical issues
                if (tokens[4].length() > 0) {
                    newInspection.setNumNonCritical(Integer.parseInt(tokens[4]));
                } else {
                    newInspection.setNumNonCritical(0);
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
                newInspection.setHazardRating(hazardRating);

                // Violations
                if (tokens.length > 6) {
                    Log.e("violations length ", "setInspectionData: " + (tokens.length - 6));

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

                    newInspection.setViolations(violationList);

                    restaurantManager.addInspection(newInspection);

                    Log.e("violations", "setInspectionData: " + violationList);
                } else {
                    restaurantManager.addInspection(newInspection);
                }

                Log.d("Inspection List", "Just created: " + newInspection);
            }
        } catch (IOException | ParseException e) {
            Log.wtf("Inspection List", "Error reading data file on line " + line, e);
            e.printStackTrace();
        }
    }

    private List<Violation> getViolationsFromString(String violationsString) {
        Log.e("getViolationsFromString", "getViolationsFromString: " + violationsString);

        String[] violations = violationsString.split("\\|");
        ArrayList<Violation> list = new ArrayList<>();

        String code;
        ViolationSeverity severity;
        String description;
        for (String s : violations) {
            Log.e("in loop", "getViolationsFromString: " + s);
            String[] tokens = s.split(",");

            code = tokens[0];

            if (tokens[1].equals("Critical")) {
                severity = ViolationSeverity.CRITICAL;
            } else {
                severity = ViolationSeverity.NON_CRITICAL;
            }

            description = tokens[2];

            Violation violation = new Violation(description, severity, code);
            Log.e("Violation OBJECT", "getViolationsFromString: " + violation);

            list.add((violation));
        }
        return list;
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
                Restaurant newRestaurant = new Restaurant();
                newRestaurant.setTrackingNumber(tokens[0]);
                newRestaurant.setName(tokens[1]);
                newRestaurant.setPhysicalAddress(tokens[2]);
                newRestaurant.setPhysicalCity(tokens[3]);
                newRestaurant.setFactType(tokens[4]);
                if (tokens[5].length() > 0) {
                    newRestaurant.setLatitude(Double.parseDouble(tokens[5]));
                } else {
                    newRestaurant.setLatitude(0);
                }
                if (tokens[6].length() > 0) {
                    newRestaurant.setAltitude(Double.parseDouble(tokens[6]));
                } else {
                    newRestaurant.setAltitude(0);
                }

                restaurantManager.addRestaurant(newRestaurant);

                Log.d("RestaurantList", "Just created: " + newRestaurant);

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
            List<Inspection> inspectionsForCurrentRestaurant = restaurantManager.getInspectionsForRestaurant(position);

            // Fill restaurant icon
            ImageView restaurantView = (ImageView) itemView.findViewById(R.id.restaurant_icon);
            restaurantView.setImageResource(restaurantIcon[position]);

            // Fill restaurant name
            TextView restaurantName = (TextView) itemView.findViewById(R.id.RestaurantDetails_text_restaurant_name);
            restaurantName.setText(currentRestaurant.getName());
            restaurantName.setTextColor(Color.BLUE);

            // Fill issues
            TextView restaurantCriticalIssues = (TextView) itemView.findViewById(R.id.text_issues_found);
            int criticalIssues;
            if (!inspectionsForCurrentRestaurant.isEmpty()) {

                for (Inspection inspection : inspectionsForCurrentRestaurant) {
                    criticalIssues = inspection.getNumCritical();
                    restaurantCriticalIssues.setText("Critical issues: " + criticalIssues);

                    break;
                }
            } else {
                restaurantCriticalIssues.setText("Critical issues: 0");
            }

            // Fill hazard icon and text
            ImageView RestaurantHazard = (ImageView) itemView.findViewById(R.id.hazard_icon);
            TextView txtRestaurantHazard = (TextView) itemView.findViewById(R.id.text_hazard_level);
            if (!inspectionsForCurrentRestaurant.isEmpty()) {

                for (Inspection inspection : inspectionsForCurrentRestaurant) {
                    HazardRating hazard = inspection.getHazardRating();
                    switch (hazard) {
                        case LOW:
                            RestaurantHazard.setImageResource(R.mipmap.green_hazard);
                            txtRestaurantHazard.setText("" + hazard);
                            txtRestaurantHazard.setTextColor(Color.GREEN);
                            break;

                        case MODERATE:
                            RestaurantHazard.setImageResource(R.mipmap.yellow_hazard);
                            txtRestaurantHazard.setText("" + hazard);
                            txtRestaurantHazard.setTextColor(Color.YELLOW);
                            break;

                        case HIGH:
                            RestaurantHazard.setImageResource(R.mipmap.red_hazard);
                            txtRestaurantHazard.setText("" + hazard);
                            txtRestaurantHazard.setTextColor(Color.RED);
                            break;
                    }

                    break;
                }
            } else {
                RestaurantHazard.setImageResource(R.mipmap.green_hazard);
                txtRestaurantHazard.setText("" + HazardRating.LOW);
                txtRestaurantHazard.setTextColor(Color.GREEN);
            }


            // Fill inspection date
            TextView restaurantDate = (TextView) itemView.findViewById(R.id.text_inspection_date);
            if (!inspectionsForCurrentRestaurant.isEmpty()) {
                for (Inspection inspection : inspectionsForCurrentRestaurant) {

                    Date inspectionDate = inspection.getDate();   // Inspection date
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd"); // Set date format
                    Date currentDate = new Date();
                    simpleDateFormat.format(currentDate);   // Current date
                    // Subtract days
                    long diffInMillies = Math.abs(currentDate.getTime() - inspectionDate.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    // Less than 30 days
                    if (diff < 30) {
                        restaurantDate.setText(diff + " days ago");
                    }
                    // Less than one year
                    else if (diff < 365) {
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
            } else {
                restaurantDate.setText("No inspections found");
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