package com.example.project_1.Model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.time.Month;
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
    private int hazardColor;
    private int hazardIcon;

    public Inspection(int year, int month, int dayOfMonth, InspectionType type, HazardRating hazardRating, String trackingNumber, int numCritical, int numNonCritical, int hazardColor, int hazardIcon) {
        setDate(year, month, dayOfMonth);
        this.type = type;
        this.hazardRating = hazardRating;
        this.trackingNumber = trackingNumber;
        this.numCritical = numCritical;
        this.numNonCritical = numNonCritical;
        this.hazardColor = hazardColor;
        this.hazardIcon = hazardIcon;
    }

    public void setDate(int year, int month, int dayOfMonth) {
        date = new GregorianCalendar(year, month, dayOfMonth);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDateString() {
        return Month.of(date.get(Calendar.MONTH)) + " " + date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR);
    }

    public InspectionType getType() {
        return type;
    }

    public HazardRating getHazardRating() {
        return hazardRating;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public int getNumCritical() {
        return numCritical;
    }

    public int getNumNonCritical() {
        return numNonCritical;
    }

    public int getHazardColor() {
        return hazardColor;
    }

    public int getHazardIcon() {
        return hazardIcon;
    }

    public List<Violation> getViolationLump() {
        return violationLump;
    }

    public void setViolationLump(List<Violation> violationLump) {
        this.violationLump = violationLump;
    }

    public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }

    public void setType(InspectionType type) {
        this.type = type;
    }

    public void setHazardRating(HazardRating hazardRating) {
        this.hazardRating = hazardRating;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setNumCritical(int numCritical) {
        this.numCritical = numCritical;
    }

    public void setNumNonCritical(int numNonCritical) {
        this.numNonCritical = numNonCritical;
    }

    public void setHazardColor(int hazardColor) {
        this.hazardColor = hazardColor;
    }

    public void setHazardIcon(int hazardIcon) {
        this.hazardIcon = hazardIcon;
    }

    @Override
    public String toString() {
        return "Inspection{" +
                "violationLump=" + violationLump +
                ", date=" + date +
                ", type=" + type +
                ", hazardRating=" + hazardRating +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", numCritical=" + numCritical +
                ", numNonCritical=" + numNonCritical +
                ", hazardColor=" + hazardColor +
                ", hazardIcon=" + hazardIcon +
                '}';
    }
}
