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
package org.apache.isis.testing.fixtures.applib;

import javax.inject.Inject;

import org.joda.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.apache.isis.applib.services.clock.ClockService;
import org.apache.isis.testing.fixtures.applib.api.PersonaWithBuilderScript;
import org.apache.isis.testing.fixtures.applib.clock.Clock;
import org.apache.isis.testing.fixtures.applib.clock.FixtureClock;
import org.apache.isis.testing.fixtures.applib.clock.TickingFixtureClock;
import org.apache.isis.testing.fixtures.applib.fixturescripts.BuilderScriptAbstract;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScript;
import org.apache.isis.testing.fixtures.applib.fixturescripts.FixtureScripts;
import org.apache.isis.testing.fixtures.applib.modules.ModuleWithFixturesService;
import org.apache.isis.testing.integtestsupport.applib.IsisIntegrationTestAbstract;

public abstract class IsisIntegrationTestAbstractWithFixtures extends IsisIntegrationTestAbstract {

    protected void run(final FixtureScript... fixtureScriptList) {
        this.fixtureScripts.run(fixtureScriptList);
    }

    protected <T> T runBuilder(final BuilderScriptAbstract<T> fixtureScript) {
        return this.fixtureScripts.runBuilderScript(fixtureScript);
    }

    protected <T> void runPersonas(final PersonaWithBuilderScript<? extends BuilderScriptAbstract<T>>... personaScripts) {
        this.fixtureScripts.runPersonas(personaScripts);
    }

    protected <T> T runPersona(final PersonaWithBuilderScript<? extends BuilderScriptAbstract<T>> personaScript) {
        return this.fixtureScripts.runPersona(personaScript);
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

    // -- DEPENDENCIES

    @BeforeEach
    protected void setupRefDataFixtures() {
        fixtureScripts.run(moduleWithFixturesService.getRefDataSetupFixture());
    }

    @AfterEach
    protected void tearDownFixtures() {
        fixtureScripts.run(moduleWithFixturesService.getTeardownFixture());
    }

    @Inject protected ModuleWithFixturesService moduleWithFixturesService;

    @Inject protected FixtureScripts fixtureScripts;


}
