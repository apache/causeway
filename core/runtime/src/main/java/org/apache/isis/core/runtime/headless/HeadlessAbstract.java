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
package org.apache.isis.core.runtime.headless;

import javax.inject.Inject;

import org.joda.time.LocalDate;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.applib.fixtures.FixtureClock;
import org.apache.isis.applib.fixtures.TickingFixtureClock;
import org.apache.isis.testing.fixtures.applib.personas.BuilderScriptAbstract;
import org.apache.isis.applib.fixturescripts.FixtureScript;
import org.apache.isis.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.fixtures.applib.personas.PersonaWithBuilderScript;
import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.metamodel.MetaModelService4;
import org.apache.isis.applib.services.registry.ServiceRegistry2;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.sessmgmt.SessionManagementService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService3;

/**
 * Reworked base class for headless access.
 *
 * <p>
 *     The most common cases are integration tests or BDD spec glue.
 * </p>
 */
public abstract class HeadlessAbstract {


    protected void runFixtureScript(final FixtureScript... fixtureScriptList) {
        this.fixtureScripts.runFixtureScript(fixtureScriptList);
    }

    protected <T> T runBuilderScript(final BuilderScriptAbstract<T> fixtureScript) {
        return this.fixtureScripts.runBuilder(fixtureScript);
    }

    protected <P extends PersonaWithBuilderScript<T,F>, T, F extends BuilderScriptAbstract<T>> T runBuilderScript(final P persona) {
        return runBuilderScript(persona.builder());
    }


    /**
     * For convenience of subclasses, remove some boilerplate
     */
    protected <T> T wrap(final T obj) {
        return wrapperFactory.wrap(obj);
    }

    /**
     * For convenience of subclasses, remove some boilerplate
     */
    protected <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        return factoryService.mixin(mixinClass, mixedIn);
    }


    /**
     * To use instead of {@link #getFixtureClock()}'s {@link FixtureClock#setDate(int, int, int)} ()}.
     */
    protected void setFixtureClockDate(final LocalDate date) {
        if(date == null) {
            return;
        }
        setFixtureClockDate(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth());
    }

    /**
     * To use instead of {@link #getFixtureClock()}'s {@link FixtureClock#setDate(int, int, int)} ()}.
     */
    protected void setFixtureClockDate(final int year, final int month, final int day) {
        final Clock instance = Clock.getInstance();

        if(instance instanceof TickingFixtureClock) {
            TickingFixtureClock.reinstateExisting();
            getFixtureClock().setDate(year, month, day);
            TickingFixtureClock.replaceExisting();
        }

        if(instance instanceof FixtureClock) {
            getFixtureClock().setDate(year, month, day);
        }
    }

    /**
     * If just require the current time, use {@link ClockService}
     */
    private FixtureClock getFixtureClock() {
        return ((FixtureClock)FixtureClock.getInstance());
    }


    /**
     * For convenience of subclasses, remove some boilerplate
     */
    protected <T> T unwrap(final T obj) {
        return wrapperFactory.unwrap(obj);
    }

    @Inject
    protected MetaModelService4 metaModelService4;
    @Inject
    protected FixtureScripts fixtureScripts;
    @Inject
    protected FactoryService factoryService;
    @Inject
    protected ServiceRegistry2 serviceRegistry;
    @Inject
    protected RepositoryService repositoryService;
    @Inject
    protected UserService userService;
    @Inject
    protected WrapperFactory wrapperFactory;
    @Inject
    protected TransactionService3 transactionService;
    @Inject
    protected SessionManagementService sessionManagementService;

}
