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
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.AbstractRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer2;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerComposite;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.core.commons.authentication.AuthenticationSession;
import org.apache.isis.core.commons.authentication.MessageBroker;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.session.IsisSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;
import org.apache.isis.core.runtime.system.transaction.IsisTransactionManager;
import org.apache.isis.core.tracing.Scope2;
import org.apache.isis.core.tracing.Span2;
import org.apache.isis.core.tracing.TraceScopeManager;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;
import org.apache.isis.viewer.wicket.ui.pages.mmverror.MmvErrorPage;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;


/**
 * Isis-specific implementation of the Wicket's {@link RequestCycle},
 * automatically opening a {@link IsisSession} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 */
public class WebRequestCycleForIsis extends AbstractRequestCycleListener {

    private static final Logger LOG = LoggerFactory.getLogger(WebRequestCycleForIsis.class);

    private PageClassRegistry pageClassRegistry;

    @Override
    public synchronized void onBeginRequest(final RequestCycle requestCycle) {

        if (!Session.exists()) {
            return;
        }


        final AuthenticatedWebSessionForIsis wicketSession = AuthenticatedWebSessionForIsis.get();
        final AuthenticationSession authenticationSession = wicketSession.getAuthenticationSession();
        if (authenticationSession == null) {
            return;
        }

        final Scope2 currScope = TraceScopeManager.get().activeScope();
        final Scope2 newScope = TraceScopeManager.get()
                .startActive("web-request-cycle-for-isis");
        newScope.span()
                .setTag(Span2.START_TAG, "onBeginRequest")
                .log("onBeginRequest")
                .setTag("requestCycle.request.url", requestCycle.getRequest().getUrl().toString());

        getIsisSessionFactory().openSession(authenticationSession);
        getTransactionManager().startTransaction();

    }

    @Override
    public void onRequestHandlerResolved(
            final RequestCycle requestCycle,
            final IRequestHandler handler) {

        TraceScopeManager.get().activeSpan()
                .log("onRequestHandlerResolved")
        ;

        if(handler instanceof RenderPageRequestHandler) {
            AdapterManager.ConcurrencyChecking.disable();

            final MetaModelInvalidException mmie = IsisContext.getMetaModelInvalidExceptionIfAny();

            if(mmie != null) {
                RenderPageRequestHandler requestHandler = (RenderPageRequestHandler) handler;
                final IRequestablePage nextPage = requestHandler.getPage();
                if(nextPage instanceof ErrorPage || nextPage instanceof MmvErrorPage) {
                    // do nothing
                    return;
                }
                throw mmie;
            }
        }
    }


