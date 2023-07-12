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

import java.util.Optional;

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
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

import lombok.val;

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

    private String titleLiteral; // cached title

    private ObjectAdapterModel objectAdapterModel; //TODO[CAUSEWAY-3522] rmo

    public EntityIconAndTitlePanel(
            final String id,
            final ObjectAdapterModel objectAdapterModel) {
        super(id, objectAdapterModel);
        guardAgainstNonEmptyAbstractSingular(objectAdapterModel);
        this.objectAdapterModel = objectAdapterModel;
        _Assert.assertFalse(objectAdapterModel.isHollow()); //TODO[CAUSEWAY-3522] debug
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

    @Override
    protected void onDetach() {
        super.onDetach();
        System.err.printf("EntityIconAndTitlePanel#detach %s%n", titleLiteral);
        objectAdapterModel.detach();
        _Assert.assertTrue(objectAdapterModel.isHollow()); //TODO[CAUSEWAY-3522] debug
    }

    /**
     * Callback for sub-classes to add additional components.
     */
    protected void onLinkWrapperCreated(final MarkupContainer linkWrapper) {}

    // -- HELPER

    private void buildGui() {
        objectAdapterModel.refreshIfHollow();
        final ManagedObject linkedDomainObject = linkedDomainObject(); //TODO[CAUSEWAY-3522] potentially a viewmodel referencing hollow entities
        //ManagedObjects.refreshViewmodel(linkedDomainObject(), null); //XXX workaround, if not using title caching
        addLinkWrapper(linkedDomainObject);
        setOutputMarkupId(true);
    }

    private void addLinkWrapper(final ManagedObject linkedDomainObject) {
        val linkWrapper = Wkt.container(ID_ENTITY_LINK_WRAPPER);
        linkWrapper.addOrReplace(createLinkWithIconAndTitle(linkedDomainObject));
        addOrReplace(linkWrapper);
        onLinkWrapperCreated(linkWrapper);
    }

    private AbstractLink createLinkWithIconAndTitle(final ManagedObject linkedDomainObject) {
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
                        Wkt.cssAppend(dummyLabel, cssClassFaFactory.asSpaceSeparatedWithAdditional("fa-2x"));
                    });

            final String title = determineTitle(linkedDomainObject);
            Wkt.labelAdd(link, ID_ENTITY_TITLE, titleAbbreviated(title));

            final String tooltipTitle = linkedDomainObject.getSpecification().getSingularName();
            final String tooltipBody = _Strings.nonEmpty(getModel().getTypeOfSpecification().getDescription())
                    .orElseGet(()->title);
            WktTooltips.addTooltip(link, tooltipTitle, tooltipBody);
        }

        return link;
    }

    private boolean isTitleSuppressed() {
        return getModel().isInlinePrompt()
                //XXX CAUSEWAY-1699 never hide titles of object references in tables
                && !getModel().getRenderingHint().isInTable();
    }

    private AbstractLink createDynamicallyVisibleLink(final ManagedObject _targetAdapter) {
        val pageParameters = PageParameterUtils
                .createPageParametersForBookmarkablePageLink(getModel(), _targetAdapter);
        val pageClass = getPageClassRegistry().getPageClass(PageType.ENTITY);

        return Wkt.bookmarkablePageLinkWithVisibility(ID_ENTITY_LINK, pageClass, pageParameters, ()->{
            // not visible if null
            // (except its null because its a detached entity,
            // which we can re-fetch due to memoized bookmark)
            val linkedDomainObject = EntityIconAndTitlePanel.this.linkedDomainObject();
            return linkedDomainObject != null
                    && (linkedDomainObject.getPojo()!=null
                            || linkedDomainObject.isBookmarkMemoized());
        });
    }

    private String titleAbbreviated(final String titleString) {
        final int maxTitleLength = abbreviateTo(getModel(), titleString);
        return abbreviated(titleString, maxTitleLength);
    }

    private String determineTitle(final ManagedObject linkedDomainObject) {
        final String title = Optional.ofNullable(this.titleLiteral)
                .orElseGet(()->
                    //this.titleLiteral =
                        MmTitleUtils.getTitleHonoringTitlePartSkipping(
                                linkedDomainObject,
                                this::isContextAdapter));
        return title;
    }

    private void hideTitle() {
        // bit of a hack... allows us to suppress the title using CSS
        Wkt.cssAppend(this, "inlinePrompt");
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
        _Assert.assertFalse(isNonEmptyAbstractScalar(obj),
                ()->String.format("model for EntityIconAndTitlePanel, "
                        + "when non-empty, must not represent abstract types; "
                        + "however, got an abstract %s for object of type %s",
                        obj.getSpecification(),
                        obj.getPojo().getClass().getName()));
    }

    private static boolean isNonEmptyAbstractScalar(final ManagedObject obj) {
        if(obj==null
                || obj.getPojo()==null
                || ManagedObjects.isPacked(obj)) {
            return false;
        }
        return obj.getSpecification().isAbstract();
    }

}
