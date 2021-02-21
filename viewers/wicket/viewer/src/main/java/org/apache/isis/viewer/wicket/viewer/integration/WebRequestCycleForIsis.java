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

package org.apache.isis.viewer.wicket.viewer.integration;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.protocol.http.PageExpiredException;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.exceprecog.Recognition;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.isis.commons.collections.Can;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.interaction.integration.IsisRequestCycle;
import org.apache.isis.core.interaction.session.InteractionFactory;
import org.apache.isis.core.interaction.session.InteractionSession;
import org.apache.isis.core.interaction.session.MessageBroker;
import org.apache.isis.core.metamodel.spec.feature.ObjectMember;
import org.apache.isis.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.isis.core.runtime.context.IsisAppCommonContext;
import org.apache.isis.viewer.wicket.model.common.CommonContextUtils;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.isis.viewer.wicket.ui.pages.login.WicketSignInPage;
import org.apache.isis.viewer.wicket.ui.pages.mmverror.MmvErrorPage;
import org.apache.isis.viewer.wicket.ui.panels.PromptFormAbstract;

import lombok.val;
import lombok.extern.log4j.Log4j2;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

/**
 * Isis-specific implementation of the Wicket's {@link RequestCycle},
 * automatically opening a {@link InteractionSession} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 *
 * @since 2.0
 */
@Log4j2
public class WebRequestCycleForIsis implements IRequestCycleListener {

    public static final MetaDataKey<IsisRequestCycle> REQ_CYCLE_HANDLE_KEY =
            new MetaDataKey<IsisRequestCycle>() {private static final long serialVersionUID = 1L; };
    public static final MetaDataKey<Boolean> SESSION_WAS_EXPIRED_KEY =
            new MetaDataKey<Boolean>() { private static final long serialVersionUID = 1L; };
    public static final MetaDataKey<Boolean> LISTENER_HANDLER_USED_KEY =
            new MetaDataKey<Boolean>() { private static final long serialVersionUID = 1L; };

    private PageClassRegistry pageClassRegistry;
    private IsisAppCommonContext commonContext;

    @Override
    public synchronized void onBeginRequest(RequestCycle requestCycle) {

        log.debug("onBeginRequest in");

        if (!Session.exists()) {
            // Track if session was created from an expired one to notify user of the refresh.
            // If there is no remember me cookie, user will be redirected to sign in and no need to notify.
            if (userHasSessionWithRememberMe(requestCycle)) {
                Session.get().setMetaData(SESSION_WAS_EXPIRED_KEY, true);
            }
            log.debug("onBeginRequest out - session was not opened (because no Session)");
            return;
        }

        val commonContext = getCommonContext();
        val authentication = AuthenticatedWebSessionForIsis.get().getAuthentication();


        if (authentication == null) {
            log.debug("onBeginRequest out - session was not opened (because no authentication)");
            return;
        }

        val isisRequestCycle = IsisRequestCycle.next(
                commonContext.lookupServiceElseFail(InteractionFactory.class));

        requestCycle.setMetaData(REQ_CYCLE_HANDLE_KEY, isisRequestCycle);

        isisRequestCycle.onBeginRequest(authentication);

        log.debug("onBeginRequest out - session was opened");
    }

    @Override
    public void onRequestHandlerResolved(final RequestCycle cycle, final IRequestHandler handler) {

        log.debug("onRequestHandlerResolved in");

        if(handler instanceof RenderPageRequestHandler) {

            val validationResult = getCommonContext().getSpecificationLoader().getValidationResult();

            if(validationResult.hasFailures()) {
                RenderPageRequestHandler requestHandler = (RenderPageRequestHandler) handler;
                final IRequestablePage nextPage = requestHandler.getPage();
                if(nextPage instanceof ErrorPage || nextPage instanceof MmvErrorPage) {
                    // do nothing
                    return;
                }
                throw new MetaModelInvalidException(validationResult.getAsLineNumberedString());
            }
        }

        val isisRequestCycle = cycle.getMetaData(REQ_CYCLE_HANDLE_KEY);
        val session = AuthenticatedWebSessionForIsis.get();
        val wasExpired = session.getMetaData(SESSION_WAS_EXPIRED_KEY);
        
        if (wasExpired != null && wasExpired && isisRequestCycle != null) {
            // If a user clicks a menu item in an expired session, no action is executed, the session simply refreshes.
            // Two requests make it here, and the first one uses ListenerRequestHandler as its first handler.
            // The whole first request has to be ignored here, or the message would disappear before user can see it.
            if (handler instanceof ListenerRequestHandler) {
                cycle.setMetaData(LISTENER_HANDLER_USED_KEY, true);
            } else if (cycle.getMetaData(LISTENER_HANDLER_USED_KEY) == null) {
                getMessageBroker().ifPresent(broker -> broker.addMessage(translate("Expired session was refreshed")));
                session.setMetaData(SESSION_WAS_EXPIRED_KEY, false);
            }
        }
        
        log.debug("onRequestHandlerResolved out");

    }


