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

package org.apache.isis.viewer.wicket.ui.components.entity.icontitle;

import java.net.ResponseCache;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.head.JavaScriptContentHeaderItem;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.viewer.wicket.model.hints.UiHintsBroadcastEvent;
import org.apache.isis.viewer.wicket.model.hints.UiHintsSetEvent;
import org.apache.isis.viewer.wicket.model.isis.WicketViewerSettings;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.mementos.PageParameterNames;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.components.entity.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.zclip.ZeroClipboardLink;
import org.apache.isis.viewer.wicket.ui.errors.JGrowlUtil;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.Links;

/**
 * {@link PanelAbstract Panel} representing the icon and title of an entity,
 * as per the provided {@link EntityModel}.
 */
public class EntityIconAndTitlePanel extends PanelAbstract<EntityModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_LINK_WRAPPER = "entityLinkWrapper";
    private static final String ID_ENTITY_LINK = "entityLink";
    private static final String ID_ENTITY_TITLE = "entityTitle";
    private static final String ID_ENTITY_ICON = "entityImage";

    @SuppressWarnings("unused")
    private Label label;
    @SuppressWarnings("unused")
    private Image image;

    public EntityIconAndTitlePanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
    }

    /**
     * For the {@link EntityActionLinkFactory}.
     */
    public EntityModel getEntityModel() {
        return getModel();
    }

    @Override
    protected void onBeforeRender() {
        buildGui();
        super.onBeforeRender();
    }

    private void buildGui() {
        addOrReplaceLinkWrapper();
        setOutputMarkupId(true);
    }

    private void addOrReplaceLinkWrapper() {
        final WebMarkupContainer entityLinkWrapper = addOrReplaceLinkWrapper(getModel());
        addOrReplace(entityLinkWrapper);
    }

    private WebMarkupContainer addOrReplaceLinkWrapper(final EntityModel entityModel) {
        final ObjectAdapter adapter = entityModel.getObject();

        final PageParameters pageParameters = entityModel.getPageParameters();
        
        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);
        
        final WebMarkupContainer entityLinkWrapper = new WebMarkupContainer(ID_ENTITY_LINK_WRAPPER);
        final AbstractLink link = createIconAndTitle(adapter, pageParameters, pageClass);
        entityLinkWrapper.addOrReplace(link);
        
        return entityLinkWrapper;
    }

    private AbstractLink createIconAndTitle(final ObjectAdapter adapter, final PageParameters pageParameters, final Class<? extends Page> pageClass) {
        final AbstractLink link = Links.newBookmarkablePageLinkWithHints(ID_ENTITY_LINK, pageParameters, pageClass);
        
        link.addOrReplace(this.label = newLabel(ID_ENTITY_TITLE));
        link.addOrReplace(this.image = newImage(ID_ENTITY_ICON, adapter));
        
        link.add(new AttributeModifier("title", adapter.getSpecification().getSingularName()));
        return link;
    }

    protected Label newLabel(final String id) {
        final String title = determineTitle();
        return new Label(id, title);
    }

    protected String determineTitle() {
        EntityModel model = getModel();
        final ObjectAdapter adapter = model.getObject();
         if (adapter != null) {
            String titleString = adapter.titleString(getContextAdapterIfAny());
            int maxTitleLength = abbreviateTo(model, titleString);
            return abbreviated(titleString, maxTitleLength);
        } else {
            return "(no object)";
        }
    }

    private int abbreviateTo(EntityModel model, String titleString) {
        if(model.getRenderingHint().isInStandaloneTableTitleColumn()) {
            return getSettings().getMaxTitleLengthInStandaloneTables();
        } 
        if(model.getRenderingHint().isInParentedTableTitleColumn()) {
            return getSettings().getMaxTitleLengthInParentedTables();
        }
        return titleString.length();
    }

    protected Image newImage(final String id, final ObjectAdapter adapter) {
        final ResourceReference imageResource = imageCache.resourceReferenceFor(adapter);
         
        final Image image = new Image(id, imageResource) {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean shouldAddAntiCacheParameter() {
                return false;
            }
        };
        return image;
    }

    public ObjectAdapter getContextAdapterIfAny() {
        EntityModel model = getModel();
        ObjectAdapterMemento contextAdapterMementoIfAny = model.getContextAdapterIfAny();
        return contextAdapterMementoIfAny != null? contextAdapterMementoIfAny.getObjectAdapter(ConcurrencyChecking.NO_CHECK): null;
    }
    
    static String abbreviated(final String str, final int maxLength) {
        int length = str.length();
        if (length <= maxLength) {
            return str;
        }
        return maxLength <= 3 ? "" : str.substring(0, maxLength - 3) + "...";
    }

    
    // ///////////////////////////////////////////////////////////////////
    // UI Hints
    // ///////////////////////////////////////////////////////////////////
    
    /* (non-Javadoc)
     * @see org.apache.wicket.Component#onEvent(org.apache.wicket.event.IEvent)
     */
    @Override
    public void onEvent(IEvent<?> event) {
        if(event.getPayload() instanceof UiHintsBroadcastEvent) {
            UiHintsBroadcastEvent ev = (UiHintsBroadcastEvent) event.getPayload();
            buildGui();
            ev.getTarget().add(this);
        }
    }
    
    // ///////////////////////////////////////////////////////////////////
    // Convenience
    // ///////////////////////////////////////////////////////////////////

    protected PageClassRegistry getPageClassRegistry() {
        final PageClassRegistryAccessor pcra = (PageClassRegistryAccessor) getApplication();
        return pcra.getPageClassRegistry();
    }


    // ///////////////////////////////////////////////
    // Dependency Injection
    // ///////////////////////////////////////////////

    @Inject
    private ImageResourceCache imageCache;
    protected ImageResourceCache getImageCache() {
        return imageCache;
    }
    
    @Inject
    private WicketViewerSettings settings;
    protected WicketViewerSettings getSettings() {
        return settings;
    }

}
