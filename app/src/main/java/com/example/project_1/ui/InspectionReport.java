package com.example.project_1.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import com.example.project_1.model.HazardRating;
import com.example.project_1.model.Inspection;
import com.example.project_1.model.InspectionType;
import com.example.project_1.model.RestaurantManager;
import com.example.project_1.model.Violation;
import com.example.project_1.model.ViolationNature;
import com.example.project_1.model.ViolationSeverity;
import com.example.project_1.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * InspectionReport class models the information about a InspectionReport activity.
 */
public class InspectionReport extends AppCompatActivity {
    public static final String RESTAURANT_INDEX = "restaurant index";
    public static final String INSPECTION_INDEX = "inspection index";
    private Inspection inspection;
    private List<Violation> violationList;

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
        int restaurantIndex = intent.getIntExtra(RESTAURANT_INDEX, -1);
        int inspectionIndex = intent.getIntExtra(INSPECTION_INDEX, -1);

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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM d, yyyy");
        String strDate = simpleDateFormat.format(inspectionDate);
        tvDate.setText(strDate);

        // Fill type

        tvInspectionType.setText(getString(R.string.ins_type) + (inspection.getType() == InspectionType.ROUTINE
                ? getString(R.string.rout) : getString(R.string.fu)));

        // Fill # issues

        int numCritical = inspection.getNumCritical();
        tvCritical.setText(getString(R.string.num_crit) + numCritical);
        int numNonCritical = inspection.getNumNonCritical();
        tvNonCritical.setText(getString(R.string.num_noncrit) + numNonCritical);

        // Fill hazard text and icon

        HazardRating hazard = inspection.getHazardRating();
        switch (hazard) {
            case LOW:
                ivHazard.setImageResource(R.mipmap.green_hazard);
                tvHazard.setText(getString(R.string.hazard_level__) + hazard);
                tvHazard.setTextColor(Color.GREEN);
                break;

            case MODERATE:
                ivHazard.setImageResource(R.mipmap.yellow_hazard);
                tvHazard.setText(getString(R.string.hazard_level__) + hazard);
                tvHazard.setTextColor(Color.YELLOW);
                break;

            case HIGH:
                ivHazard.setImageResource(R.mipmap.red_hazard);
                tvHazard.setText(getString(R.string.hazard_level__) + hazard);
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
                            .setTitle("Violation Description")
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
                tvNature.setText(getString(R.string.nature_) + violation.getNature());

                // Fill severity text
                TextView tvSeverity = itemView.findViewById(R.id.InspectionReport_listItem_text_severity);
                tvSeverity.setText(getString(R.string.severity_) + (violation.getSeverity() == ViolationSeverity.CRITICAL
                        ? getString(R.string.crit) : getString(R.string.non_crit)));
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



