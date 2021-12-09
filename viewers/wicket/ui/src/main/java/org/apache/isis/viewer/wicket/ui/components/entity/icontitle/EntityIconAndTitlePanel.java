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

import java.util.Optional;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.metamodel.facets.members.cssclassfa.CssClassFaFactory;
import org.apache.isis.core.metamodel.facets.object.projection.ProjectionFacet;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.core.metamodel.spec.ManagedObjects.EntityUtil;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.isis.viewer.wicket.model.models.PageType;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.Tooltips;
import org.apache.isis.viewer.wicket.ui.util.Wkt;

import lombok.val;

/**
 * {@link PanelAbstract Panel} representing the icon and title of an entity,
 * as per the provided {@link EntityModel}.
 */
public class EntityIconAndTitlePanel
extends PanelAbstract<ManagedObject, ObjectAdapterModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_ENTITY_LINK_WRAPPER = "entityLinkWrapper";
    private static final String ID_ENTITY_FONT_AWESOME = "entityFontAwesome";
    private static final String ID_ENTITY_LINK = "entityLink";
    private static final String ID_ENTITY_TITLE = "entityTitle";
    private static final String ID_ENTITY_ICON = "entityImage";

    @SuppressWarnings("unused")
    private Label label;
    @SuppressWarnings("unused")
    private Image image;

    public EntityIconAndTitlePanel(
            final String id,
            final ObjectAdapterModel objectAdapterModel) {
        super(id, objectAdapterModel);
    }

    public ObjectAdapterModel getEntityModel() {
        return getModel();
    }

    @Override
    protected void onBeforeRender() {
        buildGui();
        super.onBeforeRender();
    }

    private void buildGui() {
        addOrReplaceLinkWrapper();

        if(isTitleSuppressed()) {
            // bit of a hack... allows us to suppress the title using CSS
            Wkt.cssAppend(this, "inlinePrompt");
        }

        setOutputMarkupId(true);
    }

    private boolean isTitleSuppressed() {
        return getModel().isInlinePrompt()
                //XXX ISIS-1699 never hide titles of object references in tables
                && !getModel().getRenderingHint().isInTable();
    }

    private void addOrReplaceLinkWrapper() {
        ObjectAdapterModel entityModel = getModel();
        final WebMarkupContainer entityLinkWrapper = addOrReplaceLinkWrapper(entityModel);
        addOrReplace(entityLinkWrapper);
    }

    protected WebMarkupContainer addOrReplaceLinkWrapper(final ObjectAdapterModel entityModel) {
        val adapter = entityModel.getObject();

        final WebMarkupContainer entityLinkWrapper = new WebMarkupContainer(ID_ENTITY_LINK_WRAPPER);

        entityLinkWrapper.addOrReplace(createLinkWithIconAndTitle(adapter));

        return entityLinkWrapper;
    }

    private AbstractLink createLinkWithIconAndTitle(final ManagedObject adapterIfAny) {
        final AbstractLink link = createDynamicallyVisibleLink();

        if(adapterIfAny != null) {

            val spec = adapterIfAny.getSpecification();

            final String iconName = spec.getIconName(adapterIfAny);
            final CssClassFaFactory cssClassFaFactory = spec.getCssClassFaFactory().orElse(null);
            if (iconName != null || cssClassFaFactory == null) {
                link.addOrReplace(this.image = newImage(ID_ENTITY_ICON, adapterIfAny));
                Components.permanentlyHide(link, ID_ENTITY_FONT_AWESOME);
            } else {
                Label dummy = Wkt.labelAdd(link, ID_ENTITY_FONT_AWESOME, "");
                Wkt.cssAppend(dummy, cssClassFaFactory.asSpaceSeparatedWithAdditional("fa-2x"));
                Components.permanentlyHide(link, ID_ENTITY_ICON);
            }

            final String title = determineTitle();
            this.label = Wkt.labelAdd(link, ID_ENTITY_TITLE, titleAbbreviated(title));

            String entityTypeName = determineFriendlyType() // from actual underlying model
                    .orElseGet(adapterIfAny.getSpecification()::getSingularName); // not sure if this code path is ever reached
            Tooltips.addTooltip(link, entityTypeName, title);
        }

        return link;
    }

    private AbstractLink createDynamicallyVisibleLink() {

        final ObjectAdapterModel entityModel = getModel();

        val targetAdapter = entityModel.getObject();
        final ObjectAdapterModel redirectToModel;

        if(targetAdapter != null) {

            EntityUtil.refetch(targetAdapter);

            val redirectToAdapter = entityModel.getTypeOfSpecification().lookupFacet(ProjectionFacet.class)
                    .map(projectionFacet->projectionFacet.projected(targetAdapter))
                    .orElse(targetAdapter);

            redirectToModel = EntityModel.ofAdapter(super.getCommonContext(), redirectToAdapter);

        } else {
            redirectToModel = entityModel;
        }

        final PageParameters pageParameters = redirectToModel.getPageParametersWithoutUiHints();

        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);

        final BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
                ID_ENTITY_LINK, pageClass, pageParameters) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                val targetAdapter = entityModel.getObject();
                return targetAdapter != null;
            }
        };

        return link;
    }

    private String titleAbbreviated(final String titleString) {
        int maxTitleLength = abbreviateTo(getModel(), titleString);
        return abbreviated(titleString, maxTitleLength);
    }

    private Optional<String> determineFriendlyType() {
        val domainObject = getModel().getObject();
        return ManagedObjects.isSpecified(domainObject)
                ? _Strings.nonEmpty(domainObject.getSpecification().getSingularName())
                : Optional.empty();
    }

    private String determineTitle() {
        val managedObject = getModel().getObject();
        return managedObject != null
                ? managedObject.titleString(conf->conf.skipTitlePartEvaluator(this::isContextAdapter))
                : "(no object)";
    }

    private int abbreviateTo(final ObjectAdapterModel model, final String titleString) {
        if(model.getRenderingHint().isInStandaloneTableTitleColumn()) {
            return getWicketViewerSettings().getMaxTitleLengthInStandaloneTables();
        }
        if(model.getRenderingHint().isInParentedTableTitleColumn()) {
            return getWicketViewerSettings().getMaxTitleLengthInParentedTables();
        }
        return titleString.length();
    }

    protected Image newImage(final String id, final ManagedObject adapter) {
        final ResourceReference imageResource = getImageResourceCache().resourceReferenceFor(adapter);

        final Image image = new Image(id, imageResource) {
            private static final long serialVersionUID = 1L;
            @Override
            protected boolean shouldAddAntiCacheParameter() {
                return false;
            }
        };
        return image;
    }

    private boolean isContextAdapter(final ManagedObject other) {
        final ObjectAdapterModel model = getModel();
        return model.isContextAdapter(other);
    }

    static String abbreviated(final String str, final int maxLength) {
        int length = str.length();
        if (length <= maxLength) {
            return str;
        }
        return maxLength <= 3 ? "" : str.substring(0, maxLength - 3) + "...";
    }




}
