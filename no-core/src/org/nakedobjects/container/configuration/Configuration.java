package org.nakedobjects.container.configuration;

import org.nakedobjects.object.NakedObjectRuntimeException;

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;


public class Configuration {
    private static final Logger LOG = Logger.getLogger(Configuration.class);
    public static final String PREFIX = "nakedobjects.";    
    private final Properties p = new Properties();

    public Configuration() {}

    public Configuration(ConfigurationPropertiesLoader loader) {
        add(loader.getProperties());
    }

    /**
     * Add the properties from an existing Properties object.
     */
    public void add(Properties properties) {
        Enumeration e = properties.propertyNames();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();

            p.put(name, properties.getProperty(name));
        }
    }

    /**
     * Adds a key-value pair to this set of properties
     */
    public void add(String key, String value) {
        p.put(key, value);
    }
    
    /**
     * @deprecated
     */
    public String fullName(String name) {
        return PREFIX + name;
    }

    /**
     * Gets the boolean value for the specified name where no value or 'on' will
     * result in true being returned; anything gives false. If no boolean
     * property is specified with this name then false is returned.
     * 
     * @param name
     *                       the property name
     */
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    /**
     * Gets the boolean value for the specified name. If no property is
     * specified with this name then the specified default boolean value is
     * returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the value to use as a default
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String value = getProperty(name);

        if (value == null) {
            return defaultValue;
        }

        return value.equals("on") || value.equals("yes") || value.equals("true") || value.equals("");
    }

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then null is returned.
     * 
     * @param name
     *                       the property name
     */
    public Color getColor(String name) {
        return getColor(name, null);
    }

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then the specified default color is returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the value to use as a default
     */
    public Color getColor(String name, Color defaultValue) {
        String color = getProperty(name);

        if (color == null) {
            return defaultValue;
        }

        return Color.decode(color);
    }

    /**
     * Gets the font for the specified name. If no font property is specified
     * with this name then null is returned.
     * 
     * @param name
     *                       the property name
     */
    public Font getFont(String name) {
        return getFont(name, null);
    }

    /**
     * Gets the font for the specified name. If no font property is specified
     * with this name then the specified default font is returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the color to use as a default
     */
    public Font getFont(String name, Font defaultValue) {
        String font = getProperty(name);

        if (font == null) {
            return defaultValue;
        }

        return Font.decode(font);
    }

    /**
     * Gets the number value for the specified name. If no property is specified
     * with this name then 0 is returned.
     * 
     * @param name
     *                       the property name
     */
    public int getInteger(String name) {
        return getInteger(name, 0);
    }

    /**
     * Gets the nimber value for the specified name. If no property is specified
     * with this name then the specified default number value is returned.
     * 
     * @param name
     *                       the property name
     * @param defaultValue
     *                       the value to use as a default
     */
    public int getInteger(String name, int defaultValue) {
        String value = getProperty(name);

        if (value == null) {
            return defaultValue;
        }

        return Integer.valueOf(value).intValue();
    }

    /**
     * Gets the set of properties with the specified prefix
     */
    public Properties getProperties(String withPrefix) {
        return getProperties(withPrefix, "");
    }

    /**
     * Gets the set of properties with the specified prefix
     */
    public Properties getProperties(String withPrefix, String stripPrefix) {
        int prefixLength = stripPrefix.length();            
        
        Properties pp = new Properties();
        Enumeration e = p.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.startsWith(withPrefix)) {
                String modifiedKey = key.substring(prefixLength);
                pp.put(modifiedKey, p.get(key));
            }
        }
        return pp;
    }


    private String getProperty(String name) {
        return getProperty(name, null);
    }

    private String getProperty(String name, String defaultValue) {
        String key = fullName(name);
        if (key.indexOf("..") >= 0) {
            throw new NakedObjectRuntimeException("property names should not have '..' within them: " + name);
        }
        String property = p.getProperty(key, defaultValue);
        LOG.debug("property: " + key + " =  <" + property + ">");
        return property;
    }
/*
    public Properties getPropertySubset(String prefix) {
        prefix = PREFIX + prefix + ".";
        Properties p = getProperties(prefix);
        Enumeration e = p.keys();
        int prefixLength = prefix.length();

        Properties modifiedProperties = new Properties();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = (String) p.get(key);
            String modifiedKey = key.substring(prefixLength);
            LOG.debug(key + " modified to: " + modifiedKey);
            modifiedProperties.put(modifiedKey, value);
        }

        return modifiedProperties;
    }
*/

    /**
     * Returns the configuration property with the specified name. If there is
     * no matching property then null is returned.
     */
    public String getString(String name) {
        return getProperty(name);
    }

    public String getString(String name, String defaultValue) {
        return getProperty(name, defaultValue);
    }

    public boolean hasProperty(String name) {
        String key = fullName(name);
        return p.containsKey(key);
    }

    public String toString() {
        return "ConfigurationParameters [properties=" + p + "]";
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */