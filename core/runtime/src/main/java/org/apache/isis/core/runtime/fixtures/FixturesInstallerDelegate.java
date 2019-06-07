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

package org.apache.isis.core.runtime.fixtures;

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.fixtures.FixtureType;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.fixturescripts.events.FixturesInstalledEvent;
import org.apache.isis.applib.fixturescripts.events.FixturesInstallingEvent;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.context.session.RuntimeEventService;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManagerJdoInternal;

import lombok.val;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class FixturesInstallerDelegate {

    // -- Constructor, fields

    public FixturesInstallerDelegate() {

    }

    private final List<Object> fixtures = _Lists.newArrayList();

    /**
     * Automatically flattens any {@link List}s, recursively (depth-first) if
     * necessary.
     */
    public void addFixture(final Object fixture) {
        if (fixture instanceof List) {
            final List<Object> fixtureList = ObjectExtensions.asListT(fixture, Object.class);
            for (final Object eachFixture : fixtureList) {
                addFixture(eachFixture);
            }
        } else {
            fixtures.add(fixture);
        }
    }

    /**
     * Returns all fixtures that have been {@link #addFixture(Object) added}.
     */
    protected List<Object> getFixtures() {
        return Collections.unmodifiableList(fixtures);
    }



    // -- installFixtures

    /**
     * Installs all {{@link #addFixture(Object) added fixtures} fixtures (ie as
     * returned by {@link #getFixtures()}).
     *
     * <p>
     * The set of fixtures (as per {@link #getFixtures()}) is <i>not</i> cleared
     * after installation; the intention being to allow the
     * {@link org.apache.isis.core.runtime.fixtures.FixturesInstallerAbstract} to be
     * reused across multiple tests (REVIEW: does that make sense?)
     */
    public void installFixtures() {
        val configuration = getConfiguration();
        val fireEvents = configuration.getBoolean("isis.fixtures.fireEvents", true);
        val eventService = getRuntimeEventService();
        try {
            if(fireEvents) {
            	eventService.fireFixturesInstalling(new FixturesInstallingEvent(this));
            }
            installFixtures(getFixtures());
        } finally {
            if(fireEvents) {
            	eventService.fireFixturesInstalled(new FixturesInstalledEvent(this));
            }
        }
    }

    private IsisConfiguration getConfiguration() {
        return _Config.getConfiguration();
    }

    private void installFixtures(final List<Object> fixtures) {
        for (final Object fixture : fixtures) {
            installFixtureInTransaction(fixture);
        }
    }

    private void installFixtureInTransaction(final Object fixture) {
        getServiceInjector().injectServicesInto(fixture);

        // now, install the fixture itself
        try {
            log.info("installing fixture: {}", fixture);
            getTransactionManager().startTransaction();
            installFixture(fixture);
            getTransactionManager().endTransaction();
            log.info("fixture installed");
        } catch (final RuntimeException e) {
            log.error("installing fixture {} failed; aborting ", fixture.getClass().getName() , e);
            try {
                getTransactionManager().abortTransaction();
            } catch (final Exception e2) {
                log.error("failure during abort", e2);
            }
            throw e;
        }
    }

    private void installFixture(final Object fixture) {
        getServiceInjector().injectServicesInto(fixture);

        if (fixture instanceof InstallableFixture) {
            final InstallableFixture installableFixture = (InstallableFixture) fixture;
            if (shouldInstallFixture(installableFixture)) {
                installableFixture.install();
            }
        }

    }

    private boolean shouldInstallFixture(final InstallableFixture installableFixture) {
        final FixtureType fixtureType = installableFixture.getType();

        if(fixtureType.isAlwaysInstall()) {
        return true;
    }

        //TODO [2033] what, why? only install if already installed?
        return IsisSession.currentOrElseNull().getFixturesInstalledState().isInstalled();
    }



    // -- dependencies (derived)

    private ServiceInjector getServiceInjector() {
        return IsisContext.getServiceInjector();
    }

    private RuntimeEventService getRuntimeEventService() {
        return IsisContext.getServiceRegistry().lookupServiceElseFail(RuntimeEventService.class);
    }

    private IsisTransactionManagerJdoInternal getTransactionManager() {
        return IsisSession.transactionManager().orElse(null);
    }



}
