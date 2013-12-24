package dnanalytics.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * Stores permanent properties. Use this class to store settings in hard disk,
 * so they can be loaded in other sessions. Values are stored in a XML file
 * called 'Settings.xml' as Strings, so it is possible to edit it manually.
 * Settings also controls the Locale of the System, allowing to set or get it.
 *
 * @author Pascual Lorente Arencibia
 */
public class Settings {

    private static final Properties properties = new Properties();
    private final static String settingsFile = "Settings.xml";
    private final static Locale[] locales = {
        new Locale("es", "es"),
        new Locale("en", "us")};

    /**
     * Gets a property.
     *
     * @param key The key of the property.
     * @return The property or null if the key is not present in Settings.
     */
    public static String getProperty(String key) {
        try {
            // In case file doesn't exist, we create an empty xml
            File f = new File(settingsFile);
            if (!f.exists()) {
                f.createNewFile();
                properties.storeToXML(new FileOutputStream(f), null);
            }

            properties.loadFromXML(new FileInputStream(settingsFile));
            return properties.getProperty(key);


        } catch (IOException ex) {
            System.err.println(
                    "Settings file couldn't be created. Please, give root access to DNAnalytics");
        }
        return null;
    }

    /**
     * Stores a property with the given key. If the key already exists, the
     * value is overwritten. Forbidden keys are : <b>country, language and
     * genome</b>.
     *
     * @param key The key of the property.
     * @param value The new value for the property.
     */
    public static void setProperty(String key, String value) {
        try {
            properties.setProperty(key, value);
            properties.storeToXML(new FileOutputStream(settingsFile), null);
        } catch (IOException ex) {
            System.err.println(
                    "Settings file couldn't be created. Please, give root access to DNAnalytics");
        }
    }

    /**
     * Returns current Locale. Take into account that view Locale is loaded at
     * beginning of the application, so this value is not necessary the same.
     *
     * @return the locale of the Settings.
     */
    public static Locale getLocale() {
        String country = getProperty("country");
        String language = getProperty("language");
        return (country == null) ? Locale.getDefault() : new Locale(language,
                country);
    }

    /**
     * Sets the Settings locale.
     *
     * @param l The new Locale.
     */
    public static void setLocale(Locale l) {
        setProperty("country", l.getCountry());
        setProperty("language", l.getLanguage());
        System.out.println("Setting locale to: " + l);
    }

    /**
     * Gets an array with all the application locales.
     *
     * @return An array containing all the locales.
     */
    public static Locale[] getLocales() {
        return locales;
    }

    /**
     * As this is a frequently used setting, it has its own method. It stores
     * the genome with key <b>genome</b>.
     *
     * @param genome The new value for the genome.
     */
    public static void setGenome(String genome) {
        properties.setProperty("genome", genome);
    }

    /**
     * As genome is required by most Workers, it can be accessed from this
     * method.
     *
     * @return The value of the genome property.
     */
    public static File getGenome() {
        String name = properties.getProperty("genome");
        if (name != null) {
            File g = new File(name);
            return g.exists() ? g : null;
        }
        return null;
    }
}
