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

package org.apache.isis.viewer.wicket.ui.components.collection.selector;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import org.apache.isis.applib.services.bookmark.Bookmark;
import org.apache.isis.core.commons.lang.StringExtensions;
import org.apache.isis.viewer.wicket.model.hints.IsisSelectorEvent;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.util.ComponentHintKey;
import org.apache.isis.viewer.wicket.ui.CollectionContentsAsFactory;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;

/**
 * Provides a list of links for selecting other views that support
 * {@link org.apache.isis.viewer.wicket.ui.ComponentType#COLLECTION_CONTENTS} with a backing
 * {@link org.apache.isis.viewer.wicket.model.models.EntityCollectionModel}.
 */
public class CollectionSelectorPanel extends PanelAbstract<EntityCollectionModel>  {

    private static final long serialVersionUID = 1L;

    private static final String ID_VIEWS = "views";
    private static final String ID_VIEW_LIST = "viewList";
    private static final String ID_VIEW_LINK = "viewLink";
    private static final String ID_VIEW_ITEM = "viewItem";
    private static final String ID_VIEW_ITEM_TITLE = "viewItemTitle";
    private static final String ID_VIEW_ITEM_ICON = "viewItemIcon";

    private static final String ID_VIEW_BUTTON_TITLE = "viewButtonTitle";
    private static final String ID_VIEW_BUTTON_ICON = "viewButtonIcon";

    private final CollectionSelectorHelper selectorHelper;
    private final ComponentHintKey componentHintKey;

    private ComponentFactory selectedComponentFactory;

    public CollectionSelectorPanel(
            final String id,
            final EntityCollectionModel model) {
        this(id, model, ComponentHintKey.<String>noop());
    }

    public CollectionSelectorPanel(
            final String id,
            final EntityCollectionModel model,
            final ComponentHintKey componentHintKey) {
        super(id, model);
        this.componentHintKey = componentHintKey;

        selectorHelper = new CollectionSelectorHelper(
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
        } else {
            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            this.selectedComponentFactory = selectorHelper.find(selected);
            componentFactoryModel.setObject(this.selectedComponentFactory);

            final WebMarkupContainer views = new WebMarkupContainer(ID_VIEWS);

            final Label viewButtonTitle = new Label(ID_VIEW_BUTTON_TITLE, "Hidden");
            views.addOrReplace(viewButtonTitle);

            final Label viewButtonIcon = new Label(ID_VIEW_BUTTON_ICON, "");
            views.addOrReplace(viewButtonIcon);

            final WebMarkupContainer container = new WebMarkupContainer(ID_VIEW_LIST);

            views.addOrReplace(container);
            views.setOutputMarkupId(true);

            this.setOutputMarkupId(true);

            final ListView<ComponentFactory> listView = new ListView<ComponentFactory>(ID_VIEW_ITEM, componentFactories) {

                private static final long serialVersionUID = 1L;

                @Override
                protected void populateItem(ListItem<ComponentFactory> item) {

                    final ComponentFactory componentFactory = item.getModelObject();
                    final AbstractLink link = new AjaxLink<Void>(ID_VIEW_LINK) {
                        private static final long serialVersionUID = 1L;
                        @Override
                        public void onClick(AjaxRequestTarget target) {
                            CollectionSelectorPanel linksSelectorPanel = CollectionSelectorPanel.this;
                            linksSelectorPanel.setViewHintAndBroadcast(componentFactory.getName(), target);

                            linksSelectorPanel.selectedComponentFactory = componentFactory;
                            componentHintKey.set(domainObjectBookmarkIfAny(), componentFactory.getName());
                            target.add(linksSelectorPanel, views);
                        }

                        Bookmark domainObjectBookmarkIfAny() {
                            final EntityCollectionModel entityCollectionModel = CollectionSelectorPanel.this.getModel();
                            final EntityModel entityModel = entityCollectionModel.getEntityModel();
                            return entityModel != null
                                    ? entityModel.getObjectAdapterMemento().asHintingBookmarkIfSupported()
                                            : null;
                        }

                        @Override
                        protected void onComponentTag(ComponentTag tag) {
                            super.onComponentTag(tag);
                            Buttons.fixDisabledState(this, tag);
                        }
                    };

                    IModel<String> title = nameFor(componentFactory);
                    Label viewItemTitleLabel = new Label(ID_VIEW_ITEM_TITLE, title);
                    link.add(viewItemTitleLabel);

                    Label viewItemIcon = new Label(ID_VIEW_ITEM_ICON, "");
                    link.add(viewItemIcon);

                    final boolean selected = componentFactory == CollectionSelectorPanel.this.selectedComponentFactory;
                    if (selected) {
                        viewButtonTitle.setDefaultModel(title);
                        IModel<String> cssClass = cssClassFor(componentFactory, viewButtonIcon);
                        viewButtonIcon.add(AttributeModifier.replace("class", "ViewLinkItem " + cssClass.getObject()));
                        link.setVisible(false);
                    } else {
                        IModel<String> cssClass = cssClassFor(componentFactory, viewItemIcon);
                        viewItemIcon.add(new CssClassAppender(cssClass));
                    }

                    item.add(link);
                }

                private IModel<String> cssClassFor(final ComponentFactory componentFactory, Label viewIcon) {
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

                private IModel<String> nameFor(final ComponentFactory componentFactory) {
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
            };
            container.add(listView);
            addOrReplace(views);
        }
    }


    protected void setViewHintAndBroadcast(String viewName, AjaxRequestTarget target) {
        final CollectionSelectorPanel component = CollectionSelectorPanel.this;
        final IsisSelectorEvent selectorEvent =
                new IsisSelectorEvent(component, CollectionSelectorHelper.UIHINT_EVENT_VIEW_KEY, viewName, target);
        send(getPage(), Broadcast.EXACT, selectorEvent);
    }
}



