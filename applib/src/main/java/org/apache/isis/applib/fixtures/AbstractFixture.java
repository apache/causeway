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

package org.apache.isis.applib.fixtures;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.fixtures.switchuser.SwitchUserService;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserServiceAware;

/**
 * Convenience class for creating fixtures.
 * 
 * <p>
 * Most subclasses will simply override {@link #install()} to setup objects. In
 * addition though fixtures may also:
 * <ul>
 * <li>change the date/time within the course of fixture installation, using
 * {@link #setDate(int, int, int)} and {@link #setTime(int, int)}.
 * <li>change the current user using {@link #switchUser(String, String...)}.
 * <li>create composite fixtures using {@link #addFixture(Object)}.
 * <li>search for existing objects using
 * {@link #firstMatch(Class, org.apache.isis.applib.Filter)} or
 * {@link #uniqueMatch(Class, org.apache.isis.applib.Filter)} (and various
 * overloads thereof).
 * </ul>
 * 
 * <p>
 * To automatically logon for the demo/test, use {@link LogonFixture}.
 */
public abstract class AbstractFixture extends BaseFixture implements CompositeFixture, SwitchUserServiceAware {

    private final List<Object> fixtures = new ArrayList<Object>();

    // ///////////////////////////////////////////////////////////////
    // Constructor
    // ///////////////////////////////////////////////////////////////

    /**
     * Assumed to be {@link FixtureType#DOMAIN_OBJECTS data} fixture.
     */
    public AbstractFixture() {
        this(FixtureType.DOMAIN_OBJECTS);
    }

    public AbstractFixture(final FixtureType fixtureType) {
        super(fixtureType);
    }

    // ///////////////////////////////////////////////////////////////
    // install() hook (for non-composites)
    // ///////////////////////////////////////////////////////////////

    /**
     * Most subclasses will override this method, but composite fixtures should
     * instead call {@link #addFixture(Object)} in their constructor.
     */
    @Override
    public void install() {
    }

    // ///////////////////////////////////////////////////////////////
    // CompositeFixture impl
    // ///////////////////////////////////////////////////////////////

    /**
     * Allows the fixture to act as a composite (call within constructor).
     */
    protected void addFixture(final Object fixture) {
        fixtures.add(fixture);
    }

    /**
     * Returns an array of any fixtures that have been
     * {@link #addFixture(Object) added}.
     */
    @Override
    public List<Object> getFixtures() {
        return Collections.unmodifiableList(fixtures);
    }

    // ///////////////////////////////////////////////////////////////
    // Date and time
    // ///////////////////////////////////////////////////////////////

    /**
     * Will print warning message and do nothing if {@link FixtureClock} could
     * not be {@link FixtureClock#initialize() initialized}.
     */
    public void earlierDate(final int years, final int months, final int days) {
        if (shouldIgnoreCallBecauseNoClockSetup("earlierDate()")) {
            return;
        }
        clock.addDate(-years, -months, -days);
    }

    /**
     * Will print warning message and do nothing if {@link FixtureClock} could
     * not be {@link FixtureClock#initialize() initialized}.
     */
    public void earlierTime(final int hours, final int minutes) {
        if (shouldIgnoreCallBecauseNoClockSetup("earlierTime()")) {
            return;
        }
        clock.addTime(-hours, -minutes);
    }

    /**
     * Will print warning message and do nothing if {@link FixtureClock} could
     * not be {@link FixtureClock#initialize() initialized}.
     */
    public void laterDate(final int years, final int months, final int days) {
        if (shouldIgnoreCallBecauseNoClockSetup("laterDate()")) {
            return;
        }
        clock.addDate(years, months, days);
    }

    /**
     * Will print warning message and do nothing if {@link FixtureClock} could
     * not be {@link FixtureClock#initialize() initialized}.
     */
    public void laterTime(final int hours, final int minutes) {
        if (shouldIgnoreCallBecauseNoClockSetup("laterTime()")) {
            return;
        }
        clock.addTime(hours, minutes);
    }

    // {{ User
    protected void switchUser(final String username, final String... roles) {
        switchUserService.switchUser(username, roles);
    }

    // }}

    // {{ Injected: SwitchUserService
    private SwitchUserService switchUserService;

    @Override
    public void setService(final SwitchUserService fixtureService) {
        this.switchUserService = fixtureService;
    }
    // }}

}
