package com.rahulrathore.controller;

import fi.iki.elonen.NanoHTTPD;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class JsonResponseController {

    private final String jsonFilePath;

    public JsonResponseController(String jsonFilePath) {
        this.jsonFilePath = jsonFilePath;
    }

    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) {
        // Ensure the request is a GET request
        if (!NanoHTTPD.Method.GET.equals(session.getMethod())) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "application/json", "{\"error\":\"Only GET requests are allowed\"}");
        }

        // Load JSON response from file
        try {
            String jsonResponse = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", jsonResponse);
        } catch (IOException e) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.INTERNAL_ERROR, "application/json", "{\"error\":\"Unable to load JSON response\"}");
        }
    }
}

