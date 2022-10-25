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

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.causeway.core.metamodel.commons.StringExtensions;
import org.apache.causeway.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.causeway.viewer.commons.model.components.UiComponentType;
import org.apache.causeway.viewer.commons.model.layout.UiPlacementDirection;
import org.apache.causeway.viewer.wicket.model.hints.CausewaySelectorEvent;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.util.ComponentHintKey;
import org.apache.causeway.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.causeway.viewer.wicket.ui.ComponentFactory;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktLinks;
import org.apache.causeway.viewer.wicket.ui.util.WktTooltips;

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

    private static final String ID_VIEW_BUTTON_TITLE = "viewButtonTitle";
    private static final String ID_VIEW_BUTTON_ICON = "viewButtonIcon";

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
        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();
        final String selected = selectorHelper.honourViewHintElseDefault(this);

        // selector
        if (componentFactories.size() <= 1) {
            permanentlyHide(ID_VIEWS);
            return;
        }

        this.selectedComponentFactory = selectorHelper.find(selected);

        final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);
        final WebMarkupContainer container = new WebMarkupContainer(ID_VIEW_LIST);

        WktTooltips.addTooltip(UiPlacementDirection.TOP,
                views, translate("Click to change view or see export options."));

        views.addOrReplace(container);
        views.setOutputMarkupId(true);

        this.setOutputMarkupId(true);

        final Label viewButtonTitle = Wkt.labelAdd(views, ID_VIEW_BUTTON_TITLE, translate("Hidden"));
        final Label viewButtonIcon = Wkt.labelAdd(views, ID_VIEW_BUTTON_ICON, "");

        Wkt.listViewAdd(container, ID_VIEW_ITEM, componentFactories, item->{
            final ComponentFactory componentFactory = item.getModelObject();

            // add direct download link instead of a panel
            if(componentFactory.getComponentType() == UiComponentType.COLLECTION_CONTENTS_EXPORT) {

                DownloadLink downloadLink = (DownloadLink)
                        componentFactory.createComponent(ID_VIEW_LINK, getModel());

                item.addOrReplace(downloadLink);

                // add title and icon to the link
                WktLinks.listItemAsDropdownLink(item, downloadLink,
                        ID_VIEW_ITEM_TITLE, CollectionPresentationSelectorPanel::nameFor,
                        ID_VIEW_ITEM_ICON, null,
                        CollectionPresentationSelectorPanel::cssClassFor);
                return;
            }

            // on click: make the clicked item the new selected item
            val link = Wkt.linkAdd(item, ID_VIEW_LINK, target->{
                final CollectionPresentationSelectorPanel linksSelectorPanel = CollectionPresentationSelectorPanel.this;
                linksSelectorPanel.setViewHintAndBroadcast(componentFactory.getName(), target);

                linksSelectorPanel.selectedComponentFactory = componentFactory;

                CollectionPresentationSelectorPanel.this.getModel().parentedHintingBookmark()
                .ifPresent(bookmark->componentHintKey.set(bookmark, componentFactory.getName()));

                target.add(linksSelectorPanel, views);
            });

            // add title and icon to the link
            WktLinks.listItemAsDropdownLink(item, link,
                    ID_VIEW_ITEM_TITLE, CollectionPresentationSelectorPanel::nameFor,
                    ID_VIEW_ITEM_ICON, null,
                    CollectionPresentationSelectorPanel::cssClassFor);

            // hide the selected item
            val isSelected = componentFactory == CollectionPresentationSelectorPanel.this.selectedComponentFactory;
            if (isSelected) {
                viewButtonTitle.setDefaultModel(nameFor(componentFactory));
                final IModel<String> cssClass = cssClassFor(componentFactory, viewButtonIcon);
                Wkt.cssReplace(viewButtonIcon, "ViewLinkItem " + cssClass.getObject());
                link.setVisible(false);
            }

        });

        addOrReplace(views);
    }

    private static IModel<String> cssClassFor(final ComponentFactory componentFactory, final Label viewIcon) {
        IModel<String> cssClass = null;
        if (componentFactory instanceof CollectionContentsAsFactory) {
            CollectionContentsAsFactory collectionContentsAsFactory = (CollectionContentsAsFactory) componentFactory;
            cssClass = collectionContentsAsFactory.getCssClass();
            viewIcon.setDefaultModelObject("");
            viewIcon.setEscapeModelStrings(true);
        }
        if (cssClass == null) {
            String name = componentFactory.getName();
            cssClass = Model.of(StringExtensions.asLowerDashed(name));
            // Small hack: if there is no specific CSS class then we assume that background-image is used
            // the span.ViewItemLink should have some content to show it
            // FIX: find a way to do this with CSS (width and height don't seems to help)
            viewIcon.setDefaultModelObject("&#160;&#160;&#160;&#160;&#160;");
            viewIcon.setEscapeModelStrings(false);
        }
        return cssClass;
    }

    private static IModel<String> nameFor(final ComponentFactory componentFactory) {
        IModel<String> name = null;
        if (componentFactory instanceof CollectionContentsAsFactory) {
            CollectionContentsAsFactory collectionContentsAsFactory = (CollectionContentsAsFactory) componentFactory;
            name = collectionContentsAsFactory.getTitleLabel();
        }
        if (name == null) {
            name = Model.of(componentFactory.getName());
        }
        return name;
    }

    protected void setViewHintAndBroadcast(final String viewName, final AjaxRequestTarget target) {
        final CollectionPresentationSelectorPanel component = CollectionPresentationSelectorPanel.this;
        send(getPage(), Broadcast.EXACT,
                new CausewaySelectorEvent(component, CollectionPresentationSelectorHelper.UIHINT_EVENT_VIEW_KEY, viewName, target));
    }

}



