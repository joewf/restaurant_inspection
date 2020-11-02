package com.example.project_1.Model;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RestaurantManager {
    public static List<Restaurant> restaurants = new ArrayList<>();
    private static RestaurantManager instance;

    // Return restaurant for MyListAdapter
    public List<Restaurant> getRestaurants() {
        return restaurants;
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

    public void add(Restaurant restaurant) {
        restaurants.add(restaurant);
    }

    public void remove(Restaurant restaurant) {
        restaurants.remove(restaurant);
    }

    public Restaurant get(int index) {
        return restaurants.get(index);
    }

    /*@Override
    public Iterator<Restaurant> iterator() {
        return restaurants.iterator();
    }*/
}
