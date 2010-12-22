/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.core.metamodel.config.internal;

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.resource.ResourceStreamSource;
import org.apache.isis.core.metamodel.config.ConfigurationConstants;
import org.apache.isis.core.metamodel.config.ConfigurationException;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.config.IsisConfigurationAware;


public class PropertiesConfiguration implements IsisConfiguration {
    private static final Logger LOG = Logger.getLogger(PropertiesConfiguration.class);
    private final Properties p = new Properties();
	private final ResourceStreamSource resourceStreamSource;
    
    //////////////////////////////////////////////////
    // Constructor
    //////////////////////////////////////////////////

    public PropertiesConfiguration() {
        this(null);
    }

    public PropertiesConfiguration(ResourceStreamSource resourceStreamSource) {
        this.resourceStreamSource = resourceStreamSource;
        LOG.info("from :" + nameOf(resourceStreamSource));
    }

	private String nameOf(ResourceStreamSource resourceStreamSource) {
		return resourceStreamSource != null? resourceStreamSource.getName(): null;
	}

    
    //////////////////////////////////////////////////
    // ResourceStreamSource
    //////////////////////////////////////////////////
    
    
	public ResourceStreamSource getResourceStreamSource() {
		return resourceStreamSource;
	}

	
    //////////////////////////////////////////////////
    // add
    //////////////////////////////////////////////////

