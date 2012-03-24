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

package org.apache.isis.runtimes.dflt.runtime.system.session;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.lang.ToString;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.runtime.imageloader.TemplateImageLoader;
import org.apache.isis.core.runtime.userprofile.UserProfile;
import org.apache.isis.runtimes.dflt.runtime.system.DeploymentType;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;

/**
 * Analogous to a Hibernate <tt>Session</tt>, holds the current set of
 * components for a specific execution context (such as on a thread).
 * 
 * <p>
 * The <tt>IsisContext</tt> class (in <tt>nof-core</tt>) is responsible for
 * locating the current execution context.
 * 
 * @see IsisSessionFactory
 */
public class IsisSessionDefault implements IsisSession {

    private static final Logger LOG = Logger.getLogger(IsisSessionDefault.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM HH:mm:ss,SSS");
    private static int nextId = 1;

    private final IsisSessionFactory executionContextFactory;

    private final AuthenticationSession authenticationSession;
    private PersistenceSession persistenceSession; // only non-final so can be
                                                   // replaced in tests.
    private final UserProfile userProfile;

    private final int id;
    private long accessTime;
    private String debugSnapshot;

    public IsisSessionDefault(final IsisSessionFactory sessionFactory, final AuthenticationSession authenticationSession, final PersistenceSession persistenceSession, final UserProfile userProfile) {

        // global context
        ensureThatArg(sessionFactory, is(not(nullValue())), "execution context factory is required");

        // session
        ensureThatArg(authenticationSession, is(not(nullValue())), "authentication session is required");
        ensureThatArg(persistenceSession, is(not(nullValue())), "persistence session is required");
        ensureThatArg(userProfile, is(not(nullValue())), "user profile is required");

        this.executionContextFactory = sessionFactory;

        this.authenticationSession = authenticationSession;
        this.persistenceSession = persistenceSession;
        this.userProfile = userProfile;

        setSessionOpenTime(System.currentTimeMillis());

        this.id = nextId++;
    }

    // //////////////////////////////////////////////////////
    // open, close
    // //////////////////////////////////////////////////////

    @Override
    public void open() {
        persistenceSession.open();
    }

    /**
     * Closes session.
     */
    @Override
    public void close() {
        takeSnapshot();
        getPersistenceSession().close();
    }

    // //////////////////////////////////////////////////////
    // shutdown
    // //////////////////////////////////////////////////////

    /**
     * Shuts down all components.
     */
    @Override
    public void closeAll() {
        close();

        shutdownIfRequired(persistenceSession);
    }

    private void shutdownIfRequired(final Object o) {
        if (o instanceof SessionScopedComponent) {
            final SessionScopedComponent requiresSetup = (SessionScopedComponent) o;
            requiresSetup.close();
        }
    }

    // //////////////////////////////////////////////////////
    // ExecutionContextFactory
    // //////////////////////////////////////////////////////

    @Override
    public IsisSessionFactory getSessionFactory() {
        return executionContextFactory;
    }

    /**
     * Convenience method.
     */
    public DeploymentType getDeploymentType() {
        return executionContextFactory.getDeploymentType();
    }

    /**
     * Convenience method.
     */
    public IsisConfiguration getConfiguration() {
        return executionContextFactory.getConfiguration();
    }

    /**
     * Convenience method.
     */
    public SpecificationLoader getSpecificationLoader() {
        return executionContextFactory.getSpecificationLoader();
    }

    /**
     * Convenience method.
     */
    public TemplateImageLoader getTemplateImageLoader() {
        return executionContextFactory.getTemplateImageLoader();
    }

    // //////////////////////////////////////////////////////
    // AuthenticationSession
    // //////////////////////////////////////////////////////

    /**
     * Returns the security session representing this user for this execution
     * context.
     */
    @Override
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    private String getSessionUserName() {
        return getAuthenticationSession().getUserName();
    }

    // //////////////////////////////////////////////////////
    // Id
    // //////////////////////////////////////////////////////

    /**
     * Returns an descriptive identifier for this {@link IsisSessionDefault}.
     */
    @Override
    public String getId() {
        return "#" + id + getSessionUserName();
    }

    // //////////////////////////////////////////////////////
    // Persistence Session
    // //////////////////////////////////////////////////////

    @Override
    public PersistenceSession getPersistenceSession() {
        return persistenceSession;
    }

    // //////////////////////////////////////////////////////
    // Perspective
    // //////////////////////////////////////////////////////

    @Override
    public UserProfile getUserProfile() {
        return userProfile;
    }

    // //////////////////////////////////////////////////////
    // Session Open Time
    // //////////////////////////////////////////////////////

    protected long getSessionOpenTime() {
        return accessTime;
    }

    private void setSessionOpenTime(final long accessTime) {
        this.accessTime = accessTime;
    }

    // //////////////////////////////////////////////////////
    // Transaction
    // //////////////////////////////////////////////////////

    /**
     * Convenience method that returns the {@link IsisTransaction} of the
     * session, if any.
     */
    @Override
    public IsisTransaction getCurrentTransaction() {
        return getTransactionManager().getTransaction();
    }

    // //////////////////////////////////////////////////////
    // testSetObjectPersistor
    // //////////////////////////////////////////////////////

    /**
     * Should only be called in tests.
     */
    public void testSetObjectPersistor(final PersistenceSession objectPersistor) {
        this.persistenceSession = objectPersistor;
    }

    // //////////////////////////////////////////////////////
    // toString
    // //////////////////////////////////////////////////////

    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("context", getId());
        appendState(asString);
        return asString.toString();
    }

