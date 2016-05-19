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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.DeploymentType;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;

import static org.apache.isis.core.commons.ensure.Ensure.ensureThatArg;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;

/**
 * Analogous to (and in essence a wrapper for) a JDO <code>PersistenceManager</code>;
 * holds the current set of components for a specific execution context (such as on a thread).
 * 
 * <p>
 * The <code>IsisContext</code> class is responsible for locating the current execution context.
 * 
 * @see IsisSessionFactory
 */
public class IsisSession implements SessionScopedComponent {

    private static final Logger LOG = LoggerFactory.getLogger(IsisSession.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM HH:mm:ss,SSS");
    private static int nextId = 1;

    private final IsisSessionFactory isisSessionFactory;

    private final AuthenticationSession authenticationSession;
    private PersistenceSession persistenceSession; // only non-final so can be
    // replaced in tests.
    private final int id;
    private long accessTime;
    private String debugSnapshot;

    //region > constructor
    public IsisSession(
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
    //endregion

    //region > open, close
    public void open() {
        persistenceSession.open();
    }

    /**
     * Closes session.
     */
    public void close() {
        final PersistenceSession persistenceSession = getPersistenceSession();
        if(persistenceSession != null) {
            persistenceSession.close();
        }
    }

    //endregion

    //region > shutdown
    /**
     * Shuts down all components.
     *
     * Normal lifecycle is managed using callbacks in
     * {@link SessionScopedComponent}. This method is to allow the outer
     * {@link ApplicationScopedComponent}s to shutdown, closing any and all
     * running {@link IsisSession}s.
     */
    public void closeAll() {
        close();

        persistenceSession.close();
    }

    //endregion

    //region > convenience methods
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
    public SpecificationLoader getSpecificationLoader() {
        return isisSessionFactory.getSpecificationLoader();
    }
    //endregion

    //region > AuthenticationSession
    /**
     * Returns the {@link AuthenticationSession} representing this user for this
     * {@link IsisSession}.
     */
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }

    private String getSessionUserName() {
        return getAuthenticationSession().getUserName();
    }
    //endregion

    //region > id
    /**
     * A descriptive identifier for this {@link IsisSession}.
     */
    public String getId() {
        return "#" + id + getSessionUserName();
    }
    //endregion

    //region > Persistence Session
    /**
     * The {@link PersistenceSession} within this {@link IsisSession}.
     */
    public PersistenceSession getPersistenceSession() {
        return persistenceSession;
    }

    //endregion

    private void setSessionOpenTime(final long accessTime) {
        this.accessTime = accessTime;
    }
    //endregion

    //region > transaction

    /**
     * Convenience method that returns the {@link IsisTransaction} of the
     * session, if any.
     */
    public IsisTransaction getCurrentTransaction() {
        return getTransactionManager().getTransaction();
    }

    //endregion

    //region > toString
    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("context", getId());
        asString.append("authenticationSession", getAuthenticationSession());
        asString.append("persistenceSession", getPersistenceSession());
        asString.append("transaction", getCurrentTransaction());
        return asString.toString();
    }
    //endregion

    //region > Dependencies (from constructor)

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }
    //endregion

}
