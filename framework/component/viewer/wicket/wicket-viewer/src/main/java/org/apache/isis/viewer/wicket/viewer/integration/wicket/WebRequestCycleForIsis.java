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

package org.apache.isis.viewer.wicket.viewer.integration.wicket;

import org.apache.log4j.Logger;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.session.IsisSession;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransaction;
import org.apache.isis.runtimes.dflt.runtime.system.transaction.IsisTransactionManager;

/**
 * Isis-specific implementation of the Wicket's {@link WebRequestCycle},
 * automatically opening a {@link IsisSession} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 */
public class WebRequestCycleForIsis /*extends WebRequestCycle*/ extends AbstractRequestCycleListener {

    private static final Logger LOG = Logger.getLogger(WebRequestCycleForIsis.class);

//    public WebRequestCycleForIsis(final WebApplication application, final WebRequest request, final Response response) {
//        super(application, request, response);
//    }
//
//    /**
//     * Convenience, downcasts.
//     */
//    @Override
//    public AuthenticatedWebSessionForIsis getWebSession() {
//        return (AuthenticatedWebSessionForIsis) super.getWebSession();
//    }

      private AuthenticatedWebSessionForIsis getWebSession() {
          return (AuthenticatedWebSessionForIsis) WebSession.get();
      }

    @Override
    public synchronized void onBeginRequest(RequestCycle requestCycle) {
        //super.onBeginRequest();
        final AuthenticatedWebSessionForIsis wicketSession = getWebSession();
        if (wicketSession == null) {
            return;
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("thread usage: " + wicketSession.getThreadUsage());
        }
        final AuthenticationSession authenticationSession = wicketSession.getAuthenticationSession();
        if (authenticationSession == null) {
            return;
        }

        getIsisContext().openSessionInstance(authenticationSession);
        getTransactionManager().startTransaction();
    }

    @Override
    public synchronized void onEndRequest(RequestCycle requestCycle) {
        final IsisSession session = getIsisContext().getSessionInstance();
        if (session != null) {
            // in session
            commitTransactionIfAny();
            getIsisContext().closeSessionInstance();
        }
        //super.onEndRequest();
    }

    private void commitTransactionIfAny() {
        final IsisTransaction transaction = getTransactionManager().getTransaction();
        if (transaction != null) {
            if (transaction.getState() == IsisTransaction.State.MUST_ABORT) {
                getTransactionManager().abortTransaction();
            } else if (transaction.getState() == IsisTransaction.State.IN_PROGRESS) {
                getTransactionManager().endTransaction();
            }
        }
    }

    /**
     * Factored out so can be overridden in testing.
     */
    protected IsisContext getIsisContext() {
        return IsisContext.getInstance();
    }

//    /**
//     * Simply downcasts superclass' implementation, for convenience of callers.
//     */
//    @Override
//    protected WebClientInfo newClientInfo() {
//        return (WebClientInfo) super.newClientInfo();
//    }

    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }
}
