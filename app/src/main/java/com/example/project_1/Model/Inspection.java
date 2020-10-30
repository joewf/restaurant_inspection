package com.example.project_1.Model;

import java.util.ArrayList;
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
}
