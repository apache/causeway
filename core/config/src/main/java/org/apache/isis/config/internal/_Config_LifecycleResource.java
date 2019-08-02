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

import java.util.Map;
import java.util.Optional;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.config.IsisConfiguration;

import static org.apache.isis.commons.internal.base._With.requires;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
class _Config_LifecycleResource {

    private final Map<String, String> mutableProperties; 
    private final _Lazy<IsisConfiguration> configuration;

    _Config_LifecycleResource(final Map<String, String> mutableProperties) {
        requires(mutableProperties, "mutableProperties");
        this.mutableProperties = mutableProperties;
        this.configuration = _Lazy.threadSafe(this::build);
    }

    IsisConfiguration getConfiguration() {
        return configuration.get();
    }

    /**
     * Returns an empty Optional, if getConfiguration() was called earlier. This 
     * is to ensure, immutability once config was 'finalized'. 
     * @return
     */
    Optional<Map<String, String>> getMutableProperties() {
        if(!configuration.isMemoized()) {
            return Optional.of(mutableProperties);    
        }
        return Optional.empty();
    }

    private IsisConfiguration build() {

        // we throw an exception, catch it and keep it for later, to provide
        // causal information, in case the builder is accessed after it already
        // built the configuration
        try {
            throw new IllegalStateException("IsisConfiguration Build (previously already triggered by ...)");
        } catch(IllegalStateException e) {
            this.configurationBuildStacktrace = e;
        }

        val config =  _Config_Instance.ofProperties(
                _Config_trim.trim(mutableProperties), 
                _Config_Parsers::parseBoolean,
                _Config_Parsers::parseInteger,
                _Config_Parsers::parseColor,
                _Config_Parsers::parseFont,
                _Config_Parsers::parseList);

        log.info("=== Config Finalized ===");

        return config;

    }

    // -- HELPER -- EXCEPTIONS

    private IllegalStateException configurationBuildStacktrace;

    IllegalStateException configurationAlreadyInUse() {

        IllegalStateException cause = configurationBuildStacktrace;

        return new IllegalStateException("The IsisConfigurationBuilder is no longer valid, because it has "
                + "already built the IsisConfiguration for this application's life-cycle.", cause);
    }


}
