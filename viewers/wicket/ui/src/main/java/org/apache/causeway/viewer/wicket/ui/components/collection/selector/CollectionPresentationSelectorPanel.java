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
package org.apache.causeway.viewer.wicket.ui.components.collection.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.wicket.model.hints.CausewaySelectorEvent;
import org.apache.causeway.viewer.wicket.model.links.Menuable;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.model.util.PageUtils;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.widgets.links.AjaxLinkNoPropagate;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;

import lombok.NonNull;

/**
 * Provides a list of links for selecting other views that support
 * {@link org.apache.causeway.viewer.commons.model.components.UiComponentType#COLLECTION_CONTENTS} with a backing
 * {@link org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel}.
 */
public class CollectionPresentationSelectorPanel
extends PanelAbstract<DataTableInteractive, CollectionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_VIEWS = "views";
    private static final String ID_VIEW_LIST = "viewList";
    private static final String ID_VIEW_LINK = "viewLink";
    private static final String ID_VIEW_ITEM = "viewItem";
    private static final String ID_VIEW_ITEM_TITLE = "viewItemTitle";
    private static final String ID_VIEW_ITEM_ICON = "viewItemIcon";
    private static final String ID_VIEW_ITEM_CHECKMARK = "viewItemCheckmark"; // indicator for the selected item
    private static final String ID_VIEW_BUTTON_ICON = "viewButtonIcon";
    private static final String ID_SECTION_SEPARATOR = "sectionSeparator";
    private static final String ID_SECTION_LABEL = "sectionLabel";

    private CollectionPresentationSelectorHelper selectorHelper;
    private final Can<CollectionPresentationChoice> presentationChoices;
    private final ComponentHintKey componentHintKey;

    private CollectionPresentationChoice selectedCollectionPresentationChoice;

    public CollectionPresentationSelectorPanel(
            final String id,
            final CollectionModel model) {
        this(id, model, ComponentHintKey.noop());
    }

    public CollectionPresentationSelectorPanel(
            final String id,
            final CollectionModel model,
            final ComponentHintKey componentHintKey) {
        super(id, model);
        this.componentHintKey = componentHintKey;

        this.selectorHelper = new CollectionPresentationSelectorHelper(
                model, getComponentFactoryRegistry(), componentHintKey);
        this.presentationChoices = selectorHelper.collectionPresentationChoices();
    }

    /**
     * Build UI only after added to parent.
     */
    @Override
    public void onInitialize() {
        super.onInitialize();
        addDropdown();
    }

    private void addDropdown() {

        final String selected = selectorHelper.honourViewHintElseDefault(this);

        // selector
        if (!presentationChoices.isCardinalityMultiple()) {
            WktComponents.permanentlyHide(this, ID_VIEWS);
            return;
        }

        this.selectedCollectionPresentationChoice = selectorHelper.find(presentationChoices, selected);

        final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);
        final WebMarkupContainer container = new WebMarkupContainer(ID_VIEW_LIST);

        views.addOrReplace(container);
        views.setOutputMarkupId(true);

        this.setOutputMarkupId(true);

        final Label viewButtonIcon = Wkt.labelAdd(views, ID_VIEW_BUTTON_ICON, "");

        Wkt.listViewAdd(container, ID_VIEW_ITEM, sorted(presentationChoices), item->{
            var menuable = item.getModelObject();
            var menuableKind = menuable.menuableKind();

            Wkt.cssAppend(item, menuableKind.getCssClassForLiElement());

            switch(menuableKind) {
            case SECTION_SEPARATOR:
                WktComponents.permanentlyHide(item, ID_SECTION_LABEL);
                WktComponents.permanentlyHide(item, ID_VIEW_LINK);
                Wkt.labelAdd(item, ID_SECTION_SEPARATOR, "");
                return;
            case SECTION_LABEL:
                WktComponents.permanentlyHide(item, ID_SECTION_SEPARATOR);
                WktComponents.permanentlyHide(item, ID_VIEW_LINK);
                Wkt.labelAdd(item, ID_SECTION_LABEL, ((Menuable.SectionLabel)menuable).getSectionLabel());
                return;
            case LINK:
                WktComponents.permanentlyHide(item, ID_SECTION_SEPARATOR);
                WktComponents.permanentlyHide(item, ID_SECTION_LABEL);
                break; // fall through
            default:
                return;
            }

            var linkEntry = (LinkEntry) menuable;
            var choice = linkEntry.choice;

            // add direct download link instead of a panel
            if(choice.isExporter()) {

                final DownloadLink downloadLink = (DownloadLink)
                        choice.componentFactory().createComponent(ID_VIEW_LINK, getModel());
                WktComponents.permanentlyHide(downloadLink, ID_VIEW_ITEM_CHECKMARK);

                item.addOrReplace(downloadLink);

                // add title and icon to the link
                addLinkWithIconAndTitle(item, downloadLink);
                return;
            }

            // on click: make the clicked item the new selected item
            var link = Wkt.linkAdd(item, ID_VIEW_LINK, target->{
                final CollectionPresentationSelectorPanel linksSelectorPanel = CollectionPresentationSelectorPanel.this;
                linksSelectorPanel.setViewHintAndBroadcast(choice.id(), target);

                linksSelectorPanel.selectedCollectionPresentationChoice = choice;

                CollectionPresentationSelectorPanel.this.getModel().parentedHintingBookmark()
                    .ifPresent(bookmark->componentHintKey.set(bookmark, choice.id()));

                /* [CAUSEWAY-3415] do a full page reload when required,
                 * to properly trigger all client side java-script, that decorates HTML (datatable.net, vega, ...) */
                if(linkEntry.isPageReloadRequiredOnTableViewActivation()) {
                    PageUtils.pageReload();
                } else {
                    target.add(linksSelectorPanel, views);
                }
            });

            // add title and icon to the link
            addLinkWithIconAndTitle(item, link);
            var checkmarkForSelectedPresentation = Wkt.labelAdd(link, ID_VIEW_ITEM_CHECKMARK, "");

            var isSelectedPresentation = linkEntry.isSelectedIn(this);
            checkmarkForSelectedPresentation.setVisible(isSelectedPresentation);
            if (isSelectedPresentation) {
                linkEntry.markAsSelected(viewButtonIcon, link);
            }

        });

        addOrReplace(views);
    }

    /**
     * Sorts given CollectionContentsAsFactory(s) by their orderOfAppearanceInUiDropdown,
     * in order of discovery otherwise.
     * @param filter
     * @see CollectionContentsAsFactory#orderOfAppearanceInUiDropdown()
     */
    private List<Menuable> sorted(final Can<CollectionPresentationChoice> presentationChoices) {
        var presentations = sorted(presentationChoices, CollectionPresentationChoice::isPresenter);
        var exports = sorted(presentationChoices, CollectionPresentationChoice::isExporter);
        var sortedWithSeparators = new ArrayList<Menuable>(
                presentations.size() + exports.size() + 2); // heap optimization, not strictly required

        boolean needsSpacer = false;

        if(!presentations.isEmpty()) {
            sortedWithSeparators.add(Menuable.sectionLabel(translate("Presentations")));
            sortedWithSeparators.addAll(presentations);
            needsSpacer = true;
        }
        if(!exports.isEmpty()) {
            if(needsSpacer) {
                sortedWithSeparators.add(Menuable.sectionSeparator());
            }
            sortedWithSeparators.add(Menuable.sectionLabel(translate("Exports")));
            sortedWithSeparators.addAll(exports);
        }

        return sortedWithSeparators;
    }

    private List<LinkEntry> sorted(
            final Can<CollectionPresentationChoice> presentationChoices,
            final Predicate<? super CollectionPresentationChoice> filter) {
        final List<LinkEntry> sorted = presentationChoices.stream()
            .filter(filter)
            .sorted(CollectionPresentationChoice.orderByOrderOfAppearanceInUiDropdown())
            .map(LinkEntry::linkEntry)
            .collect(Collectors.toList());
        return sorted;
    }

    protected void setViewHintAndBroadcast(final String viewName, final AjaxRequestTarget target) {
        final CollectionPresentationSelectorPanel component = CollectionPresentationSelectorPanel.this;
        send(getPage(), Broadcast.EXACT,
                new CausewaySelectorEvent(component, CollectionPresentationSelectorHelper.UIHINT_EVENT_VIEW_KEY, viewName, target));
    }

    // -- UTILITY

    @SuppressWarnings("unchecked")
    static void addLinkWithIconAndTitle(
            final @NonNull ListItem<? extends Menuable> item,
            final @NonNull MarkupContainer link) {
        WktLinks.listItemAsDropdownLink((ListItem<LinkEntry>)item, link,
                ID_VIEW_ITEM_TITLE, LinkEntry::nameFor,
                ID_VIEW_ITEM_ICON, null,
                LinkEntry::cssClassFor);
    }

    @lombok.Value
    static class LinkEntry implements Menuable {
        private static final long serialVersionUID = 1L;

        // -- FACTORIES

        public static LinkEntry linkEntry(final @NonNull CollectionPresentationChoice choice) {
            return new LinkEntry(choice);
        }

        // -- CONSTRUCTION

        final CollectionPresentationChoice choice;

        @Override
        public Kind menuableKind() { return Kind.LINK; }

        // -- PREDICATES

        boolean isSelectedIn(final CollectionPresentationSelectorPanel panel) {
           return Objects.equals(this.choice.factoryKey(), panel.selectedCollectionPresentationChoice.factoryKey());
        }
        boolean isPageReloadRequiredOnTableViewActivation() {
            return choice.isPageReloadRequiredOnTableViewActivation();
        }
        boolean isPresenter() {
            return choice.isPresenter();
        }
        boolean isExporter() {
            return choice.isExporter();
        }

        // -- MARK

        /**
         * Disables the selected presentation's link,
         * also sets the icon left of the drop-down caret to the one,
         * that corresponds to the selected presentation.
         */
        void markAsSelected(final Label viewButtonIcon, final AjaxLinkNoPropagate link) {
            initViewIcon(this, viewButtonIcon);
            Wkt.cssReplace(viewButtonIcon, "ViewLinkItem " + choice.cssClass());
            link.setEnabled(false);
        }

        // -- HELPER

        private static IModel<String> nameFor(final LinkEntry linkEntry) {
            return Model.of(linkEntry.choice.label());
        }
        private static IModel<String> cssClassFor(final LinkEntry linkEntry, final Label viewIcon) {
            initViewIcon(linkEntry, viewIcon);
            return Model.of(linkEntry.choice.cssClass());
        }

        private static void initViewIcon(final LinkEntry linkEntry, final Label viewIcon) {
            if(CollectionContentsAsFactory.class.isAssignableFrom(componentFactoryClass(linkEntry))) {
                viewIcon.setDefaultModelObject("");
                viewIcon.setEscapeModelStrings(true);
            } else {
                // Small hack: if there is no specific CSS class then we assume that background-image is used
                // the span.ViewItemLink should have some content to show it
                // FIX: find a way to do this with CSS (width and height don't seems to help)
                viewIcon.setDefaultModelObject("&#160;&#160;&#160;&#160;&#160;");
                viewIcon.setEscapeModelStrings(false);
            }
        }

        private static Class<? extends ComponentFactory> componentFactoryClass(final LinkEntry linkEntry) {
            return linkEntry.choice.factoryKey().factoryClass();
        }
    }

}
