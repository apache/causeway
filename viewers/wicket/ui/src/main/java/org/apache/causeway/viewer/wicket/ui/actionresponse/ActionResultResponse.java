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

import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.value.OpenUrlStrategy;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.viewer.wicket.model.models.ActionModel;
import org.apache.causeway.viewer.wicket.ui.pages.obj.DomainObjectPage;

import org.jspecify.annotations.NonNull;

/**
 * The response to provide as a result of interpreting the response;
 * either to show a {@link #toPage(PageRedirectRequest) page}, or to
 * {@link #withHandler(IRequestHandler) redirect} to a
 * handler (eg a download).
 */
public record ActionResultResponse(
    ActionResultResponseHandlingStrategy handlingStrategy,
    /**
     * Populated only if {@link #handlingStrategy()}
     * is {@link ActionResultResponseHandlingStrategy#SCHEDULE_HANDLER}
     */
    IRequestHandler handler,
    /**
     * Populated only if {@link #handlingStrategy()}
     * is {@link ActionResultResponseHandlingStrategy#REDIRECT_TO_PAGE}
     */
    PageRedirectRequest<?> pageRedirect,
    /**
     * Populated only if {@link #handlingStrategy()} is
     * either {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_NEW_BROWSER_WINDOW}
     * or {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_SAME_BROWSER_WINDOW}
     */
    AjaxRequestTarget ajaxTarget,
    /**
     * Populated only if {@link #handlingStrategy()} is
     * either {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_NEW_BROWSER_WINDOW}
     * or {@link ActionResultResponseHandlingStrategy#OPEN_URL_IN_SAME_BROWSER_WINDOW}
     */
    String url) {

    public static ActionResultResponse toDomainObjectPage(final @NonNull ManagedObject entityOrViewmodel) {
        var pageRedirectRequest = PageRedirectRequest.forPageClassAndBookmark(
                DomainObjectPage.class, entityOrViewmodel.refreshBookmark().orElseThrow());
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
            final AjaxRequestTarget ajaxTarget,
            final String url,
            final @NonNull OpenUrlStrategy openUrlStrategy) {
        return new ActionResultResponse(
                openUrlStrategy.isNewWindow()
                    ? ActionResultResponseHandlingStrategy.OPEN_URL_IN_NEW_BROWSER_WINDOW
                    : ActionResultResponseHandlingStrategy.OPEN_URL_IN_SAME_BROWSER_WINDOW,
                null, null, ajaxTarget, url);
    }
}
