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

import java.util.function.Supplier;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.configbuilder.IsisConfigurationBuilder;

import static org.apache.isis.commons.internal.base._With.mapIfPresentElse;
import static org.apache.isis.commons.internal.base._With.mapIfPresentElseThrow;
import static org.apache.isis.commons.internal.base._With.requires;

public class _Config {
    
    // -- CONFIG SUPPLIER INTERFACE
    
    @FunctionalInterface
    public static interface ConfigSupplier extends Supplier<IsisConfiguration> {
        
    }
    
    // -- CONFIG RETRIEVAL
    
    public static IsisConfiguration getConfiguration() {
        final ConfigSupplier supplier = _Context.getIfAny(ConfigSupplier.class);
        return mapIfPresentElse(supplier, ConfigSupplier::get, null);
    }
    
    public static IsisConfiguration getConfigurationElseThrow() {
        final ConfigSupplier supplier = _Context.getOrThrow(ConfigSupplier.class, 
                ()->new IllegalStateException("No ConfigSupplier registered on current context."));
        
        return mapIfPresentElseThrow(supplier, ConfigSupplier::get,
                ()->new IllegalStateException("The ConfigSupplier registered on current context did return null."));
    }

    /**
     * Sets the current context's configuration supplier via provided parameter. Will not override
     * any ConfigSupplier instance, that is already registered with the current context, 
     * because the ConfigSupplier is expected to be a singleton within an application's 
     * life-cycle.
     */
    public static void registerConfigurationSupplierIfNotAlready(ConfigSupplier configSupplier) {
        requires(configSupplier, "configSupplier");
        _Context.computeIfAbsent(ConfigSupplier.class, __->configSupplier);
    }
    
    public static IsisConfigurationBuilder configurationBuilderForTesting() {
        final IsisConfigurationBuilder builder = new IsisConfigurationBuilder();
        final ConfigSupplier configSupplier = new _Config_SupplierUsingBuilder(builder);
        registerConfigurationSupplierIfNotAlready(configSupplier);
        return builder; 
    }
    
    
}
