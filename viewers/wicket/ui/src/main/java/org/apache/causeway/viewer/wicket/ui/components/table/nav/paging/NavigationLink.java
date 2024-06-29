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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationBehavior;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.navigation.paging.IPageable;

import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigationBehavior;
import de.agilecoders.wicket.core.util.Attributes;

class NavigationLink extends AjaxPagingNavigationLink {

    private static final long serialVersionUID = 1L;

    public NavigationLink(final String id, final IPageable pageable, final long pageNumber) {
        super(id, pageable, pageNumber);
    }

    @Override
    public void onClick(final AjaxRequestTarget target) {
        super.onClick(target);
        ((DataTableWithPagesAndFilter<?, ?>)super.pageable).setPageNumberHintAndBroadcast(target);
    }

    @Override
    protected AjaxPagingNavigationBehavior newAjaxPagingNavigationBehavior(final IPageable pageable, final String event) {
        return new BootstrapAjaxPagingNavigationBehavior(this, pageable, event);
    }

	@Override
	protected void onComponentTag(final ComponentTag tag) {
		super.onComponentTag(tag);
		Attributes.addClass(tag, "page-link");
	}

}
