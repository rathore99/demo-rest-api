package com.rahulrathore.model;

import org.json.JSONObject;

public class Country {
    private String code;
    private String name;
    private String capital;
    private long population;

    public Country(String code, String name, String capital, long population) {
        this.code = code;
        this.name = name;
        this.capital = capital;
        this.population = population;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCapital() {
        return capital;
    }

    public long getPopulation() {
        return population;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("name", name);
        json.put("capital", capital);
        json.put("population", population);
        return json;
    }
}

