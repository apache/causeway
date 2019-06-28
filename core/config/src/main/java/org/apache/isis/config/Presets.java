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

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * Introduced to support Spring's {@code @PropertySource} annotation.
 * 
 * @since 2.0
 *
 */
public final class Presets  {

	public static final String ISIS_PERSISTOR                   = "isis.persistor.";
    public static final String ISIS_PERSISTOR_DATANUCLEUS       = ISIS_PERSISTOR + "datanucleus.";
    public static final String ISIS_PERSISTOR_DATANUCLEUS_IMPL  = ISIS_PERSISTOR_DATANUCLEUS + "impl.";
	
    @RequiredArgsConstructor
    private static enum Providers {
    
        H2InMemory(Providers::withH2InMemoryProperties),
        HsqlDbInMemory(Providers::withHsqlDbInMemoryProperties),
        DataNucleus(Providers::withDataNucleusProperties),
        IsisIntegTest(Providers::withIsisIntegTestProperties),
		NoTranslations(map->map.put("isis.services.translation.po.mode", "disable")),
        ;
        
        private final Consumer<Map<String, String>> populator;
        
        private static Map<String,String> withH2InMemoryProperties(final Map<String, String> map) {
            //map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionURL", "jdbc:h2:mem:test-" + UUID.randomUUID().toString());
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionURL", "jdbc:h2:mem:test");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionDriverName", "org.h2.Driver");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionUserName", "sa");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionPassword", "");
            
            return map;
        }
        
        private static Map<String,String> withHsqlDbInMemoryProperties(final Map<String, String> map) {
            //map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test-" + UUID.randomUUID().toString());
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:test");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionUserName", "sa");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "javax.jdo.option.ConnectionPassword", "");
            
            return map;
        }

        private static Map<String,String> withDataNucleusProperties(final Map<String, String> map) {

            // Don't do validations that consume setup time.
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.schema.autoCreateAll", "true");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.schema.validateAll", "false");

            // other properties as per WEB-INF/persistor_datanucleus.properties
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.persistenceByReachabilityAtCommit", "false");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.identifier.case", "MixedCase");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.cache.level2.type"  ,"none");
            map.put(ISIS_PERSISTOR_DATANUCLEUS_IMPL + "datanucleus.cache.level2.mode", "ENABLE_SELECTIVE");

            return map;
        }

        private static Map<String,String> withIsisIntegTestProperties(final Map<String, String> map) {

            // automatically install any fixtures that might have been registered
            map.put(ISIS_PERSISTOR_DATANUCLEUS + "install-fixtures", "true");
            map.put(ISIS_PERSISTOR + "enforceSafeSemantics", "false");
            map.put("isis.services.eventbus.allowLateRegistration", "true");

            return map;
        }

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
