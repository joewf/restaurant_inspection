package com.example.project_1.Model;

public class Violation {
    private String description;
    private ViolationNature nature;
    private ViolationSeverity severity;
    private int code;

    public Violation(String description, ViolationSeverity severity, int code) {
        this.description = description;
        this.severity = severity;
        this.code = code;
    }
}
