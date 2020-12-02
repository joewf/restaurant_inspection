package com.example.project_1.model;

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

    private List<Restaurant> restaurants = new ArrayList<>();
    private List<Inspection> inspections = new ArrayList<>();
    private List<Restaurant> favRestaurants = new ArrayList<>();
    private static RestaurantManager instance;
    private final HashMap<Restaurant, Integer> favInspectionNumMap = new HashMap<>();

    // Return restaurant for MyListAdapter
    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    private RestaurantManager() {

        // Initialize hash map
        for (Restaurant current: favRestaurants) {
            favInspectionNumMap.put(current, 0);
        }
    }

    public static RestaurantManager getInstance() {
        if (instance == null) {
            instance = new RestaurantManager();
        }
        return instance;
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
    }

    public void removeFavRestaurant(Restaurant restaurant) {
        favRestaurants.remove(restaurant);
    }

    public void addInspection(Inspection inspection) {
        inspections.add(inspection);
    }

    public Restaurant get(int index) {
        return restaurants.get(index);
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

    public void initFavInspectionNumMap() {
        ArrayList<Inspection> list = new ArrayList<>();

        for (Restaurant currentRestaurant : favRestaurants) {
            int count = 0;
            for (Inspection inspection : inspections) {
                if (currentRestaurant.getTrackingNumber().equals(inspection.getTrackingNumber())) {
                    count++;
                }
            }
            favInspectionNumMap.put(currentRestaurant, count);
        }
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

    @Override
    public String toString() {
        return "RestaurantManager{" +
                "restaurants=" + restaurants +
                ", inspections=" + inspections +
                '}';
    }

    public HashMap<Restaurant, Integer> getFavInspectionNumMap() {
        return favInspectionNumMap;
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

    public boolean isFavorite(Restaurant restaurant) {
        return favRestaurants.contains(restaurant);
    }
}
