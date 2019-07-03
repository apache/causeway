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

package org.apache.isis.extensions.fixtures.legacy;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import org.apache.isis.applib.clock.Clock;
import org.apache.isis.extensions.fixtures.legacy.FixtureClock;

public class FixtureClockInstantiationTest {

    @Before
    public void setUp() {

    }

    @Test
    public void shouldSetupClockSingletonWithFixtureClockWhenInitialize() {
        FixtureClock.initialize();
        assertThat(Clock.getInstance(), is(instanceOf(FixtureClock.class)));
    }

    @Test
    public void canInitializeFixtureClockMultipleTimesButAlwaysGetTheSameFixtureClock() {
        final FixtureClock fixtureClock1 = FixtureClock.initialize();
        final FixtureClock fixtureClock2 = FixtureClock.initialize();
        assertThat(fixtureClock1, is(fixtureClock2));
    }

    @Test
    public void canRemoveFixtureClock() {
        FixtureClock.initialize();
        assertThat(FixtureClock.remove(), is(true));
        assertThat(FixtureClock.remove(), is(false)); // already removed.
    }

}
