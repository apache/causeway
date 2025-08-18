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
package org.apache.causeway.viewer.wicket.ui.components.object.icontitle;

import java.io.Serializable;
import java.util.Objects;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.html.link.AbstractLink;

import org.apache.causeway.applib.layout.component.CssClassFaPosition;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.base._Text;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconEmbedded;
import org.apache.causeway.core.metamodel.facets.object.icon.ObjectIconUrlBased;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ManagedObjects;
import org.apache.causeway.core.metamodel.object.MmTitleUtils;
import org.apache.causeway.viewer.wicket.model.models.ObjectAdapterModel;
import org.apache.causeway.viewer.wicket.model.models.PageType;
import org.apache.causeway.viewer.wicket.model.models.UiObjectWkt;
import org.apache.causeway.viewer.wicket.model.util.PageParameterUtils;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.ColumnAbbreviationOptions;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.Builder;

/**
 * {@link PanelAbstract Panel} representing the icon and title of an entity,
 * as per the provided {@link UiObjectWkt}.
 */
class ObjectIconAndTitlePanel
extends PanelAbstract<ManagedObject, ObjectAdapterModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_OBJECT_LINK_WRAPPER = "objectLinkWrapper";
    private static final String ID_OBJECT_FONT_AWESOME_LEFT = "objectIconFaLeft";
    private static final String ID_OBJECT_FONT_AWESOME_RIGHT = "objectIconFaRight";
    private static final String ID_OBJECT_LINK = "objectLink";
    private static final String ID_OBJECT_TITLE = "objectTitle";
    private static final String ID_OBJECT_ICON = "objectImage";

    public ObjectIconAndTitlePanel(
            final String id,
            final ObjectAdapterModel objectAdapterModel) {
        super(id, objectAdapterModel);
        guardAgainstNonEmptyAbstractSingular(objectAdapterModel);
    }

    protected ManagedObject linkedDomainObject() {
        var linkedDomainObject = getModel().getObject();
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
        var linkWrapper = Wkt.container(ID_OBJECT_LINK_WRAPPER);
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
            WktComponents.permanentlyHide(link, ID_OBJECT_ICON);
            Wkt.labelAdd(link, ID_OBJECT_TITLE, titleAbbreviated("(no object)"));
        } else {

            linkedDomainObject.eitherIconOrFaLayers()
            .accept(
                    objectIcon->{
                        if(objectIcon instanceof ObjectIconEmbedded iconEmbedded) {
                            //TODO[causeway-viewer-wicket-ui-CAUSEWAY-3889] for embedded images we me might want to have a different CSS class
                            //e.g. don't constrain image sizes, as these should be driven by embedded data
                            Wkt.imageAddEmbedded(link, ID_OBJECT_ICON, iconEmbedded.dataUri());
                        } else if(objectIcon instanceof ObjectIconUrlBased iconUrlBased) {
                            Wkt.imageAddCachable(link, ID_OBJECT_ICON,
                                    getImageResourceCache().resourceReferenceForObjectIcon(iconUrlBased));
                        } else {
                            throw new IllegalArgumentException("Unexpected value: " + objectIcon);
                        }

                        WktComponents.permanentlyHide(link, ID_OBJECT_FONT_AWESOME_LEFT);
                        WktComponents.permanentlyHide(link, ID_OBJECT_FONT_AWESOME_RIGHT);
                    },
                    faLayers->{
                        WktComponents.permanentlyHide(link, ID_OBJECT_ICON);
                        if(CssClassFaPosition.isLeftOrUnspecified(faLayers.position())) {
                            Wkt.faIconLayersAdd(link, ID_OBJECT_FONT_AWESOME_LEFT, faLayers);
                            WktComponents.permanentlyHide(link, ID_OBJECT_FONT_AWESOME_RIGHT);
                        } else {
                            WktComponents.permanentlyHide(link, ID_OBJECT_FONT_AWESOME_LEFT);
                            Wkt.faIconLayersAdd(link, ID_OBJECT_FONT_AWESOME_RIGHT, faLayers);
                        }
                    });

            final TitleRecord title = determineTitle(linkedDomainObject);
            Wkt.labelAdd(link, ID_OBJECT_TITLE, title.abbreviatedTitle());

            // If the link title is abbreviated or not shown, add the full-title to the tooltip body.
            if(isTitleSuppressed() || title.isTitleAbbreviated()) {
                String body = title.isFullTitleEqualToBody() ? title.tooltipBody() : title.tooltipBodyIncludingFullTitle();
                WktTooltips.addTooltip(link, title.tooltipTitle(), body);
            } else if(title.isTooltipTitleEqualToBody()) {
                WktTooltips.addTooltip(link, title.tooltipBody());
            } else {
                String body = title.isFullTitleEqualToBody() ? title.tooltipBody() : title.tooltipBodyIncludingFullTitle();
                WktTooltips.addTooltip(link, title.tooltipTitle(), body);
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
        var pageParameters = PageParameterUtils
                .createPageParametersForBookmarkablePageLink(linkedDomainObject);
        var pageClass = getPageClassRegistry().getPageClass(PageType.DOMAIN_OBJECT);

        return Wkt.bookmarkablePageLinkWithVisibility(ID_OBJECT_LINK, pageClass, pageParameters,
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
        return _Text.abbreviated(titleString, maxTitleLength);
    }

    /**
     * Holder of titles for various UI contexts (Java record candidate).
     */
    @Builder
    private record TitleRecord(
            String fullTitle,
            String abbreviatedTitle,
            String tooltipTitle,
            String tooltipBody) implements Serializable {

        /**
         * Whether tooltip-title and tooltip-body are the same ignoring case.
         * <p>
         * UI note: No need to show a tooltip-title that is equal to the tooltip-body.
         */
        final boolean isTooltipTitleEqualToBody() {
            return _Strings.nullToEmpty(tooltipTitle)
                    .equalsIgnoreCase(_Strings.nullToEmpty(tooltipBody));
        }

        public boolean isFullTitleEqualToBody() {
            return _Strings.nullToEmpty(fullTitle)
                    .equalsIgnoreCase(_Strings.nullToEmpty(tooltipBody));
        }

        /**
         * Whether not {@link #abbreviatedTitle()} equals {@link #fullTitle()}.
         * <p>
         * UI note: If the link title is abbreviated or not shown, add the full-title to the tooltip body.
         */
        final boolean isTitleAbbreviated() {
            return !Objects.equals(abbreviatedTitle, fullTitle);
        }
        final String tooltipBodyIncludingFullTitle() {
            return _Strings.nullToEmpty(fullTitle)
                    + "\n-\n"
                    + _Strings.nullToEmpty(tooltipBody);
        }

    }

    private TitleRecord cachedTitle;

    private TitleRecord determineTitle(final ManagedObject linkedDomainObject) {
        if(cachedTitle!=null) return cachedTitle;

        var fullTitle = MmTitleUtils.getTitleHonoringTitlePartSkipping(linkedDomainObject, this::isContextAdapter);
        return this.cachedTitle = TitleRecord.builder()
                .fullTitle(fullTitle)
                .abbreviatedTitle(titleAbbreviated(fullTitle))
                .tooltipTitle(_Strings.nullToEmpty(linkedDomainObject.objSpec().getSingularName()))
                .tooltipBody(_Strings.nonEmpty(linkedDomainObject.objSpec().getDescription())
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
            .map(ColumnAbbreviationOptions::maxElementTitleLength)
            .orElse(-1);
        if(maxTitleLengthOverride>-1) {
            return maxTitleLengthOverride;
        }

        if(model.getRenderingHint().isInStandaloneTableTitleColumn()) {
            return getWicketViewerSettings().maxTitleLengthInStandaloneTables();
        }
        if(model.getRenderingHint().isInParentedTableTitleColumn()) {
            return getWicketViewerSettings().maxTitleLengthInParentedTables();
        }
        return titleString.length();
    }

    private boolean isContextAdapter(final ManagedObject other) {
        return getModel().isContextAdapter(other);
    }

    private static void guardAgainstNonEmptyAbstractSingular(final ObjectAdapterModel objectAdapterModel) {
        var obj = objectAdapterModel.getObject();
        _Assert.assertFalse(isNonEmptyAbstractSingular(obj),
                ()->String.format("model for EntityIconAndTitlePanel, "
                        + "when non-empty, must not represent abstract types; "
                        + "however, got an abstract %s for object of type %s",
                        obj.objSpec(),
                        obj.getPojo().getClass().getName()));
    }

    private static boolean isNonEmptyAbstractSingular(final ManagedObject obj) {
        if(obj==null
                || obj.getPojo()==null
                || ManagedObjects.isPacked(obj)) {
            return false;
        }
        return obj.objSpec().isAbstract();
    }

}
