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
package org.apache.isis.core.integtestsupport;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.event.Level;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.Module;
import org.apache.isis.core.runtime.headless.HeadlessTransactionSupport;
import org.apache.isis.core.runtime.headless.HeadlessWithBootstrappingAbstract;
import org.apache.isis.core.runtime.headless.IsisSystem;
import org.apache.isis.core.runtime.headless.logging.LogConfig;

/**
 * Base class for integration tests for the JUnit 5 Jupiter Engine,
 * uses a {@link Module} to bootstrap, rather than an {@link AppManifest}.
 *
 * @since 2.0.0
 */
@ExtendWith(IntegrationTestJupiter.HeadlessTransactionRule.class)
public abstract class IntegrationTestJupiter extends HeadlessWithBootstrappingAbstract {

    public static class HeadlessTransactionRule extends TransactionRuleAbstract implements AfterEachCallback, BeforeEachCallback {

        @Override
        public void beforeEach(ExtensionContext context) throws Exception {
            final IntegrationTestJupiter testInstance = testInstance(context);
            testInstance.bootstrapAndSetupIfRequired();
        }

        @Override
        public void afterEach(ExtensionContext context) throws Exception {

            try {
                final IsisSystem isft = IsisSystem.get();
                isft.getService(HeadlessTransactionSupport.class).endTransaction();
            } catch(final Exception e) {
                handleTransactionContextException(e);
            } finally {
                final IntegrationTestJupiter testInstance = testInstance(context);
                testInstance.tearDownAllModules();
            }
        }

        // -- HELPER
        private IntegrationTestJupiter testInstance(ExtensionContext context) {
            final IntegrationTestJupiter testInstance = (IntegrationTestJupiter) context.getTestInstance().get();
            return testInstance;
        }

    }

    protected IntegrationTestJupiter(final Module module) {
        this(new LogConfig(Level.INFO), module, AppManifest.Util.MemDb.HSQLDB);
    }

    protected IntegrationTestJupiter(final Module module, final AppManifest.Util.MemDb memDb) {
        this(new LogConfig(Level.INFO), module, memDb);
    }

    protected IntegrationTestJupiter(
            final LogConfig logConfig,
            final Module module) {
        this(logConfig, module, AppManifest.Util.MemDb.HSQLDB);
    }

    protected IntegrationTestJupiter(
            final LogConfig logConfig,
            final Module module,
            final AppManifest.Util.MemDb memDb) {
        super(logConfig,
                ModuleBuilder.create(module)
                .withHeadlessTransactionSupport()
                .withIntegrationTestConfigFallback(memDb)
                .build() );
    }

    @Override
    protected void bootstrapAndSetupIfRequired() {

        super.bootstrapAndSetupIfRequired();

        log("### TEST: " + this.getClass().getCanonicalName());
    }

    @Override
    protected void tearDownAllModules() {

        super.tearDownAllModules();
    }

}