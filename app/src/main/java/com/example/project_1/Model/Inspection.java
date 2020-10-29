package com.example.project_1.Model;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class Inspection {
    InspectionType type;
    HazardRating hazardRating;
    List<Violation> violationLump = new ArrayList<>();
    String trackingNumber;
    int numCritical;
    int numNonCritical;
    int hazardColor;
    int hazardIcon;
    GregorianCalendar date;
}
