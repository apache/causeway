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

import org.apache.wicket.Component;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.commons.collections.Can;
import org.apache.isis.core.metamodel.interactions.managed.nonscalar.DataTableModel;
import org.apache.isis.viewer.common.model.components.ComponentType;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModelParented;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelUtil;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Panel for rendering entity collection; analogous to (any concrete subclass
 * of) {@link ScalarPanelAbstract}.
 */
public class CollectionPanel
extends PanelAbstract<DataTableModel, EntityCollectionModelParented>
implements
    CollectionSelectorProvider,
    BulkActionsProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_FEEDBACK = "feedback";

    private Component collectionContents;

    private Label label;

    public CollectionPanel(
            final String id,
            final EntityCollectionModelParented collectionModel) {
        super(id, collectionModel);

        val associatedActions = collectionModel.getAssociatedActions();

        val entityActionLinks = LinkAndLabelUtil
                .asActionLinksForAdditionalLinksPanel(
                        collectionModel.getEntityModel(),
                        associatedActions.stream(),
                        null)
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
    public void setSelectorDropdownPanel(final CollectionSelectorPanel selectorDropdownPanel) {
        this.selectorDropdownPanel = selectorDropdownPanel;
    }


    // -- BulkActionsProvider
    GenericToggleboxColumn toggleboxColumn;

    @Override
    public GenericToggleboxColumn getToggleboxColumn() {

        if(toggleboxColumn == null) {
            val entityCollectionModel = getModel();

            //FIXME this logic belongs to the DataTableModel
            val associatedActions = entityCollectionModel.getActionsWithChoicesFrom();
            if(associatedActions.isEmpty()) {
                return null;
            }

            toggleboxColumn = new GenericToggleboxColumn(super.getCommonContext());
        }

        return toggleboxColumn;
    }

}
