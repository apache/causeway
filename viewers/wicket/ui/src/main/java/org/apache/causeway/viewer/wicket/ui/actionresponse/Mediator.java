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
package org.apache.causeway.viewer.wicket.ui.actionresponse;

import java.time.Duration;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.IResourceStream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.OpenUrlStrategy;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.model.models.RedirectRequestHandlerWithOpenUrlStrategy;
import org.apache.causeway.viewer.wicket.ui.pages.obj.DomainObjectPage;

/**
 * {@link #handle()} handles the execution response using given {@link ExecutionResultHandlingStrategy}.
 */
record Mediator(
    ExecutionResultHandlingStrategy handlingStrategy,
    /**
     * Populated only if {@link #handlingStrategy()}
     * is {@link ExecutionResultHandlingStrategy#SCHEDULE_HANDLER}
     */
    IRequestHandler handler,
    /**
     * Populated only if {@link #handlingStrategy()}
     * is {@link ExecutionResultHandlingStrategy#REDIRECT_TO_PAGE}
     */
    PageRedirectRequest<?> pageRedirect,
    /**
     * Populated only if {@link #handlingStrategy()} is
     * either {@link ExecutionResultHandlingStrategy#OPEN_URL_IN_NEW_BROWSER_WINDOW}
     * or {@link ExecutionResultHandlingStrategy#OPEN_URL_IN_SAME_BROWSER_WINDOW}
     */
    AjaxRequestTarget ajaxTarget,
    /**
     * Populated only if {@link #handlingStrategy()} is
     * either {@link ExecutionResultHandlingStrategy#OPEN_URL_IN_NEW_BROWSER_WINDOW}
     * or {@link ExecutionResultHandlingStrategy#OPEN_URL_IN_SAME_BROWSER_WINDOW}
     */
    String url) {

    enum ExecutionResultHandlingStrategy {
        REDIRECT_TO_PAGE,
        OPEN_URL_IN_NEW_BROWSER_WINDOW,
        OPEN_URL_IN_SAME_BROWSER_WINDOW,
        SCHEDULE_HANDLER
    }

    static Mediator toDomainObjectPage(final @NonNull ManagedObject entityOrViewmodel) {
        var pageRedirectRequest = PageRedirectRequest.forPageClassAndBookmark(
                DomainObjectPage.class, entityOrViewmodel.refreshBookmark().orElseThrow());
        return Mediator.toPage(pageRedirectRequest);
    }

    static Mediator determineAndInterpretResult(
            final ActionModel actionModel,
            final @Nullable AjaxRequestTarget targetIfAny,
            final @Nullable ManagedObject resultAdapter) {
        return MediatorFactory.determineAndInterpretResult(actionModel, targetIfAny, resultAdapter);
    }

    static Mediator withHandler(final IRequestHandler handler) {
        return new Mediator(
                ExecutionResultHandlingStrategy.SCHEDULE_HANDLER, handler, null, null, null);
    }

    static Mediator toPage(final PageRedirectRequest<?> page) {
        return new Mediator(
                ExecutionResultHandlingStrategy.REDIRECT_TO_PAGE, null, page, null, null);
    }

    static Mediator openUrlInBrowser(
            final AjaxRequestTarget ajaxTarget,
            final String url,
            final @NonNull OpenUrlStrategy openUrlStrategy) {
        return new Mediator(
                openUrlStrategy.isNewWindow()
                    ? ExecutionResultHandlingStrategy.OPEN_URL_IN_NEW_BROWSER_WINDOW
                    : ExecutionResultHandlingStrategy.OPEN_URL_IN_SAME_BROWSER_WINDOW,
                null, null, ajaxTarget, url);
    }

    void handle() {
        switch(handlingStrategy()) {
            case REDIRECT_TO_PAGE->{
                // force any changes in state etc to happen now prior to the redirect;
                // in the case of an object being returned, this should cause our page mementos
                // (eg EntityModel) to hold the correct state.  I hope.
                MetaModelContext.instance().ifPresent(mmc->mmc.getTransactionService().flushTransaction());
                // "redirect-after-post"
                this.pageRedirect().apply();
            }
            case OPEN_URL_IN_NEW_BROWSER_WINDOW -> {
                final String fullUrl = expanded(RequestCycle.get(), url());
                scheduleJs(ajaxTarget(), javascriptFor_newWindow(fullUrl), 100);
            }
            case OPEN_URL_IN_SAME_BROWSER_WINDOW -> {
                final String fullUrl = expanded(RequestCycle.get(), url());
                scheduleJs(ajaxTarget(), javascriptFor_sameWindow(fullUrl), 100);
            }
            case SCHEDULE_HANDLER -> {
                var requestCycle = RequestCycle.get();
                var ajaxTarget = requestCycle.find(AjaxRequestTarget.class).orElse(null);

                if (ajaxTarget == null) {
                    // non-Ajax request => just stream the Lob to the browser
                    // or if this is a no-arg action, there also will be no parent for the component
                    requestCycle.scheduleRequestHandlerAfterCurrent(handler());
                } else {
                    // otherwise,
                    // Ajax request => respond with a redirect to be able to stream the Lob to the client
                    final IRequestHandler requestHandler = handler();
                    if(requestHandler instanceof ResourceStreamRequestHandler scheduledHandler) {
                        var streamingBehavior = new StreamAfterAjaxResponseBehavior(scheduledHandler);
                        var page = ajaxTarget.getPage();
                        page.add(streamingBehavior);
                        CharSequence callbackUrl = streamingBehavior.getCallbackUrl();
                        scheduleJs(ajaxTarget, javascriptFor_sameWindow(callbackUrl), 10);
                    } else if(requestHandler instanceof RedirectRequestHandlerWithOpenUrlStrategy redirectHandler) {

                        final String url = redirectHandler.getRedirectUrl();
                        final String fullUrl = expanded(requestCycle, url);

                        if(redirectHandler.getOpenUrlStrategy().isNewWindow()) {
                            scheduleJs(ajaxTarget, javascriptFor_newWindow(fullUrl), 100);
                        } else {
                            scheduleJs(ajaxTarget, javascriptFor_sameWindow(fullUrl), 100);
                        }
                    } else {
                        throw _Exceptions.unrecoverable(
                                "no logic implemented to handle IRequestHandler of type %s",
                                requestHandler.getClass().getName());
                    }
                }
            }
        }
    }

    // -- HELPER

    /**
     * @see #expanded(String)
     */
    private static String expanded(final RequestCycle requestCycle, final String url) {
        String urlStr = expanded(url);
        return requestCycle.getUrlRenderer().renderFullUrl(Url.parse(urlStr));
    }

    /**
     * very simple template support, the idea being that "antiCache=${currentTimeMillis}"
     * will be replaced automatically.
     */
    private static String expanded(String urlStr) {
        if(urlStr.contains("antiCache=${currentTimeMillis}")) {
            urlStr = urlStr.replace("antiCache=${currentTimeMillis}", "antiCache="+System.currentTimeMillis());
        }
        return urlStr;
    }

    private static String javascriptFor_newWindow(final CharSequence url) {
        return "function(){Wicket.Event.publish(Causeway.Topic.OPEN_IN_NEW_TAB, '" + url + "');}";
    }

    private static String javascriptFor_sameWindow(final CharSequence url) {
        return "\"window.location.href='" + url + "'\"";
    }

    private static void scheduleJs(final AjaxRequestTarget target, final String js, final int millis) {
        // the timeout is needed to let Wicket release the channel
        target.appendJavaScript(String.format("setTimeout(%s, %d);", js, millis));
    }

    /**
     * A special Ajax behavior that is used to stream the contents of a Lob after
     * an Ajax request.
     */
    private static class StreamAfterAjaxResponseBehavior extends AbstractAjaxBehavior {
        private static final long serialVersionUID = 1L;

        private final String fileName;
        private final IResourceStream resourceStream;
        private final Duration cacheDuration;

        public StreamAfterAjaxResponseBehavior(final ResourceStreamRequestHandler scheduledHandler) {
            this.fileName = scheduledHandler.getFileName();
            this.resourceStream = scheduledHandler.getResourceStream();
            this.cacheDuration = scheduledHandler.getCacheDuration();
        }

        @Override public void onRequest() {
            var handler = new ResourceStreamRequestHandler(resourceStream, fileName);
            handler.setCacheDuration(cacheDuration);
            handler.setContentDisposition(ContentDisposition.ATTACHMENT);
            var page = getComponent();
            page.getRequestCycle().scheduleRequestHandlerAfterCurrent(handler);
            page.remove(this);
        }
    }

}
