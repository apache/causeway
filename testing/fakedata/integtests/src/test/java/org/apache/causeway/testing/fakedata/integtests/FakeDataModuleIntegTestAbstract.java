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
package org.apache.causeway.testing.fakedata.integtests;

import jakarta.transaction.Transactional;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.jpa.applib.CausewayModulePersistenceJpaApplib;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.fakedata.fixtures.CausewayModuleTestingFakeDataFixtures;
import org.apache.causeway.testing.fixtures.applib.CausewayIntegrationTestAbstractWithFixtures;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;

@SpringBootTest(
        classes = FakeDataModuleIntegTestAbstract.TestManifest.class
)
@ContextConfiguration
@Transactional
public abstract class FakeDataModuleIntegTestAbstract extends CausewayIntegrationTestAbstractWithFixtures {

        @Configuration
        @PropertySources({
                @PropertySource(CausewayPresets.NoTranslations),
        })
        @Import({
                CausewayModuleCoreRuntimeServices.class,
                CausewayModuleSecurityBypass.class,
                CausewayModulePersistenceJpaApplib.class,
                CausewayModuleTestingFixturesApplib.class,
                CausewayModuleTestingFakeDataFixtures.class
        })
        public static class TestManifest {
        }

}
