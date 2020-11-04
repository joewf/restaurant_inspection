package com.example.project_1.Model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Month;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Inspection {
    private List<Violation> violationLump = new ArrayList<>();

    private GregorianCalendar date;
    private InspectionType type;
    private HazardRating hazardRating;
    private String trackingNumber;
    private int numCritical;
    private int numNonCritical;
    private List<Violation> violations;

    public Inspection(String trackingNumber, int year, int month, int dayOfMonth, InspectionType type, int numCritical,
                      int numNonCritical, HazardRating hazardRating, List<Violation> violations) {

        setDate(year, month, dayOfMonth);
        this.trackingNumber = trackingNumber;
        this.type = type;
        this.numCritical = numCritical;
        this.numNonCritical = numNonCritical;
        this.hazardRating = hazardRating;
        this.violations = violations;
    }

    public void setDate(int year, int month, int dayOfMonth) {
        date = new GregorianCalendar(year, month, dayOfMonth);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDateString() {
        return Month.values()[(date.get(Calendar.MONTH))] + " " + date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String toString() {
        return "Inspection{" +
                "violationLump=" + violationLump +
                ", date=" + getDateString() +
                ", type=" + type +
                ", hazardRating=" + hazardRating +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", numCritical=" + numCritical +
                ", numNonCritical=" + numNonCritical +
                ", violations=" + violations +
                '}';
    }

    enum Month {
        January, February, March, April, May, June, July, August, September, October, November, December
    }
}
