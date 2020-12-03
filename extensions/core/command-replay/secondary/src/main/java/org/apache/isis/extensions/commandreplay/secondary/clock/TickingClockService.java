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
package org.apache.isis.extensions.commandreplay.secondary.clock;

import java.sql.Timestamp;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;
import javax.inject.Named;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.core.config.IsisConfiguration;
import org.apache.isis.testing.fixtures.applib.clock.Clock;
import org.apache.isis.testing.fixtures.applib.clock.TickingFixtureClock;

import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.extern.log4j.Log4j2;

/**
 * Only enabled for the <tt>secondary</tt> profile, where it sets up the
 * framework to use {@link TickingFixtureClock} so that time can be changed
 * dynamically when running.
 *
 * <p>
 *     As an additional safeguard, if the configuration keys to access the
 *     primary are not provided, then the service will not initialize.
 * </p>
 *
 * <p>
 *     IMPORTANT: the methods provided by this service are not thread-safe,
 *     because the clock is a globally-scoped singleton rather than a
 *     thread-local.  These methods should therefore only be used in single-user
 *     systems, eg a replay secondary.
 * </p>
 */
@Service()
@Named("isisExtensionsCommandReplaySecondary.TickingClockService")
@Order(OrderPrecedence.MIDPOINT)
@Log4j2
@RequiredArgsConstructor
public class TickingClockService {

    final IsisConfiguration isisConfiguration;

    @PostConstruct
    public void init() {
        val baseUrl = isisConfiguration.getExtensions().getCommandReplay().getPrimaryAccess().getBaseUrlRestful();
        val user = isisConfiguration.getExtensions().getCommandReplay().getPrimaryAccess().getUser();
        val password = isisConfiguration.getExtensions().getCommandReplay().getPrimaryAccess().getPassword();

        if( !baseUrl.isPresent()||
            !user.isPresent() ||
            !password.isPresent()) {
            log.warn("init() - skipping, one or more 'isis.extensions.command-replay.primary' configuration properties has not been set");
            return;
        }

        log.info("init() - replacing existing clock with TickingFixtureClock");
        TickingFixtureClock.replaceExisting();
    }

    public boolean isInitialized() {
        return Clock.getInstance() instanceof TickingFixtureClock;
    }


    /**
     * Executes the runnable, setting the clock to be the specified time
     * beforehand (and reinstating it to its original time afterwards).
     *
     * <p>
     *     IMPORTANT: this method is not thread-safe, because the clock is a
     *     globally-scoped singleton rather than a thread-local.  This method
     *     should therefore only be used in single-user systems, eg a replay
     *     secondary.
     * </p>
     */
    public void at(Timestamp timestamp, Runnable runnable) {
        ensureInitialized();

        val tickingFixtureClock = (TickingFixtureClock) TickingFixtureClock.getInstance();
        val previous = TickingFixtureClock.getEpochMillis();
        val wallTime0 = System.currentTimeMillis();
        try {
            tickingFixtureClock.setTime(timestamp);
            runnable.run();
        } finally {
            final long wallTime1 = System.currentTimeMillis();
            tickingFixtureClock.setTime(previous + wallTime1 - wallTime0);
        }
    }

    /**
     * Executes the callable, setting the clock to be the specified time
     * beforehand (and reinstating it to its original time afterwards).
     *
     * <p>
     *     IMPORTANT: this method is not thread-safe, because the clock is a
     *     globally-scoped singleton rather than a thread-local.  This method
     *     should therefore only be used in single-user systems, eg a replay
     *     secondary.
     * </p>
     */
    public <T> T at(Timestamp timestamp, Supplier<T> supplier) {
        ensureInitialized();

        val tickingFixtureClock = (TickingFixtureClock) TickingFixtureClock.getInstance();

        val previous = TickingFixtureClock.getEpochMillis();
        val wallTime0 = System.currentTimeMillis();

        try {
            tickingFixtureClock.setTime(timestamp);
            return supplier.get();
        } finally {
            final long wallTime1 = System.currentTimeMillis();
            tickingFixtureClock.setTime(previous + wallTime1 - wallTime0);
        }
    }

    private void ensureInitialized() {
        if(!isInitialized()) {
            throw new IllegalStateException(
                "Not initialized.  " +
                "Make sure that the application is configured as a " +
                "replay secondary by configuring the " +
                "'isis.extensions.command-replay.primary' " +
                "configuration properties.");
        }
    }

}