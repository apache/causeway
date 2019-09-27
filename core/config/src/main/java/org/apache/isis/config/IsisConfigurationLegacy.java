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

package org.apache.isis.config;

import java.awt.Color;
import java.awt.Font;
import java.util.Map;
import java.util.TreeMap;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.plugins.environment.IsisSystemEnvironment;

import lombok.val;

/**
 * Immutable set of properties representing the configuration of the running
 * system.
 */
public interface IsisConfigurationLegacy {

    //TODO[2112] remove comment    
    //    /**
    //     * How to handle the case when the configuration already contains the key being added.
    //     */
    //    public enum ContainsPolicy {
    //        /**
    //         * If the configuration already contains the key, then ignore the new value.
    //         */
    //        IGNORE,
    //        /**
    //         * If the configuration already contains the key, then overwrite with the new.
    //         */
    //        OVERWRITE,
    //        /**
    //         * If the configuration already contains the key, then throw an exception.
    //         */
    //        EXCEPTION
    //    }


    // -- VERSION

    public static String getVersion() {
        return "2.0.0-M3";
    }

    // --

    /**
     * Creates a copy of this instance, that is a new IsisConfiguration populated 
     * with a copy of the underlying key/value pairs.
     *
     */
    IsisConfigurationLegacy copy();

    /**
     * Creates a new IsisConfiguration containing the properties starting with
     * the specified prefix. The names of the new properties will have the
     * prefixed stripped. This is similar to the {@link #subset(String)}
     * method, except the property names have their prefixes removed.
     *
     * @see #subset(String)
     */
    IsisConfigurationLegacy subsetWithNamesStripped(String prefix);

    /**
     * Creates a new IsisConfiguration containing the properties starting with
     * the specified prefix. The names of the properties in the copy are the
     * same as in the original, ie the prefix is not removed. This is similar to
     * the {@link #subsetWithNamesStripped(String)} method except the names of the
     * properties are not altered when copied.
     *
     * @see #subsetWithNamesStripped(String)
     */
    IsisConfigurationLegacy subset(String withPrefix);

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
     * Returns the configuration property with the specified name. If there is
     * no matching property then null is returned.
     */
    String getString(String name);

    String getString(String name, String defaultValue);

    boolean hasProperty(String name);

    boolean isEmpty();


    /**
     * A mutable copy of the current set of properties (name/values) held in this configuration.
     */
    Map<String, String> copyToMap();

    /**
     * pre-bootstrapping configuration
     */
    default public IsisSystemEnvironment getEnvironment() {
        return _Context.getEnvironment();
    }

    // -- TO STRING

    default public String toStringFormatted() {

        val sb = new StringBuilder();
        val configuration = this.subset("isis");

        final Map<String, String> map = 
                ConfigurationConstants.maskIfProtected(configuration.copyToMap(), TreeMap::new);

        String head = String.format("APACHE ISIS %s (%s) ", 
                IsisConfigurationLegacy.getVersion(), getEnvironment().getDeploymentType().name());
        final int fillCount = 46-head.length();
        final int fillLeft = fillCount/2;
        final int fillRight = fillCount-fillLeft;
        head = _Strings.padStart("", fillLeft, ' ') + head + _Strings.padEnd("", fillRight, ' ');

        sb.append("================================================\n");
        sb.append("="+head+"=\n");
        sb.append("================================================\n");
        map.forEach((k,v)->{
            sb.append(k+" -> "+v).append("\n");
        });
        sb.append("================================================\n");

        return sb.toString();
    }

}
