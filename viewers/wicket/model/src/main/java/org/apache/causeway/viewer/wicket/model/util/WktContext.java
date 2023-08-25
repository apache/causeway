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
package org.apache.causeway.viewer.wicket.model.util;

import java.util.Optional;

import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.core.request.handler.ListenerRequestHandler;
import org.apache.wicket.request.cycle.RequestCycle;
import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.context.HasMetaModelContext;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

/**
 * @since 2.0
 */
@UtilityClass
public class WktContext {

    @Nullable
    public String getApplicationKey() {
        final Application application = ThreadContext.getApplication(); // nullable
        return Optional.ofNullable(application)
                .map(Application::getName)
                .orElse(null);
    }

    @Nullable
    public MetaModelContext getMetaModelContext() {
        final Application application = ThreadContext.getApplication(); // nullable
        return _Casts.castTo(HasMetaModelContext.class, application)
                .map(HasMetaModelContext::getMetaModelContext)
                .orElse(null);
    }

    @Nullable
    public MetaModelContext getMetaModelContext(final @NonNull String applicationKey) {
        final Application application = Application.get(applicationKey); // nullable
        return _Casts.castTo(HasMetaModelContext.class, application)
                .map(HasMetaModelContext::getMetaModelContext)
                .orElse(null);
    }

    @Nullable
    public MetaModelContext computeIfAbsent(final MetaModelContext mmc) {
        return mmc!=null
                ? mmc
                : getMetaModelContext();
    }

    @Nullable
    public MetaModelContext computeIfAbsent(
            final @Nullable MetaModelContext mmc, final @Nullable String applicationKey) {
        return mmc!=null
                ? mmc
                : _Strings.isEmpty(applicationKey)
                        ? getMetaModelContext()
                        : getMetaModelContext(applicationKey);
    }

    public void pageReload() {
        var cycle = RequestCycle.get();
        var handler = cycle.getActiveRequestHandler();
        if(handler instanceof ListenerRequestHandler) {
            var currentPage = ((ListenerRequestHandler)handler).getPage();
            cycle.setResponsePage(currentPage);
        }
    }

}
