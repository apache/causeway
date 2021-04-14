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
package org.apache.isis.testing.fakedata.integtests;

import javax.transaction.Transactional;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.runtimeservices.IsisModuleCoreRuntimeServices;
import org.apache.isis.persistence.jdo.applib.IsisModulePersistenceJdoApplib;
import org.apache.isis.security.bypass.IsisModuleSecurityBypass;
import org.apache.isis.testing.fakedata.fixtures.IsisModuleTestingFakeDataFixtures;
import org.apache.isis.testing.fixtures.applib.IsisIntegrationTestAbstractWithFixtures;
import org.apache.isis.testing.fixtures.applib.IsisModuleTestingFixturesApplib;

@SpringBootTest(
        classes = FakeDataModuleIntegTestAbstract.AppManifest.class
)
@TestPropertySource(IsisPresets.UseLog4j2Test)
@ContextConfiguration
@Transactional
public abstract class FakeDataModuleIntegTestAbstract extends IsisIntegrationTestAbstractWithFixtures {

        @Configuration
        @PropertySources({
                @PropertySource(IsisPresets.NoTranslations),
                @PropertySource(IsisPresets.DatanucleusAutocreateNoValidate),
        })
        @Import({
                IsisModuleCoreRuntimeServices.class,
                IsisModuleSecurityBypass.class,
                IsisModulePersistenceJdoApplib.class,
                IsisModuleTestingFixturesApplib.class,
                IsisModuleTestingFakeDataFixtures.class
        })
        public static class AppManifest {
        }

}
