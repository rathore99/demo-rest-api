package com.rahulrathore.controller;

import com.rahulrathore.model.Country;
import com.rahulrathore.service.CountryService;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;

public class CountryDetailController {
    private CountryService countryService;

    public CountryDetailController(CountryService countryService) {
        this.countryService = countryService;
    }

    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        String uri = session.getUri();
        String[] pathSegments = uri.split("/");
        String countryCode = pathSegments[pathSegments.length - 1];

        Country country = countryService.getCountryByCode(countryCode);
        String response = (country != null) ? country.toJson().toString() : new JSONObject().put("error", "Country not found").toString();

        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", response);
    }
}
