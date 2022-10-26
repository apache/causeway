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
package org.apache.causeway.extensions.excel.integtests;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.extensions.excel.fixtures.CausewayModuleExtExcelFixtures;
import org.apache.causeway.persistence.jdo.applib.CausewayModulePersistenceJdoApplib;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testing.fixtures.applib.CausewayIntegrationTestAbstractWithFixtures;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;

@SpringBootTest(
        classes = ExcelModuleIntegTestAbstract.AppManifest.class
)
@TestPropertySource(CausewayPresets.UseLog4j2Test)
@ContextConfiguration
@Transactional
public abstract class ExcelModuleIntegTestAbstract extends CausewayIntegrationTestAbstractWithFixtures {

    @Configuration
    @PropertySources({
            @PropertySource(CausewayPresets.NoTranslations),
            @PropertySource(CausewayPresets.DatanucleusAutocreateNoValidate),
    })
    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModulePersistenceJdoApplib.class,
            CausewayModuleTestingFixturesApplib.class,

            CausewayModuleExtExcelFixtures.class
    })
    public static class AppManifest {
    }

}
