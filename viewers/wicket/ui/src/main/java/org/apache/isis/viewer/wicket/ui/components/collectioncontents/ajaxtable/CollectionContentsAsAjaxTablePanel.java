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
package org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.List;
import java.util.Optional;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.Model;

import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.objectmanager.memento.ObjectMemento;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.isis.viewer.wicket.model.models.EntityCollectionModel.Variant;
import org.apache.isis.viewer.wicket.ui.components.collection.bulk.BulkActionsProvider;
import org.apache.isis.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterPropertyColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterTitleColumn;
import org.apache.isis.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ObjectAdapterToggleboxColumn;
import org.apache.isis.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTablePanel
extends PanelAbstract<List<ManagedObject>, EntityCollectionModel>
implements CollectionCountProvider {

    private static final long serialVersionUID = 1L;

    private static final String ID_TABLE = "table";

    private IsisAjaxFallbackDataTable<ManagedObject, String> dataTable;

    public CollectionContentsAsAjaxTablePanel(final String id, final EntityCollectionModel model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    private void buildGui() {

        final List<IColumn<ManagedObject, String>> columns = _Lists.newArrayList();

        // bulk actions
        final BulkActionsProvider bulkActionsProvider = getBulkActionsProvider();

        ObjectAdapterToggleboxColumn toggleboxColumn = null;
        if(bulkActionsProvider != null) {

            toggleboxColumn = bulkActionsProvider.getToggleboxColumn();
            if(toggleboxColumn != null) {
                columns.add(toggleboxColumn);
            }

        }

        val collectionModel = getModel();
        addTitleColumn(
                columns,
                collectionModel.getVariant(),
                getWicketViewerSettings().getMaxTitleLengthInParentedTables(),
                getWicketViewerSettings().getMaxTitleLengthInStandaloneTables());

        addPropertyColumnsIfRequired(columns);

        val dataProvider = new CollectionContentsSortableDataProvider(collectionModel);
        dataTable = new IsisAjaxFallbackDataTable<>(
                ID_TABLE, columns, dataProvider, collectionModel.getPageSize(), toggleboxColumn);
        addOrReplace(dataTable);

    }

    private BulkActionsProvider getBulkActionsProvider() {
        Component component = this;
        while(component != null) {
            if(component instanceof BulkActionsProvider) {
                return (BulkActionsProvider) component;
            }
            component = component.getParent();
        }
        return null;
    }


    private void addTitleColumn(
            final List<IColumn<ManagedObject, String>> columns,
            final Variant variant,
            final int maxTitleParented,
            final int maxTitleStandalone) {

        final int maxTitleLength = getModel().isParented()? maxTitleParented: maxTitleStandalone;
        columns.add(new ObjectAdapterTitleColumn(
                super.getCommonContext(), variant, maxTitleLength));
    }

    private void addPropertyColumnsIfRequired(final List<IColumn<ManagedObject, String>> columns) {

        val collectionModel = getModel();
        val elementTypeSpec = collectionModel.getTypeOfSpecification();
        if(elementTypeSpec == null) {
            return;
        }

        final Optional<ManagedObject> parentObject = collectionModel.parentedParentObject();
        val memberIdentifier = collectionModel.getIdentifier();

        // add all ordered columns to the table
        elementTypeSpec.streamPropertiesForColumnRendering(memberIdentifier, parentObject)
        .map(this::createObjectAdapterPropertyColumn)
        .forEach(columns::add);

    }

    private ObjectAdapterPropertyColumn createObjectAdapterPropertyColumn(final OneToOneAssociation property) {

        val collectionModel = getModel();

        final boolean escaped = true;

        final String parentTypeName = property.getOnType().getLogicalTypeName();

        val commonContext = super.getCommonContext();

        return new ObjectAdapterPropertyColumn(
                commonContext,
                collectionModel.getVariant(),
                Model.of(property.getCanonicalFriendlyName()),
                property.getId(),
                property.getId(),
                escaped,
                parentTypeName,
                property.getCanonicalDescription());
    }



    @Override
    protected void onModelChanged() {
        buildGui();
    }

    @Override
    public Integer getCount() {
        final EntityCollectionModel model = getModel();
        return model.getCount();
    }



}
