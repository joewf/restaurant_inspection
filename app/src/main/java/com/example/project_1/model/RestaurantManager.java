package com.example.project_1.model;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * RestaurantManager class manages a list of Restaurant Objects.
 */
public class RestaurantManager implements Iterable<Restaurant>, Serializable {
    private static final String TAG = RestaurantManager.class.getSimpleName();


    private List<Restaurant> restaurants = new ArrayList<>();
    private List<Inspection> inspections = new ArrayList<>();
    private List<Restaurant> favRestaurants = new ArrayList<>();
    private List<Restaurant> markerRestaurants = new ArrayList<>();
    private List<String> favTrackingNumList = new ArrayList<>();
    private static RestaurantManager instance;
    private HashMap<String, Integer> favMap = new HashMap<>();
    private static boolean mapInitialized = false;

    // Return restaurant for MyListAdapter
    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    public List<Restaurant> getMarkerRestaurants() {
        return markerRestaurants;
    }

    private RestaurantManager() {

        // Initialize hash map
        initHashMap();
    }

    private void initHashMap() {
        for (Restaurant current : favRestaurants) {
            favMap.put(current.getTrackingNumber(), 0);
        }
    }

    public static RestaurantManager getInstance() {
        if (instance == null) {
            instance = new RestaurantManager();
        }
        return instance;
    }

