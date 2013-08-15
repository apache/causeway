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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;

/**
 * Isis-specific implementation of the Wicket's {@link WebRequestCycle},
 * automatically opening a {@link IsisSession} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 */
public class WebRequestCycleForIsis extends AbstractRequestCycleListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebRequestCycleForIsis.class);

    private AuthenticatedWebSessionForIsis getWebSession() {
        return (AuthenticatedWebSessionForIsis) WebSession.get();
    }

    @Override
    public synchronized void onBeginRequest(RequestCycle requestCycle) {
        
        final AuthenticatedWebSessionForIsis wicketSession = getWebSession();
        if (wicketSession == null) {
            return;
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
                // will redirect to error page after this, 
                // so make sure there is a new transaction ready to go.
                if(getTransactionManager().getTransaction().getState().isComplete()) {
                    getTransactionManager().startTransaction();
                }
                if(handler instanceof RenderPageRequestHandler) {
                    RenderPageRequestHandler requestHandler = (RenderPageRequestHandler) handler;
                    if(requestHandler.getPage() instanceof ErrorPage) {
                        // do nothing
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
        return new RenderPageRequestHandler(errorPageProviderFor(ex), RedirectPolicy.ALWAYS_REDIRECT);
    }

    protected PageProvider errorPageProviderFor(Exception ex) {
        return new PageProvider(errorPageFor(ex));
    }

    protected IRequestablePage errorPageFor(Exception ex) {
        List<ExceptionRecognizer> exceptionRecognizers = Collections.emptyList();
        if(inIsisSession()) {
            exceptionRecognizers = getServicesInjector().lookupServices(ExceptionRecognizer.class);
        } else {
            LOG.warn("Unable to obtain exceptionRecognizers (no session), will be treated as unrecognized exception");
        }
        String recognizedMessageIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        ExceptionModel exceptionModel = ExceptionModel.create(recognizedMessageIfAny, ex);
        
        if( isSignedIn()) {
            return new ErrorPage(exceptionModel);
        } else {
            return new WicketSignInPage(null, exceptionModel);
        }
    }

    /**
     * TODO: this is very hacky...
     * 
     * <p>
     * Matters should improve once ISIS-299 gets implemented...
     */
    protected boolean isSignedIn() {
        if(!inIsisSession()) {
            return false;
        }
        if(getAuthenticationSession() == null) {
            return false;
        }
        return getWicketAuthenticationSession().isSignedIn();
    }



    
    ///////////////////////////////////////////////////////////////
    // Dependencies (from isis' context)
    ///////////////////////////////////////////////////////////////
    
    protected ServicesInjector getServicesInjector() {
        return IsisContext.getPersistenceSession().getServicesInjector();
    }
    
    protected IsisContext getIsisContext() {
        return IsisContext.getInstance();
    }

    protected IsisTransactionManager getTransactionManager() {
        return IsisContext.getTransactionManager();
    }

    protected boolean inIsisSession() {
        return IsisContext.inSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return IsisContext.getAuthenticationSession();
    }

    ///////////////////////////////////////////////////////////////
    // Dependencies (from wicket)
    ///////////////////////////////////////////////////////////////

    
    protected AuthenticatedWebSession getWicketAuthenticationSession() {
        return AuthenticatedWebSession.get();
    }

}
