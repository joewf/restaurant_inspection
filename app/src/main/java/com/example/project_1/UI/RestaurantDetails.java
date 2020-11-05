package com.example.project_1.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.project_1.Model.HazardRating;
import com.example.project_1.Model.Inspection;
import com.example.project_1.Model.Restaurant;
import com.example.project_1.Model.RestaurantManager;
import com.example.project_1.R;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RestaurantDetails extends AppCompatActivity {
    public static final String RESTAURANT_INDEX = "Restaurant index";
    private Restaurant restaurant;
    private RestaurantManager manager;
    private int restaurantIndex;
    private List<Inspection> inspectionList = new ArrayList<>();
    private TextView tvName;
    private TextView tvAddress;
    private TextView tvGPS;

    public static Intent makeIntent(Context context, int restaurantIndex) {
        Intent intent = new Intent(context, RestaurantDetails.class);
        intent.putExtra(RESTAURANT_INDEX, restaurantIndex);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        RestaurantManager manager = RestaurantManager.getInstance();
        restaurantIndex = getIntent().getIntExtra(RESTAURANT_INDEX, -1);

        if (restaurantIndex != -1) {
            restaurant = manager.get(restaurantIndex);
            inspectionList = manager.getInspectionsForRestaurant(restaurantIndex);
        }

        getRestaurantDetails();

        populateListView();
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

    private void populateListView() {
        if (!inspectionList.isEmpty()) {
            ArrayAdapter<Inspection> adapter = new MyListAdapter();
            ListView list = (ListView) findViewById(R.id.list_inspection_report);
            list.setAdapter(adapter);
        } /*else {
            String text[] = {"No inspections found!"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.inspection_report_view_no_inspections_found, text);
            ListView list = (ListView) findViewById(R.id.list_inspection_report);
            list.setAdapter(adapter);
        }*/
    }

    private class MyListAdapter extends ArrayAdapter<Inspection> {
        public MyListAdapter() {
            super(RestaurantDetails.this, R.layout.inspection_report_view, inspectionList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Make sure we have a view to work with
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.inspection_report_view, parent, false);
            }

            if (!inspectionList.isEmpty()) {
                Inspection inspection = inspectionList.get(position);

                // Fill hazard icon
                ImageView hazardIcon = (ImageView) itemView.findViewById(R.id.icon_hazard_level);

                HazardRating hazard = inspection.getHazardRating();
                switch (hazard) {
                    case LOW:
                        hazardIcon.setImageResource(R.drawable.green_hazard);
                        // Light green
                        itemView.setBackgroundColor(Color.rgb(128,255,128));
                        break;

                    case MODERATE:
                        hazardIcon.setImageResource(R.drawable.yellow_hazard);
                        // Light yellow
                        itemView.setBackgroundColor(Color.rgb(255,255,128));
                        break;

                    case HIGH:
                        hazardIcon.setImageResource(R.drawable.red_hazard);
                        // Light red
                        itemView.setBackgroundColor(Color.rgb(255,128,128));
                        break;
                }

            /*else {
                hazardIcon.setImageResource(R.drawable.green_hazard);
            }*/

                // Fill inspection date
                TextView restaurantDate = (TextView) itemView.findViewById(R.id.text_inspection_occurred);

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

                // Fill issues
                TextView textViewNumCritical = (TextView) itemView.findViewById(R.id.text_criticalIssues_found);
                TextView textViewNumNonCritical = (TextView) itemView.findViewById(R.id.text_non_criticalIssues_found);

                int numCritical = inspection.getNumCritical();
                textViewNumCritical.setText("# critical issues: " + numCritical);
                int numNonCritical = inspection.getNumNonCritical();
                textViewNumNonCritical.setText("# non-critical issues: " + numNonCritical);

            }

            return itemView;
        }
    }
}