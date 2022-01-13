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

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import org.apache.isis.commons.internal.assertions._Assert;
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

    public EntityIconAndTitlePanel(
            final String id,
            final ObjectAdapterModel objectAdapterModel) {
        super(id, objectAdapterModel);

        val model = getModel();
        val obj = model.getObject();

        val isNonEmptyAbstract = isNonEmptyAbstract(obj);
        // GUARD against non-empty abstract
        _Assert.assertFalse(isNonEmptyAbstract,
                ()->"model for EntityIconAndTitlePanel, when non-empty, must not represent abstract types");
    }

    protected ManagedObject getTargetAdapter() {
        val targetAdapter = EntityUtil.refetch(getModel().getObject());
        return targetAdapter;
    }

    static boolean isNonEmptyAbstract(final ManagedObject obj) {
        if(obj==null
                || obj.getPojo()==null) {
            return false;
        }
        return obj.getSpecification().isAbstract();
    }

    @Override
    protected void onBeforeRender() {
        buildGui();
        super.onBeforeRender();
    }

    // -- HELPER

    private void buildGui() {
        addLinkWrapper();

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

    protected MarkupContainer addLinkWrapper() {
        val linkWrapper = Wkt.container(ID_ENTITY_LINK_WRAPPER);
        linkWrapper.addOrReplace(createLinkWithIconAndTitle());
        addOrReplace(linkWrapper);
        return linkWrapper;
    }

    private AbstractLink createLinkWithIconAndTitle() {

        final ManagedObject targetAdapter = getTargetAdapter();

        final AbstractLink link = createDynamicallyVisibleLink(targetAdapter);

        if(targetAdapter != null) {

            val spec = targetAdapter.getSpecification();

            final String iconName = spec.getIconName(targetAdapter);
            final CssClassFaFactory cssClassFaFactory = spec.getCssClassFaFactory().orElse(null);
            if (iconName != null || cssClassFaFactory == null) {
                Wkt.imageAddCachable(link, ID_ENTITY_ICON,
                                getImageResourceCache().resourceReferenceFor(targetAdapter));
                Components.permanentlyHide(link, ID_ENTITY_FONT_AWESOME);
            } else {
                Label dummy = Wkt.labelAdd(link, ID_ENTITY_FONT_AWESOME, "");
                Wkt.cssAppend(dummy, cssClassFaFactory.asSpaceSeparatedWithAdditional("fa-2x"));
                Components.permanentlyHide(link, ID_ENTITY_ICON);
            }

            final String title = determineTitle();
            Wkt.labelAdd(link, ID_ENTITY_TITLE, titleAbbreviated(title));

            String entityTypeName = determineFriendlyType() // from actual underlying model
                    .orElseGet(targetAdapter.getSpecification()::getSingularName); // not sure if this code path is ever reached
            Tooltips.addTooltip(link, entityTypeName, title);
        }

        return link;
    }

    private AbstractLink createDynamicallyVisibleLink(final ManagedObject targetAdapter) {

        final ObjectAdapterModel entityModel = getModel();
        final PageParameters pageParameters = pageParametersFor(targetAdapter);
        final Class<? extends Page> pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);

        final BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>(
                ID_ENTITY_LINK, pageClass, pageParameters) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                // not visible if null
                // (except its null because its a detached entity,
                // which we can re-fetch due to memoized bookmark)
                val targetAdapter = entityModel.getObject();
                return targetAdapter != null
                        && (targetAdapter.getPojo()!=null
                                || targetAdapter.isBookmarkMemoized());
            }
        };

        return link;
    }

    private PageParameters pageParametersFor(final ManagedObject targetAdapter) {
        return
                ManagedObjects.isNullOrUnspecifiedOrEmpty(targetAdapter)
                ? getModel().getPageParametersWithoutUiHints()
                : EntityModel.ofAdapter(
                    super.getCommonContext(),
                    targetAdapter.getSpecification().lookupFacet(ProjectionFacet.class)
                    .map(projectionFacet->projectionFacet.projected(targetAdapter))
                    .orElse(targetAdapter))
                    .getPageParametersWithoutUiHints();
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

    private boolean isContextAdapter(final ManagedObject other) {
        return getModel().isContextAdapter(other);
    }

    static String abbreviated(final String str, final int maxLength) {
        int length = str.length();
        if (length <= maxLength) {
            return str;
        }
        return maxLength <= 3 ? "" : str.substring(0, maxLength - 3) + "...";
    }

}
