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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.wicket.model.hints.CausewaySelectorEvent;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.components.widgets.links.AjaxLinkNoPropagate;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;

import lombok.NonNull;
import lombok.val;

/**
 * Provides a list of links for selecting other views that support
 * {@link org.apache.causeway.viewer.commons.model.components.UiComponentType#COLLECTION_CONTENTS} with a backing
 * {@link org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel}.
 */
public class CollectionPresentationSelectorPanel
extends PanelAbstract<DataTableModel, EntityCollectionModel> {

    private static final long serialVersionUID = 1L;

    private static final String ID_VIEWS = "views";
    private static final String ID_VIEW_LIST = "viewList";
    private static final String ID_VIEW_LINK = "viewLink";
    private static final String ID_VIEW_ITEM = "viewItem";
    private static final String ID_VIEW_ITEM_TITLE = "viewItemTitle";
    private static final String ID_VIEW_ITEM_ICON = "viewItemIcon";
    private static final String ID_VIEW_BUTTON_ICON = "viewButtonIcon";
    private static final String ID_SECTION_LABEL = "sectionLabel";

    private final CollectionPresentationSelectorHelper selectorHelper;
    private final ComponentHintKey componentHintKey;

    private ComponentFactory selectedComponentFactory;

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
        final Can<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();
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
            val eitherComponentFactoryOrSectionLabel = item.getModelObject();

            if(eitherComponentFactoryOrSectionLabel.isSectionLabel()) {
                Wkt.cssAppend(item, "list-section-label");
                Wkt.labelAdd(item, ID_SECTION_LABEL, eitherComponentFactoryOrSectionLabel.getSectionLabel());
                WktComponents.permanentlyHide(item, ID_VIEW_LINK);
                return;
            }

            Wkt.cssAppend(item, "viewItem");
            WktComponents.permanentlyHide(item, ID_SECTION_LABEL);

            final ComponentFactory componentFactory = eitherComponentFactoryOrSectionLabel.getComponentFactory();

            // add direct download link instead of a panel
            if(componentFactory.getComponentType() == UiComponentType.COLLECTION_CONTENTS_EXPORT) {

                final DownloadLink downloadLink = (DownloadLink)
                        componentFactory.createComponent(ID_VIEW_LINK, getModel());

                item.addOrReplace(downloadLink);

                // add title and icon to the link
                EitherComponentFactoryOrSectionLabel.addLinkWithIconAndTitle(item, downloadLink);
                return;
            }

            // on click: make the clicked item the new selected item
            val link = Wkt.linkAdd(item, ID_VIEW_LINK, target->{
                final CollectionPresentationSelectorPanel linksSelectorPanel = CollectionPresentationSelectorPanel.this;
                linksSelectorPanel.setViewHintAndBroadcast(componentFactory.getName(), target);

                linksSelectorPanel.selectedComponentFactory = componentFactory;

                CollectionPresentationSelectorPanel.this.getModel().parentedHintingBookmark()
                    .ifPresent(bookmark->componentHintKey.set(bookmark, componentFactory.getName()));

                /* [CAUSEWAY-3415] do a full page reload when required,
                 * to properly trigger all client side java-script, that decorates HTML (datatable.net, vega, ...) */
                if(eitherComponentFactoryOrSectionLabel.isPageReloadRequiredOnTableViewActivation()) {
                    linksSelectorPanel.reloadPage();
                } else {
                    target.add(linksSelectorPanel, views);
                }
            });

            // add title and icon to the link
            EitherComponentFactoryOrSectionLabel.addLinkWithIconAndTitle(item, link);

            // mark the selected item as active
            if (eitherComponentFactoryOrSectionLabel.isSelectedIn(this)) {
                eitherComponentFactoryOrSectionLabel.markAsActive(viewButtonIcon, link);
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
    private List<EitherComponentFactoryOrSectionLabel> sorted(final Can<ComponentFactory> componentFactories) {
        val presentations = sorted(componentFactories, _Util.filterTablePresentations());
        val exports = sorted(componentFactories, _Util.filterTableExports());
        val sortedWithSeparators = new ArrayList<EitherComponentFactoryOrSectionLabel>(
                presentations.size() + exports.size() + 2); // heap optimization, not strictly required

        if(!presentations.isEmpty()) {
            sortedWithSeparators.add(EitherComponentFactoryOrSectionLabel.of(translate("Presentations")));
            sortedWithSeparators.addAll(presentations);
        }
        if(!exports.isEmpty()) {
            sortedWithSeparators.add(EitherComponentFactoryOrSectionLabel.of(translate("Exports")));
            sortedWithSeparators.addAll(exports);
        }

        return sortedWithSeparators;
    }

    private List<EitherComponentFactoryOrSectionLabel> sorted(
            final Can<ComponentFactory> componentFactories,
            final Predicate<? super ComponentFactory> filter) {
        final List<EitherComponentFactoryOrSectionLabel> sorted = componentFactories.stream()
            .filter(filter)
            .sorted(_Util.orderByOrderOfAppearanceInUiDropdown())
            .map((final ComponentFactory factory)->EitherComponentFactoryOrSectionLabel.of(factory))
            .collect(Collectors.toList());
        return sorted;
    }

    protected void setViewHintAndBroadcast(final String viewName, final AjaxRequestTarget target) {
        final CollectionPresentationSelectorPanel component = CollectionPresentationSelectorPanel.this;
        send(getPage(), Broadcast.EXACT,
                new CausewaySelectorEvent(component, CollectionPresentationSelectorHelper.UIHINT_EVENT_VIEW_KEY, viewName, target));
    }

    @lombok.Value
    static class EitherComponentFactoryOrSectionLabel implements Serializable {
        private static final long serialVersionUID = 1L;
        public static EitherComponentFactoryOrSectionLabel of(final @NonNull ComponentFactory componentFactory) {
            return new EitherComponentFactoryOrSectionLabel(componentFactory, null);
        }
        public static EitherComponentFactoryOrSectionLabel of(final @NonNull String sectionLabel) {
            return new EitherComponentFactoryOrSectionLabel(null, sectionLabel);
        }
        final ComponentFactory componentFactory;
        final String sectionLabel;
        boolean isComponentFactory() { return componentFactory!=null; }
        boolean isSectionLabel() { return sectionLabel!=null; }
        boolean isSelectedIn(final CollectionPresentationSelectorPanel panel) {
           return isComponentFactory()
                   && componentFactory == panel.selectedComponentFactory;
        }
        boolean isPageReloadRequiredOnTableViewActivation() {
            return isComponentFactory()
                    && _Util.isPageReloadRequiredOnTableViewActivation(componentFactory);
        }
        void markAsActive(final Label viewButtonIcon, final AjaxLinkNoPropagate link) {
            final IModel<String> cssClass = _Util.cssClassFor(componentFactory, viewButtonIcon);
            Wkt.cssReplace(viewButtonIcon, "ViewLinkItem " + cssClass.getObject());
            Wkt.cssAppend(link, "active");
            link.setEnabled(false);
        }
        // -- UTILITY
        static void addLinkWithIconAndTitle(
                final @NonNull ListItem<EitherComponentFactoryOrSectionLabel> item,
                final @NonNull MarkupContainer link) {
            WktLinks.listItemAsDropdownLink(item, link,
                    ID_VIEW_ITEM_TITLE, EitherComponentFactoryOrSectionLabel::nameFor,
                    ID_VIEW_ITEM_ICON, null,
                    EitherComponentFactoryOrSectionLabel::cssClassFor);
        }
        // -- HELPER
        private static IModel<String> nameFor(final EitherComponentFactoryOrSectionLabel either) {
            return _Util.nameFor(either.getComponentFactory());
        }
        private static IModel<String> cssClassFor(final EitherComponentFactoryOrSectionLabel either, final Label viewIcon) {
            return _Util.cssClassFor(either.getComponentFactory(), viewIcon);
        }
    }

}



