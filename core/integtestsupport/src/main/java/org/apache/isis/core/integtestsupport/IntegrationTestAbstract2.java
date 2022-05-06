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

import javax.inject.Inject;

import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.applib.AppManifestAbstract;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.integtestsupport.scenarios.ScenarioExecutionForIntegration;
import org.apache.isis.objectstore.jdo.datanucleus.IsisConfigurationForJdoIntegTests;

/**
 * Extended base class for integration tests.
 *
 * @deprecated - to be replaced by {@link IntegrationTestAbstract3}
 */
@Deprecated
public abstract class IntegrationTestAbstract2 extends IntegrationTestAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(IntegrationTestAbstract2.class);

    /**
     * Convenience, will call {@link AppManifestAbstract.Builder#build() build} on the provided
     * {@link AppManifestAbstract.Builder Builder}, then delegate to {@link #bootstrapUsing(AppManifest)}.
     */
    protected static void bootstrapUsing(final AppManifestAbstract.Builder builder) {
        bootstrapUsing(builder.build());
    }

    /**
     * Intended to be called from the subclass' <code>@BeforeClass init()</code> method.
     */
    protected static void bootstrapUsing(final AppManifest appManifest) {
        org.apache.log4j.PropertyConfigurator.configure("logging-integtest.properties");
        IsisSystemForTest isft = IsisSystemForTest.getElseNull();
        if(isft == null) {
            isft = new IsisSystemForTest.Builder()
                    .withLoggingAt(org.apache.log4j.Level.INFO)
                    .with(appManifest)
                    .with(new IsisConfigurationForJdoIntegTests())
                    .build()
                    .setUpSystem();
            IsisSystemForTest.set(isft);
        }

        // instantiating will install onto ThreadLocal
        new ScenarioExecutionForIntegration();
    }

    /**
     * Replacement for the deprecated {@link #runScript(FixtureScript...)}.
     */
    protected void runFixtureScript(final FixtureScript... fixtureScriptList) {
        if(fixtureScriptList.length == 1) {
            fixtureScripts.runFixtureScript(fixtureScriptList[0], null);
        } else {
            fixtureScripts.runFixtureScript(new org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript() {
                @Override
                protected void execute(final FixtureScript.ExecutionContext executionContext) {
                    for (FixtureScript fixtureScript : fixtureScriptList) {
                        executionContext.executeChild(this, fixtureScript);
                    }
                }
            }, null);
        }
        nextTransaction();
    }

    protected static TypeSafeMatcher<Throwable> causedBy(final Class<?> type) {
        return ThrowableMatchers.causedBy(type);
    }

    @Inject
    protected FixtureScripts fixtureScripts;

    @Inject
    protected FactoryService factoryService;

    @javax.inject.Inject
    protected ServiceRegistry2 serviceRegistry;

    @Inject
    RepositoryService repositoryService;

    @javax.inject.Inject
    protected UserService userService;

    @javax.inject.Inject
    protected WrapperFactory wrapperFactory;

    @Inject
    protected TransactionService transactionService;

    @Inject
    protected SessionManagementService sessionManagementService;

}

