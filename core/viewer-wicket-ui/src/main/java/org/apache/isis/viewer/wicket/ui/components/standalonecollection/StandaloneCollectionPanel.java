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

package org.apache.isis.viewer.wicket.ui.components.standalonecollection;

import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.common.OnSelectionHandler;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.ActionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.ui.ComponentFactory;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.app.registry.ComponentFactoryRegistry;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.AdditionalLinksPanel;
import org.apache.isis.viewer.wicket.ui.components.actionprompt.ActionPromptModalWindow;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsHelper;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsLinkFactory;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorHelper;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.isis.viewer.wicket.ui.util.Components;
import org.apache.isis.viewer.wicket.ui.util.CssClassAppender;

public class StandaloneCollectionPanel extends PanelAbstract<EntityCollectionModel>
        implements CollectionCountProvider, CollectionSelectorProvider, BulkActionsProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_STANDALONE_COLLECTION = "standaloneCollection";
    private static final String ID_ACTION_NAME = "actionName";

    private static final String ID_ACTION_PROMPT_MODAL_WINDOW = "actionPromptModalWindow";
    private static final String ID_ADDITIONAL_LINKS = "additionalLinks";
    private static final String ID_ADDITIONAL_LINK = "additionalLink";

    private static final String ID_SELECTOR_DROPDOWN = "selectorDropdown";

    private final ActionPromptModalWindow actionPromptModalWindow;
    private final CollectionSelectorPanel selectorDropdownPanel;
    private final BulkActionsHelper bulkActionsHelper;

    private boolean additionalLinksAdded;

    private MarkupContainer outerDiv = this;

    /**
     * note that the bulk actions components are added in {@link #configureBulkActions(org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn)}.
     */
    public StandaloneCollectionPanel(final String id, final EntityCollectionModel entityCollectionModel) {
        super(id, entityCollectionModel);

        outerDiv = new WebMarkupContainer(ID_STANDALONE_COLLECTION);

        addOrReplace(outerDiv);

        ActionModel actionModel = entityCollectionModel.getActionModelHint();
        ObjectAction action = actionModel.getActionMemento().getAction(entityCollectionModel.getSpecificationLoader());
        outerDiv.addOrReplace(new Label(StandaloneCollectionPanel.ID_ACTION_NAME, Model.of(action.getName())));

        CssClassAppender.appendCssClassTo(outerDiv,
                CssClassAppender.asCssStyle("isis-" + action.getOnType().getSpecId().asString().replace('.', '-') + "-" + action.getId()));
        CssClassAppender.appendCssClassTo(outerDiv,
                CssClassAppender.asCssStyle("isis-" + entityCollectionModel.getTypeOfSpecification().getSpecId().asString().replace('.','-')));

        // action prompt
        this.actionPromptModalWindow = ActionPromptModalWindow.newModalWindow(ID_ACTION_PROMPT_MODAL_WINDOW);
        outerDiv.addOrReplace(actionPromptModalWindow);

        // selector
        final CollectionSelectorHelper selectorHelper = new CollectionSelectorHelper(entityCollectionModel, getComponentFactoryRegistry());

        final List<ComponentFactory> componentFactories = selectorHelper.getComponentFactories();

        if (componentFactories.size() <= 1) {
            Components.permanentlyHide(outerDiv, ID_SELECTOR_DROPDOWN);
            this.selectorDropdownPanel = null;
        } else {
            CollectionSelectorPanel selectorDropdownPanel = new CollectionSelectorPanel(ID_SELECTOR_DROPDOWN, entityCollectionModel);

            final Model<ComponentFactory> componentFactoryModel = new Model<>();

            final String selected = selectorHelper.honourViewHintElseDefault(selectorDropdownPanel);
            ComponentFactory selectedComponentFactory = selectorHelper.find(selected);

            componentFactoryModel.setObject(selectedComponentFactory);

            outerDiv.setOutputMarkupId(true);
            outerDiv.addOrReplace(selectorDropdownPanel);

            this.selectorDropdownPanel = selectorDropdownPanel;
        }

        final ComponentFactoryRegistry componentFactoryRegistry = getComponentFactoryRegistry();
        componentFactoryRegistry.addOrReplaceComponent(outerDiv, ComponentType.COLLECTION_CONTENTS, entityCollectionModel);

        bulkActionsHelper = new BulkActionsHelper(entityCollectionModel);
    }


    //region > BulkActionsProvider

    ObjectAdapterToggleboxColumn toggleboxColumn;

    @Override
    public ObjectAdapterToggleboxColumn getToggleboxColumn() {

        if (toggleboxColumn == null) {
            final List<ObjectAction> bulkActions = bulkActionsHelper.getBulkActions(getIsisSessionFactory());

            final EntityCollectionModel entityCollectionModel = getModel();
            if(bulkActions.isEmpty() || entityCollectionModel.isParented()) {
                return null;
            }

            toggleboxColumn = new ObjectAdapterToggleboxColumn();
            final OnSelectionHandler handler = new OnSelectionHandler() {

                private static final long serialVersionUID = 1L;

                @Override
                public void onSelected(
                        final Component context,
                        final ObjectAdapter selectedAdapter,
                        final AjaxRequestTarget ajaxRequestTarget) {
                    getModel().toggleSelectionOn(selectedAdapter);
                }

            };
            toggleboxColumn.setOnSelectionHandler(handler);
        }

        return toggleboxColumn;
    }

    @Override
    public void configureBulkActions(final ObjectAdapterToggleboxColumn toggleboxColumn) {

        if(additionalLinksAdded) {
            return;
        }
        final BulkActionsLinkFactory linkFactory =
                new BulkActionsLinkFactory(getModel(), toggleboxColumn);

        final List<ObjectAction> bulkActions = bulkActionsHelper.getBulkActions(getIsisSessionFactory());

        List<LinkAndLabel> linkAndLabels = Lists.transform(bulkActions, new Function<ObjectAction, LinkAndLabel>(){
            @Override
            public LinkAndLabel apply(ObjectAction objectAction) {
                return linkFactory.newLink(objectAction, ID_ADDITIONAL_LINK, null);
            }
        });

        AdditionalLinksPanel.addAdditionalLinks(
                outerDiv, ID_ADDITIONAL_LINKS, linkAndLabels, AdditionalLinksPanel.Style.INLINE_LIST);
        additionalLinksAdded = true;

    }

    //endregion




    //region > CollectionSelectorProvider

    @Override
    public CollectionSelectorPanel getSelectorDropdownPanel() {
        return selectorDropdownPanel;
    }

    //endregion

    //region > CollectionCountProvider

    @Override
    public Integer getCount() {
        final EntityCollectionModel model = getModel();
        return model.getCount();
    }

    //endregion

}
