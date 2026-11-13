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
package org.apache.causeway.viewer.wicket.ui.exec;

import org.apache.wicket.core.request.handler.BookmarkablePageRequestHandler;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;

record PageRedirectRequest<T extends IRequestablePage>(
    @NonNull Class<T> pageClass,
    @Nullable PageParameters pageParameters,
    @Nullable IRequestablePage pageInstance) {

    static <T extends IRequestablePage> PageRedirectRequest<T> forPageParameters(
            final @NonNull Class<T> pageClass,
            final @NonNull PageParameters pageParameters) {
        return new PageRedirectRequest<>(pageClass, pageParameters, null);
    }

    static <T extends IRequestablePage> PageRedirectRequest<T> forBookmark(
            final @NonNull Class<T> pageClass,
            final @NonNull Bookmark bookmark) {
        return forPageParameters(
                        pageClass,
                        PageParameterUtils.createPageParametersForBookmark(bookmark));
    }

    static <T extends IRequestablePage> PageRedirectRequest<T> forPageClass(
            final @NonNull Class<T> pageClass) {
        return new PageRedirectRequest<>(pageClass, null, null);
    }

    static <T extends IRequestablePage> PageRedirectRequest<T> forPage(
            final @NonNull Class<T> pageClass,
            final @NonNull T pageInstance) {
        return new PageRedirectRequest<>(pageClass, null, pageInstance);
    }

    /**
     * Canonical constructor, honors either {@link #pageParameters} as directly given
     * or indirectly from the {@link #pageInstance} if present.
     * If neither is present, pageParameters are <code>null</code>
     *
     * @param pageClass page class
     * @param pageParameters (nullable)
     * @param pageInstance (nullable)
     */
    PageRedirectRequest {
        pageParameters = pageParameters!=null
            ? pageParameters
            : pageInstance!=null
                ? pageInstance.getPageParameters()
                : null;
    }

    /**
     * Relative page URL
     */
    String toUrl() {
        var handler = new BookmarkablePageRequestHandler(
                new PageProvider(pageClass, pageParameters));
        return RequestCycle.get().urlFor(handler)
            .toString();
    }

    void apply() {
        var requestCycle = RequestCycle.get();
        if(requestCycle==null) return;
        if(pageInstance!=null) {
            requestCycle.setResponsePage(pageInstance);
            return;
        }
        if(pageParameters!=null) {
            requestCycle.setResponsePage(pageClass, pageParameters);
            return;
        }
        requestCycle.setResponsePage(pageClass);
    }

}
