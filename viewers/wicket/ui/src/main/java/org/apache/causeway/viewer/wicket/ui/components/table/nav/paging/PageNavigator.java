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
package org.apache.causeway.viewer.wicket.ui.components.table.nav.paging;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigator;

public class PageNavigator extends BootstrapAjaxPagingNavigator {

    private static final long serialVersionUID = 1L;

    public PageNavigator(final String id, final IPageable pageable) {
        this(id, pageable, null);
    }

    public PageNavigator(final String id, final IPageable pageable, final IPagingLabelProvider labelProvider) {
        super(id, pageable, labelProvider);
        setSize(Size.Small);
    }

    @Override
    protected AbstractLink newPagingNavigationLink(final String id, final IPageable pageable, final int pageNumber) {
        return new NavigationLink(id, pageable, pageNumber);
    }

    @Override
    protected AbstractLink newPagingNavigationIncrementLink(final String id, final IPageable pageable, final int increment) {
        return new NavigationIncrementLink(id, pageable, increment);
    }

    @Override
    protected PagingNavigation newNavigation(final String id, final IPageable pageable, final IPagingLabelProvider labelProvider) {
        return new Navigation(id, pageable, labelProvider);
    }

    @Override
    protected void onComponentTag(final ComponentTag tag) {
        tag.setName("ul");
        super.onComponentTag(tag);
    }
}
