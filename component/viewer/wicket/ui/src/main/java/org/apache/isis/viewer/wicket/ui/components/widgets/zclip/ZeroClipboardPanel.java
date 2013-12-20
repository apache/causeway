/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.viewer.wicket.ui.components.widgets.zclip;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.hints.UiHintContainer;
import org.apache.isis.viewer.wicket.model.hints.UiHintsBroadcastEvent;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Links;

public class ZeroClipboardPanel extends PanelAbstract<IModel<Void>> {

    private static final long serialVersionUID = 1L;
    
    private static final String ID_SUBSCRIBING_LINK = "subscribingLink";
    private static final String ID_COPY_LINK = "copyLink";

    private AbstractLink subscribingLink;
    private AjaxLink<ObjectAdapter> copyLink;

    public ZeroClipboardPanel(String id) {
        super(id);
    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();
        if(copyLink == null) {
            copyLink = new ZeroClipboardLink(ID_COPY_LINK, "#subscribingLink");
            addOrReplace(copyLink);
        }
        addSubscribingLink(null);
    }

    private void addSubscribingLink(UiHintContainer uiHintContainer) {
        subscribingLink = createSubscribingLink(uiHintContainer);
        addOrReplace(subscribingLink);
        subscribingLink.setOutputMarkupId(true);
    }

    private AbstractLink createSubscribingLink(UiHintContainer uiHintContainer) {
        if(uiHintContainer == null || !(uiHintContainer instanceof EntityModel)) {
            // return a no-op
            AbstractLink link = new AbstractLink(ID_SUBSCRIBING_LINK) {
                private static final long serialVersionUID = 1L;
            };
            return link;
        } else {
            final EntityModel entityModel = (EntityModel) uiHintContainer;
            final PageParameters pageParameters = entityModel.getPageParameters();
            final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);
            return Links.newBookmarkablePageLink(ID_SUBSCRIBING_LINK, pageParameters, pageClass);
        }
    }

    // //////////////////////////////////////
    
    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        if(event.getPayload() instanceof UiHintsBroadcastEvent) {
            UiHintsBroadcastEvent ev = (UiHintsBroadcastEvent) event.getPayload();
            addSubscribingLink(ev.getUiHintContainer());
            ev.getTarget().add(subscribingLink);
        }
    }
    
    // //////////////////////////////////////

    protected PageClassRegistry getPageClassRegistry() {
        final PageClassRegistryAccessor pcra = (PageClassRegistryAccessor) getApplication();
        return pcra.getPageClassRegistry();
    }

    
}
