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
package org.apache.causeway.viewer.wicket.ui.pages.home;

import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.causeway.applib.services.message.MessageService;
import org.apache.causeway.applib.services.user.UserMemento;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.ui.components.widgets.breadcrumbs.BreadcrumbModelProvider;
import org.apache.causeway.viewer.wicket.ui.pages.PageAbstract;
import org.apache.causeway.viewer.wicket.ui.pages.entity.EntityPage;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

import lombok.val;

/**
 * Web page representing the home page (showing a welcome message).
 */
@AuthorizeInstantiation(UserMemento.AUTHORIZED_USER_ROLE)
public class HomePage extends PageAbstract {

    private static final long serialVersionUID = 1L;

    public HomePage(final PageParameters parameters) {
        super(parameters, null);

        addChildComponents(themeDiv, null);
        buildGui();

        addBookmarkedPages(themeDiv);
    }

    private void buildGui() {

        if(super.getPageParameters() == null) {
            super.getMetaModelContext().lookupServiceElseFail(MessageService.class)
            .informUser("Page timeout");
        }

        val homePageAdapter = super.getMetaModelContext().getHomePageAdapter();

        if(ManagedObjects.isSpecified(homePageAdapter)) {
            val requestCycle = RequestCycle.get();
            val page = EntityPage.forAdapter(getMetaModelContext(), homePageAdapter);
            requestCycle.setResponsePage(page);
        } else {
            WktComponents.permanentlyHide(themeDiv, UiComponentType.ACTION_PROMPT);
            getComponentFactoryRegistry().addOrReplaceComponent(themeDiv, UiComponentType.WELCOME, null);
        }

        val breadcrumbModelProvider = (BreadcrumbModelProvider) getSession();
        val breadcrumbModel = breadcrumbModelProvider.getBreadcrumbModel();
        breadcrumbModel.visitedHomePage();
    }



}