    /**
     * Add the properties from an existing Properties object.
     */
    public void add(final Properties properties) {
        final Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()) {
            final String name = (String) e.nextElement();
            p.put(name, properties.getProperty(name));
        }
    }

    /**
     * Adds a key-value pair to this set of properties
     */
    public void add(final String key, final String value) {
        if (key == null) {
            return;
        }
        if (p.containsKey(key)) {
            LOG.info("replacing " + key + "=" + p.get(key) + " with " + value);
        }
        p.put(key, value);
    }

    public IsisConfiguration createSubset(final String prefix) {
        final PropertiesConfiguration subset = new PropertiesConfiguration(resourceStreamSource);

        String startsWith = prefix;
        if (!startsWith.endsWith(".")) {
            startsWith = startsWith + '.';
        }
        final int prefixLength = startsWith.length();

        final Enumeration e = p.keys();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            if (key.startsWith(startsWith)) {
                final String modifiedKey = key.substring(prefixLength);
                subset.p.put(modifiedKey, p.get(key));
            }
        }
        return subset;
    }

    //////////////////////////////////////////////////
    // getXxx
    //////////////////////////////////////////////////

    /**
     * Gets the boolean value for the specified name where no value or 'on' will result in true being
     * returned; anything gives false. If no boolean property is specified with this name then false is
     * returned.
     * 
     * @param name
     *            the property name
     */
    public boolean getBoolean(final String name) {
        return getBoolean(name, false);
    }

    /**
     * Gets the boolean value for the specified name. If no property is specified with this name then the
     * specified default boolean value is returned.
     * 
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    public boolean getBoolean(final String name, final boolean defaultValue) {
        String value = getProperty(name);
        if (value == null) {
            return defaultValue;
        }
        value = value.toLowerCase();
        if (value.equals("on") || value.equals("yes") || value.equals("true") || value.equals("")) {
            return true;
        }
        if (value.equals("off") || value.equals("no") || value.equals("false")) {
            return false;
        }

        throw new ConfigurationException("Illegal flag for " + name + "; must be one of on, off, yes, no, true or false");
    }

    /**
     * Gets the color for the specified name. If no color property is specified with this name then null is
     * returned.
     * 
     * @param name
     *            the property name
     */
    public Color getColor(final String name) {
        return getColor(name, null);
    }

    /**
     * Gets the color for the specified name. If no color property is specified with this name then the
     * specified default color is returned.
     * 
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    public Color getColor(final String name, final Color defaultValue) {
        final String color = getProperty(name);

        if (color == null) {
            return defaultValue;
        }

        return Color.decode(color);
    }

    public void debugData(final DebugString str) {
        final Enumeration names = p.propertyNames();
        while (names.hasMoreElements()) {
            final String name = (String) names.nextElement();
            str.append(name, 55);
            str.append(" = ");
            str.appendln(p.getProperty(name));
        }
    }

    public String debugTitle() {
        return "Properties Configuration";
    }

    /**
     * Gets the font for the specified name. If no font property is specified with this name then null is
     * returned.
     * 
     * @param name
     *            the property name
     */
    public Font getFont(final String name) {
        return getFont(name, null);
    }

    /**
     * Gets the font for the specified name. If no font property is specified with this name then the
     * specified default font is returned.
     * 
     * @param name
     *            the property name
     * @param defaultValue
     *            the color to use as a default
     */
    public Font getFont(final String name, final Font defaultValue) {
        final String font = getProperty(name);

        if (font == null) {
            return defaultValue;
        }

        return Font.decode(font);
    }

    /**
     * Gets the number value for the specified name. If no property is specified with this name then 0 is
     * returned.
     * 
     * @param name
     *            the property name
     */
    public int getInteger(final String name) {
        return getInteger(name, 0);
    }

    /**
     * Gets the number value for the specified name. If no property is specified with this name then the
     * specified default number value is returned.
     * 
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    public int getInteger(final String name, final int defaultValue) {
        final String value = getProperty(name);

        if (value == null) {
            return defaultValue;
        }

        return Integer.valueOf(value).intValue();
    }

    public String[] getList(final String name) {
        final String list = getString(name);
        if (list == null) {
            return new String[0];
        } else {
            final StringTokenizer tokens = new StringTokenizer(list, ConfigurationConstants.LIST_SEPARATOR);
            final String array[] = new String[tokens.countTokens()];
            int i = 0;
            while (tokens.hasMoreTokens()) {
                array[i++] = tokens.nextToken().trim();
            }
            return array;
        }
    }

    public IsisConfiguration getProperties(final String withPrefix) {
        final int prefixLength = "".length();

        final Properties pp = new Properties();
        final Enumeration e = p.keys();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            if (key.startsWith(withPrefix)) {
                final String modifiedKey = key.substring(prefixLength);
                pp.put(modifiedKey, p.get(key));
            }
        }
        final PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(resourceStreamSource);
        propertiesConfiguration.add(pp);
        return propertiesConfiguration;
    }

    private String getProperty(final String name) {
        return getProperty(name, null);
    }

    private String getProperty(final String name, final String defaultValue) {
        final String key = referedToAs(name);
        if (key.indexOf("..") >= 0) {
            throw new IsisException("property names should not have '..' within them: " + name);
        }
        String property = p.getProperty(key, defaultValue);
        property = property != null ? property.trim() : null;
        LOG.debug("property: '" + key + "' =  '" + property + "'");
        return property;
    }

    /**
     * Returns the configuration property with the specified name. If there is no matching property then null
     * is returned.
     */
    public String getString(final String name) {
        return getProperty(name);
    }

    public String getString(final String name, final String defaultValue) {
        return getProperty(name, defaultValue);
    }

    public boolean hasProperty(final String name) {
        final String key = referedToAs(name);
        return p.containsKey(key);
    }

    public boolean isEmpty() {
        return p.isEmpty();
    }

    public Enumeration<String> propertyNames() {
        final Enumeration<Object> keys = p.keys();
    	return new Enumeration<String>() {

			public boolean hasMoreElements() {
				return keys.hasMoreElements();
			}

			public String nextElement() {
				return (String) keys.nextElement();
			}
    	};
    }

    /**
     * Returns as a String that the named property is refered to as. For example in a simple properties file
     * the property z might be specified in the file as x.y.z.
     */
    private String referedToAs(final String name) {
        return name;
    }

    public int size() {
        return p.size();
    }

    @Override
    public String toString() {
        return "ConfigurationParameters [properties=" + p + "]";
    }

    
    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    public void injectInto(Object candidate) {
        if (IsisConfigurationAware.class.isAssignableFrom(candidate.getClass())) {
            IsisConfigurationAware cast = IsisConfigurationAware.class.cast(candidate);
            cast.setIsisConfiguration(this);
        }
    }

}
