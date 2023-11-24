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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationBehavior;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigationIncrementLink;
import org.apache.wicket.markup.html.navigation.paging.IPageable;

import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.ajax.BootstrapAjaxPagingNavigationBehavior;

public class IsisAjaxPagingNavigationIncrementLink extends AjaxPagingNavigationIncrementLink {

    private static final long serialVersionUID = 1L;

    private final IsisAjaxDataTable dataTable;
    private final Component component;

    public IsisAjaxPagingNavigationIncrementLink(final String id, final IPageable pageable, final int increment) {
        super(id, pageable, increment);
        dataTable = (IsisAjaxDataTable) pageable;
        component = pageable instanceof Component ? (Component) pageable : null;
    }

    @Override
    public void onClick(final AjaxRequestTarget target) {
        super.onClick(target);
        dataTable.setPageNumberHintAndBroadcast(target);
    }

    @Override
    protected AjaxPagingNavigationBehavior newAjaxPagingNavigationBehavior(final IPageable pageable, final String event) {
        return new BootstrapAjaxPagingNavigationBehavior(this, pageable, event);
    }

    public UiHintContainer getUiHintContainer() {
        return UiHintContainer.Util.hintContainerOf(component);
    }
}
