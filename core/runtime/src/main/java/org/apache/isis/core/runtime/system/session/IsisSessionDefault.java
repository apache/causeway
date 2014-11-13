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

package org.apache.isis.core.runtime.system.session;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.*;

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

    private static final Logger LOG = LoggerFactory.getLogger(IsisSessionDefault.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM HH:mm:ss,SSS");
    private static int nextId = 1;

    private final IsisSessionFactory isisSessionFactory;

    private final AuthenticationSession authenticationSession;
    private PersistenceSession persistenceSession; // only non-final so can be
                                                   // replaced in tests.
    private final int id;
    private long accessTime;
    private String debugSnapshot;

    public IsisSessionDefault(
            final IsisSessionFactory sessionFactory,
            final AuthenticationSession authenticationSession,
            final PersistenceSession persistenceSession) {

        // global context
        ensureThatArg(sessionFactory, is(not(nullValue())), "execution context factory is required");

        // session
        ensureThatArg(authenticationSession, is(not(nullValue())), "authentication session is required");
        ensureThatArg(persistenceSession, is(not(nullValue())), "persistence session is required");

        this.isisSessionFactory = sessionFactory;

        this.authenticationSession = authenticationSession;
        this.persistenceSession = persistenceSession;

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

    /**
     * Convenience method.
     */
    public DeploymentType getDeploymentType() {
        return isisSessionFactory.getDeploymentType();
    }

    /**
     * Convenience method.
     */
    public IsisConfiguration getConfiguration() {
        return isisSessionFactory.getConfiguration();
    }

    /**
     * Convenience method.
     */
    public SpecificationLoaderSpi getSpecificationLoader() {
        return isisSessionFactory.getSpecificationLoader();
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
    }

    @Override
    public void debugState(final DebugBuilder debug) {
        debug.appendln("authenticationSession", getAuthenticationSession());
        debug.appendln("persistenceSession", getPersistenceSession());
        debug.appendln("transaction", getCurrentTransaction());
    }

    // /////////////////////////////////////////////////////
    // Dependencies (from constructor)
    // /////////////////////////////////////////////////////

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }


}
