package sirs.framework.config;

import java.io.IOException;
import java.util.Properties;

public class Config {

    private static final Config fINSTANCE = new Config();

    public static synchronized Config getInstance() {
        return fINSTANCE;
    }

    private Config() {
        _props = null;
    }

    public static final String DEFAULT_PROPERTY_FILE_RESOURCE_PATH = "/config.properties";
    public static final boolean OPTION_AUTO_LOAD_DEFAULT_PROPERTY_FILE = true;
    private Properties _props = null;

    /**
     * Load default resource file into configuration properties.
     * @throws IOException when properties file is not found
     */
    public synchronized void load() throws ConfigException {
        load(DEFAULT_PROPERTY_FILE_RESOURCE_PATH);
    }

    /**
     * Load resource file into configuration properties.
     * @throws ConfigException when properties file is not found
     */
    public synchronized void load(String propertyFileResourcePath) throws ConfigException {
        loadWithPrefix(propertyFileResourcePath, "");
    }

    /**
     * Load default resource file into configuration properties adding specified prefix
     * to property names.
     * @throws IOException when properties file is not found
     */
    public synchronized void loadWithPrefix(String propertyNamePrefix) throws ConfigException {
        loadWithPrefix(DEFAULT_PROPERTY_FILE_RESOURCE_PATH, propertyNamePrefix);
    }

    /**
     * Load resource file into configuration properties adding specified prefix
     * to property names.
     * @throws ConfigException when properties file is not found
     */
    public synchronized void loadWithPrefix(String propertyFileResourcePath, String propertyNamePrefix) throws ConfigException {
        // check if prefix is null and replace it with empty string
        if(propertyNamePrefix == null)
            propertyNamePrefix = "";

        // load properties
        Properties newProps = ConfigUtil.getResourceAsProperties(propertyFileResourcePath);

        // throw exception if configuration file doesn't exist or isn't readable
        if(newProps == null) {
            throw new ConfigException("Failed to access resource " + propertyFileResourcePath + ".");
        }

        // copy new properties to global config properties
        if(!newProps.isEmpty()) {

            // if necessary, create global config properties
            if(_props == null)
                _props = new Properties();

            // copy new keys using prefix
            ConfigUtil.copyProperties(newProps, _props, propertyNamePrefix, "");
        }

    }


    //
    // Initialization parameter access methods
    //

    /**
     * Reads a configuration parameter.
     * If the configuration has not been initialized, tries to load default property file.
     *
     * @param key string that identifies the parameter
     * @return parameter value or null if parameter not found
     */
    public synchronized String getInitParameter(String key) {
        if(_props == null) {
            // if option enabled, try to load default file
            tryLoadingDefaultPropertyFile();

            if(_props == null) {
                // if global properties are still null, it means that
                // default properties loading has failed, so
                // throw illegal state exception
                String errorMessage = "Error when trying to access initialization parameter '" +
                                      key +
                                      "'. Configuration must be loaded before initialization parameters can be accessed!";
                throw new IllegalStateException(errorMessage);
            }
        }
        // properties are not null; return property or null if it doesn't exist
        return _props.getProperty(key);
    }

    /**
     * Tries to load default property file. If attempt fails, ignores error.
     */
    private void tryLoadingDefaultPropertyFile() {
        if(OPTION_AUTO_LOAD_DEFAULT_PROPERTY_FILE) {
            try {
                this.load();
            } catch(ConfigException e) {
            }
        }
    }

    /**
     * Returns a shallow clone of the configuration parameters
     * @return properties collection
     */
    public synchronized Properties getInitProperties() {
        if(_props == null)
            return null;
        else
            return (Properties) _props.clone();
    }

    /**
     * Clears the configuration
     */
    public synchronized void reset() {
        _props = null;
    }

}
