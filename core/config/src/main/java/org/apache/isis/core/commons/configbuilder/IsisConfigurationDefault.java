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

package org.apache.isis.core.commons.configbuilder;

import java.awt.Color;
import java.awt.Font;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Nature;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.commons.config.ConfigurationConstants;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.config.IsisConfigurationException;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.resource.ResourceStreamSource;

import static org.apache.isis.commons.internal.base._With.requires;


/**
 * This object will typically be registered as the implementation of the {@link ConfigurationServiceInternal}
 * (internal) domain service, using
 * {@link ServicesInjector#addFallbackIfRequired(Class, Object)}.
 *
 * <p>
 *     If an integration test is running, then the <code>IsisConfigurationForJdoIntegTests</code> will be used instead.
 * </p>
 */
//[2039] @DomainObject(objectType=...) fixes meta-data validation complaining, otherwise not required ...
@DomainObject(nature=Nature.INMEMORY_ENTITY, objectType="internal.IsisConfiguration") 
class IsisConfigurationDefault implements IsisConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(IsisConfigurationDefault.class);
    private final ResourceStreamSource resourceStreamSource;
    private final Properties properties = new Properties();
    /**
     * derived lazily from {@link #properties}.
     */
    private Properties applicationProperties;
    private AppManifest appManifest;

    // ////////////////////////////////////////////////
    // Constructor
    // ////////////////////////////////////////////////

    public IsisConfigurationDefault() {
        this(null);
    }

    public IsisConfigurationDefault(final ResourceStreamSource resourceStreamSource) {
        this.resourceStreamSource = resourceStreamSource;
        LOG.debug("configuration initialised with stream: {}", nameOf(resourceStreamSource));
    }

    private String nameOf(final ResourceStreamSource resourceStreamSource) {
        return resourceStreamSource != null ? resourceStreamSource.getName() : null;
    }

    // ////////////////////////////////////////////////
    // Property - Manifest
    // ////////////////////////////////////////////////
    
    @Override
    public AppManifest getAppManifest() {
        return appManifest;
    }
    
    public void setAppManifest(AppManifest appManifest) {
        requires(appManifest, "appManifest");
        this.appManifest = appManifest;
    }
    
    // ////////////////////////////////////////////////
    // Type Discovery
    // ////////////////////////////////////////////////
    
    _Lazy<Integer> typeDiscovery = _Lazy.threadSafe(()->ModulePackageHelper.runTypeDiscovery(this.getAppManifest()));
    
    public void triggerTypeDiscovery() {
        typeDiscovery.get();
    }
    
    // ////////////////////////////////////////////////
    // ResourceStreamSource
    // ////////////////////////////////////////////////

    @Override
    public ResourceStreamSource getResourceStreamSource() {
        return resourceStreamSource;
    }

    // ////////////////////////////////////////////////
    // add
    // ////////////////////////////////////////////////

    /**
     * Add the properties from an existing Properties object.
     */
    public void add(final Properties properties, final ContainsPolicy containsPolicy) {
        for(Object key: properties.keySet()) {
            Object value = properties.get(key);
            addPerPolicy((String) key, (String) value, containsPolicy);
        }
    }

    /**
     * Adds a key-value pair to this set of properties; if the key exists in the configuration then will be ignored.
     *
     * <p>
     * @see #addPerPolicy(String, String, ContainsPolicy)
     * @see #put(String, String)
     */
    public void add(final String key, final String value) {
        addPerPolicy(key, value, ContainsPolicy.IGNORE);
    }

    /**
     * Adds a key-value pair to this set of properties; if the key exists in the configuration then will be replaced.
     *
     * <p>
     * @see #add(String, String)
     * @see #addPerPolicy(String, String, ContainsPolicy)
     */
    public void put(final String key, final String value) {
        addPerPolicy(key, value, ContainsPolicy.OVERWRITE);
    }

    /**
     * Adds a key-value pair to this set of properties; if the key exists in the configuration then the
     * {@link ContainsPolicy} will be applied.
     *
     * @see #add(String, String)
     * @see #put(String, String)
     */
    private void addPerPolicy(final String key, final String value, final ContainsPolicy policy) {
        if (value == null) {
            LOG.debug("ignoring {} as value is null", key);
            return;
        }
        if (key == null) {
            return;
        }
        if (properties.containsKey(key)) {
            switch (policy) {
            case IGNORE:
                LOG.info("ignoring '{}' = '{}' as value already set (with {})", key, value, properties.get(key));
                break;
            case OVERWRITE:
                LOG.info("overwriting '{}' = '{}' (previous value was {})", key, value, properties.get(key));
                properties.put(key, value);
                break;
            case EXCEPTION:
                throw new IllegalStateException(String.format(
                        "Configuration already has a key {}, value of {}%s, value of %s", key, properties.get(key)));
            }
        } else {
            LOG.info("adding '{}' = '{}'", key , ConfigurationConstants.maskIfProtected(key, value));
            properties.put(key, value);
        }
    }

    public IsisConfiguration createSubset(final String prefix) {
        final IsisConfigurationDefault subset = new IsisConfigurationDefault(resourceStreamSource);

        String startsWith = prefix;
        if (!startsWith.endsWith(".")) {
            startsWith = startsWith + '.';
        }
        final int prefixLength = startsWith.length();

        for(Object keyObj: properties.keySet()) {
            final String key = (String)keyObj;
            if (key.startsWith(startsWith)) {
                final String modifiedKey = key.substring(prefixLength);
                subset.properties.put(modifiedKey, properties.get(key));
            }
        }
        return subset;
    }

    // ////////////////////////////////////////////////
    // getXxx
    // ////////////////////////////////////////////////

    /**
     * Gets the boolean value for the specified name where no value or 'on' will
     * result in true being returned; anything gives false. If no boolean
     * property is specified with this name then false is returned.
     *
     * @param name
     *            the property name
     */
    @Override
    public boolean getBoolean(final String name) {
        return getBoolean(name, false);
    }

    /**
     * Gets the boolean value for the specified name. If no property is
     * specified with this name then the specified default boolean value is
     * returned.
     *
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    @Override
    public boolean getBoolean(final String name, final boolean defaultValue) {
        String value = getPropertyElseNull(name);
        if (_Strings.isNullOrEmpty(value)) {
            return defaultValue;
        }
        value = value.toLowerCase();
        if (value.equals("on") || value.equals("yes") || value.equals("true") ) {
            return true;
        }
        if (value.equals("off") || value.equals("no") || value.equals("false")) {
            return false;
        }

        throw new IsisConfigurationException("Illegal flag for " + name
                + "; must be one of on, off, yes, no, true or false");
    }

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then null is returned.
     *
     * @param name
     *            the property name
     */
    @Override
    public Color getColor(final String name) {
        return getColor(name, null);
    }

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then the specified default color is returned.
     *
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    @Override
    public Color getColor(final String name, final Color defaultValue) {
        final String color = getPropertyElseNull(name);

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
     *            the property name
     */
    @Override
    public Font getFont(final String name) {
        return getFont(name, null);
    }

    /**
     * Gets the font for the specified name. If no font property is specified
     * with this name then the specified default font is returned.
     *
     * @param name
     *            the property name
     * @param defaultValue
     *            the color to use as a default
     */
    @Override
    public Font getFont(final String name, final Font defaultValue) {
        final String font = getPropertyElseNull(name);

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
     *            the property name
     */
    @Override
    public int getInteger(final String name) {
        return getInteger(name, 0);
    }

    /**
     * Gets the number value for the specified name. If no property is specified
     * with this name then the specified default number value is returned.
     *
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    @Override
    public int getInteger(final String name, final int defaultValue) {
        final String value = getPropertyElseNull(name);

        if (value == null) {
            return defaultValue;
        }

        return Integer.valueOf(value).intValue();
    }

    @Override
    public String[] getList(final String name) {
        final String listAsCommaSeparatedArray = getString(name);
        return stringAsList(listAsCommaSeparatedArray);
    }

    @Override
    public String[] getList(String name, String defaultListAsCommaSeparatedArray) {
        final String listAsCommaSeparatedArray = getString(name, defaultListAsCommaSeparatedArray);
        return stringAsList(listAsCommaSeparatedArray);
    }

    private String[] stringAsList(String list) {
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

    @Override
    public IsisConfiguration getProperties(final String withPrefix) {
        final int prefixLength = "".length();

        final Properties properties = new Properties();
        final Enumeration<?> e = this.properties.keys();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            if (key.startsWith(withPrefix)) {
                final String modifiedKey = key.substring(prefixLength);
                properties.put(modifiedKey, this.properties.get(key));
            }
        }
        final IsisConfigurationDefault isisConfigurationDefault = new IsisConfigurationDefault(resourceStreamSource);
        isisConfigurationDefault.add(properties, ContainsPolicy.IGNORE);
        return isisConfigurationDefault;
    }

    private String getPropertyElseNull(final String name) {
        return getProperty(name, null);
    }

    private String getProperty(final String name, final String defaultValue) {
        final String key = referedToAs(name);
        if (key.indexOf("..") >= 0) {
            throw new IsisException("property names should not have '..' within them: " + name);
        }
        String property = properties.getProperty(key, defaultValue);
        property = property != null ? property.trim() : null;
        LOG.debug("get property: '{} = '{}'", key, property);
        return property;
    }

    /**
     * Returns the configuration property with the specified name. If there is
     * no matching property then null is returned.
     */
    @Override
    public String getString(final String name) {
        return getPropertyElseNull(name);
    }

    @Override
    public String getString(final String name, final String defaultValue) {
        return getProperty(name, defaultValue);
    }

    @Override
    public boolean hasProperty(final String name) {
        final String key = referedToAs(name);
        return properties.containsKey(key);
    }

    @Override
    public boolean isEmpty() {
        return properties.isEmpty();
    }

    @Override
    public Iterator<String> iterator() {
        return asIterable().iterator();
    }

    @Override
    public Iterable<String> asIterable() {
        return properties.stringPropertyNames();
    }

    /**
     * Returns as a String that the named property is refered to as. For example
     * in a simple properties file the property z might be specified in the file
     * as x.y.z.
     */
    private String referedToAs(final String name) {
        return name;
    }

    @Override
    public int size() {
        return properties.size();
    }

    @Override
    public String toString() {
        return "ConfigurationParameters [properties=" + properties + "]";
    }

    // ////////////////////////////////////////////////////////////////////
    // injectInto
    // ////////////////////////////////////////////////////////////////////

    @Override
    public Map<String,String> asMap() {
        final Map<String, String> map = _Maps.newHashMap();
        for(String propertyName: this.asIterable()) {
            final String propertyValue = this.getPropertyElseNull(propertyName);
            map.put(propertyName, propertyValue);
        }
        return map;
    }


    // -- ConfigurationService impl
//    @Override
    public String getProperty(final String name) {
        initAppPropertiesIfRequired();
        return applicationProperties.getProperty(name);
    }

    private void initAppPropertiesIfRequired() {
        if(applicationProperties == null) {
            applicationProperties = deriveApplicationProperties();
        }
    }

    private Properties deriveApplicationProperties() {
        final Properties applicationProperties = new Properties();
        final IsisConfiguration applicationConfiguration = getProperties("application");
        for (final String key : applicationConfiguration.asIterable()) {
            final String value = applicationConfiguration.getString(key);
            final String newKey = key.substring("application.".length());
            applicationProperties.setProperty(newKey, value);
        }
        return applicationProperties;
    }


//    @Override
    public List<String> getPropertyNames() {
        initAppPropertiesIfRequired();
        final List<String> list = _Lists.newArrayList();
        for (final Object key : applicationProperties.keySet()) {
            list.add((String) key);
        }
        return list;
    }


}
