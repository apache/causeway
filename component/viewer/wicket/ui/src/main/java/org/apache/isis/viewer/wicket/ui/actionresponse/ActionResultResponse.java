/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.actionresponse;

import org.apache.wicket.request.IRequestHandler;

import org.apache.isis.viewer.wicket.ui.pages.PageAbstract;

/**
 * The response to provide as a result of interpreting the response;
 * either to show a {@link #toPage(PageAbstract) page}, or to {@link #withHandler(IRequestHandler) redirect} to a 
 * handler (eg a download).
 */
public class ActionResultResponse {
    private final ActionResultResponseType resultType;
    private final IRequestHandler handler;
    private final PageAbstract page;
    public static ActionResultResponse withHandler(ActionResultResponseType resultType, IRequestHandler handler) {
        return new ActionResultResponse(resultType, handler, null);
    }
    public static ActionResultResponse toPage(ActionResultResponseType resultType, PageAbstract page) {
        return new ActionResultResponse(resultType, null, page);
    }
    private ActionResultResponse(ActionResultResponseType resultType, IRequestHandler handler, PageAbstract page) {
        this.resultType = resultType;
        this.handler = handler;
        this.page = page;
    }
    public boolean isRedirect() {
        return handler != null;
    }
    public boolean isToPage() {
        return page != null;
    }
    public IRequestHandler getHandler() {
        return handler;
    }
    public PageAbstract getToPage() {
        return page;
    }
    public ActionResultResponseType getResultType() {
        return resultType;
    }
}