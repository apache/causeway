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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.util.ToString;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.security.authentication.AuthenticationSession;

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

    // -- constructor, fields

    private final AuthenticationSession authenticationSession;
    private PersistenceSession persistenceSession; // only non-final so can be replaced in tests.

    public IsisSession(
            final AuthenticationSession authenticationSession,
            final PersistenceSession persistenceSession) {

        this.authenticationSession = authenticationSession;
        this.persistenceSession = persistenceSession;
    }


    // -- open, close
    void open() {
        persistenceSession.open();
    }

    /**
     * Closes session.
     */
    void close() {
        if(persistenceSession != null) {
            persistenceSession.close();
        }
    }




    // -- AuthenticationSession
    /**
     * Returns the {@link AuthenticationSession} representing this user for this
     * {@link IsisSession}.
     */
    public AuthenticationSession getAuthenticationSession() {
        return authenticationSession;
    }


    // -- Persistence Session
    /**
     * The {@link PersistenceSession} within this {@link IsisSession}.
     */
    public PersistenceSession getPersistenceSession() {
        return persistenceSession;
    }



    // -- transaction

    /**
     * Convenience method that returns the {@link IsisTransaction} of the
     * session, if any.
     */
    public IsisTransaction getCurrentTransaction() {
        return getTransactionManager().getCurrentTransaction();
    }



    // -- toString
    @Override
    public String toString() {
        final ToString asString = new ToString(this);
        asString.append("authenticationSession", getAuthenticationSession());
        asString.append("persistenceSession", getPersistenceSession());
        asString.append("transaction", getCurrentTransaction());
        return asString.toString();
    }


    // -- Dependencies (from constructor)

    private IsisTransactionManager getTransactionManager() {
        return getPersistenceSession().getTransactionManager();
    }


}
