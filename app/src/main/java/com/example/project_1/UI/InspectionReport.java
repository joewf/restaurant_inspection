package com.example.project_1.UI;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.example.project_1.Model.Inspection;
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
    }

    private List<Inspection> Inspectionlist = new ArrayList<>();

    private void getinspectiondata() {

        InputStream is = getResources().openRawResource(R.raw.inspectionreports_itr1);
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, Charset.forName("UTF-8")));
        String line = "";
        while (true) {
            try {
                //Step over headers
                reader.readLine();
                while (!((line = reader.readLine()) != null)) {
                    Log.d("InspectionReport", "line: " + line);
                    //split upon commas
                    String[] tokens = line.split(",");
                    //reading in data
                    Inspection inspectdata = new Inspection();
                    inspectdata.setTrackingNumber(tokens[0]);
                    inspectdata.setDate(tokens[1]);
                    inspectdata.setType(tokens[2]);
                    inspectdata.setNumCritical(tokens[3]);
                    inspectdata.setNumNonCritical(tokens[4]);
                    inspectdata.setHazardRating(tokens[5]);
                    inspectdata.setViolationLump(tokens[6]);
                    Inspectionlist.add(inspectdata);

                    Log.d("InspectionReport", "created" + inspectdata);
                }
            } catch (IOException e) {
                Log.wtf("InspectionReport", "error reading file on line", e);
                e.printStackTrace();
            }

        }


    }
}


