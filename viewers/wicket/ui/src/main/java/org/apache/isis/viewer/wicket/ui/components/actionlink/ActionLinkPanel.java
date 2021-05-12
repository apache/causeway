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

package org.apache.isis.viewer.wicket.ui.components.actionlink;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Links;

/**
 * {@link PanelAbstract Panel} representing the icon and title of an entity,
 * as per the provided {@link EntityModel}.
 */
public class ActionLinkPanel
extends PanelAbstract<ManagedObject, ActionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ACTION_LINK_WRAPPER = "actionLinkWrapper";
    private static final String ID_ACTION_LINK = "actionLink";
    private static final String ID_ACTION_TITLE = "actionTitle";

    private Label label;

    public ActionLinkPanel(final String id, final ActionModel actionModel) {
        super(id, actionModel);
    }

    @Override
    protected void onBeforeRender() {
        buildGui();
        super.onBeforeRender();
    }

    private void buildGui() {
        addOrReplaceLinkWrapper();
    }

    private void addOrReplaceLinkWrapper() {
        final WebMarkupContainer entityLinkWrapper = addOrReplaceLinkWrapper(getModel());
        addOrReplace(entityLinkWrapper);
    }

    private WebMarkupContainer addOrReplaceLinkWrapper(final ActionModel actionModel) {

        final PageParameters pageParameters = actionModel.getPageParameters();
        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ACTION_PROMPT);
        final AbstractLink link = newLink(ID_ACTION_LINK, pageClass, pageParameters);

        label = new Label(ID_ACTION_TITLE, determineTitle());
        link.add(label);

        final WebMarkupContainer actionLinkWrapper = new WebMarkupContainer(ID_ACTION_LINK_WRAPPER);
        actionLinkWrapper.addOrReplace(link);
        return actionLinkWrapper;
    }

    private String determineTitle() {
        return getModel().getMetaModel().getId();
    }

    private AbstractLink newLink(final String linkId, final Class<? extends Page> pageClass, final PageParameters pageParameters) {
        return Links.newBookmarkablePageLink(linkId, pageParameters, pageClass);
    }

}
