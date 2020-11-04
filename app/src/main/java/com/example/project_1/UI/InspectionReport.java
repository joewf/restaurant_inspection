package com.example.project_1.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.project_1.Model.HazardRating;
import com.example.project_1.Model.Inspection;
import com.example.project_1.Model.InspectionType;
import com.example.project_1.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InspectionReport extends AppCompatActivity {
    public static Intent makeIntent(Context context) {
        return new Intent(context, InspectionReport.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_report);

        readInspectionData();
    }

    private List<Inspection> inspectionList = new ArrayList<>();

    private void readInspectionData() {

        InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8)
        );

        String line = "";
        try {
            //Step over headers
            reader.readLine();

            InspectionType inspectionType;
            HazardRating hazardRating;
            String trackingNumber;
            int numCritical;
            int numNonCritical;
            while (((line = reader.readLine()) != null)) {
                Log.e("line", "readInspectionData: " + line);

                // Split by ','
                String[] tokens = line.split(",");

                // Read the data
                trackingNumber = tokens[0].substring(1, tokens[0].length() - 1);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                Date date = simpleDateFormat.parse(tokens[1]);

                if (tokens[2].equals("\"Routine\"")) {
                    inspectionType = InspectionType.ROUTINE;
                } else {
                    inspectionType = InspectionType.FOLLOW_UP;
                }

                numCritical = Integer.parseInt(tokens[3]);

                numNonCritical = Integer.parseInt(tokens[4]);

                if ("\"Low\"".equals(tokens[5])) {
                    hazardRating = HazardRating.LOW;
                } else if ("\"Moderate\"".equals(tokens[5])) {
                    hazardRating = HazardRating.MODERATE;
                } else {
                    hazardRating = HazardRating.HIGH;
                }

                Inspection inspection = new Inspection(trackingNumber, date,
                        inspectionType, numCritical, numNonCritical, hazardRating, null);

                inspectionList.add(inspection);

                Log.e("InspectionReport", "Just created " + inspectionList.size() + ": " + inspection);
            }
        } catch (IOException | ParseException e) {
            Log.wtf("InspectionReport", "error reading file on line" + line, e);
            e.printStackTrace();
        }

    }

    private int[] parseDateString(String token) {
        int[] date = new int[3];

        Log.e("parseDateString", "parseDateString: " + token);
        date[0] = Integer.parseInt(token.substring(0, 4));
        Log.e("parseDateString", "parseDateString: " + date[0]);
        date[1] = Integer.parseInt(token.substring(4, 6));
        Log.e("parseDateString", "parseDateString: " + date[1]);
        date[2] = Integer.parseInt(token.substring(6));
        Log.e("parseDateString", "parseDateString: " + date[2]);

        return date;
    }
}



