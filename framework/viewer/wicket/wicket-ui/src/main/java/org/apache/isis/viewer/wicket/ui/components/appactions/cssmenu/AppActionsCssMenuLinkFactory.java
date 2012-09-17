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

package org.apache.isis.viewer.wicket.ui.components.appactions.cssmenu;

import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.util.Actions;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuLinkFactory;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageType;
import org.apache.isis.viewer.wicket.ui.util.Links;

class AppActionsCssMenuLinkFactory implements CssMenuLinkFactory {

    private static final long serialVersionUID = 1L;

    @Override
    public LinkAndLabel newLink(final ObjectAdapterMemento adapterMemento, final ObjectAction action, final String linkId) {
        final PageParameters pageParameters = ActionModel.createPageParameters(adapterMemento.getObjectAdapter(ConcurrencyChecking.NO_CHECK), action, null, ActionModel.SingleResultsMode.REDIRECT);

        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ACTION);

        final AbstractLink link = Links.newBookmarkablePageLink(linkId, pageParameters, pageClass);
        final String actionLabel = Actions.labelFor(action);

        return new LinkAndLabel(link, actionLabel);
    }

    

    // ////////////////////////////////////////////////////////////
    // Dependencies
    // ////////////////////////////////////////////////////////////

    protected PageClassRegistry getPageClassRegistry() {
        return ((PageClassRegistryAccessor) Application.get()).getPageClassRegistry();
    }

}