    /**
     * Is called prior to {@link #onEndRequest(RequestCycle)}, and offers the opportunity to
     * throw an exception.
     */
    @Override
    public void onRequestHandlerExecuted(RequestCycle requestCycle, IRequestHandler handler) {
        log.debug("onRequestHandlerExecuted: handler: {}", handler);

        try {

            val isisRequestCycle = requestCycle.getMetaData(REQ_CYCLE_HANDLE_KEY);

            if(isisRequestCycle!=null) {
                isisRequestCycle.onRequestHandlerExecuted();
            }

        } catch(Exception ex) {

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

    /**
     * It is not possible to throw exceptions here, hence use of {@link #onRequestHandlerExecuted(RequestCycle, IRequestHandler)}.
     */
    @Override
    public synchronized void onEndRequest(RequestCycle requestCycle) {

        log.debug("onEndRequest");

        val isisRequestCycle = requestCycle.getMetaData(REQ_CYCLE_HANDLE_KEY);
        requestCycle.setMetaData(REQ_CYCLE_HANDLE_KEY, null);

        if(isisRequestCycle!=null) {
            isisRequestCycle.onEndRequest();
        }

    }

    @Override
    public void onDetach(RequestCycle requestCycle) {
        // detach the current @RequestScope, if any
        IRequestCycleListener.super.onDetach(requestCycle);
    }


    @Override
    public IRequestHandler onException(RequestCycle cycle, Exception ex) {

        log.debug("onException");

        val validationResult = getCommonContext().getSpecificationLoader().getValidationResult();
        if(validationResult.hasFailures()) {
            val mmvErrorPage = new MmvErrorPage(validationResult.getMessages("[%d] %s"));
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
                return respondGracefully(cycle);
            }

            // handle recognized exceptions gracefully also
            val exceptionRecognizerService = getExceptionRecognizerService();
            val recognizedIfAny = exceptionRecognizerService.recognize(ex);
            if(recognizedIfAny.isPresent()) {
                return respondGracefully(cycle);
            }

            final List<Throwable> causalChain = _Exceptions.getCausalChain(ex);
            final Optional<Throwable> hiddenIfAny = causalChain.stream()
                    .filter(ObjectMember.HiddenException::isInstanceOf).findFirst();
            if(hiddenIfAny.isPresent()) {
                addMessage("hidden");
                return respondGracefully(cycle);
            }
            final Optional<Throwable> disabledIfAny = causalChain.stream()
                    .filter(ObjectMember.DisabledException::isInstanceOf).findFirst();
            if(disabledIfAny.isPresent()) {
                addTranslatedMessage(disabledIfAny.get().getMessage());
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

        getMessageBroker().ifPresent(broker->{

            final String translatedPrefix = translate("Action no longer available");
            final String message = translatedSuffixIfAny != null
                    ? String.format("%s (%s)", translatedPrefix, translatedSuffixIfAny)
                    : translatedPrefix;

            broker.addMessage(message);

        });
    }

    private String translate(final String text) {
        if(text == null) {
            return null;
        }
        return getCommonContext().getTranslationService().translate(WebRequestCycleForIsis.class.getName(), text);
    }

    protected PageProvider errorPageProviderFor(Exception ex) {
        IRequestablePage errorPage = errorPageFor(ex);
        return errorPage != null? new PageProvider(errorPage): null;
    }

    // special case handling for PageExpiredException, otherwise infinite loop
    private static final ExceptionRecognizerForType pageExpiredExceptionRecognizer =
            new ExceptionRecognizerForType(PageExpiredException.class, $->"Requested page is no longer available.");

    protected IRequestablePage errorPageFor(Exception ex) {

        final Optional<Recognition> recognition;

        if(isInInteraction()) {
            val exceptionRecognizerService = getCommonContext().getServiceRegistry()
            .lookupServiceElseFail(ExceptionRecognizerService.class);

            recognition = exceptionRecognizerService
                    .recognizeFromSelected(
                            Can.<ExceptionRecognizer>ofSingleton(pageExpiredExceptionRecognizer)
                            .addAll(exceptionRecognizerService.getExceptionRecognizers()),
                            ex);

        } else {

            recognition = Optional.empty();

            val validationResult = getCommonContext().getSpecificationLoader().getValidationResult();
            if(validationResult.hasFailures()) {
                return new MmvErrorPage(validationResult.getMessages("[%d] %s"));
            }
            // not sure whether this can ever happen now...
            log.warn("Unable to obtain exceptionRecognizers (no session), "
                    + "will be treated as unrecognized exception", ex);
        }
        val exceptionModel = ExceptionModel.create(getCommonContext(), recognition, ex);

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
        if(!isInInteraction()) {
            return false;
        }
        return getWicketAuthenticatedWebSession().isSignedIn();
    }
    
    private boolean userHasSessionWithRememberMe(RequestCycle requestCycle) {
        val containerRequest = requestCycle.getRequest().getContainerRequest();
        
        if (containerRequest instanceof HttpServletRequest) {
            val cookies = ((HttpServletRequest) containerRequest).getCookies();
            String cookieKey = getCommonContext().getConfiguration().getViewer().getWicket().getRememberMe().getCookieKey();
            
            for (Cookie c : cookies) {
                if (c.getName().equals(cookieKey)) {
                    return true;
                }
            }
        }
        return false;
    }


    public void setPageClassRegistry(PageClassRegistry pageClassRegistry) {
        this.pageClassRegistry = pageClassRegistry;
    }

    // -- DEPENDENCIES

    public IsisAppCommonContext getCommonContext() {
        return commonContext = CommonContextUtils.computeIfAbsent(commonContext);
    }

    private ExceptionRecognizerService getExceptionRecognizerService() {
        return getCommonContext().getServiceRegistry().lookupServiceElseFail(ExceptionRecognizerService.class);
    }

    private boolean isInInteraction() {
        return commonContext.getInteractionTracker().isInInteractionSession();
    }

    private Optional<MessageBroker> getMessageBroker() {
        return commonContext.getInteractionTracker().currentMessageBroker();
    }

    private AuthenticatedWebSession getWicketAuthenticatedWebSession() {
        return AuthenticatedWebSession.get();
    }

}
