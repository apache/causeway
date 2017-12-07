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

import java.util.List;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import org.apache.isis.applib.AppManifest;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.authentication.AuthenticationRequest;
import org.apache.isis.core.runtime.system.session.IsisSessionFactoryBuilder;
import org.apache.isis.core.specsupport.scenarios.DomainServiceProvider;

/**
 * Wraps a plain {@link IsisSessionFactoryBuilder}, and provides a number of features to assist with testing.
 */
public class IsisSystemForTest extends IsisSystem<IsisSystemForTest>
        implements org.junit.rules.TestRule, DomainServiceProvider {

    //region > getElseNull, get, set

    public static IsisSystemForTest getElseNull() {
        return (IsisSystemForTest) IsisSystem.getElseNull();
    }

    public static IsisSystemForTest get() {
        return (IsisSystemForTest) IsisSystem.get();
    }

    public static void set(IsisSystem isft) {
        IsisSystem.set(isft);
    }
    //endregion

    //region > Builder

    public static class Builder extends IsisSystem.Builder<IsisSystemForTest.Builder, IsisSystemForTest> {

        public IsisSystemForTest build() {
            final IsisSystemForTest isisSystemForTest =
                    new IsisSystemForTest(
                            appManifestIfAny,
                            configuration,
                            authenticationRequest,
                            listeners);
            return configure(isisSystemForTest);
        }


    }

    public static Builder builder() {
        return new Builder();
    }

    //endregion

    //region > constructor, fields

    // these fields 'xxxForComponentProvider' are used to initialize the IsisComponentProvider, but shouldn't be used thereafter.


    IsisSystemForTest(
            final AppManifest appManifestIfAny,
            final IsisConfiguration configurationOverride,
            final AuthenticationRequest authenticationRequestIfAny,
            final List<Listener> listeners) {
        super(appManifestIfAny, configurationOverride, authenticationRequestIfAny, listeners);
    }

    //endregion


    //region > setup (also componentProvider)


    //region > JUnit @Rule integration

    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                setUpSystem();
                try {
                    base.evaluate();
                    closeSession();
                } catch(Throwable ex) {
                    try {
                        closeSession();
                    } catch(Exception ex2) {
                        // ignore, since already one pending
                    }
                    throw ex;
                }
            }
        };
    }

    //endregion


    //region > getService, replaceService

    @Override
    public <T> void replaceService(final T originalService, final T replacementService) {
        final ServicesInjector servicesInjector = isisSessionFactory.getServicesInjector();
        servicesInjector.replaceService(originalService, replacementService);
    }

    //endregion


}
