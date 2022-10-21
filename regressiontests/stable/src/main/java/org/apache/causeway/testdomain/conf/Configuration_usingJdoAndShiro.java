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
package org.apache.causeway.testdomain.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.jdo.datanucleus.CausewayModulePersistenceJdoDatanucleus;
import org.apache.causeway.security.shiro.CausewayModuleSecurityShiro;
import org.apache.causeway.testdomain.jdo.JdoTestDomainModule;
import org.apache.causeway.testdomain.util.kv.KVStoreForTesting;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;

@Configuration
@Import({
    CausewayModuleCoreRuntimeServices.class,
    CausewayModuleSecurityShiro.class,
    CausewayModulePersistenceJdoDatanucleus.class,
    CausewayModuleTestingFixturesApplib.class,
    KVStoreForTesting.class, // Helper for JUnit Tests
})
@ComponentScan(
        basePackageClasses= {
                JdoTestDomainModule.class
        })
@PropertySources({
    @PropertySource(CausewayPresets.NoTranslations),
    @PropertySource(CausewayPresets.DatanucleusAutocreateNoValidate),
    @PropertySource(CausewayPresets.H2InMemory_withUniqueSchema),
})
public class Configuration_usingJdoAndShiro {

}
