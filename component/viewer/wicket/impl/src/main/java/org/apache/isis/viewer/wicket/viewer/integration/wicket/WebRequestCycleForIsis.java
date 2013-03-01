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

import java.util.Collections;
import java.util.List;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransaction;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.runtime.system.transaction.MessageBroker;
import org.apache.isis.core.runtime.system.transaction.MessageBrokerDefault;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.log4j.Logger;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

/**
 * Isis-specific implementation of the Wicket's {@link WebRequestCycle},
 * automatically opening a {@link IsisSession} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 */
public class WebRequestCycleForIsis extends AbstractRequestCycleListener {

    private static final Logger LOG = Logger.getLogger(WebRequestCycleForIsis.class);

      private AuthenticatedWebSessionForIsis getWebSession() {
          return (AuthenticatedWebSessionForIsis) WebSession.get();
      }

    @Override
    public synchronized void onBeginRequest(RequestCycle requestCycle) {
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

    
    /**
     * Is called prior to {@link #onEndRequest(RequestCycle)}, and offers the opportunity to
     * throw an exception.
     */
    @Override
    public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
        LOG.info("onRequestHandlerExecuted: handler: " + handler);
        
        final IsisSession session = getIsisContext().getSessionInstance();
        if (session != null) {
            try {
                // will commit (or abort) the transaction;
                // an abort will cause the exception to be thrown.
                getTransactionManager().endTransaction();
            } catch(Exception ex) {
                if(handler instanceof RenderPageRequestHandler) {
                    RenderPageRequestHandler requestHandler = (RenderPageRequestHandler) handler;
                    if(requestHandler.getPage() instanceof ErrorPage) {
                        // do nothing; 
                        return;
                    }
                }
                throw new RestartResponseException(errorPageProviderFor(ex), RedirectPolicy.ALWAYS_REDIRECT);
            }
        }
    }

    /**
     * It is not possible to throw exceptions here, hence use of {@link #onRequestHandlerExecuted(RequestCycle, IRequestHandler)}.
     */
    @Override
    public synchronized void onEndRequest(RequestCycle cycle) {
        final IsisSession session = getIsisContext().getSessionInstance();
        if (session != null) {
            try {
                // belt and braces
                getTransactionManager().endTransaction();
            } finally {
                getIsisContext().closeSessionInstance();
            }
        }
    }


    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception ex) {
        // previously we had a handler in here.  However, it seems to be sufficient to just
        // use the exception handling in the onRequestHandlerExecuted(...) callback
        // which fires before the onEndRequest(...) callback.
        //return super.onException(cycle, ex);
        
        // hmm, maybe not.  making in edit page do a flush seems to belie that.
        
        return new RenderPageRequestHandler(errorPageProviderFor(ex), RedirectPolicy.ALWAYS_REDIRECT);
    }

    protected PageProvider errorPageProviderFor(Exception ex) {
        return new PageProvider(errorPageFor(ex));
    }

    protected ErrorPage errorPageFor(Exception ex) {
        List<ExceptionRecognizer> exceptionRecognizers;
        try {
            exceptionRecognizers = getServicesInjector().lookupServices(ExceptionRecognizer.class);
        } catch(Exception ex2) {
            LOG.warn("Unable to obtain exceptionRecognizers (no session?)");
            exceptionRecognizers = Collections.emptyList();
        }
        String message = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        final ErrorPage page = message != null ? new ErrorPage(message) : new ErrorPage(ex);
        return page;
    }


    
    ///////////////////////////////////////////////////////////////
    // Dependencies (from context)
    ///////////////////////////////////////////////////////////////
    
    /**
     * Factored out so can be overridden in testing.
     */
    protected ServicesInjector getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }
    
    /**
     * Factored out so can be overridden in testing.
     */
    protected IsisContext getIsisContext() {
        return IsisContext.getInstance();
    }

    /**
     * Factored out so can be overridden in testing.
     */
    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }
}
