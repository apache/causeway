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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.value.OpenUrlStrategy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.ui.pages.entity.EntityPage;

import lombok.Getter;
import lombok.NonNull;

/**
 * The response to provide as a result of interpreting the response;
 * either to show a {@link #toPage(PageRedirectRequest) page}, or to
 * {@link #withHandler(IRequestHandler) redirect} to a
 * handler (eg a download).
 */
public class ActionResultResponse {

    @Getter
    private final ActionResultResponseHandlingStrategy handlingStrategy;
    private final IRequestHandler handler;
    private final PageRedirectRequest<?> pageRedirect;
    private final AjaxRequestTarget target;
    private final String url;

    public static ActionResultResponse toEntityPage(final @NonNull ManagedObject entityOrViewmodel) {
        var pageRedirectRequest = PageRedirectRequest.forPageClassAndBookmark(
                EntityPage.class, entityOrViewmodel.refreshBookmark().orElseThrow());
        return ActionResultResponse.toPage(pageRedirectRequest);
    }

    public static ActionResultResponse determineAndInterpretResult(
            final ActionModel actionModel,
            final @Nullable AjaxRequestTarget targetIfAny,
            final @Nullable ManagedObject resultAdapter) {
        return _ResponseUtil.determineAndInterpretResult(actionModel, targetIfAny, resultAdapter);
    }

    static ActionResultResponse withHandler(final IRequestHandler handler) {
        return new ActionResultResponse(
                ActionResultResponseHandlingStrategy.SCHEDULE_HANDLER, handler, null, null, null);
    }

    static ActionResultResponse toPage(final PageRedirectRequest<?> page) {
        return new ActionResultResponse(
                ActionResultResponseHandlingStrategy.REDIRECT_TO_PAGE, null, page, null, null);
    }

    static ActionResultResponse openUrlInBrowser(
            final AjaxRequestTarget target,
            final String url,
            final @NonNull OpenUrlStrategy openUrlStrategy) {
        return new ActionResultResponse(
                openUrlStrategy.isNewWindow()
                    ? ActionResultResponseHandlingStrategy.OPEN_URL_IN_NEW_BROWSER_WINDOW
                    : ActionResultResponseHandlingStrategy.OPEN_URL_IN_SAME_BROWSER_WINDOW,
                null, null, target, url);
    }

    ActionResultResponse(
            final ActionResultResponseHandlingStrategy strategy,
            final IRequestHandler handler,
            final PageRedirectRequest<?> pageRedirect,
            final AjaxRequestTarget target,
            final String url) {
        this.handlingStrategy = strategy;
        this.handler = handler;
        this.pageRedirect = pageRedirect;
        this.target = target;
        this.url = url;
    }

    //TODO[CAUSEWAY-3815] WIP should create URL from current page then open in new browser
    ActionResultResponse withForceNewBrowserWindow() {
        var url = this.url!=null
                ? this.url
                : RequestCycle.get().getRequest().getOriginalUrl().toString();

        return new ActionResultResponse(
                ActionResultResponseHandlingStrategy.OPEN_URL_IN_NEW_BROWSER_WINDOW,
                handler, pageRedirect, target, url);
    }

    //TODO[CAUSEWAY-3815] WIP should force reload the entire page (or do a proper partial page update (AJAX) of the originating table)
    ActionResultResponse withForceReload() {
        return this;
    }

    /**
     * Populated only if {@link #getHandlingStrategy() handling strategy}
     * is {@link ActionResultResponseHandlingStrategy#SCHEDULE_HANDLER}
     */
    public IRequestHandler getHandler() {
        return handler;
    }

    /**
     * Populated only if {@link #getHandlingStrategy() handling strategy}
     * is {@link ActionResultResponseHandlingStrategy#REDIRECT_TO_PAGE}
     */
    public PageRedirectRequest<?> getPageRedirect() {
        return pageRedirect;
    }

    /**
     * Populated only if {@link #getHandlingStrategy() handling strategy} is
     * either {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_NEW_BROWSER_WINDOW}
     * or {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_SAME_BROWSER_WINDOW}
     */
    public AjaxRequestTarget getAjaxTarget() {
        return target;
    }

    /**
     * Populated only if {@link #getHandlingStrategy() handling strategy} is
     * either {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_NEW_BROWSER_WINDOW}
     * or {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_SAME_BROWSER_WINDOW}
     */
    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return String.format("ActionResultResponse["
                + "handlingStrategy=%s,"
                + "requestHandler=%s,"
                + "pageRedirect=%s,"
                + "ajaxTarget=%s,"
                + "url=%s"
                + "]",
                handlingStrategy.name(),
                handler,
                pageRedirect,
                target,
                url);
    }

    /** introduced for debugging */
    public String toStringMultiline() {
        return String.format("ActionResultResponse {\n"
                + "\thandlingStrategy=%s,\n"
                + "\trequestHandler=%s,\n"
                + "\tpageRedirect=%s,\n"
                + "\tajaxTarget=%s,\n"
                + "\turl=%s\n"
                + "}",
                handlingStrategy.name(),
                handler,
                pageRedirect,
                target,
                url);
    }

}
