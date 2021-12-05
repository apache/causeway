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
package org.apache.isis.viewer.wicket.ui.actionresponse;

import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.lang.Nullable;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageRedirectRequest<T extends IRequestablePage> {

    private final @NonNull Class<T> pageClass;
    private final @Nullable PageParameters pageParameters;
    private final @Nullable IRequestablePage pageInstance;

    public static <T extends IRequestablePage> PageRedirectRequest<T> forPageClass(
            final @NonNull Class<T> pageClass,
            final @NonNull PageParameters pageParameters) {
        return new PageRedirectRequest<>(pageClass, pageParameters, null);
    }

    public static <T extends IRequestablePage> PageRedirectRequest<T> forPageClass(
            final @NonNull Class<T> pageClass) {
        return new PageRedirectRequest<>(pageClass, null, null);
    }

    public static <T extends IRequestablePage> PageRedirectRequest<T> forPage(
            final @NonNull Class<T> pageClass,
            final @NonNull T pageInstance) {
        return new PageRedirectRequest<>(pageClass, null, pageInstance);
    }

    public void applyTo(
            final @Nullable RequestCycle requestCycle) {
        if(requestCycle==null) {
            return;
        }
        if(pageParameters!=null) {
            requestCycle.setResponsePage(pageClass, pageParameters);
            return;
        }
        if(pageInstance!=null) {
            requestCycle.setResponsePage(pageInstance);
            return;
        }
        requestCycle.setResponsePage(pageClass);
    }

}
