package com.example.project_1.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.project_1.R;
import com.example.project_1.model.HazardRating;
import com.example.project_1.model.InputStreamVolleyRequest;
import com.example.project_1.model.Inspection;
import com.example.project_1.model.InspectionType;
import com.example.project_1.model.Restaurant;
import com.example.project_1.model.RestaurantManager;
import com.example.project_1.model.Violation;
import com.example.project_1.model.ViolationSeverity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * RestaurantList class models the information about a RestaurantList activity.
 */
public class RestaurantList extends AppCompatActivity {

    private static final String TAG = RestaurantList.class.getSimpleName();
    private static final String LAST_UPDATED_TIME = "last updated time";
    private static final String LAST_MODIFIED_TIME = "last modified time";

    private static boolean loadedFromSave = false;

    private RestaurantManager restaurantManager;
    private int[] restaurantIcon = new int[8];
    ProgressDialog pDialog;

    private long lastUpdatedTimeInMilliseconds;
    private long lastModifiedTimeInMilliseconds;


    // Add the request to the RequestQueue.


    // Instantiate the cache
    //Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

    // Set up the network to use HttpURLConnection as the HTTP client.
    //Network network = new BasicNetwork(new HurlStack());

    // Instantiate the RequestQueue with the cache and network.
    //RequestQueue requestQueue = new RequestQueue(this);
    //RequestQueue requestQueue = Volley.newRequestQueue(this);

    //RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
    int count;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_list);

        lastUpdatedTimeInMilliseconds = getIntent().getLongExtra(LAST_UPDATED_TIME, 0);
        lastModifiedTimeInMilliseconds = getIntent().getLongExtra(LAST_MODIFIED_TIME, 0);

        Log.e(TAG, "onCreate: " + lastUpdatedTimeInMilliseconds);
        ;
        restaurantManager = RestaurantManager.getInstance();

