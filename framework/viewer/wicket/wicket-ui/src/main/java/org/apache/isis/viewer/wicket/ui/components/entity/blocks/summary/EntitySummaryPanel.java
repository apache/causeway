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

package org.apache.isis.viewer.wicket.ui.components.entity.blocks.summary;

import java.util.List;

import com.google.inject.Inject;

import org.apache.wicket.markup.html.PackageResource;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facets.object.icon.IconFacet;
import org.apache.isis.core.metamodel.spec.ActionType;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.ObjectActionContainer.Contributed;
import org.apache.isis.viewer.wicket.model.mementos.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.ui.app.imagecache.ImageCache;
import org.apache.isis.viewer.wicket.ui.components.actions.ActionInvokeHandler;
import org.apache.isis.viewer.wicket.ui.components.entity.blocks.action.EntityActionLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuBuilder;
import org.apache.isis.viewer.wicket.ui.components.widgets.cssmenu.CssMenuPanel;
import org.apache.isis.viewer.wicket.ui.pages.action.ActionPage;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

/**
 * {@link PanelAbstract Panel} representing the summary details (title, icon and
 * actions) of an entity, as per the provided {@link EntityModel}.
 */
public class EntitySummaryPanel extends PanelAbstract<EntityModel> implements ActionInvokeHandler {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_TITLE = "entityTitle";
    private static final String ID_ENTITY_IMAGE = "entityImage";
    private static final String ID_ENTITY_ACTIONS = "entityActions";

    private final EntityActionLinkFactory linkFactory;

    private ImageCache imageCache;

    public EntitySummaryPanel(final String id, final EntityModel entityModel) {
        super(id, entityModel);
        linkFactory = new EntityActionLinkFactory(getEntityModel(), this);
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
        addOrReplaceTitleAndImage();
        buildEntityActionsGui();
    }

    private void addOrReplaceTitleAndImage() {
        final String titleString = determineTitle();
        addOrReplace(new Label(ID_ENTITY_TITLE, titleString));
        addOrReplaceImage();
    }

    private String determineTitle() {
        final ObjectAdapter adapter = getModel().getObject();
        final String titleString = adapter != null ? adapter.titleString() : "(no object)"; // TODO:
                                                                                            // i18n
        return titleString;
    }

    private void addOrReplaceImage() {
        final ObjectAdapter adapter = getModel().getObject();
        ObjectSpecification typeOfSpec;
        PackageResource imageResource = null;
        if (adapter != null) {
            typeOfSpec = adapter.getSpecification();
            final IconFacet iconFacet = typeOfSpec.getFacet(IconFacet.class);
            if (iconFacet != null) {
                final String iconName = iconFacet.iconName(adapter);
                imageResource = getImageCache().findImage(iconName);
            }
        }
        if (imageResource == null) {
            typeOfSpec = getModel().getTypeOfSpecification();
            imageResource = getImageCache().findImage(typeOfSpec);
        }

        if (imageResource != null) {
            addOrReplace(new Image(ID_ENTITY_IMAGE, imageResource));
        } else {
            permanentlyHide(ID_ENTITY_IMAGE);
        }
    }

    private void buildEntityActionsGui() {
        final EntityModel model = getModel();
        final ObjectAdapter adapter = model.getObject();
        final ObjectAdapterMemento adapterMemento = model.getObjectAdapterMemento();
        if (adapter != null) {
            final List<ObjectAction> userActions = adapter.getSpecification().getObjectActions(ActionType.USER, Contributed.INCLUDED);

            final CssMenuBuilder cssMenuBuilder = new CssMenuBuilder(adapterMemento, getServiceAdapters(), userActions, linkFactory);
            // TODO: i18n
            final CssMenuPanel cssMenuPanel = cssMenuBuilder.buildPanel(ID_ENTITY_ACTIONS, "Actions");

            this.addOrReplace(cssMenuPanel);
        } else {
            permanentlyHide(ID_ENTITY_ACTIONS);
        }
    }

    @Override
    public void onClick(final ActionModel actionModel) {
        setResponsePage(new ActionPage(actionModel));
    }

    // ///////////////////////////////////////////////
    // Dependency Injection
    // ///////////////////////////////////////////////

    protected ImageCache getImageCache() {
        return imageCache;
    }

    @Inject
    public void setImageCache(final ImageCache imageCache) {
        this.imageCache = imageCache;
    }

}
