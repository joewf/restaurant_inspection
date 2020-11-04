package com.example.project_1.Model;

public class Violation {
    private String description;
    private ViolationNature nature;
    private ViolationSeverity severity;
    private String code;

    public Violation() {
    }

    public Violation(String description, ViolationSeverity severity, String code) {
        setNatureFromCode(code);
        this.description = description;
        this.severity = severity;
        this.code = code;
    }

    private void setNatureFromCode(String code) {
        char firstCh = code.charAt(0);
        if (code.equals("304") || code.equals("305")) {
            nature = ViolationNature.PEST;
        }

        switch (firstCh) {
            case '1':
                nature = ViolationNature.PERMIT;
                break;
            case '2':
                nature = ViolationNature.FOOD;
                break;
            case '3':
                nature = ViolationNature.EQUIPMENT;
                break;
            case '4':
            case '5':
                nature = ViolationNature.EMPLOYEE;
                break;
        }
    }

    public String getDescription() {
        return description;
    }

    public ViolationNature getNature() {
        return nature;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public String getCode() {
        return code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNature(ViolationNature nature) {
        this.nature = nature;
    }

    public void setSeverity(ViolationSeverity severity) {
        this.severity = severity;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
