package org.nakedobjects.utility;

import org.nakedobjects.object.NakedObjectRuntimeException;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * Holds the available properties for this Naked Objects session.
 */
public final class Configuration {
    private static Configuration instance = new Configuration();
    private static final Logger LOG = Logger.getLogger(Configuration.class);
    private static final String PREFIX = "nakedobjects.";

    /**
     * Returns the singleton that is to be used to access the properties.
     */
    public final static Configuration getInstance() {
        return instance;
    }

    public static void installConfiguration(String resource) throws ConfigurationException {
        Properties p = loadProperties(resource);
        getInstance().load(p);
    }

    public static void installConfiguration(URL resource) throws ConfigurationException {
        Properties p = loadProperties(resource);
        getInstance().load(p);
    }

    public static Properties loadProperties(String resource) throws ConfigurationException {

        try {
            URL url = Configuration.class.getResource(resource);
            if (url == null) { throw new ConfigurationException("Configuration resource not found: " + resource); }
            return loadProperties(url);

        } catch (ConfigurationException e) {
            try {
                // try as if it is a file name
                URL url = new URL("file:///" + new File(resource).getAbsolutePath());

                return loadProperties(url);
            } catch (SecurityException ee) {
                throw new ConfigurationException("Access not granted to configuration resource: " + resource);
            } catch (MalformedURLException ee) {
                throw new ConfigurationException("Configuration resource not found: " + resource);
            }
        }
    }

    public static Properties loadProperties(URL resource) throws ConfigurationException {
        InputStream in;

        try {
            URLConnection connection = resource.openConnection();
            in = connection.getInputStream();
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("Could not find configuration resource: " + resource);
        } catch (IOException e) {
            throw new ConfigurationException("Error reading configuration resource: " + resource, e);
        }

        try {
            Properties p = new Properties();
            p.load(in);

            return p;
        } catch (IOException e) {
            throw new ConfigurationException("Error reading properties" + e.getMessage());
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {
                ;
            }
        }
    }

    private Properties p = new Properties();

    /**
     * Adds a key-value pair to this set of properties
     * 
     * @param key
     * @param value
     */
    public void add(String key, String value) {
        p.put(key, value);
    }

    public String fullName(String name) {
        return PREFIX + name;
    }

    /**
     * Gets the boolean value for the specified name where no value or 'on' will result in true
     * being returned; anything gives false. If no boolean property is specified with this name then
     * false is returned.
     * 
     * @param name
     *                   the property name
     */
    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }

    /**
     * Gets the boolean value for the specified name. If no property is specified with this name
     * then the specified default boolean value is returned.
     * 
     * @param name
     *                   the property name
     * @param defaultValue
     *                   the value to use as a default
     */
    public boolean getBoolean(String name, boolean defaultValue) {
        String value = getProperty(name);

        if (value == null) { return defaultValue; }

        return value.equals("on") || value.equals("yes") || value.equals("true") || value.equals("");
    }

    /**
     * Gets the color for the specified name. If no color property is specified with this name then
     * null is returned.
     * 
     * @param name
     *                   the property name
     */
    public Color getColor(String name) {
        return getColor(name, null);
    }

    /**
     * Gets the color for the specified name. If no color property is specified with this name then
     * the specified default color is returned.
     * 
     * @param name
     *                   the property name
     * @param defaultValue
     *                   the value to use as a default
     */
    public Color getColor(String name, Color defaultValue) {
        String color = getProperty(name);

        if (color == null) { return defaultValue; }

        return Color.decode(color);
    }

    /**
     * Gets the font for the specified name. If no font property is specified with this name then
     * null is returned.
     * 
     * @param name
     *                   the property name
     */
    public Font getFont(String name) {
        return getFont(name, null);
    }

    /**
     * Gets the font for the specified name. If no font property is specified with this name then
     * the specified default font is returned.
     * 
     * @param name
     *                   the property name
     * @param defaultValue
     *                   the color to use as a default
     */
    public Font getFont(String name, Font defaultValue) {
        String font = getProperty(name);

        if (font == null) { return defaultValue; }

        return Font.decode(font);
    }

    /**
     * Gets the number value for the specified name. If no property is specified with this name then
     * 0 is returned.
     * 
     * @param name
     *                   the property name
     */
    public int getInteger(String name) {
        return getInteger(name, 0);
    }

    /**
     * Gets the nimber value for the specified name. If no property is specified with this name then
     * the specified default number value is returned.
     * 
     * @param name
     *                   the property name
     * @param defaultValue
     *                   the value to use as a default
     */
    public int getInteger(String name, int defaultValue) {
        String value = getProperty(name);

        if (value == null) { return defaultValue; }

        return Integer.valueOf(value).intValue();
    }

    /**
     * Gets the set of properties with the specified prefix
     */
    public Properties getProperties(String prefix) {
        Properties pp = new Properties();

        Enumeration e = p.keys();

        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();

            if (key.startsWith(prefix)) {
                pp.put(key, p.get(key));
            }
        }

        return pp;
    }

    private String getProperty(String name) {
        return getProperty(name, null);
    }

    private String getProperty(String name, String defaultValue) {
        String key = fullName(name);
        if (key.indexOf("..") >= 0) { throw new NakedObjectRuntimeException("property names should not have '..' within them: "
                + name); }
        String property = p.getProperty(key, defaultValue);
        LOG.debug("property: " + key + " =  <" + property + ">");
        return property;
    }

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

     /**
     * Returns the configuration property with the specified name. If there is no matching property
     * then null is returned.
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

    /**
     * Loads the properties from an existing Properties object.
     */
    public void load(Properties properties) {
        Enumeration e = properties.propertyNames();

        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();

            p.put(name, properties.getProperty(name));
        }
    }

    public void load(String resource) throws ConfigurationException {
        Properties p = loadProperties(resource);
        load(p);
    }

    /**
     * Loads the properties via a URL
     * 
     * @throws ConfigurationException
     */
    public void load(URL resource) throws ConfigurationException {
        Properties p = loadProperties(resource);
        load(p);
    }

    public String toString() {
        return "ConfigurationParameters [properties=" + p + "]";
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the
 * user. Copyright (C) 2000 - 2003 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects
 * Group is Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */