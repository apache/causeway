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
package org.apache.causeway.viewer.wicket.viewer.registries.pages;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Page;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.commons.internal.collections._Maps;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassList;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.causeway.viewer.wicket.ui.pages.PageClassRegistrySpi;
import org.apache.causeway.viewer.wicket.viewer.CausewayModuleViewerWicketViewer;

/**
 * Default implementation of {@link PageClassRegistry}; just delegates to an
 * underlying {@link PageClassList}.
 */
public class PageClassRegistryDefault implements PageClassRegistry, PageClassRegistrySpi {

    public static final String LOGICAL_TYPE_NAME =
            CausewayModuleViewerWicketViewer.NAMESPACE + ".PageClassRegistryDefault";

    @Configuration
    public static class AutoConfiguration {
        @Bean
        @Named(LOGICAL_TYPE_NAME)
        @Order(PriorityPrecedence.MIDPOINT)
        @Qualifier("Default")
        public PageClassRegistryDefault pageClassRegistryDefault(PageClassList pageClassList) {
            return new PageClassRegistryDefault(pageClassList);
        }
    }

    private static final long serialVersionUID = 1L;

    private final PageClassList pageClassList; // serializable
    private final Map<PageType, Class<? extends Page>> pageClassByType = _Maps.newHashMap();
    private final Map<Class<? extends Page>, PageType> typeByPageClass = _Maps.newHashMap();

    @Inject
    public PageClassRegistryDefault(PageClassList pageClassList) {
        this.pageClassList = pageClassList;
    }

    @PostConstruct
    public void init() {
        pageClassList.registerPages(this);
        ensureAllPageTypesRegistered();
    }

    private void ensureAllPageTypesRegistered() {
        for (final PageType pageType : PageType.values()) {
            if (getPageClass(pageType) == null) {
                throw new IllegalStateException("No page registered for " + pageType);
            }
        }
    }

    @Override
    public final Class<? extends Page> getPageClass(final PageType pageType) {
        return pageClassByType.get(pageType);
    }

    @Override
    public PageType getPageType(Class<? extends Page> pageClass) {
        return typeByPageClass.get(pageClass);
    }

    @Override
    public PageType getPageType(PageAbstract page) {
        return getPageType(page.getClass());
    }

    @Override
    public final void registerPage(final PageType pageType, final Class<? extends Page> pageClass) {
        pageClassByType.put(pageType, pageClass);
        typeByPageClass.put(pageClass, pageType);
    }
}
