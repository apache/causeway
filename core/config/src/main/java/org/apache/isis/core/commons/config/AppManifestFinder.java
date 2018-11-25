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

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.context._Context;

import static org.apache.isis.commons.internal.base._Casts.uncheckedCast;
import static org.apache.isis.commons.internal.base._Strings.isNullOrEmpty;

/**
 * 
 * @since 2.0.0-M2
 * 
 * @deprecated does not work, use {@link AppManifestLocator instead}
 */
class AppManifestFinder {
    
    private static final Logger LOG = LoggerFactory.getLogger(AppManifestFinder.class);

    static AppManifest findAppMainfestOrThrow() {
        
        // lookup all sorts of places for 'isis.properties' and other;
        // see if we can find an entry 'isis.appManifest=xxx'
        // that points to the AppManifest to use
        
        final String manifestKey = ConfigurationConstants.APP_MANIFEST_KEY;
        
        final String appManifestClassName = 
            _Strings.read(_Context.getDefaultClassLoader().getResourceAsStream("/META-INF/app.manifest"), 
                    StandardCharsets.UTF_8);
        
        //final String appManifestClassName = _Config.peekAtString(manifestKey);
        if(isNullOrEmpty(appManifestClassName)) {
            throw new IsisConfigurationException(
                    String.format("Failed to find AppManifest from config properties. No value for key '%s' found.", 
                            manifestKey));
        }
        Object appManifest;
        try {
            appManifest = _Context.loadClassAndInitialize(appManifestClassName);
        } catch (ClassNotFoundException e) {
            throw new IsisConfigurationException(
                    String.format("Failed to instantiate AppManifest from config value '%s'.", 
                            appManifestClassName));
        }
        return uncheckedCast(appManifest);
    }
    
    static Optional<AppManifest> findAppMainfest() {
        try {
            return Optional.ofNullable(findAppMainfestOrThrow());
        } catch (Exception e) {
            LOG.warn(e.getMessage());
            return Optional.empty();
        }
    }
    
    
}
