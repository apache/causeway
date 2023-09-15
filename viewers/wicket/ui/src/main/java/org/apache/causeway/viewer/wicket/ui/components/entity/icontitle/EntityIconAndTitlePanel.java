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
package org.apache.causeway.viewer.wicket.ui.components.entity.icontitle;

import java.io.Serializable;
import java.util.Objects;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmTitleUtils;
import org.apache.causeway.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbbreviationOptions;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.Builder;
import lombok.val;
import lombok.experimental.Accessors;

/**
 * {@link PanelAbstract Panel} representing the icon and title of an entity,
 * as per the provided {@link UiObjectWkt}.
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
        guardAgainstNonEmptyAbstractSingular(objectAdapterModel);
    }

    protected ManagedObject linkedDomainObject() {
        val linkedDomainObject = getModel().getObject();
        return linkedDomainObject;
    }

    @Override
    protected void onBeforeRender() {
        buildGui();
        super.onBeforeRender();
    }

    /**
     * Callback for sub-classes to add additional components.
     */
    protected void onLinkWrapperCreated(final MarkupContainer linkWrapper) {}

    // -- HELPER

    private void buildGui() {
        addLinkWrapper();
        setOutputMarkupId(true);
    }

    private void addLinkWrapper() {
        val linkWrapper = Wkt.container(ID_ENTITY_LINK_WRAPPER);
        linkWrapper.addOrReplace(createLinkWithIconAndTitle());
        addOrReplace(linkWrapper);
        onLinkWrapperCreated(linkWrapper);
    }

    private AbstractLink createLinkWithIconAndTitle() {
        final ManagedObject linkedDomainObject = linkedDomainObject();
        final AbstractLink link = createDynamicallyVisibleLink(linkedDomainObject);

        if(isTitleSuppressed()) {
            hideTitle();
        }

        if (ManagedObjects.isNullOrUnspecifiedOrEmpty(linkedDomainObject)) {
            WktComponents.permanentlyHide(link, ID_ENTITY_ICON);
            Wkt.labelAdd(link, ID_ENTITY_TITLE, titleAbbreviated("(no object)"));
        } else {
            linkedDomainObject.eitherIconOrFaClass()
            .accept(
                    objectIcon->{
                        Wkt.imageAddCachable(link, ID_ENTITY_ICON,
                                getImageResourceCache().resourceReferenceForObjectIcon(objectIcon));
                        WktComponents.permanentlyHide(link, ID_ENTITY_FONT_AWESOME);
                    },
                    cssClassFaFactory->{
                        WktComponents.permanentlyHide(link, ID_ENTITY_ICON);
                        final Label dummyLabel = Wkt.labelAdd(link, ID_ENTITY_FONT_AWESOME, "");
                        val faSizeModifier = getModel().getRenderingHint().isInTable()
                                ? "fa-lg"
                                : "fa-2x";
                        Wkt.cssAppend(dummyLabel, cssClassFaFactory.asSpaceSeparatedWithAdditional(faSizeModifier));
                    });

            final TitleRecord title = determineTitle(linkedDomainObject);
            Wkt.labelAdd(link, ID_ENTITY_TITLE, title.abbreviatedTitle());

            if(title.isTooltipTitleSuppressed()) {
                WktTooltips.addTooltip(link, title.tooltipBody());
            } else {
                WktTooltips.addTooltip(link, title.tooltipTitle(), title.tooltipBody());
            }
        }

        return link;
    }

    private boolean isTitleSuppressed() {
        return getModel().isInlinePrompt()
                //XXX CAUSEWAY-1699 never hide titles of object references in tables
                && getModel().getRenderingHint().isNotInTable();
    }

    private AbstractLink createDynamicallyVisibleLink(final ManagedObject linkedDomainObject) {
        val pageParameters = PageParameterUtils
                .createPageParametersForBookmarkablePageLink(linkedDomainObject);
        val pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);

        return Wkt.bookmarkablePageLinkWithVisibility(ID_ENTITY_LINK, pageClass, pageParameters,
                ()->isLinkVisible(linkedDomainObject()));
    }

    private boolean isLinkVisible(final ManagedObject linkedDomainObject) {
        /* not visible if null, except its null because its a detached entity,
         * which we can re-fetch due to memoized bookmark) */
        return linkedDomainObject != null
                && (linkedDomainObject.getPojo()!=null
                        || linkedDomainObject.isBookmarkMemoized());
    }

    private String titleAbbreviated(final String titleString) {
        final int maxTitleLength = abbreviateTo(getModel(), titleString);
        return abbreviated(titleString, maxTitleLength);
    }

    /**
     * Holder of titles for various UI contexts (Java record candidate).
     */
    @Builder
    @lombok.Value @Accessors(fluent=true)
    private static class TitleRecord implements Serializable {
        private static final long serialVersionUID = 1L;
        final String fullTitle;
        final String abbreviatedTitle;
        final String tooltipTitle;
        final String tooltipBody;
        /**
         * No need to show a tooltip-title that is equal to the tooltip-body.
         */
        final boolean isTooltipTitleSuppressed() {
            return Objects.equals(tooltipTitle, tooltipBody);
        }
    }

    private TitleRecord cachedTitle;

    private TitleRecord determineTitle(final ManagedObject linkedDomainObject) {
        if(cachedTitle!=null) {
            return cachedTitle;
        }
        val fullTitle = MmTitleUtils.getTitleHonoringTitlePartSkipping(linkedDomainObject, this::isContextAdapter);
        return this.cachedTitle = TitleRecord.builder()
                .fullTitle(fullTitle)
                .abbreviatedTitle(titleAbbreviated(fullTitle))
                .tooltipTitle(_Strings.nullToEmpty(linkedDomainObject.getSpecification().getSingularName()))
                .tooltipBody(_Strings.nonEmpty(linkedDomainObject.getSpecification().getDescription())
                        .orElseGet(()->fullTitle))
                .build();
    }

    private void hideTitle() {
        // bit of a hack... allows us to suppress the title using CSS
        Wkt.cssAppend(this, "inlinePrompt");
    }

    /**
     * @implNote In effect can govern title suppression by returning 0.
     */
    private int abbreviateTo(final ObjectAdapterModel model, final String titleString) {
        /* Allows any higher-order component factory to customize appearance,
         * if the context requires it.
         * Eg. don't suppress titles for tables that have no property columns. */
        final int maxTitleLengthOverride = ColumnAbbreviationOptions.lookupIn(this)
            .map(opts->opts.getMaxElementTitleLength())
            .orElse(-1);
        if(maxTitleLengthOverride>-1) {
            return maxTitleLengthOverride;
        }

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

    //JUnit support (public)
    public static String abbreviated(final String str, final int maxLength) {
        int length = str.length();
        if (length <= maxLength) {
            return str;
        }
        return maxLength <= 3 ? "" : str.substring(0, maxLength - 3) + "...";
    }

    private static void guardAgainstNonEmptyAbstractSingular(final ObjectAdapterModel objectAdapterModel) {
        val obj = objectAdapterModel.getObject();
        _Assert.assertFalse(isNonEmptyAbstractSingular(obj),
                ()->String.format("model for EntityIconAndTitlePanel, "
                        + "when non-empty, must not represent abstract types; "
                        + "however, got an abstract %s for object of type %s",
                        obj.getSpecification(),
                        obj.getPojo().getClass().getName()));
    }

    private static boolean isNonEmptyAbstractSingular(final ManagedObject obj) {
        if(obj==null
                || obj.getPojo()==null
                || ManagedObjects.isPacked(obj)) {
            return false;
        }
        return obj.getSpecification().isAbstract();
    }

}
