package com.example.project_1.UI;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
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
import com.example.project_1.Model.RestaurantManager;
import com.example.project_1.Model.Violation;
import com.example.project_1.Model.ViolationNature;
import com.example.project_1.Model.ViolationSeverity;
import com.example.project_1.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class InspectionReport extends AppCompatActivity {
    public static final String RESTAURANT_INDEX = "restaurant index";
    public static final String INSPECTION_INDEX = "inspection index";
    private Inspection inspection;
    private List<Violation> violationList;
    private int restaurantIndex;
    private int inspectionIndex;

    public static Intent makeIntent(Context context, int restaurantIndex, int inspectionIndex) {
        Intent intent = new Intent(context, InspectionReport.class);
        intent.putExtra(RESTAURANT_INDEX, restaurantIndex);
        intent.putExtra(INSPECTION_INDEX, inspectionIndex);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_report);

        Intent intent = getIntent();
        restaurantIndex = intent.getIntExtra(RESTAURANT_INDEX, -1);
        inspectionIndex = intent.getIntExtra(INSPECTION_INDEX, -1);

        RestaurantManager manager = RestaurantManager.getInstance();
        inspection = manager.getInspectionsForRestaurant(restaurantIndex).get(inspectionIndex);
        Log.e("inspection", "onCreate: " + inspection);

        violationList = inspection.getViolations();

        Log.e("violationList", "onCreate: " + violationList);

        setInspectionDetails();
        populateListView();
    }

    private void setInspectionDetails() {
        TextView tvDate = findViewById(R.id.InspectionReport_text_date);
        TextView tvInspectionType = findViewById(R.id.InspectionReport_text_inspection_type);
        TextView tvCritical = findViewById(R.id.InspectionReport_text_num_critical_issues);
        TextView tvNonCritical = findViewById(R.id.InspectionReport_text_num_non_critical_issues);
        TextView tvHazard = findViewById(R.id.InspectionReport_text_hazard_level);
        ImageView ivHazard = findViewById(R.id.InspectionReport_icon_hazard_level);

        // Fill date

        Date inspectionDate = inspection.getDate();   // Inspection date
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd"); // Set date format
        Date currentDate = new Date();
        simpleDateFormat.format(currentDate);   // Current date
        // Subtract days
        long diffInMillies = Math.abs(currentDate.getTime() - inspectionDate.getTime());
        long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
        // Less than 30 days
        if (diff < 30) {
            tvDate.setText(diff + " days ago");
        }
        // Less than one year
        else if (diff < 365) {
            simpleDateFormat = new SimpleDateFormat("MMMM dd");
            String strDate = simpleDateFormat.format(inspectionDate);
            tvDate.setText(strDate);
        }
        // More than one year
        else {
            simpleDateFormat = new SimpleDateFormat("MMMM yyyy");
            String strDate = simpleDateFormat.format(inspectionDate);
            tvDate.setText(strDate);
        }

        // Fill type

        tvInspectionType.setText("Inspection type: " + (inspection.getType() == InspectionType.ROUTINE
                ? "ROUTINE" : "FOLLOW-UP"));

        // Fill # issues

        int numCritical = inspection.getNumCritical();
        tvCritical.setText("Critical issues: " + numCritical);
        int numNonCritical = inspection.getNumNonCritical();
        tvNonCritical.setText("Non-Critical issues: " + numNonCritical);

        // Fill hazard text and icon

        HazardRating hazard = inspection.getHazardRating();
        switch (hazard) {
            case LOW:
                ivHazard.setImageResource(R.mipmap.green_hazard);
                tvHazard.setText("Hazard Level:  " + hazard);
                tvHazard.setTextColor(Color.GREEN);
                break;

            case MODERATE:
                ivHazard.setImageResource(R.mipmap.yellow_hazard);
                tvHazard.setText("Hazard Level:  " + hazard);
                tvHazard.setTextColor(Color.YELLOW);
                break;

            case HIGH:
                ivHazard.setImageResource(R.mipmap.red_hazard);
                tvHazard.setText("Hazard Level:  " + hazard);
                tvHazard.setTextColor(Color.RED);
                break;
        }

    }

    private void populateListView() {
        if (!violationList.isEmpty()) {
            ArrayAdapter<Violation> adapter = new MyListAdapter();
            ListView list = (ListView) findViewById(R.id.InspectionReport_list_inspection_report);
            list.setAdapter(adapter);

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Violation violation = violationList.get(position);
                    new AlertDialog.Builder(InspectionReport.this).setMessage(violation.getDescription())
                            .setTitle("Description")
                            .show();
                }
            });
        }
    }

    private class MyListAdapter extends ArrayAdapter<Violation> {
        public MyListAdapter() {
            super(InspectionReport.this, R.layout.violation_view, violationList);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Make sure we have a view to work with
            View itemView = convertView;
            if (itemView == null) {
                itemView = getLayoutInflater().inflate(R.layout.violation_view, parent, false);
            }

            if (!violationList.isEmpty()) {
                Violation violation = violationList.get(position);

                // Fill nature icon
                ImageView natureIcon = (ImageView) itemView.findViewById(R.id.InspectionReport_listItem_nature_icon);

                ViolationNature nature = violation.getNature();
                switch (nature) {
                    case PEST:
                        natureIcon.setImageResource(R.mipmap.pest);
                        break;

                    case FOOD:
                        natureIcon.setImageResource(R.mipmap.food);
                        break;

                    case PERMIT:
                        natureIcon.setImageResource(R.mipmap.permit);
                        break;

                    case EMPLOYEE:
                        natureIcon.setImageResource(R.mipmap.employee);
                        break;

                    case EQUIPMENT:
                        natureIcon.setImageResource(R.mipmap.equipment);
                        break;
                }

                // Fill severity icon
                ImageView severityIcon = (ImageView) itemView.findViewById(R.id.InspectionReport_listItem_severity_icon);

                ViolationSeverity severity = violation.getSeverity();
                switch (severity) {
                    case CRITICAL:
                        severityIcon.setImageResource(R.mipmap.critical);
                        break;

                    case NON_CRITICAL:
                        severityIcon.setImageResource(R.mipmap.non_critical);
                        break;

                }

                // Fill description
                TextView tvDescription = itemView.findViewById(R.id.InspectionReport_listItem_text_description);
                tvDescription.setText(violation.getDescription());

                // Fill nature text
                TextView tvNature = itemView.findViewById(R.id.InspectionReport_listItem_text_nature);
                tvNature.setText("Nature: " + violation.getNature());

                // Fill severity text
                TextView tvSeverity = itemView.findViewById(R.id.InspectionReport_listItem_text_severity);
                tvSeverity.setText("Severity: " + (violation.getSeverity() == ViolationSeverity.CRITICAL
                        ? "CRITICAL" : "NON-CRITICAL"));
            }

            return itemView;
        }
    }
}



