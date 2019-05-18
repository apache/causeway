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

import org.apache.isis.commons.internal.collections._Lists;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.spec.feature.ObjectAction;
import org.apache.isis.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.isis.core.runtime.memento.ObjectAdapterMemento;
import org.apache.isis.viewer.wicket.model.common.OnSelectionHandler;
import org.apache.isis.viewer.wicket.model.links.LinkAndLabel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityModel;
import org.apache.isis.viewer.wicket.model.models.ToggledMementosProvider;
import org.apache.isis.viewer.wicket.ui.ComponentType;
import org.apache.isis.viewer.wicket.ui.components.actionmenu.entityactions.LinkAndLabelUtil;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorPanel;
import org.apache.isis.viewer.wicket.ui.components.collection.selector.CollectionSelectorProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.components.scalars.ScalarPanelAbstract2;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;

/**
 * Panel for rendering entity collection; analogous to (any concrete subclass
 * of) {@link ScalarPanelAbstract2}.
 */
public class CollectionPanel extends PanelAbstract<EntityCollectionModel> implements CollectionSelectorProvider,
BulkActionsProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_FEEDBACK = "feedback";

    private Component collectionContents;

    private Label label;

    private final AssociatedWithActionsHelper associatedWithActionsHelper;

    public CollectionPanel(
            final String id,
            final EntityCollectionModel collectionModel) {
        super(id, collectionModel);

        final List<LinkAndLabel> entityActionLinks = _Lists.newArrayList();

        final OneToManyAssociation otma = collectionModel.getCollectionMemento().getCollection(collectionModel.getSpecificationLoader());
        final EntityModel entityModel = collectionModel.getEntityModel();
        final ObjectAdapter adapter = entityModel.load(ConcurrencyChecking.NO_CHECK);

        final List<ObjectAction> associatedActions =
                ObjectAction.Util.findForAssociation(adapter, otma);

        associatedWithActionsHelper = new AssociatedWithActionsHelper(collectionModel);

        final ToggledMementosProvider toggledMementosProvider =
                new MyToggledMementosProvider(collectionModel, this, this);

        entityActionLinks.addAll(
                LinkAndLabelUtil
                .asActionLinksForAdditionalLinksPanel(
                        entityModel, associatedActions, null, toggledMementosProvider));

        collectionModel.addLinkAndLabels(entityActionLinks);

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
            final List<ObjectAction> associatedActions =
                    associatedWithActionsHelper.getAssociatedActions(getIsisSessionFactory());

            final EntityCollectionModel entityCollectionModel = getModel();
            if(associatedActions.isEmpty() || entityCollectionModel.isStandalone()) {
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
        public List<ObjectAdapterMemento> getToggles() {
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
