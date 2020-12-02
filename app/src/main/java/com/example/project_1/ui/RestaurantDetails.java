package com.example.project_1.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import android.widget.Toast;

import com.example.project_1.model.HazardRating;
import com.example.project_1.model.Inspection;
import com.example.project_1.model.Restaurant;
import com.example.project_1.model.RestaurantManager;
import com.example.project_1.R;
import com.like.LikeButton;
import com.like.OnLikeListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * RestaurantDetails class models the information about a RestaurantDetails activity.
 */
public class RestaurantDetails extends AppCompatActivity {
    public static final String RESTAURANT_INDEX = "Restaurant index";
    private Restaurant restaurant;
    private int restaurantIndex;
    private List<Inspection> inspectionList = new ArrayList<>();
    private List<Restaurant> restaurantList = new ArrayList<>();
    private RestaurantManager restaurantManager;

    public static Intent makeIntent(Context context, int restaurantIndex) {
        Intent intent = new Intent(context, RestaurantDetails.class);
        intent.putExtra(RESTAURANT_INDEX, restaurantIndex);
        return intent;
    }

    public static Intent makeIntent(Context context, String tracking) {
        Intent intent = new Intent(context, RestaurantDetails.class);
        intent.putExtra("tracking number", tracking);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_details);

        restaurantManager = RestaurantManager.getInstance();
        restaurantIndex = getIntent().getIntExtra(RESTAURANT_INDEX, -1);
        String trackingNumber = getIntent().getStringExtra("tracking number");


        if (restaurantIndex != -1) {
            restaurant = restaurantManager.getRestaurants().get(restaurantIndex);
            inspectionList = restaurantManager.getInspectionsForRestaurant(restaurantIndex);
        }

        getRestaurantDetails();
        populateListView();


        setFavoriteButtonCallback();
    }

    private void setFavoriteButtonCallback() {
        LikeButton favBtn = findViewById(R.id.star_button);

        if (restaurantManager.getFavTrackingNumList().contains(restaurant.getTrackingNumber().replaceAll("\"", ""))){
            restaurant.setFavorite(true);
            favBtn.setLiked(true);
        }

        favBtn.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                restaurant.setFavorite(true);
                restaurantManager.addFavRestaurant(restaurant);

                //Toast.makeText(RestaurantDetails.this, "liked", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                restaurant.setFavorite(false);
                restaurantManager.removeFavRestaurant(restaurant);

                //Toast.makeText(RestaurantDetails.this, "unliked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getRestaurantDetails() {

        // Set name
        String name = restaurant.getName();
        TextView tvName = (TextView) findViewById(R.id.RestaurantDetails_text_restaurant_name);
        tvName.setText(name);
        tvName.setTextColor(Color.BLUE);

        // Set Address
        String address = restaurant.getPhysicalAddress();
        TextView tvAddress = (TextView) findViewById(R.id.RestaurantDetails_text_address);
        tvAddress.setText(getString(R.string.address_) + address);

        // Set GPS
        double latitude = restaurant.getLatitude();
        double altitude = restaurant.getLongitude();
        TextView tvGPS = (TextView) findViewById(R.id.RestaurantDetails_text_GPS);
        tvGPS.setText(getString(R.string.gps) + altitude + getString(R.string.comma) + latitude);

    }

    private void populateListView() {

        ArrayAdapter<Inspection> adapter = new MyListAdapter();
        ListView list = (ListView) findViewById(R.id.RestaurantDetails_list_inspection_report);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(InspectionReport.makeIntent(RestaurantDetails.this, restaurantIndex, position));
            }
        });
    }

    public void myOnClick(View view) {
        Log.e("TAG", "myOnClick: " + restaurant.getLatitude() + "," + restaurant.getLongitude() );
        startActivity(MapsActivity.makeIntent(getApplicationContext(), restaurant.getLatitude(), restaurant.getLongitude(), restaurant.getTrackingNumber(), BackFrom.RestaurantDetails));
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
                ImageView hazardIcon = itemView.findViewById(R.id.icon_hazard_level);

                HazardRating hazard = inspection.getHazardRating();
                switch (hazard) {
                    case LOW:
                        hazardIcon.setImageResource(R.mipmap.green_hazard);
                        itemView.setBackgroundColor(Color.rgb(204, 255, 204));
                        break;

                    case MODERATE:
                        hazardIcon.setImageResource(R.mipmap.yellow_hazard);
                        itemView.setBackgroundColor(Color.rgb(255, 255, 204));

                        break;

                    case HIGH:
                        hazardIcon.setImageResource(R.mipmap.red_hazard);
                        itemView.setBackgroundColor(Color.rgb(255, 204, 204));

                        break;
                }

                // Fill inspection date
                TextView restaurantDate = itemView.findViewById(R.id.text_inspection_occurred);

                Date inspectionDate = inspection.getDate();   // Inspection date
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd"); // Set date format
                Date currentDate = new Date();
                simpleDateFormat.format(currentDate);   // Current date
                // Subtract days
                long diffInMillis = Math.abs(currentDate.getTime() - inspectionDate.getTime());
                long diff = TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);

                // Less than 30 days
                if (diff < 30) {
                    restaurantDate.setText(diff + getString(R.string.days_ago));
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
                TextView textViewNumCritical = itemView.findViewById(R.id.text_criticalIssues_found);
                TextView textViewNumNonCritical = itemView.findViewById(R.id.text_non_criticalIssues_found);

                int numCritical = inspection.getNumCritical();
                textViewNumCritical.setText(getString(R.string.critical_iss) + numCritical);
                int numNonCritical = inspection.getNumNonCritical();
                textViewNumNonCritical.setText(getString(R.string.non_critical_iss) + numNonCritical);

            }

            return itemView;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
