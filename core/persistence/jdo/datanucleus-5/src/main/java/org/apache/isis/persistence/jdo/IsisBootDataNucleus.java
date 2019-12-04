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
package org.apache.isis.persistence.jdo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.isis.persistence.jdo.datanucleus.DataNucleusSettings;
import org.apache.isis.persistence.jdo.datanucleus.service.JdoPersistenceLifecycleService;
import org.apache.isis.persistence.jdo.jdosupport.IsisJdoSupportDN5;
import org.apache.isis.persistence.jdo.jdosupport.mixins.Persistable_datanucleusIdLong;
import org.apache.isis.persistence.jdo.metamodel.JdoProgrammingModelPlugin;
import org.apache.isis.persistence.jdo.metrics.MetricsServiceDefault;
import org.apache.isis.persistence.jdo.persistence.IsisPlatformTransactionManagerForJdo;
import org.apache.isis.persistence.jdo.persistence.PersistenceSessionFactory5;

@Configuration
@Import({
    DataNucleusSettings.class, // config bean
    JdoProgrammingModelPlugin.class, // metamodel extensions
    JdoPersistenceLifecycleService.class,
    MetricsServiceDefault.class,
    IsisJdoSupportDN5.class,
    IsisPlatformTransactionManagerForJdo.class,
    PersistenceSessionFactory5.class
})
@ComponentScan(
        basePackageClasses= {
                // bring in the mixins
                Persistable_datanucleusIdLong.class,
        })
public class IsisBootDataNucleus {
    
    // reserved for datanucleus' own config props
    @ConfigurationProperties(prefix = "isis.persistor.datanucleus.impl")
    @Bean("dn-settings")
    public Map<String, String> getAsMap() {
        return new HashMap<>();
    }
    
}
