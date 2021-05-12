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

package org.apache.isis.viewer.wicket.ui.components.collection;

import java.io.Serializable;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.runtime.memento.ObjectMemento;
import org.apache.isis.viewer.wicket.model.common.OnSelectionHandler;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.isis.viewer.wicket.model.models.ToggledMementosProvider;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelUtil;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.components.widgets.checkbox.ContainedToggleboxPanel;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Panel for rendering entity collection; analogous to (any concrete subclass
 * of) {@link ScalarPanelAbstract}.
 */
public class CollectionPanel
extends PanelAbstract<List<ManagedObject>, EntityCollectionModelParented>
implements CollectionSelectorProvider, BulkActionsProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_FEEDBACK = "feedback";

    private Component collectionContents;

    private Label label;

    public CollectionPanel(
            final String id,
            final EntityCollectionModelParented collectionModel) {
        super(id, collectionModel);

        val associatedActions = collectionModel.getAssociatedActions();

        val toggledMementosProvider =
                new MyToggledMementosProvider(collectionModel, this, this);

        val entityActionLinks = LinkAndLabelUtil
                .asActionLinksForAdditionalLinksPanel(
                        collectionModel.getEntityModel(),
                        associatedActions.stream(),
                        null,
                        toggledMementosProvider)
                .collect(Can.toCan());

        collectionModel.setLinkAndLabels(entityActionLinks);

    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {
        collectionContents = getComponentFactoryRegistry().addOrReplaceComponent(this, ComponentType.COLLECTION_CONTENTS, getModel());

        addOrReplace(new NotificationPanel(ID_FEEDBACK, collectionContents, new ComponentFeedbackMessageFilter(collectionContents)));

        setOutputMarkupId(true);
    }

    public Label createLabel(final String id, final String collectionName) {
        this.label = new Label(id, collectionName);
        label.setOutputMarkupId(true);
        return this.label;
    }

    // -- SelectorDropdownPanel (impl)

    private CollectionSelectorPanel selectorDropdownPanel;

    @Override
    public CollectionSelectorPanel getSelectorDropdownPanel() {
        return selectorDropdownPanel;
    }
    public void setSelectorDropdownPanel(CollectionSelectorPanel selectorDropdownPanel) {
        this.selectorDropdownPanel = selectorDropdownPanel;
    }


    // -- BulkActionsProvider
    ObjectAdapterToggleboxColumn toggleboxColumn;

    @Override
    public ObjectAdapterToggleboxColumn getToggleboxColumn() {

        if(toggleboxColumn == null) {
            val entityCollectionModel = getModel();

            val associatedActions = entityCollectionModel.getActionsWithChoicesFrom();
            if(associatedActions.isEmpty()) {
                return null;
            }

            toggleboxColumn = new ObjectAdapterToggleboxColumn(super.getCommonContext());

            val handler = new OnSelectionHandler() {

                private static final long serialVersionUID = 1L;

                @Override
                public void onSelected(
                        final Component context,
                        final ManagedObject selectedAdapter,
                        final AjaxRequestTarget ajaxRequestTarget) {

                    val togglePanel = (ContainedToggleboxPanel) context;

                    val isSelected = getModel().toggleSelectionOn(selectedAdapter);
                    togglePanel.setModel(isSelected); // sync the checkbox's model
                }

            };
            toggleboxColumn.setOnSelectionHandler(handler);
        }

        return toggleboxColumn;
    }


    @Override
    public void configureBulkActions(final ObjectAdapterToggleboxColumn toggleboxColumn) {
    }

    private static class MyToggledMementosProvider implements ToggledMementosProvider, Serializable {
        private static final long serialVersionUID = 1L;
        private final EntityCollectionModel collectionModel;
        private final BulkActionsProvider bulkActionsProvider;
        private final CollectionPanel collectionPanel;

        MyToggledMementosProvider(
                final EntityCollectionModel collectionModel,
                final BulkActionsProvider bulkActionsProvider,
                final CollectionPanel collectionPanel) {
            this.collectionModel = collectionModel;
            this.bulkActionsProvider = bulkActionsProvider;
            this.collectionPanel = collectionPanel;
        }

        @Override
        public Can<ObjectMemento> getToggles() {
            return collectionModel.getToggleMementosList();
        }

        @Override
        public void clearToggles(final AjaxRequestTarget target) {
            collectionModel.clearToggleMementosList();

            final ObjectAdapterToggleboxColumn toggleboxColumn = bulkActionsProvider.getToggleboxColumn();
            if(toggleboxColumn != null) {
                toggleboxColumn.clearToggles();
                target.add(collectionPanel);
            }
        }
    }




}