    // //////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////

    @Override
    public void debugAll(final DebugBuilder debug) {
        debug.startSection("Isis Context Snapshot");
        debug.appendln(debugSnapshot);
        debug.endSection();
    }

    @Override
    public void debug(final DebugBuilder debug) {
        debug.appendAsHexln("hash", hashCode());
        debug.appendln("context id", id);
        debug.appendln("accessed", DATE_FORMAT.format(new Date(getSessionOpenTime())));
        debugState(debug);
    }

    public void takeSnapshot() {
        if (!LOG.isDebugEnabled()) {
            return;
        }
        final DebugString debug = new DebugString();
        debug(debug);
        debug.indent();
        debug.appendln();

        debug(debug, getPersistenceSession());
        if (getCurrentTransaction() != null) {
            debug(debug, getCurrentTransaction().getUpdateNotifier());
            debug(debug, getCurrentTransaction().getMessageBroker());
        }
        debugSnapshot = debug.toString();

        LOG.debug(debugSnapshot);
    }

    private void debug(final DebugBuilder debug, final Object object) {
        if (object instanceof DebuggableWithTitle) {
            final DebuggableWithTitle d = (DebuggableWithTitle) object;
            debug.startSection(d.debugTitle());
            d.debugData(debug);
            debug.endSection();
        } else {
            debug.appendln("no debug for " + object);
        }
    }

    public void appendState(final ToString asString) {
        asString.append("authenticationSession", getAuthenticationSession());
        asString.append("persistenceSession", getPersistenceSession());
        asString.append("transaction", getCurrentTransaction());
        if (getCurrentTransaction() != null) {
            asString.append("messageBroker", getCurrentTransaction().getMessageBroker());
            asString.append("updateNotifier", getCurrentTransaction().getUpdateNotifier());
        }
    }

    @Override
    public void debugState(final DebugBuilder debug) {
        debug.appendln("authenticationSession", getAuthenticationSession());
        debug.appendln("persistenceSession", getPersistenceSession());
        debug.appendln("transaction", getCurrentTransaction());
        if (getCurrentTransaction() != null) {
            debug.appendln("messageBroker", getCurrentTransaction().getMessageBroker());
            debug.appendln("updateNotifier", getCurrentTransaction().getUpdateNotifier());
        }
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }

}
