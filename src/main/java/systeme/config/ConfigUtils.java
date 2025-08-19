package systeme.config;

import java.io.*;
import java.util.Properties;

public class ConfigUtils {

    /////////// Charger les propriétés par défaut /////////////////////////////
    static Properties loadDefaultProperties() {

        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config/default_system_config.properties");
                FileInputStream fis2 = new FileInputStream("config/default_gameplay_config.properties")) {
            properties.load(fis);
            properties.load(fis2);
        } catch (IOException e) {
            System.err.println("Error loading properties file: " + e.getMessage());
        }
        return properties;
    }

    ////////////// Lire les valeurs des propriétés //////////////////////////
    static String getString(Properties props, String key, String defaultValue) {
        return props.getProperty(key, defaultValue);
    }

    static int getInt(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    static boolean getBoolean(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        return value != null ? Boolean.parseBoolean(value) : defaultValue;
    }


    static float getFloat(Properties props, String key, float defaultValue) {
        String value = props.getProperty(key);
        if (value == null) return defaultValue;

        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    //////////////// Modifier et Enregistrer les propriétés //////////////////////////
    static void saveProperties(Properties props, File file, String comment) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, comment);
            System.out.println("Hardware config saved to: " + file);
        } catch (IOException e) {
            System.err.println("I/O error saving config: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security policy prevents config file creation");
        }
    }

    static void setProperty(Properties props, String key, Object value) {
        props.setProperty(key, String.valueOf(value));
    }

    static Properties loadUserProperties(File file) {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("Error loading user properties file: " + e.getMessage());
        }
        return properties;
    }
}