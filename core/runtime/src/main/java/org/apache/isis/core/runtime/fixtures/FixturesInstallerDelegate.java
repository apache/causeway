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

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.fixtures.CompositeFixture;
import org.apache.isis.applib.fixtures.FixtureType;
import org.apache.isis.applib.fixtures.InstallableFixture;
import org.apache.isis.applib.fixtures.LogonFixture;
import org.apache.isis.applib.fixtures.switchuser.SwitchUserService;
import org.apache.isis.core.commons.lang.ObjectExtensions;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

// REVIEW: could probably inline
public class FixturesInstallerDelegate {

    private static final Logger LOG = LoggerFactory.getLogger(FixturesInstallerDelegate.class);

    //region > Constructor, fields

    private final IsisSessionFactory isisSessionFactory;
    private final SwitchUserService switchUserService;

    //endregion
    public FixturesInstallerDelegate(final IsisSessionFactory isisSessionFactory) {
        this.isisSessionFactory = isisSessionFactory;
        switchUserService = isisSessionFactory.getServicesInjector().lookupService(SwitchUserService.class);
    }

    //region > addFixture, getFixtures

    protected final List<Object> fixtures = Lists.newArrayList();

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

    //endregion

    //region > installFixtures

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
    public final void installFixtures() {
        installFixtures(getFixtures());
    }


    private void installFixtures(final List<Object> fixtures) {
        for (final Object fixture : fixtures) {
            installFixtureInTransaction(fixture);
        }
    }

    private void installFixtureInTransaction(final Object fixture) {
        getServicesInjector().injectServicesInto(fixture);

        installFixtures(getFixtures(fixture));

        // now, install the fixture itself
        try {
            LOG.info("installing fixture: " + fixture);
            getTransactionManager().startTransaction();
            installFixture(fixture);
            saveLogonFixtureIfRequired(fixture);
            getTransactionManager().endTransaction();
            LOG.info("fixture installed");
        } catch (final RuntimeException e) {
            LOG.error("installing fixture " + fixture.getClass().getName() + " failed; aborting ", e);
            try {
                getTransactionManager().abortTransaction();
            } catch (final Exception e2) {
                LOG.error("failure during abort", e2);
            }
            throw e;
        }
    }

    /**
     * Obtain any child fixtures for this fixture.
     * 
     * @param fixture
     */
    private List<Object> getFixtures(final Object fixture) {
        if (fixture instanceof CompositeFixture) {
            final CompositeFixture compositeFixture = (CompositeFixture) fixture;
            return compositeFixture.getFixtures();
        }
        return Collections.emptyList();
    }

    private void installFixture(final Object fixture) {
        isisSessionFactory.getServicesInjector().injectServicesInto(fixture);

        if (fixture instanceof InstallableFixture) {
            final InstallableFixture installableFixture = (InstallableFixture) fixture;
            if (shouldInstallFixture(installableFixture)) {
                installableFixture.install();
            }
        }

        if (fixture instanceof LogonFixture) {
            this.logonFixture = (LogonFixture) fixture;
        }
    }

    private boolean shouldInstallFixture(final InstallableFixture installableFixture) {
        final FixtureType fixtureType = installableFixture.getType();
        if (fixtureType == FixtureType.DOMAIN_OBJECTS) {
            return !isisSessionFactory.getCurrentSession().getPersistenceSession().isFixturesInstalled();
        }

        // fixtureType is OTHER; always install.
        return true;
    }

    //endregion

    //region > logonFixture

    /**
     * The requested {@link LogonFixture}, if any.
     *
     * <p>
     * Each fixture is inspected as it is {@link #installFixture(Object)}; if it
     * implements {@link LogonFixture} then it is remembered so that it can be
     * used later to automatically logon.
     */
    private LogonFixture logonFixture;


    /**
     * The {@link LogonFixture}, if any.
     * 
     * <p>
     * Used to automatically logon if in {@link DeploymentType#SERVER_PROTOTYPE} mode.
     */
    public LogonFixture getLogonFixture() {
        return logonFixture;
    }

    private void saveLogonFixtureIfRequired(final Object fixture) {
        if (fixture instanceof LogonFixture) {
            if (logonFixture != null) {
                LOG.warn("Already specified logon fixture, using latest provided");
            }
            this.logonFixture = (LogonFixture) fixture;
        }
    }


    //endregion

    //region > dependencies (derived)

    private ServicesInjector getServicesInjector() {
        return isisSessionFactory.getServicesInjector();
    }


    private PersistenceSession getPersistenceSession() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

    //endregion

}
