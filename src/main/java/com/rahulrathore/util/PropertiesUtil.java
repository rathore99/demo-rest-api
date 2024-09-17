package com.rahulrathore.util;

import com.rahulrathore.Config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.rahulrathore.Config.loadResourceOrExternalFile;

public class PropertiesUtil {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = loadResourceOrExternalFile(Config.userPropertiesPath)) {
            if (input == null) {
                System.out.println("Sorry, unable to find users.properties");
            } else {
                properties.load(input);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
