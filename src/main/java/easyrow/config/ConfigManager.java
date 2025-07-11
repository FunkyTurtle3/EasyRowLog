package easyrow.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    public static void init() {
        File configFile = new File("config.properties");
        Properties config = new Properties();

        if (!configFile.exists()) {
            // Nur beim ersten Start: Datei mit Default-Werten erstellen
            config.setProperty("user.language", "en");
            config.setProperty("theme", "dark");

            try (FileOutputStream output = new FileOutputStream(configFile)) {
                config.store(output, "Standard App-Einstellungen");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static String readConfig(String key) {
        Properties config = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            config.load(input);
            return config.getProperty(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "This configuration doesn't exist";
    }

    public static void editProperty(String key, String entry) {
        Properties config = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            config.load(input);
            config.setProperty(key, entry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
