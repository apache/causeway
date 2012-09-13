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

package org.apache.isis.viewer.wicket.ui.components.entity.blocks.icontitle;

import com.google.inject.Inject;

import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ResourceReference;
import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.PackageResourceReference;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.resources.PackagedResourceReference;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ImageResourceCache;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.action.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistry;
import org.apache.isis.viewer.wicket.ui.pages.PageClassRegistryAccessor;
import org.apache.isis.viewer.wicket.ui.pages.PageType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;
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


    private ImageResourceCache imageCache;

    private Label label;
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
    }

    private void addOrReplaceLinkWrapper() {
        final WebMarkupContainer entityLinkWrapper = addOrReplaceLinkWrapper(getModel());
        addOrReplace(entityLinkWrapper);
    }

    private WebMarkupContainer addOrReplaceLinkWrapper(final EntityModel entityModel) {
        final ObjectAdapter adapter = entityModel.getObject();

        final PageParameters pageParameters = EntityModel.createPageParameters(adapter);
        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);
        final AbstractLink link = newLink(ID_ENTITY_LINK, pageClass, pageParameters);
        
        label = new Label(ID_ENTITY_TITLE, determineTitle());
        link.add(label);
        
        final Class<?> correspondingClass = entityModel.getObject().getSpecification().getCorrespondingClass();
        final String specName = correspondingClass.getSimpleName();

        final ResourceReference imageResource = new ResourceReference(correspondingClass, specName + ".png");
        image = new Image(ID_ENTITY_ICON, imageResource);
        link.addOrReplace(image);
        
        final WebMarkupContainer entityLinkWrapper = new WebMarkupContainer(ID_ENTITY_LINK_WRAPPER);
        entityLinkWrapper.addOrReplace(link);
        return entityLinkWrapper;
    }

    private String determineTitle() {
        final ObjectAdapter adapter = getModel().getObject();
         // TODO: i18n
        return adapter != null ? adapter.titleString() : "(no object)";
    }

    private AbstractLink newLink(final String linkId, final Class<? extends Page> pageClass, final PageParameters pageParameters) {
        return Links.newBookmarkablePageLink(linkId, pageParameters, pageClass);
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

    protected ImageResourceCache getImageCache() {
        return imageCache;
    }

    @Inject
    public void setImageCache(final ImageResourceCache imageCache) {
        this.imageCache = imageCache;
    }

}
