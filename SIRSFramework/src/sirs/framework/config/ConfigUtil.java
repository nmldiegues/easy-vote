package sirs.framework.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigUtil {

    /**
     * Create new properties object from resource file.
     *
     * @param resourcePath resource file path (e.g. /file.properties)
     * @return properties object with loaded properties or null if resource not found
     */
    public static Properties getResourceAsProperties(String resourcePath) {
        Properties newProps = null;
        InputStream is = null;
        try {
            is = Config.class.getResourceAsStream(resourcePath);
            if(is == null) {
                return null;
            }
            newProps = new Properties();
            newProps.load(is);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if(is != null)
                    is.close();
            } catch (IOException e) {
            }
        }
        return newProps;
    }

    /**
     * Copy all source properties to destination properties, one by one.
     */
    public static void copyProperties(Properties sourceProps, Properties destinationProps) {
        copyProperties(sourceProps, destinationProps, "", "");
    }

    /**
     * Copy all source properties to destination properties, one by one,
     * adding the specified prefix and suffix to the key name
     */
    public static void copyProperties(Properties sourceProps, Properties destinationProps,
                                      String propNamePrefix, String propNameSuffix) {
        // check arguments
        if(sourceProps == null)
            throw new IllegalArgumentException("Can't copy properties from null source!");
        if(destinationProps == null)
            throw new IllegalArgumentException("Can't copy properties to null destination!");
        if(propNamePrefix == null)
            propNamePrefix = "";
        if(propNameSuffix == null)
            propNameSuffix = "";

        // copy keys using prefix and suffix
        for(Object keyObject : sourceProps.keySet()) {
            String keyString = (String) keyObject;
            int size = propNamePrefix.length() + keyString.length() + propNameSuffix.length();
            if(size > keyString.length()) {
                // new key is different, build it
                StringBuilder sb = new StringBuilder(size);
                sb.append(propNamePrefix);
                sb.append(keyString);
                sb.append(propNameSuffix);
                keyString = sb.toString();
            }
            destinationProps.setProperty(keyString,(String)sourceProps.get((String)keyObject));
        }
    }

    /**
     * Copy all source properties to destination properties, one by one,
     * adding the specified prefix and suffix to the key name
     */
    public static void removePropertiesWithPrefix(Properties props, String prefix) {
        // check arguments
        if(props == null)
            throw new IllegalArgumentException("Can't remove properties with prefix from null properties!");
        if(prefix == null)
            throw new IllegalArgumentException("Can't remove properties with null prefix!");

        // typesafe for-each loop
        for (String key : props.stringPropertyNames()) {
        	if (key.startsWith(prefix)) {
                props.remove(key);
            }
        }
    }

    /**
     *  Recognize if string contains "true" or one of its synonyms: "yes" and "on".
     *  Everything else is considered false (including null).
     *
     *  Surrounding whitespace is trimmed.
     *  Character case (Tt) is ignored.
     */
    public static boolean recognizeAsTrue(String propertyValue) {
        if(propertyValue == null)
            return false;
        propertyValue = propertyValue.trim();
        if(propertyValue.equalsIgnoreCase("true") ||
           propertyValue.equalsIgnoreCase("yes") ||
           propertyValue.equalsIgnoreCase("on"))
            return true;
        else
            return false;
    }

}
