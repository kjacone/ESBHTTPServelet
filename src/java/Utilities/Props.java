/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author mutura
 */
public final class Props {

    private transient Properties props;

    /**
     * A list of any errors that occurred while loading the properties.
     */
    private transient List<String> loadErrors;

    private transient final String error1 = "ERROR: %s is <= 0 or may not have been set";
    private transient final String error2 = "ERROR: %s may not have been set";
    private static final String PROPS_FILE = System.getenv("JBOSS_HOME") + "/external_configs/configs.properties";
    private transient String databaseContextURL;
    private transient String logsPath;

    /**
     * Instantiates a new Props.
     */
    public Props() {
        loadProperties(PROPS_FILE);
    }

    /**
     * Load properties.
     *
     * @param propsFileName the props file name
     */
    public void loadProperties(final String propsFileName) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(propsFileName);

            props = new Properties();

            props.load(inputStream);

            databaseContextURL = readString("MAIN_DataSourse_EBANK");
            

            //logsPath = readString("LOGS_PATH");
        } catch (IOException ex) {
            System.err.print("ERROR: Failed to load properties file.\nCause: " + ex.getMessage());
            Logger.getLogger(Props.class.getName()).log(Level.SEVERE, "ERROR: Failed to load properties file.\nCause: \n", ex);

        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(Props.class.getName()).log(Level.SEVERE, "ERROR: Failed to load properties file.\nCause: \n", ex);
            }
        }
    }

    /**
     * Read string string. - This function reads a String from the properties
     * file
     *
     * @param propertyName the property name
     * @return the string
     */
    public String readString(String propertyName) {
        String property = props.getProperty(propertyName);
        if (property.isEmpty()) {
            getLoadErrors().add(String.format(error2, propertyName));
        }
        return property;
    }

    /**
     * Read int int. - This function gets a String property from the properties
     * file and parses it into and INT.
     *
     * @param propertyName the property name
     * @return the int
     */
    public int readInt(String propertyName) {
        int property = 0;
        String propertyString = props.getProperty(propertyName);
        if (propertyString.isEmpty()) {
            getLoadErrors().add(String.format(error1, propertyName));
        } else {
            property = Integer.parseInt(propertyString);
            if (property < 0) {
                getLoadErrors().add(String.format(error1,
                        propertyName));
            }
        }
        return property;
    }

    /**
     * Read float float. - This function gets a String property from the
     * properties file and parses it into and FLOAT.
     *
     * @param propertyName the property name
     * @return the float
     */
    public float readFloat(String propertyName) {
        float property = 0;
        String propertyString = props.getProperty(propertyName);
        if (propertyString.isEmpty()) {
            getLoadErrors().add(String.format(error1, propertyName));
        } else {
            property = Float.parseFloat(propertyString);
            if (property < 0) {
                getLoadErrors().add(String.format(error1,
                        propertyName));
            }
        }
        return property;
    }

    /**
     * Read double double. - This function gets a String property from the
     * properties file and parses it into and DOUBLE.
     *
     * @param propertyName the property name
     * @return the double
     */
    public double readDouble(String propertyName) {
        double property = 0.0;
        String propertyString = props.getProperty(propertyName);
        if (propertyString.isEmpty()) {
            getLoadErrors().add(String.format(error1, propertyName));
        } else {
            property = Double.parseDouble(propertyString);
            if (property < 0) {
                getLoadErrors().add(String.format(error1,
                        propertyName));
            }
        }
        return property;
    }

    /**
     * Gets load errors.
     *
     * @return the load errors
     */
    public List<String> getLoadErrors() {
        return loadErrors;
    }

    /**
     * Get Database Context URL
     *
     * @return the database context URL
     */
    public String getDatabaseContextURL() {
        return databaseContextURL;
    }
    

    /**
     * Gets logs path.
     *
     * @return the logs path
     */
    public String getLogsPath() {
        return logsPath;
    }

}
