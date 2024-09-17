package com.rahulrathore.controller;

import com.rahulrathore.util.JwtUtil;
import com.rahulrathore.util.PasswordEncoderUtil;
import com.rahulrathore.util.PropertiesUtil;
import fi.iki.elonen.NanoHTTPD;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    public NanoHTTPD.Response handle(NanoHTTPD.IHTTPSession session) throws NanoHTTPD.ResponseException, IOException {
        // Ensure the request is a POST request
        if (!NanoHTTPD.Method.POST.equals(session.getMethod())) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED, "application/json", "{\"error\":\"Only POST requests are allowed\"}");
        }

        // Check for the correct Content-Type
        String contentType = session.getHeaders().get("content-type");
        if (contentType == null || !contentType.equals("application/json")) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.UNSUPPORTED_MEDIA_TYPE, "application/json", "{\"error\":\"Unsupported content type\"}");
        }

        // Check for the Authorization header
        String authHeader = session.getHeaders().get("authorization");
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.UNAUTHORIZED, "application/json", "{\"error\":\"Missing or invalid Authorization header\"}");
        }

        // Decode the Basic Auth credentials
        String base64Credentials = authHeader.substring(6);
        String credentials = new String(Base64.getDecoder().decode(base64Credentials), StandardCharsets.UTF_8);
        String[] values = credentials.split(":", 2);
        if (values.length != 2) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.UNAUTHORIZED, "application/json", "{\"error\":\"Invalid Authorization header format\"}");
        }

        String username = values[0];
        String password = values[1];

        // Validate the username and password
        String encodedPassword = PropertiesUtil.getProperty(username);
        System.out.println(encodedPassword);
        System.out.println("password received: "+password);
        if (encodedPassword == null || !PasswordEncoderUtil.matches(password, encodedPassword)) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.UNAUTHORIZED, "application/json", "{\"error\":\"Invalid credentials\"}");
        }

        // Parse the JSON request body
        Map<String, String> body= new HashMap<>();
        session.parseBody(body);

        String jsonString = body.get("postData");
        JSONObject jsonRequest;
        try {
            jsonRequest = new JSONObject(jsonString);
        } catch (Exception e) {
            System.out.println("Inside exception "+ e);
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Invalid JSON\"}");
        }

        // Check for the grant type
        String grantType = jsonRequest.optString("grant", "");
        if (!"client_credentials".equals(grantType)) {
            return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.BAD_REQUEST, "application/json", "{\"error\":\"Unsupported grant type\"}");
        }

        // Generate JWT token
        String token = JwtUtil.generateToken(username);
        JSONObject response = new JSONObject();
        response.put("token", token);
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", response.toString());
    }
}
