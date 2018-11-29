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

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.builder.IsisConfigurationBuilder;

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
    
    public static void acceptBuilder(Consumer<IsisConfigurationBuilder> builderConsumer) {
        builderConsumer.accept(getConfigurationBuilder());
    }
    
    public static <T> T applyBuilder(Function<IsisConfigurationBuilder, T> builderMapper) {
        return builderMapper.apply(getConfigurationBuilder());
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
    
    // -- CONFIG SHORTCUTS
    
    
    
    // -- BUILDER SHORTCUTS
    
    public static void put(String key, String value) {
        getConfigurationBuilder().put(key, value);
    }
    
    public static void put(String key, boolean value) {
        getConfigurationBuilder().put(key, ""+value);
    }
    
    public static String peekAtString(String key) {
        return getConfigurationBuilder().peekAtString(key);
    }
    
    public static String peekAtString(String key, String defaultValue) {
        return getConfigurationBuilder().peekAtString(key, defaultValue);
    }
    
    public static boolean peekAtBoolean(String key) {
        return getConfigurationBuilder().peekAtBoolean(key);
    }
    
    public static boolean peekAtBoolean(String key, boolean defaultValue) {
        return getConfigurationBuilder().peekAtBoolean(key, defaultValue);
    }
    
    
    // -- HELPER -- BUILDER
    
    private static IsisConfigurationBuilder getConfigurationBuilder() {
        _Config_LifecycleResource lifecycle = getLifecycleResource();
        IsisConfigurationBuilder builder = lifecycle.getBuilder()
                .orElseThrow(lifecycle::configurationAlreadyInUse);
        return builder;
    }
    
    private static IsisConfigurationBuilder createBuilder() {
        final IsisConfigurationBuilder builder = IsisConfigurationBuilder.getDefault(); 
        return builder;
    }

    // -- HELPER -- LIFECYCLE
    
    private static _Config_LifecycleResource getLifecycleResource() {
        final _Config_LifecycleResource lifecycle = 
                _Context.computeIfAbsent(_Config_LifecycleResource.class, __->createLifecycleResource());
        return lifecycle;
    }

    private static _Config_LifecycleResource createLifecycleResource() {
        return new _Config_LifecycleResource(createBuilder());
    }
    
    
}
