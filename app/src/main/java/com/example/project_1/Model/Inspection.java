package com.example.project_1.Model;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Inspection {
    //private List<Violation> violationLump = new ArrayList<>();

    private InspectionType type;
    private HazardRating hazardRating;
    private Date date;
    private String trackingNumber;
    private int numCritical;
    private int numNonCritical;
    private int hazardColor;
    private int hazardIcon;
    private List<Violation> violations;


    //int year, int month, int dayOfMonth,
    //int hazardColor, int hazardIcon

    public Inspection() {
        this.trackingNumber = null;
        this.date = null;
        this.type = null;
        this.numCritical = 0;
        this.numNonCritical = 0;
        this.hazardRating = null;
        this.violations = null;
    }

    public Inspection(String trackingNumber, Date date, InspectionType type, int numCritical,
                      int numNonCritical, HazardRating hazardRating, List<Violation> violations) {


        this.trackingNumber = trackingNumber;
        this.date = date;
        this.type = type;
        this.numCritical = numCritical;
        this.numNonCritical = numNonCritical;
        this.hazardRating = hazardRating;
        this.violations = violations;
        //this.hazardColor = hazardColor;
        //this.hazardIcon = hazardIcon;
    }

    /*public void setDate(int year, int month, int dayOfMonth) {
        date = new GregorianCalendar(year, month, dayOfMonth);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getDateString() {
        return Month.of(date.get(Calendar.MONTH)) + " " + date.get(Calendar.DAY_OF_MONTH) + ", " + date.get(Calendar.YEAR);
    }*/

    public List<Violation> getViolations() {
        return violations;
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

    public Date getDate() {
        return date;
    }

    /*public GregorianCalendar getDate() {
        return date;
    }

    public void setDate(GregorianCalendar date) {
        this.date = date;
    }*/

    public void setType(InspectionType type) {
        this.type = type;
    }

    public void setHazardRating(HazardRating hazardRating) {
        this.hazardRating = hazardRating;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setNumCritical(int numCritical) {
        this.numCritical = numCritical;
    }

    public void setNumNonCritical(int numNonCritical) {
        this.numNonCritical = numNonCritical;
    }

    public void setViolations(List<Violation> violations) {
        this.violations = violations;
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
