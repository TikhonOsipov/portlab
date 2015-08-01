package com.tixon.portlab.app;

import java.util.ArrayList;

public class Array {
    private String name;
    private ArrayList<Double> values;
    private String valuesString;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Double> getValues() {
        return values;
    }

    public void setValues(ArrayList<Double> values) {
        this.values = values;
    }

    public String getValuesString() {
        return valuesString;
    }

    public void setValuesString(String valuesString) {
        this.valuesString = valuesString;
    }
}
