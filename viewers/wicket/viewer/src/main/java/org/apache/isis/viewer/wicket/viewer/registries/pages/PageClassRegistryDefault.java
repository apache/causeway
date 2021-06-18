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

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.wicket.Page;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassList;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistrySpi;

/**
 * Default implementation of {@link PageClassRegistry}; just delegates to an
 * underlying {@link PageClassList}.
 */
@Service
@Named("isis.viewer.wicket.PageClassRegistryDefault")
@javax.annotation.Priority(OrderPrecedence.MIDPOINT)
@Primary
@Qualifier("Default")
public class PageClassRegistryDefault implements PageClassRegistry, PageClassRegistrySpi {

    private static final long serialVersionUID = 1L;

    private final PageClassList pageClassList; // serializable
    private final Map<PageType, Class<? extends Page>> pagesByType = _Maps.newHashMap();

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

    // /////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////

    @Override
    public final Class<? extends Page> getPageClass(final PageType pageType) {
        return pagesByType.get(pageType);
    }

    // /////////////////////////////////////////////////////////
    // API
    // /////////////////////////////////////////////////////////

    @Override
    public final void registerPage(final PageType pageType, final Class<? extends Page> pageClass) {
        pagesByType.put(pageType, pageClass);
    }
}
