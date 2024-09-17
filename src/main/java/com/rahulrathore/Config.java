package com.rahulrathore;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Config {
    public  static   String userPropertiesPath ;
    public  static   String keystorePath ;
    public  static   String configPropertiesPath ;
     public  static  int port ;

    public static InputStream loadResourceOrExternalFile(String filePath) throws IOException {
        // Check if file exists in external path
        File externalFile = new File(filePath);
        if (externalFile.exists()) {
            return new FileInputStream(externalFile);
        }

        // If not, load from resources folder
        InputStream resourceStream = DemoApplication.class.getClassLoader().getResourceAsStream(filePath);
        if (resourceStream == null) {
            throw new IOException("File not found: " + filePath);
        }
        return resourceStream;
    }



}
