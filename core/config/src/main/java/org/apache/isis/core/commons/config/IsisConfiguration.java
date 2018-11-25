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

package org.apache.isis.core.commons.config;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;
import java.util.Map;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.Module;
import org.apache.isis.applib.PropertyResource;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;
import org.apache.isis.core.commons.resource.ResourceStreamSource;

import static org.apache.isis.commons.internal.base._NullSafe.stream;
import static org.apache.isis.config.internal._Config.acceptBuilder;
import static org.apache.isis.config.internal._Config.clear;
import static org.apache.isis.config.internal._Config.getConfiguration;

/**
 * Immutable set of properties representing the configuration of the running
 * system.
 *
 * <p>
 * The {@link IsisConfiguration} is one part of a mutable/immutable pair pattern
 * (cf {@link String} and {@link StringBuilder}). What this means is, as
 * components are loaded they can discover their own configuration resources.
 * These are added to {@link IsisConfigurationBuilder}.
 *
 * <p>
 * Thus the {@link IsisConfiguration} held by different components may vary, but
 * with each being a possible superset of the previous.
 */
public interface IsisConfiguration {
    
    /**
     * How to handle the case when the configuration already contains the key being added.
     */
    public enum ContainsPolicy {
        /**
         * If the configuration already contains the key, then ignore the new value.
         */
        IGNORE,
        /**
         * If the configuration already contains the key, then overwrite with the new.
         */
        OVERWRITE,
        /**
         * If the configuration already contains the key, then throw an exception.
         */
        EXCEPTION
    }
    
    /**
     * 
     * @param topModule
     * @param additionalPropertyResources
     * @return
     * @since 2.0.0-M2
     */
    static IsisConfiguration buildFromModuleTree(Module topModule, PropertyResource ... additionalPropertyResources) {
        clear();
        acceptBuilder(builder->{
            stream(additionalPropertyResources)
            .forEach(builder::addPropertyResource);
            builder.addTopModule(topModule);
        });
        return getConfiguration();
    }
    
    /**
     * 
     * @param topModule
     * @param additionalPropertyResources
     * @return
     * @since 2.0.0-M2
     */
    static IsisConfiguration buildFromAppManifest(AppManifest appManifest) {
        clear();
        acceptBuilder(builder->{
            builder.addAppManifest(appManifest);
        });
        return getConfiguration();
    }
    
    public AppManifest getAppManifest();

    /**
     * Creates a new IsisConfiguration containing the properties starting with
     * the specified prefix. The names of the new properties will have the
     * prefixed stripped. This is similar to the {@link #getProperties(String)}
     * method, except the property names have their prefixes removed.
     *
     * @see #getProperties(String)
     */
    IsisConfiguration createSubset(String prefix);

    /**
     * Gets the boolean value for the specified name where no value or 'on' will
     * result in true being returned; anything gives false. If no boolean
     * property is specified with this name then false is returned.
     *
     * @param name
     *            the property name
     */
    boolean getBoolean(String name);

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
    boolean getBoolean(String name, boolean defaultValue);

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then null is returned.
     *
     * @param name
     *            the property name
     */
    Color getColor(String name);

    /**
     * Gets the color for the specified name. If no color property is specified
     * with this name then the specified default color is returned.
     *
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    Color getColor(String name, Color defaultValue);

    /**
     * Gets the font for the specified name. If no font property is specified
     * with this name then null is returned.
     *
     * @param name
     *            the property name
     */
    Font getFont(String name);

    /**
     * Gets the font for the specified name. If no font property is specified
     * with this name then the specified default font is returned.
     *
     * @param name
     *            the property name
     * @param defaultValue
     *            the color to use as a default
     */
    Font getFont(String name, Font defaultValue);

    /**
     * Returns a list of entries for the single configuration property with the
     * specified name.
     *
     * <p>
     * If there is no matching property then returns an empty array.
     */
    String[] getList(String name);

    /**
     * Returns a list of entries for the single configuration property with the
     * specified name.
     */
    String[] getList(String name, String defaultListAsCommaSeparatedArray);

    /**
     * Gets the number value for the specified name. If no property is specified
     * with this name then 0 is returned.
     *
     * @param name
     *            the property name
     */
    int getInteger(String name);

    /**
     * Gets the number value for the specified name. If no property is specified
     * with this name then the specified default number value is returned.
     *
     * @param name
     *            the property name
     * @param defaultValue
     *            the value to use as a default
     */
    int getInteger(String name, int defaultValue);

    /**
     * Creates a new IsisConfiguration containing the properties starting with
     * the specified prefix. The names of the properties in the copy are the
     * same as in the original, ie the prefix is not removed. This is similar to
     * the {@link #createSubset(String)} method except the names of the
     * properties are not altered when copied.
     *
     * @see #createSubset(String)
     */
    IsisConfiguration getProperties(String withPrefix);

    /**
     * Returns the configuration property with the specified name. If there is
     * no matching property then null is returned.
     */
    String getString(String name);

    String getString(String name, String defaultValue);

    boolean hasProperty(String name);

    boolean isEmpty();

    /**
     * Iterates over the property names of this configuration.
     */
    Iterator<String> iterator();

    Iterable<String> asIterable();

    int size();

    /**
     * The {@link ResourceStreamSource} that was used to build this
     * configuration.
     */
    ResourceStreamSource getResourceStreamSource();

    /**
     * A mutable copy of the current set of properties (name/values) held in this configuration.
     */
    Map<String, String> asMap();

    // -- SHORTCUTS
    
    default boolean explicitAnnotationsForActions() {
        return getBoolean("isis.reflector.explicitAnnotations.action");
    }

    @Deprecated /* experimental */
    static IsisConfiguration loadDefault() {
        // TODO Auto-generated method stub
        return null;
    }

}
