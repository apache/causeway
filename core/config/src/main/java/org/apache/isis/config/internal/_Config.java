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
package org.apache.isis.config.internal;

import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;

import lombok.val;

/**
 * @since 2.0.0-M2
 */
public class _Config {
    
    // -- CONFIG ACCESS
    
    public static IsisConfiguration getConfiguration() {
        return getLifecycleResource().getConfiguration();
    }
    
    public static void acceptConfig(Consumer<IsisConfiguration> configConsumer) {
        configConsumer.accept(getConfiguration());
    }
    
    public static <T> T applyConfig(Function<IsisConfiguration, T> configMapper) {
        return configMapper.apply(getConfiguration());
    }
    
    // -- BUILDER ACCESS
    
    public static void acceptBuilder(Consumer<Properties> builderConsumer) {
        builderConsumer.accept(properties());
    }
    
    public static <T> T applyBuilder(Function<Properties, T> builderMapper) {
        return builderMapper.apply(properties());
    }
    
    /**
     * Most likely used for testing.
     * <p>
     * Throws away any configuration resources currently on this life-cycle's context.
     * Makes a new config builder singleton instance lazily available. 
     */
    public static void clear() {
        _Context.remove(_Config_LifecycleResource.class);
    }
    
    // -- CONFIG SHORTCUTS ...
    
    
    // -- PROPERTY ACCESS SHORTCUTS
    
    public static void put(String key, String value) {
        properties().put(key, value);
    }
    
    public static void put(String key, boolean value) {
        properties().put(key, ""+value);
    }
    
    public static String peekAtString(String key) {
        return properties().getProperty(key);
    }
    
    public static String peekAtString(String key, String defaultValue) {
        return properties().getProperty(key, defaultValue);
    }
    
    public static Boolean peekAtBoolean(String key) {
        return toBoolean(properties().get(key));
    }
    
    public static boolean peekAtBoolean(String key, boolean defaultValue) {
        val booleanValue = toBoolean(properties().get(key));
        return booleanValue!=null
                ? booleanValue
                        : defaultValue;
    }
    
    // -- CONVERSION
    
    public static Boolean toBoolean(Object object) {
        if(object==null) {
            return null;
        }
        val literal = ("" + object).toLowerCase();
        switch (literal) {
        case "false":
        case "0":
        case "no":
            return Boolean.FALSE;
            
        case "true":
        case "1":
        case "yes":
            return Boolean.TRUE;
            
        default:
            break;
        }
        return null;
    }
    
    // -- HELPER - PROPERTIES
    
    private static Properties properties() {
        _Config_LifecycleResource lifecycle = getLifecycleResource();
        val properties = lifecycle.getMutableProperties()
                .orElseThrow(lifecycle::configurationAlreadyInUse);
        return properties;
    }
    
    private static Properties defaultProperties() {
        val properties = new Properties(); 
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
