package com.example.project_1.UI;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.ArrayList;
import java.util.List;

public class InspectionReport extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inspection_report);

        getinspectiondata();
    }

    private List<Inspection> inspectionList = new ArrayList<>();

    private void getinspectiondata() {

        InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8")));

        String line = "";
        try {
            //Step over headers
            reader.readLine();

            while ( ((line = reader.readLine()) != null) ) {

                Log.d("InspectionReport", "line: " + line);
                //split upon commas
                String[] tokens = line.split(",");
                //reading in data
                Inspection inspectData = new Inspection();
                inspectData.setTrackingNumber(tokens[0]);
                inspectData.setDate(tokens[1]);

                // Couldn't get the type and hazard
                    if (line == "Routine") {
                        inspectData.setType(tokens[2]) = InspectionType.ROUTINE;

                    }
                    if (line == "Follow-Up"){
                        inspectData.setType(tokens[2]) = InspectionType.FOLLOW_UP;
                    }
                inspectData.setNumCritical(Integer.parseInt(tokens[3]));
                inspectData.setNumNonCritical(Integer.parseInt(tokens[4]));
                if(line == "Low") {
                    inspectData.setHazardRating(tokens[5]) = HazardRating.LOW;
                }
                if(line == "Moderate") {
                    inspectData.setHazardRating(tokens[5]) = HazardRating.MODERATE;
                }
                if(line == "High") {
                    inspectData.setHazardRating(tokens[5]) = HazardRating.HIGH;
                }


                inspectionList.add(inspectData);

                Log.d("InspectionReport", "created" + inspectData);
            }
        } catch (IOException e) {
                Log.wtf("InspectionReport", "error reading file on line", e);
                e.printStackTrace();
            }

        }


    }



