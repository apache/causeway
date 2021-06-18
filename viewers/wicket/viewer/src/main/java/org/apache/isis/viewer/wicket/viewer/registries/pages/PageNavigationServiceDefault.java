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

package org.apache.isis.viewer.wicket.viewer.registries.pages;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageNavigationService;

/**
 * Default implementation of {@link org.apache.isis.viewer.wicket.ui.pages.PageNavigationService}
 */
@Service
@Named("isis.viewer.wicket.PageNavigationServiceDefault")
@javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class PageNavigationServiceDefault implements PageNavigationService {

    private static final long serialVersionUID = 1L;

    @Inject private PageClassRegistry pageClassRegistry; // serializable

    @Override
    public void navigateTo(PageType pageType) {
        navigateTo(pageType, new PageParameters());
    }

    @Override
    public void navigateTo(PageType pageType, PageParameters parameters) {
        Class<? extends Page> pageClass = pageClassRegistry.getPageClass(pageType);
        RequestCycle.get().setResponsePage(pageClass, parameters);
    }

    @Override
    public void restartAt(PageType pageType) {
        Class<? extends Page> pageClass = pageClassRegistry.getPageClass(pageType);
        throw new RestartResponseException(pageClass);
    }

    @Override
    public void interceptAndRestartAt(PageType pageType) {
        Class<? extends Page> pageClass = pageClassRegistry.getPageClass(pageType);
        throw new RestartResponseAtInterceptPageException(pageClass);
    }
}
