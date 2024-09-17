package com.rahulrathore;

import com.rahulrathore.controller.CountryController;
import com.rahulrathore.controller.CountryDetailController;
import com.rahulrathore.controller.JsonResponseController;
import com.rahulrathore.controller.LoginController;
import com.rahulrathore.service.CountryService;
import com.rahulrathore.util.JwtUtil;
import fi.iki.elonen.NanoHTTPD;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

public class DemoApplication {

    private static CountryService countryService;
    private static NanoHTTPD server;

    public static void main(String[] args) throws IOException {
        int port = 8443; // Default port for HTTPS
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default port 8443.");
            }
        }

        countryService = new CountryService();

        // Outer loop to handle server restarts
        while (true) {
            try {
                // Initialize and start the server
                startServer(port);

                // Wait for server shutdown (this will block until an error occurs or the server is stopped)
                while (true) {
                    Thread.sleep(1000);  // Check server status periodically (1 second in this case)
                }

            } catch (Exception e) {
                System.err.println("Server encountered an error: " + e.getMessage());
                e.printStackTrace();

                // Close resources if an exception occurs
                stopServer();

                // Restart the server after a short delay
                try {
                    System.out.println("Restarting server in 5 seconds...");
                    Thread.sleep(5000); // Delay before restarting the server
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    // Method to initialize and start the NanoHTTPD server
    private static void startServer(int port) throws IOException {
        server = new NanoHTTPD(port) {
            @Override
            public Response serve(IHTTPSession session) {
                String uri = session.getUri();

                // Handle the login request
                if (uri.equals("/login")) {
                    try {
                        return new LoginController().handle(session);
                    } catch (ResponseException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                // Handle predefined JSON responses
                if (uri.equals("/api/predefined-response")) {
                    return new JsonResponseController("data/response.json").handle(session);
                }

                // Handle other API endpoints (e.g., /api/countries/)
                else if (uri.startsWith("/api/countries/")) {
                    if (!isAuthorized(session)) {
                        return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "application/json", "{\"error\":\"Unauthorized\"}");
                    }
                    return new CountryDetailController(countryService).handle(session);
                } else if (uri.startsWith("/api/countries")) {
                    if (!isAuthorized(session)) {
                        return newFixedLengthResponse(Response.Status.UNAUTHORIZED, "application/json", "{\"error\":\"Unauthorized\"}");
                    }
                    return new CountryController(countryService).handle(session);
                }

                return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json", "{\"error\":\"Not Found\"}");
            }

            private boolean isAuthorized(IHTTPSession session) {
                String authHeader = session.getHeaders().get("authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    return JwtUtil.isTokenValid(token);
                }
                return false;
            }
        };

        // Configure SSL for the server
        server.makeSecure(createSSLSocketFactory(), null);
        server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);

        System.out.println("Server started on port " + port + ". Waiting for requests...");
    }

    // Method to stop the NanoHTTPD server and release resources
    private static void stopServer() {
        if (server != null) {
            server.stop();
            System.out.println("Server stopped.");
        }
    }

    private static SSLServerSocketFactory createSSLSocketFactory() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream fis = new FileInputStream("src/main/resources/keystore.jks");
            keyStore.load(fis, "changeit".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "changeit".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext.getServerSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL socket factory", e);
        }
    }
}
