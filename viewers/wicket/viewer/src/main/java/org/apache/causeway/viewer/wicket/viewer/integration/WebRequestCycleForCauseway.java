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
package org.apache.causeway.viewer.wicket.viewer.integration;

import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.causeway.applib.services.metrics.MetricsService;

import org.apache.wicket.Application;
import org.apache.wicket.IPageFactory;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
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
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.PageRequestHandlerTracker;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.exceptions.unrecoverable.BookmarkNotFoundException;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerForType;
import org.apache.causeway.applib.services.exceprecog.ExceptionRecognizerService;
import org.apache.causeway.applib.services.exceprecog.Recognition;
import org.apache.causeway.applib.services.i18n.TranslationContext;
import org.apache.causeway.applib.services.iactn.Interaction;
import org.apache.causeway.applib.services.iactnlayer.InteractionService;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.spec.feature.ObjectMember;
import org.apache.causeway.core.metamodel.specloader.validator.MetaModelInvalidException;
import org.apache.causeway.viewer.wicket.model.models.HasCommonContext;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.errors.ExceptionModel;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.error.ErrorPage;
import org.apache.causeway.viewer.wicket.ui.pages.login.WicketSignInPage;
import org.apache.causeway.viewer.wicket.ui.pages.mmverror.MmvErrorPage;
import org.apache.causeway.viewer.wicket.ui.panels.PromptFormAbstract;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

/**
 * Causeway-specific implementation of the Wicket's {@link RequestCycle},
 * automatically opening a {@link Interaction} at the beginning of the request
 * and committing the transaction and closing the session at the end.
 *
 * @since 2.0
 */
