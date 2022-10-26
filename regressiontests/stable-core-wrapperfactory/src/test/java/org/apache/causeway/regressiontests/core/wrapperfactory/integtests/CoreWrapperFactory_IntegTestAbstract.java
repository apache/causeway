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
package org.apache.causeway.regressiontests.core.wrapperfactory.integtests;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;

import org.apache.causeway.commons.functional.ThrowingRunnable;
import org.apache.causeway.core.config.presets.CausewayPresets;
import org.apache.causeway.core.runtimeservices.CausewayModuleCoreRuntimeServices;
import org.apache.causeway.persistence.jdo.datanucleus.CausewayModulePersistenceJdoDatanucleus;
import org.apache.causeway.security.bypass.CausewayModuleSecurityBypass;
import org.apache.causeway.testdomain.wrapperfactory.Counter;
import org.apache.causeway.testdomain.wrapperfactory.CounterRepository;
import org.apache.causeway.testdomain.wrapperfactory.WrapperTestFixtures;
import org.apache.causeway.testing.fixtures.applib.CausewayIntegrationTestAbstractWithFixtures;
import org.apache.causeway.testing.fixtures.applib.CausewayModuleTestingFixturesApplib;

@SpringBootTest(
        classes = CoreWrapperFactory_IntegTestAbstract.AppManifest.class
)
@ActiveProfiles("test")
public abstract class CoreWrapperFactory_IntegTestAbstract extends CausewayIntegrationTestAbstractWithFixtures {

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({
            CausewayModuleCoreRuntimeServices.class,
            CausewayModuleSecurityBypass.class,
            CausewayModulePersistenceJdoDatanucleus.class,
            CausewayModuleTestingFixturesApplib.class,

            WrapperTestFixtures.class,
    })
    @PropertySources({
            @PropertySource(CausewayPresets.H2InMemory_withUniqueSchema),
            @PropertySource(CausewayPresets.DatanucleusAutocreateNoValidate),
            @PropertySource(CausewayPresets.DatanucleusEagerlyCreateTables),
            @PropertySource(CausewayPresets.UseLog4j2Test),
    })
    public static class AppManifest {
    }

    @BeforeAll
    static void beforeAll() {
        CausewayPresets.forcePrototyping();
    }

    protected Counter newCounter(final String name) {
        return Counter.builder().name(name).build();
    }

    protected final void runWithNewTransaction(final ThrowingRunnable runnable) {
        transactionService.runTransactional(Propagation.REQUIRES_NEW, runnable)
        .ifFailureFail();
    }

    @Inject protected CounterRepository counterRepository;
}
