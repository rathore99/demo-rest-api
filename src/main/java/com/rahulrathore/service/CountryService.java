package com.rahulrathore.service;

import com.rahulrathore.model.Country;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CountryService {
    private final List<Country> countries = new ArrayList<>();

    public CountryService() {
        countries.add(new Country("US", "United States", "Washington, D.C.", 331002651));
        countries.add(new Country("CA", "Canada", "Ottawa", 37742154));
        countries.add(new Country("MX", "Mexico", "Mexico City", 128932753));


    }

    public List<Country> getCountries(String name, String capital) {
        return countries.stream()
                .filter(country -> (name == null || country.getName().toLowerCase().contains(name.toLowerCase())) &&
                        (capital == null || country.getCapital().toLowerCase().contains(capital.toLowerCase())))
                .collect(Collectors.toList());
    }

    public Country getCountryByCode(String code) {
        return countries.stream()
                .filter(country -> country.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElse(null);
    }
}
