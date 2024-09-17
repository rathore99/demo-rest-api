package com.rahulrathore.controller;

import com.rahulrathore.model.Country;
import com.rahulrathore.service.CountryService;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;

import java.util.List;
import java.util.Map;

public class CountryController {
    private CountryService countryService;

    public CountryController(CountryService countryService) {
        this.countryService = countryService;
    }

    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        Map<String, String> params = session.getParms();
        String name = params.get("name");
        String capital = params.get("capital");

        List<Country> countries = countryService.getCountries(name, capital);
        JSONArray response = new JSONArray();
        for (Country country : countries) {
            response.put(country.toJson());
        }

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", response.toString());
    }
}

