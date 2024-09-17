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
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import static com.rahulrathore.Config.*;

public class DemoApplication {

    private static CountryService countryService;
    private static NanoHTTPD server;

    public static void main(String[] args) {
        int port = 8443; // Default port for HTTPS

        // Get file paths from command-line arguments or use default values from resources folder
         userPropertiesPath = getFilePathOrDefault(args, 1, "users.properties");
         keystorePath = getFilePathOrDefault(args, 2, "keystore.jks");
         configPropertiesPath = getFilePathOrDefault(args, 3, "config.properties");
        System.out.println("Using keystore path: " + keystorePath);
        System.out.println("Using user properties path: " + userPropertiesPath);
        System.out.println("Using config properties path: " + configPropertiesPath);

        // Initialize country service with config properties file
        countryService = new CountryService();

        // Outer loop to handle server restarts
        while (true) {
            try {
                // Initialize and start the server
                startServer(port, keystorePath);

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
    private static void startServer(int port, String keystorePath) throws IOException {
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
        server.makeSecure(createSSLSocketFactory(keystorePath), null);
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

    private static SSLServerSocketFactory createSSLSocketFactory(String keystorePath) {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            InputStream keystoreStream = loadResourceOrExternalFile(keystorePath);

            if (keystoreStream == null) {
                throw new RuntimeException("Keystore file not found");
            }

            keyStore.load(keystoreStream, "changeit".toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, "changeit".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

            return sslContext.getServerSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL socket factory", e);
        }
    }

    // Helper method to get file path from args or use default resource path
    private static String getFilePathOrDefault(String[] args, int index, String defaultFileName) {
        if (args.length > index && args[index] != null && !args[index].isEmpty()) {
            return args[index];
        }
        return defaultFileName;
    }

    // Helper method to load a file either from the resources folder or from an external path
}
