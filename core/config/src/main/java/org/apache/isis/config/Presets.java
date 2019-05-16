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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;

import org.apache.isis.applib.AppManifest;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Introduced to support Spring's {@code @PrepertySource} annotation.
 * 
 * @since 2.0.0
 *
 */
public final class Presets  {

    @RequiredArgsConstructor
    private static enum Providers {
    
        H2InMemory(AppManifest.Presets::withH2InMemoryProperties),
        HsqlDbInMemory(AppManifest.Presets::withHsqlDbInMemoryProperties),
        DataNucleus(AppManifest.Presets::withDataNucleusProperties),
        IsisIntegTest(AppManifest.Presets::withIsisIntegTestProperties),
		NoTranslations(map->map.put("isis.services.translation.po.mode", "disable")),
    
        ;
        
        private final Consumer<Map<String, String>> populator;
    }
    
    public static final String H2InMemory = "H2InMemory";
    public static final String HsqlDbInMemory = "HsqlDbInMemory";
    public static final String DataNucleus = "DataNucleus";
    public static final String IsisIntegTest = "IsisIntegTest";
    public static final String NoTranslations = "NoTranslations";
    
    
    public static class Factory implements PropertySourceFactory {

        @Override
        public PropertySource<?> createPropertySource(
                String name,
                EncodedResource resource) throws IOException {
            
            val map = new HashMap<String, String>(); 
            Providers.valueOf(name).populator.accept(map);
            
            val widenedMap = new HashMap<String, Object>();
            widenedMap.putAll(map);
            
            val propSource = new MapPropertySource(name, widenedMap);
            
            return propSource;
        }
       
    }
    
}
