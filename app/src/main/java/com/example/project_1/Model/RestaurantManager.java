package com.example.project_1.Model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RestaurantManager implements Iterable<Restaurant> {

    public static List<Restaurant> restaurants = new ArrayList<>();
    public static List<Inspection> inspections = new ArrayList<>();
    private static RestaurantManager instance;

    // Return restaurant for MyListAdapter
    public List<Restaurant> getRestaurants() {
        return restaurants;
    }

    public List<Inspection> getInspections() {
        return inspections;
    }

    /*
        Singleton support
     */
    private RestaurantManager() {
        // To prevent anyone else from instantiating
    }

    public static RestaurantManager getInstance() {
        if (instance == null) {
            instance = new RestaurantManager();
        }
        return instance;
    }

    public void addRestaurant(Restaurant restaurant) {
        restaurants.add(restaurant);
    }

    public void addInspection(Inspection inspection) {
        inspections.add(inspection);
    }


    public void removeRestaurant(Restaurant restaurant) {
        restaurants.remove(restaurant);
    }

    public Restaurant get(int index) {
        return restaurants.get(index);
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
}
