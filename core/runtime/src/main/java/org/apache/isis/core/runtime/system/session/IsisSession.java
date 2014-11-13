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

import com.google.common.eventbus.EventBus;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;

/**
 * Analogous to (and in essence a wrapper for) a JDO <code>PersistenceManager</code>;
 * holds the current set of components for a specific execution context (such as on a thread).
 * 
 * <p>
 * The <code>IsisContext</code> class is responsible for locating the current execution context.
 * 
 * @see IsisSessionFactory
 */
public interface IsisSession extends SessionScopedComponent {

    // //////////////////////////////////////////////////////
    // closeAll
    // //////////////////////////////////////////////////////

    /**
     * Normal lifecycle is managed using callbacks in
     * {@link SessionScopedComponent}. This method is to allow the outer
     * {@link ApplicationScopedComponent}s to shutdown, closing any and all
     * running {@link IsisSession}s.
     */
    public void closeAll();

    // //////////////////////////////////////////////////////
    // Id
    // //////////////////////////////////////////////////////

    /**
     * A descriptive identifier for this {@link IsisSession}.
     */
    public String getId();

    // //////////////////////////////////////////////////////
    // Authentication Session
    // //////////////////////////////////////////////////////

    /**
     * Returns the {@link AuthenticationSession} representing this user for this
     * {@link IsisSession}.
     */
    public AuthenticationSession getAuthenticationSession();

    // //////////////////////////////////////////////////////
    // Persistence Session
    // //////////////////////////////////////////////////////

    /**
     * The {@link PersistenceSession} within this {@link IsisSession}.
     * 
     * <p>
     * Would have been created by the {@link #getSessionFactory() owning
     * factory}'s
     * 
     */
    public PersistenceSession getPersistenceSession();


    
    // //////////////////////////////////////////////////////
    // Transaction (if in progress)
    // //////////////////////////////////////////////////////

    public IsisTransaction getCurrentTransaction();

    // //////////////////////////////////////////////////////
    // Debugging
    // //////////////////////////////////////////////////////

    public void debugAll(DebugBuilder debug);

    public void debug(DebugBuilder debug);

    public void debugState(DebugBuilder debug);


}
