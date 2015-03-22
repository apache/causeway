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

import java.lang.reflect.Constructor;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;
import org.apache.isis.viewer.wicket.ui.pages.mmverror.MmvErrorPage;
import org.apache.isis.viewer.wicket.viewer.IsisWicketApplication;

/**
 * Isis-specific implementation of the Wicket's {@link RequestCycle},
 * automatically opening a {@link IsisSession} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 */
public class WebRequestCycleForIsis extends AbstractRequestCycleListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebRequestCycleForIsis.class);

    private PageClassRegistry pageClassRegistry;

    @Override
    public synchronized void onBeginRequest(RequestCycle requestCycle) {
        
        if (!Session.exists()) {
            return;
        }
        final AuthenticatedWebSessionForIsis wicketSession = AuthenticatedWebSessionForIsis.get();
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
        if(LOG.isDebugEnabled()) {
            LOG.debug("onRequestHandlerExecuted: handler: " + handler);
        }
        
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
                
                // shouldn't return null given that we're in a session ...
                PageProvider errorPageProvider = errorPageProviderFor(ex);
                throw new RestartResponseException(errorPageProvider, RedirectPolicy.ALWAYS_REDIRECT);
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
        PageProvider errorPageProvider = errorPageProviderFor(ex);
        // avoid infinite redirect loops
        RedirectPolicy redirectPolicy = ex instanceof PageExpiredException? RedirectPolicy.NEVER_REDIRECT: RedirectPolicy.ALWAYS_REDIRECT;
        return errorPageProvider != null 
                ? new RenderPageRequestHandler(errorPageProvider, redirectPolicy)
                : null;
    }

    protected PageProvider errorPageProviderFor(Exception ex) {
        IRequestablePage errorPage = errorPageFor(ex);
        return errorPage != null? new PageProvider(errorPage): null;
    }

    // special case handling for PageExpiredException, otherwise infinite loop
    private final static ExceptionRecognizerForType pageExpiredExceptionRecognizer = 
            new ExceptionRecognizerForType(PageExpiredException.class, new Function<String,String>(){
                @Override
                public String apply(String input) {
                    return "Requested page is no longer available. Please navigate back to the home page or select an object from the bookmark bar.";
                }
            });

    protected IRequestablePage errorPageFor(Exception ex) {
        List<ExceptionRecognizer> exceptionRecognizers = Lists.newArrayList();
        exceptionRecognizers.add(pageExpiredExceptionRecognizer);

        if(inIsisSession()) {
            exceptionRecognizers.addAll(getServicesInjector().lookupServices(ExceptionRecognizer.class));
        } else {
            List<String> validationErrors = IsisWicketApplication.get().getValidationErrors();
            if(!validationErrors.isEmpty()) {
                return new MmvErrorPage(Model.ofList(validationErrors));
            }
            // not sure whether this can ever happen now...
            LOG.warn("Unable to obtain exceptionRecognizers (no session), will be treated as unrecognized exception");
        }
        String recognizedMessageIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
        ExceptionModel exceptionModel = ExceptionModel.create(recognizedMessageIfAny, ex);
        
        return isSignedIn() ? new ErrorPage(exceptionModel) : newSignInPage(exceptionModel);
    }

    /**
     * Tries to instantiate the configured {@link PageType#SIGN_IN signin page} with the given exception model
     *
     * @param exceptionModel A model bringing the information about the occurred problem
     * @return An instance of the configured signin page
     */
    private IRequestablePage newSignInPage(final ExceptionModel exceptionModel) {
        Class<? extends Page> signInPageClass = null;
        if (pageClassRegistry != null) {
            signInPageClass = pageClassRegistry.getPageClass(PageType.SIGN_IN);
        }
        if (signInPageClass == null) {
            signInPageClass = WicketSignInPage.class;
        }
        final PageParameters parameters = new PageParameters();
        Page signInPage;
        try {
            Constructor<? extends Page> constructor = signInPageClass.getConstructor(PageParameters.class, ExceptionModel.class);
            signInPage = constructor.newInstance(parameters, exceptionModel);
        } catch (Exception _) {
            try {
                IPageFactory pageFactory = Application.get().getPageFactory();
                signInPage = pageFactory.newPage(signInPageClass, parameters);
            } catch (Exception x) {
                throw new WicketRuntimeException("Cannot instantiate the configured sign in page", x);
            }
        }
        return signInPage;
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


    public void setPageClassRegistry(PageClassRegistry pageClassRegistry) {
        this.pageClassRegistry = pageClassRegistry;
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
