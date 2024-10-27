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
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.hints.CausewaySelectorEvent;
import org.apache.causeway.viewer.wicket.model.links.Menuable;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.model.util.PageUtils;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.app.registry.ComponentFactoryKey;
import org.apache.causeway.viewer.wicket.ui.components.widgets.links.AjaxLinkNoPropagate;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;

import lombok.NonNull;

/**
 * Provides a list of links for selecting other views that support
 * {@link org.apache.causeway.viewer.commons.model.components.UiComponentType#COLLECTION_CONTENTS} with a backing
 * {@link org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel}.
 */
public class CollectionPresentationSelectorPanel
extends PanelAbstract<DataTableInteractive, EntityCollectionModel> {

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

    private final CollectionPresentationSelectorHelper selectorHelper;
    private final ComponentHintKey componentHintKey;

    private ComponentFactoryKey selectedComponentFactory;

    public CollectionPresentationSelectorPanel(
            final String id,
            final EntityCollectionModel model) {
        this(id, model, ComponentHintKey.noop());
    }

    public CollectionPresentationSelectorPanel(
            final String id,
            final EntityCollectionModel model,
            final ComponentHintKey componentHintKey) {
        super(id, model);
        this.componentHintKey = componentHintKey;

        selectorHelper = new CollectionPresentationSelectorHelper(
                model, getComponentFactoryRegistry(), componentHintKey);
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
        final Can<ComponentFactoryKey> componentFactories = selectorHelper.getComponentFactories();
        final String selected = selectorHelper.honourViewHintElseDefault(this);

        // selector
        if (!componentFactories.isCardinalityMultiple()) {
            WktComponents.permanentlyHide(this, ID_VIEWS);
            return;
        }

        this.selectedComponentFactory = selectorHelper.find(selected);

        final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);
        final WebMarkupContainer container = new WebMarkupContainer(ID_VIEW_LIST);

        //XXX UI glitch, tooltip has tendency to not disappear
//        WktTooltips.addTooltip(UiPlacementDirection.TOP,
//                views, translate("Click to change view or see export options."));

        views.addOrReplace(container);
        views.setOutputMarkupId(true);

        this.setOutputMarkupId(true);

        final Label viewButtonIcon = Wkt.labelAdd(views, ID_VIEW_BUTTON_ICON, "");

        Wkt.listViewAdd(container, ID_VIEW_ITEM, sorted(componentFactories), item->{
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
            final ComponentFactoryKey componentFactory = linkEntry.getComponentFactoryKey();

            // add direct download link instead of a panel
            if(componentFactory.componentType() == UiComponentType.COLLECTION_CONTENTS_EXPORT) {

                final DownloadLink downloadLink = (DownloadLink)
                        componentFactory.resolve(this::getServiceRegistry).createComponent(ID_VIEW_LINK, getModel());
                WktComponents.permanentlyHide(downloadLink, ID_VIEW_ITEM_CHECKMARK);

                item.addOrReplace(downloadLink);

                // add title and icon to the link
                addLinkWithIconAndTitle(item, downloadLink);
                return;
            }

            // on click: make the clicked item the new selected item
            var link = Wkt.linkAdd(item, ID_VIEW_LINK, target->{
                final CollectionPresentationSelectorPanel linksSelectorPanel = CollectionPresentationSelectorPanel.this;
                linksSelectorPanel.setViewHintAndBroadcast(componentFactory.id(), target);

                linksSelectorPanel.selectedComponentFactory = componentFactory;

                CollectionPresentationSelectorPanel.this.getModel().parentedHintingBookmark()
                    .ifPresent(bookmark->componentHintKey.set(bookmark, componentFactory.id()));

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
    private List<Menuable> sorted(final Can<ComponentFactoryKey> componentFactories) {
        var presentations = sorted(componentFactories, _Util.filterTablePresentations());
        var exports = sorted(componentFactories, _Util.filterTableExports());
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
            final Can<ComponentFactoryKey> componentFactories,
            final Predicate<? super ComponentFactoryKey> filter) {
        final List<LinkEntry> sorted = componentFactories.stream()
            .filter(filter)
            .sorted(_Util.orderByOrderOfAppearanceInUiDropdown())
            .map((final ComponentFactoryKey factory)->LinkEntry.linkEntry(factory))
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

        public static LinkEntry linkEntry(final @NonNull ComponentFactoryKey componentFactoryKey) {
            return new LinkEntry(componentFactoryKey);
        }

        // -- CONSTRUCTION

        final ComponentFactoryKey componentFactoryKey;

        // -- PREDICATES

        boolean isSelectedIn(final CollectionPresentationSelectorPanel panel) {
           return Objects.equals(componentFactoryKey, panel.selectedComponentFactory);
        }
        boolean isPageReloadRequiredOnTableViewActivation() {
            return componentFactoryKey.isPageReloadRequiredOnTableViewActivation();
        }

        /**
         * Disables the selected presentation's link,
         * also sets the icon left of the drop-down caret to the one,
         * that corresponds to the selected presentation.
         */
        void markAsSelected(final Label viewButtonIcon, final AjaxLinkNoPropagate link) {
            final IModel<String> cssClass = Model.of(componentFactoryKey.cssClass());
            _Util.initViewIcon(componentFactoryKey, viewButtonIcon);
            Wkt.cssReplace(viewButtonIcon, "ViewLinkItem " + cssClass.getObject());
            link.setEnabled(false);
        }

        // -- HELPER

        private static IModel<String> nameFor(final LinkEntry linkEntry) {
            return Model.of(linkEntry.getComponentFactoryKey().label());
        }
        private static IModel<String> cssClassFor(final LinkEntry linkEntry, final Label viewIcon) {
            final IModel<String> cssClass = Model.of(linkEntry.componentFactoryKey.cssClass());
            _Util.initViewIcon(linkEntry.componentFactoryKey, viewIcon);
            return cssClass;
        }

        @Override
        public Kind menuableKind() { return Kind.LINK; }
    }

}
