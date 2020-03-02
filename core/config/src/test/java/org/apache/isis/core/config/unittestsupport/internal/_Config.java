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
package org.apache.isis.core.config.unittestsupport.internal;

import java.util.Map;

import org.apache.isis.core.commons.internal.collections._Maps;
import org.apache.isis.core.commons.internal.context._Context;
import org.apache.isis.core.config.unittestsupport.IsisConfigurationLegacy;

import lombok.val;

/**
 * @since 2.0
 */
public class _Config {

    // -- CONFIG ACCESS

    public static IsisConfigurationLegacy getConfiguration() {
        return getLifecycleResource().getConfiguration();
    }

    // -- CLEAR

    /**
     * Most likely used for testing.
     * <p>
     * Throws away any configuration resources currently on this life-cycle's context.
     * Makes a new config builder singleton instance lazily available. 
     */
    public static void clear() {
        _Context.remove(_Config_LifecycleResource.class);
    }

    // -- PROPERTY ACCESS BEFORE FINALIZING CONFIG

//    public static void putAll(Map<String, String> map) {
//        properties().putAll(map);
//    }
//
//    public static void put(String key, String value) {
//        properties().put(key, value);
//    }
//
//    public static void put(String key, boolean value) {
//        properties().put(key, ""+value);
//    }

    // -- PROPERTY PEEKING

    public static String peekAtString(String key) {
        return properties().get(key);
    }

    public static String peekAtString(String key, String defaultValue) {
        val stringValue = properties().get(key);
        return stringValue!=null
                ? stringValue
                        : defaultValue;
    }

    public static Boolean peekAtBoolean(String key) {
        return _Config_Parsers.parseBoolean(properties().get(key));
    }

    public static boolean peekAtBoolean(String key, boolean defaultValue) {
        val booleanValue = _Config_Parsers.parseBoolean(properties().get(key));
        return booleanValue!=null
                ? booleanValue
                        : defaultValue;
    }

    // -- HELPER - PROPERTIES

    private static Map<String, String> properties() {
        _Config_LifecycleResource lifecycle = getLifecycleResource();
        val properties = lifecycle.getMutableProperties()
                .orElseThrow(lifecycle::configurationAlreadyInUse);
        return properties;
    }

    private static Map<String, String> defaultProperties() {
        val properties = _Maps.<String, String>newHashMap(); 
        return properties;
    }

    // -- HELPER - LIFECYCLE

    private static _Config_LifecycleResource getLifecycleResource() {
        final _Config_LifecycleResource lifecycle = 
                _Context.computeIfAbsent(_Config_LifecycleResource.class, ()->createLifecycleResource());
        return lifecycle;
    }

    private static _Config_LifecycleResource createLifecycleResource() {
        return new _Config_LifecycleResource(defaultProperties());
    }




}
