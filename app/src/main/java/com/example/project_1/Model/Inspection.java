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

    private InspectionType type;
    private HazardRating hazardRating;
    private Date date;
    private String trackingNumber;
    private int numCritical;
    private int numNonCritical;
    private List<Violation> violations;


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
    }

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
    public Date getDate() {
        return date;
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

    @Override
    public String toString() {
        return "Inspection{" +
                ", date=" + date +
                ", type=" + type +
                ", hazardRating=" + hazardRating +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", numCritical=" + numCritical +
                ", numNonCritical=" + numNonCritical +
                '}';
    }


}