//        load();
//        checkUpdateOfFraserHealthRestaurantInspectionReports();
        populateListView();

    }

    public static Intent makeIntent(Context context, long lastUpdatedTimeInMilliseconds, long lastModifiedTimeInMilliseconds) {
        Intent intent = new Intent(context, RestaurantList.class);
        intent.putExtra(LAST_UPDATED_TIME, lastUpdatedTimeInMilliseconds);
        intent.putExtra(LAST_MODIFIED_TIME, lastModifiedTimeInMilliseconds);
        return intent;
    }

    private void startMap() {
        startActivity(new Intent(this, MapsActivity.class));
    }

    // check update
    private void checkUpdateOfFraserHealthRestaurantInspectionReports() {

        String urlFraserHealthRestaurantInspectionReports = "https://data.surrey.ca/api/3/action/package_show?id=fraser-health-restaurant-inspection-reports";

        //final JSONObject[] restaurantRequest = new JSONObject[1];
        StringRequest stringRequest = new StringRequest(Request.Method.GET, urlFraserHealthRestaurantInspectionReports,
                new Response.Listener<String>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject fraserHealthRestaurantInspectionReportsObj = new JSONObject(response);
                            JSONObject result = fraserHealthRestaurantInspectionReportsObj.getJSONObject("result");
                            JSONArray resources = result.getJSONArray("resources");
                            String inspectionReportsTimeStamp = resources.getJSONObject(0).getString("last_modified");
                            String inspectionReportsURL = resources.getJSONObject(0).getString("url");
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.ENGLISH);
                            LocalDateTime lastModified = LocalDateTime.parse(inspectionReportsTimeStamp, formatter);
                            long lastModifiedTimeInMilliseconds = lastModified.atOffset(ZoneOffset.ofHours(-7)).toInstant().toEpochMilli();
                            long currentTimeInMilliseconds = Instant.now().toEpochMilli();
                            Log.i("myResponse", response);
                            Log.i("myResources", resources.toString());
                            Log.i("myInspectionReportTimeStamp", inspectionReportsTimeStamp);
                            Log.i("myLastModifiedTime", "Date in milli :: FOR API >= 26 >>> " + lastModifiedTimeInMilliseconds);
                            Log.i("myCurrentTime", "current time: " + currentTimeInMilliseconds);
                            Log.i("myURL", inspectionReportsURL);

                            promptUserDownloadUpdateDialog(lastModifiedTimeInMilliseconds, currentTimeInMilliseconds, inspectionReportsURL);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });


        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


    private void promptUserDownloadUpdateDialog(long lastModifiedTimeInMilliseconds, long currentTimeInMilliseconds, String inspectionReportsURL) {

        // 20 hours = 72000000 Milliseconds
        if (currentTimeInMilliseconds - lastModifiedTimeInMilliseconds > 72000000) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(RestaurantList.this);
                    builder.setTitle("Last update is 20 hours ago");
                    builder.setMessage("Do you want to download the update?");

                    builder.setPositiveButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            Boolean updateAvailable = false;
                            File oldRestaurants = new File(getFilesDir(), "oldRestaurants");

                            setRestaurantData(updateAvailable, oldRestaurants);
                            File oldInspectionReports = new File(getFilesDir(), "oldRestaurants");
                            setInspectionData(updateAvailable, oldInspectionReports);
                            /*restaurantManager.sortRestaurantList();
                            restaurantManager.sortInspectionDate();
                            populateListView();
                            populateIcon();*/
                            //Intent home = new Intent(getApplicationContext(), OfficeActivity.class);
                            //startActivity(home);
                            //finish();
                            restaurantManager.sortRestaurantList();
                            restaurantManager.sortInspectionDate();
                            populateListView();
                            Log.e(TAG, "onClick: " + restaurantManager);
                        }
                    });

                    builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            String urlRestaurants = "https://data.surrey.ca/api/3/action/package_show?id=restaurants";

                            StringRequest stringRequest = new StringRequest(Request.Method.GET, urlRestaurants,
                                    new Response.Listener<String>() {
                                        @RequiresApi(api = Build.VERSION_CODES.O)
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                JSONObject restaurants = new JSONObject(response);
                                                JSONObject result = restaurants.getJSONObject("result");
                                                JSONArray resources = result.getJSONArray("resources");
                                                String restaurantsURL = resources.getJSONObject(0).getString("url");
                                                Log.i("myURLRestaurant", restaurantsURL);

                                                downloadUpdate(restaurantsURL, inspectionReportsURL);

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                }
                            });

                            // Add the request to the RequestQueue.
                            //RequestQueue requestQueue = Volley.newRequestQueue(this);
                            Volley.newRequestQueue(getApplicationContext()).add(stringRequest);
                            //requestQueue.add(stringRequest);

                            //Intent home = new Intent(getApplicationContext(), SchoolActivity.class);
                            //startActivity(home);
                            //finish();
                        }
                    });
                    AlertDialog alertdialog = builder.create();
                    alertdialog.show();
                }
            }, 100);

        }

    }


    private void downloadUpdate(String restaurantsURL, String inspectionReportsURL) {
        // cannot save download file to res https://stackoverflow.com/questions/3374061/write-to-res-drawable-on-the-fly/3374149#3374149
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        //pDialog.setCancelable(false);
        final boolean[] cancelDownloading = {false};
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                pDialog.dismiss();//dismiss dialog
                cancelDownloading[0] = true;
                //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).stop();
                //requestQueue.stop();
                Boolean updateAvailable = false;
                File oldRestaurants = new File(getFilesDir(), "oldRestaurants");
                setRestaurantData(updateAvailable, oldRestaurants);
                File oldInspectionReports = new File(getFilesDir(), "oldRestaurants");
                setInspectionData(updateAvailable, oldInspectionReports);
                /*restaurantManager.sortRestaurantList();
                restaurantManager.sortInspectionDate();*/
                restaurantManager.sortRestaurantList();
                restaurantManager.sortInspectionDate();
                populateListView();
                populateIcon();
            }
        });
        pDialog.show();

        InputStreamVolleyRequest request;
        request = new InputStreamVolleyRequest(Request.Method.GET, restaurantsURL, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                try {
                    if (response != null) {

                        String filename = "newRestaurants";
                        //filename = filename.replace(":", ".");
                        Log.d("myRESUME FILE NAME", filename);

                        try {
                            long lenghtOfFile = response.length;

                            //covert reponse to input stream
                            InputStream input = new ByteArrayInputStream(response);
                            //File path = Environment.getExternalStorageDirectory();
                            File newRestaurants = new File(getFilesDir(), filename);
                            map.put("resume_path", newRestaurants.toString());
                            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(newRestaurants));
                            byte data[] = new byte[1024];

                            long total = 0;

                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }

                            output.flush();
                            output.close();
                            input.close();

                            downloadInspectionReports(newRestaurants, inspectionReportsURL, cancelDownloading);


                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE. ERROR:: " + error.getMessage());
            }
        }, null);
        request.setTag("tag");
        Request<byte[]> requestQueueByte = Volley.newRequestQueue(getApplicationContext(), new HurlStack()).add(request);
        //RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext(), new HurlStack());
        //requestQueue = requestQueue.add(request);
        if (cancelDownloading[0]) {
            //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).stop();
            //Log.i("myStop", "stop");
            requestQueueByte.cancel();
        }
        //requestQueue.add(request);
    }


    private void downloadInspectionReports(File newRestaurants, String inspectionReportsURL, boolean[] cancelDownloading) {
        InputStreamVolleyRequest request;
        request = new InputStreamVolleyRequest(Request.Method.GET, inspectionReportsURL, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                try {
                    if (response != null) {

                        String filename = "newInspectionReports";
                        //filename = filename.replace(":", ".");
                        Log.d("myRESUME FILE NAME", filename);

                        try {
                            long lenghtOfFile = response.length;

                            //covert reponse to input stream
                            InputStream input = new ByteArrayInputStream(response);
                            //File path = Environment.getExternalStorageDirectory();
                            File newInspectionReports = new File(getFilesDir(), filename);
                            map.put("resume_path", newInspectionReports.toString());
                            BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(newInspectionReports));
                            byte data[] = new byte[1024];

                            long total = 0;

                            while ((count = input.read(data)) != -1) {
                                total += count;
                                output.write(data, 0, count);
                            }

                            output.flush();
                            output.close();
                            input.close();

                            int fileSizeNewRestaurants = Integer.parseInt(String.valueOf(newRestaurants.length() / 1024));
                            Log.d("myFileSize", ": " + fileSizeNewRestaurants + "KB");
                            int fileSizeNewInspectionReports = Integer.parseInt(String.valueOf(newInspectionReports.length() / 1024));
                            Log.d("myFileSize", ": " + fileSizeNewInspectionReports + "KB");

                            pDialog.dismiss();
                            Boolean updateAvailable = true;
                            if (cancelDownloading[0]) {
                                updateAvailable = false;
                            }
                            Log.i("myCancel", ": " + cancelDownloading[0]);
                            setRestaurantData(updateAvailable, newRestaurants);
                            setInspectionData(updateAvailable, newInspectionReports);

                            //change here to adapt large data
                            //sortRestaurantList();
                            //sortInspectionDate();
                            //populateListView();
                            //populateIcon();
                            restaurantManager.sortRestaurantList();
                            restaurantManager.sortInspectionDate();
                            populateListView();
                            Log.e(TAG, "onClick: " + restaurantManager);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE. ERROR:: " + error.getMessage());
            }
        }, null);
        //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).add(request);
        Request<byte[]> requestQueueByte = Volley.newRequestQueue(getApplicationContext(), new HurlStack()).add(request);
        if (cancelDownloading[0]) {
            //Volley.newRequestQueue(getApplicationContext(), new HurlStack()).stop();
            //Log.i("myStop", "stop");
            requestQueueByte.cancel();
        }
        //requestQueue.add(request);
    }


    private void setInspectionData(Boolean updateAvailable, File newInspectionReports) {
        if (updateAvailable) {
            try (BufferedReader reader = new BufferedReader(new FileReader(newInspectionReports))) {
                restaurantManager.emptyInspections();
                String line;

                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    if (line.length() == 6) {
                        break;
                    }
                    Log.i("myLine", ": " + line.length());
                    String[] tokens = line.split(",");
                    Log.i("myToken0", tokens[0]);
                    //if (tokens[0] == "") {break;}
                    Inspection sampleInspection = new Inspection();
                    sampleInspection.setTrackingNumber(tokens[0]);
                    Log.i("myTrackingNumber", tokens[0]);

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    Date date = simpleDateFormat.parse(tokens[1]);

                    sampleInspection.setDate(date);

                    // dry inspection type
                    InspectionType inspectionType;
                    if (tokens[2].equals("Routine")) {
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
                    if (tokens[tokens.length - 1].equals("Low")) {
                        hazardRating = HazardRating.LOW;
                    } else if (tokens[tokens.length - 1].equals("Moderate")) {
                        hazardRating = HazardRating.MODERATE;
                    } else if (tokens[tokens.length - 1].equals("High")) {
                        hazardRating = HazardRating.HIGH;
                    } else {
                        hazardRating = HazardRating.LOW;
                    }
                    Log.i("myHazardRating", tokens[tokens.length - 1]);
                    sampleInspection.setHazardRating(hazardRating);


                    // Violations
                    if (tokens.length > 6) {
                        Log.i("myViolationsLength ", "setInspectionData: " + (tokens.length - 6));

                        // Get violations String "..."
                        StringBuilder violationsString = new StringBuilder();
                        for (int i = 5; i < tokens.length - 1; i++) {
                            //Log.e("token[i]", "loop: " + tokens[i]);
                            if (i == 5) {
                                violationsString.append(tokens[i]);
                            } else {
                                violationsString.append(",").append(tokens[i]);
                            }
                        }

                        List<Violation> violationList = getViolationsFromString(violationsString.toString());
                        //List<Violation> violationList = new ArrayList<>();
                        sampleInspection.setViolations(violationList);

                        //Log.i("myViolations", "setInspectionData: " + violationList);
                    }

                    restaurantManager.addInspection(sampleInspection);
                    Log.i("myInspectionLength", ": " + restaurantManager.getInspections().size());
                }
                Log.i("myInspectionLengthFinal", ": " + restaurantManager.getInspections().size());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }


        } /*else if (LOAD) {
            load();
            Log.e("TAG", "setInspectionData: " + restaurantManager);
        }*/ else if (!loadedFromSave) {
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
                    Log.i("myTrackingNumber", tokens[0]);

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

                    // Violations
                    if (tokens.length > 6) {
                        //Log.e("violations length ", "setInspectionData: " + (tokens.length - 6));

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

                        sampleInspection.setViolations(violationList);

                        //Log.e("violations", "setInspectionData: " + violationList);
                    }

                    restaurantManager.addInspection(sampleInspection);

                    Log.d("Inspection List", "Just created: " + sampleInspection);
                }
            } catch (IOException | ParseException e) {
                Log.wtf("Inspection List", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }
        }


    }

    private List<Violation> getViolationsFromString(String violationsString) {
        violationsString = violationsString.replaceAll("^\"|\"$", "");
        Log.i("myGetViolationsFromString", "getViolationsFromString: " + violationsString);
        ArrayList<Violation> list = new ArrayList<>();
        if (violationsString != "") {
            String[] violations = violationsString.split("\\|");

            String code;
            ViolationSeverity severity;
            String description;
            for (String s : violations) {
                Log.i("in loop", "getViolationsFromString: " + s);
                String[] tokens = s.split(",");

                code = tokens[0];

                if (tokens[1].equals("Critical")) {
                    severity = ViolationSeverity.CRITICAL;
                } else {
                    severity = ViolationSeverity.NON_CRITICAL;
                }

                description = tokens[2];

                Violation violation = new Violation(description, severity, code);
                //Log.e("Violation OBJECT", "getViolationsFromString: " + violation);

                list.add((violation));

            }
        }
        return list;
    }


    private void setRestaurantData(Boolean updateAvailable, File newRestaurants) {
        if (updateAvailable) {
            // Common pattern to process large file
            try (BufferedReader reader = new BufferedReader(new FileReader(newRestaurants))) {
                restaurantManager.emptyRestaurants();
                String line;

                reader.readLine();
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    Restaurant newRestaurant = new Restaurant();
                    newRestaurant.setTrackingNumber(tokens[0]);
                    Log.i("myRestaurantName", tokens[1]);

                    /*sampleRestaurant.setName(tokens[1]);
                    sampleRestaurant.setPhysicalAddress(tokens[2]);
                    sampleRestaurant.setPhysicalCity(tokens[3]);
                    sampleRestaurant.setFactType(tokens[4]);
                    if (tokens[5].length() > 0) {
                    sampleRestaurant.setLatitude(tokens[5]);
                    } else {
                        sampleRestaurant.setLatitude("0");
                    }
                    if (tokens[6].length() > 0) {
                        sampleRestaurant.setAltitude(tokens[6]);
                    } else {
                        sampleRestaurant.setAltitude("0");
                    }*/
                    if (tokens.length == 7) {
                        newRestaurant.setName(tokens[1]);
                        newRestaurant.setPhysicalAddress(tokens[2]);
                        newRestaurant.setPhysicalCity(tokens[3]);
                        newRestaurant.setFactType(tokens[4]);
                        newRestaurant.setLatitude(Double.parseDouble(tokens[5]));
                        newRestaurant.setLongitude(Double.parseDouble(tokens[6]));
                    } else {
                        tokens[1] = tokens[1] + ", " + tokens[2];
                        newRestaurant.setName(tokens[1].substring(1, tokens[1].length() - 1));
                        newRestaurant.setPhysicalAddress(tokens[3]);
                        newRestaurant.setPhysicalCity(tokens[4]);
                        newRestaurant.setFactType(tokens[5]);
                        newRestaurant.setLatitude(Double.parseDouble(tokens[6]));
                        newRestaurant.setLongitude(Double.parseDouble(tokens[7]));
                    }


                    restaurantManager.addRestaurant(newRestaurant);

                    //Log.d("RestaurantList", "Just created: " + sampleRestaurant);

                }
                Log.i("myRestaurantLength", ": " + restaurantManager.getRestaurants().size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } /*else if (LOAD) {
            //load();
        }*/ else if (!loadedFromSave) {
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
                    Log.i("myRestaurantName", tokens[1]);
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
                        sampleRestaurant.setLongitude(Double.parseDouble(tokens[6]));
                    } else {
                        sampleRestaurant.setLongitude(0);
                    }
                    restaurantManager.addRestaurant(sampleRestaurant);

                    Log.d("RestaurantList", "Just created: " + sampleRestaurant);

                }
            } catch (IOException e) {
                Log.wtf("RestaurantList", "Error reading data file on line " + line, e);
                e.printStackTrace();
            }
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

    public void onClickFAB(View view) {
        startActivity(MapsActivity.makeIntent(getApplicationContext(), BackFrom.RestaurantList)
        );
    }

    private class MyListAdapter extends ArrayAdapter<Restaurant> {
        public MyListAdapter() {
            super(RestaurantList.this, R.layout.restaurant_view, restaurantManager.getMarkerRestaurants());
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
            Restaurant currentRestaurant = restaurantManager.getRestaurantMarkerIndex(position);
            List<Inspection> inspectionsForCurrentRestaurant = restaurantManager.getInspectionsForMarkerRestaurant(position);

            boolean isFav = restaurantManager.getFavTrackingNumList().contains(currentRestaurant.getTrackingNumber().replaceAll("\"",""));
            // Set Favorite
            if (isFav) {
                itemView.setBackgroundColor(Color.parseColor("#ffffcc"));
            } else {
                itemView.setBackgroundColor(Color.parseColor("#2D131212"));
            }

            // Fill restaurant icon
            ImageView restaurantView = (ImageView) itemView.findViewById(R.id.restaurant_icon);
            String currentRestaurantName = currentRestaurant.getName();
            if (currentRestaurantName.replaceAll(" ", "").contains("A&W")) {
                restaurantView.setImageResource(R.mipmap.aw_icon);
            } else if (currentRestaurantName.contains("7-Eleven")) {
                restaurantView.setImageResource(R.mipmap.seven_eleven_icon);
            } else if (currentRestaurantName.contains("McDonald")) {
                restaurantView.setImageResource(R.mipmap.mconald_icon);
            } else if (currentRestaurantName.contains("Blenz")) {
                restaurantView.setImageResource(R.mipmap.blenz_icon);
            } else if (currentRestaurantName.contains("Boston Pizza")) {
                restaurantView.setImageResource(R.mipmap.boston_pizza_icon);
            } else if (currentRestaurantName.contains("Domino")) {
                restaurantView.setImageResource(R.mipmap.domino_pizza_icon);
            } else if (currentRestaurantName.contains("Freshslice")) {
                restaurantView.setImageResource(R.mipmap.freshslice_pizza_icon);
            } else if (currentRestaurantName.contains("KFC")) {
                restaurantView.setImageResource(R.mipmap.kfc_icon);
            } else if (currentRestaurantName.contains("Wendy's")) {
                restaurantView.setImageResource(R.mipmap.wendys_icon);
            } else if (currentRestaurantName.contains("Tim Horton")) {
                restaurantView.setImageResource(R.mipmap.tim_hortons_icon);
            } else if (currentRestaurantName.contains("Starbucks")) {
                restaurantView.setImageResource(R.mipmap.starbuck_icon);
            } else if (currentRestaurantName.contains("Subway")) {
                restaurantView.setImageResource(R.mipmap.subway_icon);
            } else if (currentRestaurantName.contains("Pizza")) {
                restaurantView.setImageResource(R.drawable.icon_pizza);
            } else if (currentRestaurantName.contains("Sushi")
                    || currentRestaurantName.contains("Japanese")) {
                restaurantView.setImageResource(R.drawable.icon_tuna);
            } else if (currentRestaurantName.endsWith("Pub")
                    || currentRestaurantName.contains("Beer")) {
                restaurantView.setImageResource(R.drawable.icon_beer);
            } else if (currentRestaurantName.contains("Grill")
                    || currentRestaurantName.contains("BBQ")
                    || currentRestaurant.getName().contains("Chicken")) {
                restaurantView.setImageResource(R.drawable.icon_chicken);
            } else if (currentRestaurantName.contains("Pho")
                    || currentRestaurantName.contains("Thai")
                    || currentRestaurantName.contains("Asia")
                    || currentRestaurant.getName().contains("Chinese")) {
                restaurantView.setImageResource(R.drawable.icon_chinese_food);
            } else if (currentRestaurantName.contains("Burger")) {
                restaurantView.setImageResource(R.drawable.icon_hamburgers);
            } else if (currentRestaurantName.contains("Coffee")
                    || currentRestaurantName.contains("Cafe")) {
                restaurantView.setImageResource(R.drawable.icon_coffee);
            } else if (currentRestaurantName.contains("Indian")) {
                restaurantView.setImageResource(R.drawable.icon_indian_food);
            } else if (currentRestaurantName.contains("Korea")) {
                restaurantView.setImageResource(R.drawable.icon_korean_food);
            } else if (currentRestaurantName.contains("Fish")) {
                restaurantView.setImageResource(R.drawable.icon_fish_n_chips);
            } else if (currentRestaurantName.contains("Bubble")) {
                restaurantView.setImageResource(R.drawable.icon_bubble_tea);
            } else {
                restaurantView.setImageResource(R.drawable.icon_restaurant);
            }

            // Fill restaurant name
            TextView restaurantName = (TextView) itemView.findViewById(R.id.RestaurantDetails_text_restaurant_name);
            restaurantName.setText(currentRestaurantName);
            restaurantName.setTextColor(Color.BLUE);
            restaurantName.setSelected(true);

            // Fill issues
            TextView restaurantCriticalIssues = (TextView) itemView.findViewById(R.id.text_issues_found);
            int criticalIssues;
            if (!inspectionsForCurrentRestaurant.isEmpty()) {

                /*for (Inspection inspection : inspectionsForCurrentRestaurant)*/
                {
                    criticalIssues = inspectionsForCurrentRestaurant.get(0).getNumCritical();
                    restaurantCriticalIssues.setText("Critical issues: " + criticalIssues);

                    //break;
                }
            } else {
                restaurantCriticalIssues.setText("Critical issues: 0");
            }

            // Fill hazard icon and text
            ImageView RestaurantHazard = (ImageView) itemView.findViewById(R.id.hazard_icon);
            TextView txtRestaurantHazard = (TextView) itemView.findViewById(R.id.text_hazard_level);
            if (!inspectionsForCurrentRestaurant.isEmpty()) {

                if (isFav) {
                    txtRestaurantHazard.setBackgroundColor(Color.parseColor("#2D131212"));
                } else {
                    txtRestaurantHazard.setBackgroundColor(Color.TRANSPARENT);
                }

                //Log.e("hazard loop", "getView: " + inspectionsForCurrentRestaurant.toString());
                /*for (Inspection inspection : inspectionsForCurrentRestaurant)*/
                {
                    HazardRating hazard = inspectionsForCurrentRestaurant.get(0).getHazardRating();
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

//                    break;
                }
            } else {
                RestaurantHazard.setImageResource(R.mipmap.green_hazard);
                txtRestaurantHazard.setText("" + HazardRating.LOW);
                txtRestaurantHazard.setTextColor(Color.GREEN);
            }


            // Fill inspection date
            TextView restaurantDate = (TextView) itemView.findViewById(R.id.text_inspection_date);
            if (!inspectionsForCurrentRestaurant.isEmpty()) {
                /*for (Inspection inspection : inspectionsForCurrentRestaurant)*/
                {

                    Date inspectionDate = inspectionsForCurrentRestaurant.get(0).getDate();   // Inspection date
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

                    //break;
                }
            } else {
                restaurantDate.setText("No inspections found");
            }

            return itemView;
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Would you like to exit?")
                .setPositiveButton("Save and exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //restaurantManager.updateFavTrackingNumList();

                        Log.e(TAG, "onBackPressed: " + restaurantManager.getFavTrackingNumList());

                        restaurantManager.updateFavInspectionNumMap();
                        save();
                        finishAffinity();
                        System.exit(0);
                    }
                })
                /*.setNeutralButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(RestaurantList.this, MapsActivity.class));
                    }
                })*/
                .setNegativeButton("Cancel", null)
                .show();

    }

    public void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(this.openFileOutput("save", Context.MODE_PRIVATE));
            oos.writeObject(restaurantManager);
            oos.writeLong(lastUpdatedTimeInMilliseconds);
            oos.writeLong(lastModifiedTimeInMilliseconds);
            Log.e(TAG, "save: done");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        populateListView();
    }
}