    /**
     * Is called prior to {@link #onEndRequest(RequestCycle)}, and offers the opportunity to
     * throw an exception.
     */
    @Override
    public void onRequestHandlerExecuted(
            final RequestCycle requestCycle,
            final IRequestHandler handler) {

        TraceScopeManager.get()
                .activeSpan()
                .log("onRequestHandlerExecuted")
                .setTag(Span2.FINISH_TAG, "onRequestHandlerExecuted")
                .finish();

        TraceScopeManager.get()
                .startActive("continue")
                .span()
                .setTag(Span2.START_TAG, "onRequestHandlerExecuted")
                .log("onRequestHandlerExecuted")
                .setTag("requestCycle.request.url", requestCycle.getRequest().getUrl().toString())
                .setTag("handler.class.simpleName", handler.getClass().getSimpleName());

        if(handler instanceof RenderPageRequestHandler) {
            AdapterManager.ConcurrencyChecking.reset(AdapterManager.ConcurrencyChecking.CHECK);
        }

        if (getIsisSessionFactory().inSession()) {
            try {
                // will commit (or abort) the transaction;
                // an abort will cause the exception to be thrown.
                getTransactionManager().endTransaction();

                TraceScopeManager.get()
                        .activeSpan()
                        .log("onRequestHandlerExecuted: endTransaction OK");
            } catch(Exception ex) {

                TraceScopeManager.get()
                        .activeSpan()
                        .log("onRequestHandlerExecuted: endTransaction exception")
                        .setTag("exception-location", "onRequestHandlerExecuted")
                        .setTag("exception", Throwables.getStackTraceAsString(ex))
                        .setTag(Span2.FINISH_TAG, "onRequestHandlerExecuted")
                        .finish();

                TraceScopeManager.get()
                        .startActive("error-response")
                        .span()
                        .setTag(Span2.START_TAG, "onRequestHandlerExecuted")
                        .log("restart response after fail to commit xactn");

                // will redirect to error page after this,
                // so make sure there is a new transaction ready to go.
                if(getTransactionManager().getCurrentTransaction().getState().isComplete()) {
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
    public synchronized void onEndRequest(final RequestCycle requestCycle) {

        TraceScopeManager.get()
                .activeSpan()
                .log("onEndRequest")
        ;

        try {
            if (getIsisSessionFactory().inSession()) {
                try {
                    // belt and braces
                    getTransactionManager().endTransaction();
                } finally {
                    getIsisSessionFactory().closeSession();
                }
            }

        } finally {
            TraceScopeManager.get()
                    .activeSpan()
                    .setTag(Span2.FINISH_TAG, "onEndRequest")
                    .finish();
            TraceScopeManager.get()
                    .activeScope()
                    .close();
        }


    }


    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception ex) {

        TraceScopeManager.get()
                .activeSpan()
                .log("onException")
                .setTag("exception-location", "onException")
                .setTag("exception", Throwables.getStackTraceAsString(ex));
        TraceScopeManager.get()
                .activeScope()
                .closeAndFinish();


        final MetaModelInvalidException mmie = IsisContext.getMetaModelInvalidExceptionIfAny();
        if(mmie != null) {
            final Set<String> validationErrors = mmie.getValidationErrors();
            final MmvErrorPage mmvErrorPage = new MmvErrorPage(validationErrors);

            TraceScopeManager.get()
                    .startActive("error-response")
                    .span()
                    .setOperationName("onException")
                    .setTag("purpose", "redirect to error page on MetaModelInvalidException");

            return new RenderPageRequestHandler(new PageProvider(mmvErrorPage), RedirectPolicy.ALWAYS_REDIRECT);
        }

        try {

            // adapted from http://markmail.org/message/un7phzjbtmrrperc
            if(ex instanceof ListenerInvocationNotAllowedException) {
                final ListenerInvocationNotAllowedException linaex = (ListenerInvocationNotAllowedException) ex;
                if(linaex.getComponent() != null && PromptFormAbstract.ID_CANCEL_BUTTON.equals(linaex.getComponent().getId())) {
                    // no message.
                    // this seems to occur when press ESC twice in rapid succession on a modal dialog.
                } else {
                    addMessage(null);

                }

                TraceScopeManager.get()
                        .startActive("error-response")
                        .span()
                        .setOperationName("onException")
                        .setTag("purpose", "respond gracefully after ListenerInvocationNotAllowedException");

                return respondGracefully(cycle);
            }


            // handle recognised exceptions gracefully also
            final List<ExceptionRecognizer2> exceptionRecognizers =
                    getServicesInjector().lookupServices(ExceptionRecognizer2.class);
            String recognizedMessageIfAny = new ExceptionRecognizerComposite(exceptionRecognizers).recognize(ex);
            if(recognizedMessageIfAny != null) {

                TraceScopeManager.get()
                        .startActive("error-response")
                        .span()
                        .setOperationName("onException")
                        .setTag("purpose", "respond gracefully after recognised exception");

                return respondGracefully(cycle);
            }

            final List<Throwable> causalChain = Throwables.getCausalChain(ex);
            final Optional<Throwable> hiddenIfAny = FluentIterable.from(causalChain).filter(
                    ObjectMember.HiddenException.isInstanceOf()).first();
            if(hiddenIfAny.isPresent()) {
                addMessage("hidden");

                TraceScopeManager.get()
                        .startActive("error-response")
                        .span()
                        .setOperationName("onException")
                        .setTag("purpose", "respond gracefully if hidden");

                return respondGracefully(cycle);
            }
            final Optional<Throwable> disabledIfAny = FluentIterable.from(causalChain).filter(
                    ObjectMember.DisabledException.isInstanceOf()).first();
            if(disabledIfAny.isPresent()) {
                addTranslatedMessage(disabledIfAny.get().getMessage());

                TraceScopeManager.get()
                        .startActive("error-response")
                        .span()
                        .setOperationName("onException")
                        .setTag("purpose", "respond gracefully if disabled");

                return respondGracefully(cycle);
            }

        } catch(Exception ignoreFailedAttemptToGracefullyHandle) {
            // if any of this graceful responding fails, then fall back to original handling
        }

        PageProvider errorPageProvider = errorPageProviderFor(ex);
        // avoid infinite redirect loops
        RedirectPolicy redirectPolicy = ex instanceof PageExpiredException
                ? RedirectPolicy.NEVER_REDIRECT
                : RedirectPolicy.ALWAYS_REDIRECT;

        TraceScopeManager.get()
                .startActive("error-response")
                .span()
                .setOperationName("onException")
                .setTag("purpose", "redirect to error page on " + ex.getClass().getSimpleName());

        return errorPageProvider != null
                ? new RenderPageRequestHandler(errorPageProvider, redirectPolicy)
                : null;
    }

    private IRequestHandler respondGracefully(final RequestCycle cycle) {
        final IRequestablePage page = PageRequestHandlerTracker.getFirstHandler(cycle).getPage();
        final PageProvider pageProvider = new PageProvider(page);
        return new RenderPageRequestHandler(pageProvider);
    }

    private void addMessage(final String message) {
        final String translatedMessage = translate(message);
        addTranslatedMessage(translatedMessage);
    }

    private void addTranslatedMessage(final String translatedSuffixIfAny) {
        final String translatedPrefix = translate("Action no longer available");
        final String message = translatedSuffixIfAny != null
                ? String.format("%s (%s)", translatedPrefix, translatedSuffixIfAny)
                : translatedPrefix;
        getMessageBroker().addMessage(message);
    }

    private String translate(final String text) {
        if(text == null) {
            return null;
        }
        return getTranslationService().translate(WebRequestCycleForIsis.class.getName(), text);
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
                    return "Requested page is no longer available.";
                }
            });

    protected IRequestablePage errorPageFor(Exception ex) {
        List<ExceptionRecognizer> exceptionRecognizers = Lists.newArrayList();
        exceptionRecognizers.add(pageExpiredExceptionRecognizer);

        if(inIsisSession()) {
            exceptionRecognizers.addAll(getServicesInjector().lookupServices(ExceptionRecognizer.class));
        } else {
            final MetaModelInvalidException mmie = IsisContext.getMetaModelInvalidExceptionIfAny();
            if(mmie != null) {
                Set<String> validationErrors = mmie.getValidationErrors();
                return new MmvErrorPage(validationErrors);
            }
            // not sure whether this can ever happen now...
            LOG.warn("Unable to obtain exceptionRecognizers (no session), will be treated as unrecognized exception", ex);
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
        } catch (Exception ex) {
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

    //region > Dependencies (from isis' context)
    protected ServicesInjector getServicesInjector() {
        return getIsisSessionFactory().getServicesInjector();
    }
    
    protected IsisTransactionManager getTransactionManager() {
        return getIsisSessionFactory().getCurrentSession().getPersistenceSession().getTransactionManager();
    }

    protected boolean inIsisSession() {
        return getIsisSessionFactory().inSession();
    }

    protected AuthenticationSession getAuthenticationSession() {
        return getIsisSessionFactory().getCurrentSession().getAuthenticationSession();
    }

    protected MessageBroker getMessageBroker() {
        return getAuthenticationSession().getMessageBroker();
    }

    IsisSessionFactory getIsisSessionFactory() {
        return IsisContext.getSessionFactory();
    }


    TranslationService getTranslationService() {
        return getServicesInjector().lookupService(TranslationService.class);
    }


    //endregion

    //region > Dependencies (from wicket)

    protected AuthenticatedWebSession getWicketAuthenticationSession() {
        return AuthenticatedWebSession.get();
    }

    //endregion

}