@Log4j2
public class WebRequestCycleForCauseway
implements
    HasCommonContext,
    IRequestCycleListener {

    // introduced (CAUSEWAY-1922) to handle render 'session refreshed' messages after session was expired
    private static enum SessionLifecyclePhase {
        DONT_CARE,
        EXPIRED,
        ACTIVE_AFTER_EXPIRED;
        static boolean isExpired(final RequestCycle requestCycle) {
            return Session.exists()
                    && SessionLifecyclePhase.EXPIRED == requestCycle.getMetaData(SESSION_LIFECYCLE_PHASE_KEY);
        }
        static boolean isActiveAfterExpired() {
            return Session.exists()
                    && SessionLifecyclePhase.ACTIVE_AFTER_EXPIRED == Session.get().getMetaData(SESSION_LIFECYCLE_PHASE_KEY);
        }
        static void transferExpiredFlagToSession() {
            Session.get().setMetaData(SESSION_LIFECYCLE_PHASE_KEY, SessionLifecyclePhase.ACTIVE_AFTER_EXPIRED);
            Session.get().setAttribute("session-expiry-message-timeframe", LocalDateTime.now().plusNanos(1000_000_000L));
        }
        static void clearExpiredFlag() {
            Session.get().setMetaData(SESSION_LIFECYCLE_PHASE_KEY, SessionLifecyclePhase.DONT_CARE);
        }
        static boolean isExpiryMessageTimeframeExpired() {
            var sessionExpiryMessageTimeframe =
                    (LocalDateTime) Session.get().getAttribute("session-expiry-message-timeframe");
            return sessionExpiryMessageTimeframe==null
                    || LocalDateTime.now().isAfter(sessionExpiryMessageTimeframe);
        }

    }

    private static final MetaDataKey<SessionLifecyclePhase> SESSION_LIFECYCLE_PHASE_KEY =
            new MetaDataKey<SessionLifecyclePhase>() { private static final long serialVersionUID = 1L; };

    @Setter
    private PageClassRegistry pageClassRegistry;

    private static ThreadLocal<Timing> timings = ThreadLocal.withInitial(Timing::new);

    @Override
    public synchronized void onBeginRequest(final RequestCycle requestCycle) {

        if(log.isTraceEnabled()) {
            log.trace("onBeginRequest in");
        }

        if (!Session.exists()) {
            // Track if session was created from an expired one to notify user of the refresh.
            // If there is no remember me cookie, user will be redirected to sign in and no need to notify.
            if (userHasSessionWithRememberMe(requestCycle)) {
                requestCycle.setMetaData(SESSION_LIFECYCLE_PHASE_KEY, SessionLifecyclePhase.EXPIRED);
                if(log.isTraceEnabled()) {
                    log.trace("flagging the RequestCycle as expired (rememberMe feature is active for the current user)");
                }
            }
            if(log.isTraceEnabled()) {
                log.trace("onBeginRequest out - session was not opened (because no Session)");
            }
            return;
        }

        // participate if an InteractionContext was already provided through some other mechanism,
        // but fail early if the current user is impersonating
        // (seeing this if going back the browser history into a page, that was previously impersonated)
        var interactionService = getInteractionService();
        var authenticatedWebSession = AuthenticatedWebSessionForCauseway.get();

        /*XXX for debugging delegated user ...
        interactionService.openInteraction(InteractionContext
                .ofUserWithSystemDefaults(
                        UserMemento.ofName("delegated")
                        .withRoleAdded(UserMemento.AUTHORIZED_USER_ROLE)
                        .withAuthenticationSource(AuthenticationSource.EXTERNAL)));*/

        var currentInteractionContext = interactionService.currentInteractionContext();
        if(currentInteractionContext.isPresent()) {
            if(currentInteractionContext.get().getUser().isImpersonating()) {
                throw _Exceptions.illegalState("cannot enter a new request cycle with a left over impersonating user");
            }
            authenticatedWebSession.setPrimedInteractionContext(currentInteractionContext.get());
        }

        var interactionContext0 = authenticatedWebSession.getInteractionContext();
        if (interactionContext0 == null) {
            log.warn("onBeginRequest out - session was not opened (because no authentication)");
            return;
        }

        // impersonation support
        var interactionContext1 = lookupServiceElseFail(UserService.class)
                .lookupImpersonatedUser()
                .map(sudoUser -> interactionContext0.withUser(sudoUser))
                .orElse(interactionContext0);

        // Note: this is a no-op if an interactionContext layer was already opened and is unchanged.
        interactionService.openInteraction(interactionContext1);

        if(log.isTraceEnabled()) {
            log.trace("onBeginRequest out - session was opened");
        }

        if(log.isDebugEnabled()) {
            timings.set(new Timing());
        }
    }

    @Override
    public void onRequestHandlerResolved(final RequestCycle requestCycle, final IRequestHandler handler) {

        if(log.isTraceEnabled()) {
            log.trace("onRequestHandlerResolved in (handler: {}, hasSession: {})",
                    ()->handler.getClass().getName(),
                    ()->Session.exists() ? Session.get().hashCode() : "false");
        }

        // this nested class is hidden; it seems it is always used to create a new session after one has expired
        if("org.apache.wicket.request.flow.ResetResponseException$ResponseResettingDecorator"
                    .equals(handler.getClass().getName())
                && SessionLifecyclePhase.isExpired(requestCycle)) {

            if(log.isTraceEnabled()) {
                log.trace("Transferring the 'expired' flag into the current session.");
            }
            SessionLifecyclePhase.transferExpiredFlagToSession();

        } else if(handler instanceof RenderPageRequestHandler) {

            // using side-effect free access to MM validation result
            var validationResult = getMetaModelContext().getSpecificationLoader().getValidationResult()
            .orElseThrow(()->_Exceptions.illegalState("Application is not fully initialized yet."));

            if(validationResult.hasFailures()) {
                RenderPageRequestHandler requestHandler = (RenderPageRequestHandler) handler;
                final IRequestablePage nextPage = requestHandler.getPage();
                if(nextPage instanceof ErrorPage || nextPage instanceof MmvErrorPage) {
                    // do nothing
                    return;
                }
                throw new MetaModelInvalidException(validationResult.getAsLineNumberedString());
            }

            if(SessionLifecyclePhase.isActiveAfterExpired()) {

                // we receive multiple requests after a session had expired and was reactivated;
                // impossible to tell which one is the last that should then render the message;
                // so we just render them on all requests, until the 1 second time frame since session creation
                // has gone by, could result in the message displayed too often,
                // but thats better than no message displayed at all
                if(SessionLifecyclePhase.isExpiryMessageTimeframeExpired()) {
                    if(log.isTraceEnabled()) {
                        log.trace("clear the session's active-after-expired flag (expiry-message timeframe has expired");
                    }
                    SessionLifecyclePhase.clearExpiredFlag();
                } else {
                    getMessageBroker().ifPresent(broker -> {
                        if(log.isTraceEnabled()) {
                            log.trace("render 'expired' message");
                        }
                        broker.addMessage(translate("You have been redirected to the home page "
                                + "as your session expired (no recent activity)."));
                    });
                }
            }
        }

        if(log.isTraceEnabled()) {
            log.trace("onRequestHandlerResolved out");
        }
    }

    /**
     * Is called prior to {@link #onEndRequest(RequestCycle)}, and offers the opportunity to
     * throw an exception.
     */
    @Override
    public void onRequestHandlerExecuted(final RequestCycle requestCycle, final IRequestHandler handler) {
        if(log.isTraceEnabled()) {
            log.trace("onRequestHandlerExecuted: handler: {}", handler.getClass().getName());
        }
    }

    /**
     * It is not possible to throw exceptions here, hence use of {@link #onRequestHandlerExecuted(RequestCycle, IRequestHandler)}.
     */
    @Override
    public synchronized void onEndRequest(final RequestCycle requestCycle) {

        if(log.isDebugEnabled()) {
            var metricsServiceIfAny = getMetaModelContext().lookupService(MetricsService.class);
            long took = timings.get().took();
            if(took > 50) {  // avoid too much clutter
                if(metricsServiceIfAny.isPresent()) {
                    var metricsService = metricsServiceIfAny.get();
                    int numberEntitiesLoaded = metricsService.numberEntitiesLoaded();
                    int numberEntitiesDirtied = metricsService.numberEntitiesDirtied();
                    if(numberEntitiesLoaded > 0 || numberEntitiesDirtied > 0) {
                        log.debug("onEndRequest  took: {}ms  numberEntitiesLoaded: {}, numberEntitiesDirtied: {}", took, numberEntitiesLoaded, numberEntitiesDirtied);
                    }
                } else {
                    log.debug("onEndRequest  took: {}ms", took);
                }
            }
        }

        getMetaModelContext().lookupService(InteractionService.class).ifPresent(
            InteractionService::closeInteractionLayers
        );
    }

    @Override
    public void onDetach(final RequestCycle requestCycle) {
        // detach the current @RequestScope, if any
        IRequestCycleListener.super.onDetach(requestCycle);
    }

    @Override
    public IRequestHandler onException(final RequestCycle cycle, final Exception ex) {

        if(log.isDebugEnabled()) {
            log.debug("onException {}  took: {}ms", ex.getClass().getSimpleName(), timings.get().took());
        }

        // using side-effect free access to MM validation result
        var validationResult = getMetaModelContext().getSpecificationLoader().getValidationResult()
                .orElse(null);
        if(validationResult!=null
                && validationResult.hasFailures()) {
            var mmvErrorPage = new MmvErrorPage(validationResult.getMessages("[%d] %s"));
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
                    addActionNoLongerAvailableMessage(null);

                }
                return respondGracefully(cycle);
            }

            // handle recognized exceptions gracefully also
            var exceptionRecognizerService = getExceptionRecognizerService();
            var recognizedIfAny = exceptionRecognizerService.recognize(ex);
            if(recognizedIfAny.isPresent()) {
                addWarning(recognizedIfAny.get().toMessage(getMetaModelContext().getTranslationService()));
                return respondGracefully(cycle);
            }

            final List<Throwable> causalChain = _Exceptions.getCausalChain(ex);
            final Optional<Throwable> hiddenIfAny = causalChain.stream()
                    .filter(ObjectMember.HiddenException::isInstanceOf).findFirst();
            if(hiddenIfAny.isPresent()) {
                addActionNoLongerAvailableMessage("hidden");
                return respondGracefully(cycle);
            }
            final Optional<Throwable> disabledIfAny = causalChain.stream()
                    .filter(ObjectMember.DisabledException::isInstanceOf).findFirst();
            if(disabledIfAny.isPresent()) {
                addActionNoLongerAvailableMessage(disabledIfAny.get().getMessage());
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

    private void addWarning(final @Nullable String translatedWarning) {
        _Strings.nonEmpty(translatedWarning)
        .ifPresent(warning->{
            getMessageBroker().ifPresent(broker->{
                broker.addWarning(warning);
            });
        });
    }

    private void addActionNoLongerAvailableMessage(final @Nullable String suffixIfAny) {

        getMessageBroker().ifPresent(broker->{

            final String translatedPrefix = translate("Action no longer available");
            final String message = suffixIfAny != null
                    ? String.format("%s (%s)", translatedPrefix, translate(suffixIfAny))
                    : translatedPrefix;

            broker.addMessage(message);

        });
    }

    @Override
    public String translate(final String text) {
        return translate(
                		TranslationContext.forClassName(WebRequestCycleForCauseway.class),
                		text);
    }

    protected PageProvider errorPageProviderFor(final Exception ex) {
        IRequestablePage errorPage = errorPageFor(ex);
        return errorPage != null
                ? new PageProvider(errorPage)
                : null;
    }

    // special case handling for PageExpiredException, otherwise infinite loop
    private static final ExceptionRecognizerForType pageExpiredExceptionRecognizer =
            new ExceptionRecognizerForType(
                    PageExpiredException.class,
                    __->"Requested page is no longer available.");

    private static final ExceptionRecognizerForType exceptionRecognizerForBookmarkNotFoundException =
            new ExceptionRecognizerForType(
                    BookmarkNotFoundException.class, __ -> "Bookmark is not found.");

    protected IRequestablePage errorPageFor(final Exception ex) {

        var mmc = getMetaModelContext();

        if(mmc==null) {
            log.warn("Unable to obtain the MetaModelContext (no session?)");
            return null;
        }

        // using side-effect free access to MM validation result
        var validationResult = getMetaModelContext().getSpecificationLoader().getValidationResult()
                .orElse(null);
        if(validationResult!=null
                && validationResult.hasFailures()) {
            return new MmvErrorPage(validationResult.getMessages("[%d] %s"));
        }

        var exceptionRecognizerService = getMetaModelContext().getServiceRegistry()
            .lookupServiceElseFail(ExceptionRecognizerService.class);

        final Optional<Recognition> recognition = exceptionRecognizerService
                .recognizeFromSelected(
                        Can.<ExceptionRecognizer>of(
                                        pageExpiredExceptionRecognizer,
                                        exceptionRecognizerForBookmarkNotFoundException)
                                .addAll(exceptionRecognizerService.getExceptionRecognizers()),
                        ex);

        var exceptionModel = ExceptionModel.create(mmc, recognition, ex);

        return isSignedIn()
                ? new ErrorPage(exceptionModel)
                : newSignInPage(exceptionModel);
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
     * Matters should improve once CAUSEWAY-299 gets implemented...
     */
    protected boolean isSignedIn() {
        if(!isInInteraction()) {
            return false;
        }
        return getWicketAuthenticatedWebSession().isSignedIn();
    }

    private boolean userHasSessionWithRememberMe(final RequestCycle requestCycle) {
        var containerRequest = requestCycle.getRequest().getContainerRequest();

        if (containerRequest instanceof HttpServletRequest) {
            var cookies = Can.ofArray(((HttpServletRequest) containerRequest).getCookies());
            var cookieKey = _Strings.nullToEmpty(
                    getConfiguration().getViewer().getWicket().getRememberMe().getCookieKey());

            for (var cookie : cookies) {
                if (cookieKey.equals(cookie.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    // -- DEPENDENCIES

    private ExceptionRecognizerService getExceptionRecognizerService() {
        return getMetaModelContext().getServiceRegistry().lookupServiceElseFail(ExceptionRecognizerService.class);
    }

    private boolean isInInteraction() {
        return getMetaModelContext().getInteractionService().isInInteraction();
    }

    private AuthenticatedWebSession getWicketAuthenticatedWebSession() {
        return AuthenticatedWebSession.get();
    }

}