    public void setMarkerRestaurants(List<Restaurant> markerRestaurants) {
        this.markerRestaurants = markerRestaurants;
    }

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public void setInspections(List<Inspection> inspections) {
        this.inspections = inspections;
    }

    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
    }

    public void addFavRestaurant(Restaurant restaurant) {
        favRestaurants.add(restaurant);
        favTrackingNumList.add(restaurant.getTrackingNumber().replaceAll("\"", ""));
    }

    public void addMarkerRestaurant(Restaurant restaurant) {
        markerRestaurants.add(restaurant);
    }

    public void removeMarkerRestaurant(Restaurant restaurant) {
        markerRestaurants.remove(restaurant);
    }


    public void removeFavRestaurant(Restaurant restaurant) {
        favRestaurants.remove(restaurant);
        favTrackingNumList.remove(restaurant.getTrackingNumber().replaceAll("\"", ""));
    }

    public void addInspection(Inspection inspection) {
        inspections.add(inspection);
    }

    public Restaurant get(int index) {
        return restaurants.get(index);
    }

    public Restaurant getRestaurantMarkerIndex(int index) {
        return markerRestaurants.get(index);
    }

    public List<Violation> getViolations(int restaurantIndex, int inspectionIndex) {
        return getInspectionsForRestaurant(restaurantIndex).get(inspectionIndex).getViolations();
    }

    @Override
    public Iterator<Restaurant> iterator() {
        return restaurants.iterator();
    }

    public ArrayList<Inspection> getInspectionsForRestaurant(int position) {
        ArrayList<Inspection> list = new ArrayList<>();
        Restaurant currentRestaurant = restaurants.get(position);

        for (Inspection inspection : inspections) {
            if (currentRestaurant.getTrackingNumber().equals(inspection.getTrackingNumber())) {
                list.add(inspection);
            }
        }

        return list;
    }


    public ArrayList<Inspection> getInspectionsForMarkerRestaurant(int position) {
        ArrayList<Inspection> list = new ArrayList<>();
        Restaurant currentRestaurant = markerRestaurants.get(position);

        for (Inspection inspection : inspections) {
            if (currentRestaurant.getTrackingNumber().equals(inspection.getTrackingNumber())) {
                list.add(inspection);
            }
        }

        return list;
    }

    public void sortRestaurantList() {
        Collections.sort(restaurants, new Comparator<Restaurant>() {
            @Override
            // Sort ascendant order
            public int compare(Restaurant R1, Restaurant R2) {
                return R1.getName().compareTo(R2.getName());
            }
        });
    }

    public void sortInspectionDate() {
        Collections.sort(inspections, new Comparator<Inspection>() {
            @Override
            // Sort descendant order
            public int compare(Inspection I1, Inspection I2) {
                return I2.getDate().compareTo(I1.getDate());
            }
        });
    }

    public Inspection getMostRecentInspection(Restaurant restaurant) {
        String trackingNum = restaurant.getTrackingNumber().replaceAll("\"", "");

        for (Inspection inspection : inspections) {
            if (trackingNum.equals(inspection.getTrackingNumber().replaceAll("\"", ""))) {
                return inspection;
            }
        }

        return null;
    }

    @Override
    public String toString() {
        return "RestaurantManager{" +
                "restaurants=" + restaurants +
                ", inspections=" + inspections +
                '}';
    }

    public HashMap<String, Integer> getFavMap() {
        return favMap;
    }

    public void setFavMap(HashMap<String, Integer> favMap) {
        this.favMap = favMap;
    }

    public int getIndexFromLatLng(double latitude, double longitude) {

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant current = restaurants.get(i);
            double currentLatitude = current.getLatitude();
            double currentLongitude = current.getLongitude();
            if (currentLatitude == latitude && currentLongitude == longitude) {
                return i;
            }
        }

        return -1;
    }

    public int getIndexFromTrackingNumber(String tracking) {

        for (int i = 0; i < restaurants.size(); i++) {
            Restaurant current = restaurants.get(i);
            if (current.getTrackingNumber().equals(tracking)) {
                return i;
            }
        }

        return -1;
    }

    public void emptyRestaurants() {
        restaurants.clear();
    }

    public void emptyInspections() {
        inspections.clear();
    }

    public void emptyMarkerRestaurants() {markerRestaurants.clear(); }

    public void emptyFav() {
        favRestaurants.clear();
    }

    public boolean isFavorite(Restaurant restaurant) {
        return favRestaurants.contains(restaurant);
    }

    public ArrayList<Restaurant> getFavRestaurants() {
        return (ArrayList<Restaurant>) favRestaurants;
    }

    public void setFavRestaurants(List<Restaurant> favRestaurants) {
        this.favRestaurants = favRestaurants;
    }

    public void setFavTrackingNumList(List<String> favTrackingNumList) {
        this.favTrackingNumList = favTrackingNumList;
    }

    public List<String> getFavTrackingNumList() {
        return favTrackingNumList;
    }

    public void updateFavTrackingNumList() {
        favTrackingNumList.addAll(favMap.keySet());
    }

    public void updateFavInspectionNumMap() {
        //updateFavList();
        ArrayList<Inspection> list = new ArrayList<>();

        for (Restaurant currentRestaurant : favRestaurants) {
            int count = 0;
            for (Inspection inspection : inspections) {
                if (currentRestaurant.getTrackingNumber().replaceAll("\"", "").equals(inspection.getTrackingNumber().replaceAll("\"", ""))) {
                    count++;
                }
            }
            favMap.put(currentRestaurant.getTrackingNumber().replaceAll("\"", ""), count);
        }
    }

    public void updateFavList() {
        Log.e(TAG, "updateFavList: " + favTrackingNumList);
        Log.e(TAG, "updateFavList: " + restaurants);

        favRestaurants.clear();

        for (String currentTN : favTrackingNumList) {
            for (Restaurant current : restaurants) {
                if (current.getTrackingNumber().replaceAll("\"", "").equals(currentTN.replaceAll("\"", ""))) {

                    Log.e(TAG, "updateFavList: got one");

                    favRestaurants.add(current);
                }
            }
        }
    }

    public ArrayList<Restaurant> getFavRestaurantWithNewInspection(HashMap<String, Integer> restaurantIntegerHashMap) {
        updateFavInspectionNumMap();
        ArrayList<Restaurant> list = new ArrayList<>();

        for (Restaurant current : favRestaurants) {
            String trackingNum = current.getTrackingNumber().replaceAll("\"", "");
            if (restaurantIntegerHashMap.get(trackingNum) < (favMap.get(trackingNum))) {
                list.add(current);
            }
        }

        return list;
    }

}
