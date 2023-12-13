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
package org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.model.Model;

import org.apache.causeway.commons.internal.collections._Lists;
import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.tabular.interactive.DataTableInteractive;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel;
import org.apache.causeway.viewer.wicket.model.models.EntityCollectionModel.Variant;
import org.apache.causeway.viewer.wicket.ui.components.collection.bulk.MultiselectToggleProvider;
import org.apache.causeway.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ColumnAbbreviationOptions;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.GenericColumn;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.PluralColumn;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.PluralColumn.RenderOptions;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.SingularColumn;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.TitleColumn;
import org.apache.causeway.viewer.wicket.ui.components.collectioncontents.ajaxtable.columns.ToggleboxColumn;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;

import lombok.val;

/**
 * {@link PanelAbstract Panel} that represents a {@link EntityCollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
public class CollectionContentsAsAjaxTablePanel
extends PanelAbstract<DataTableInteractive, EntityCollectionModel>
implements CollectionCountProvider {

    private static final long serialVersionUID = 1L;
    private static final String ID_TABLE = "table";

    public CollectionContentsAsAjaxTablePanel(final String id, final EntityCollectionModel model) {
        super(id, model);
    }

    @Override
    public Integer getCount() {
        return getModel().getDataTableModel().getElementCount();
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        buildGui();
    }

    @Override
    protected void onModelChanged() {
        //buildGui();
    }

    // -- HELPER

    private EntityCollectionModel entityCollectionModel() {
        return getModel();
    }

    private void buildGui() {

        final List<GenericColumn> columns = _Lists.newArrayList();

        // multi select check boxes
        final MultiselectToggleProvider multiselectToggleProvider = getMultiselectToggleProvider();
        final ToggleboxColumn toggleboxColumn = multiselectToggleProvider != null
            ? multiselectToggleProvider.getToggleboxColumn()
            : null;

        val collectionModel = entityCollectionModel();

        // first create property columns, so we know how many columns there are
        addPropertyColumnsIfRequired(columns);
        // prepend title column, which may have distinct rendering hints,
        // based on whether there are any property columns or not
        prependTitleColumn(
                columns,
                collectionModel.getVariant(),
                getWicketViewerSettings());

        // prepend togglebox column (left most), if enabled
        if(toggleboxColumn != null) {
            columns.add(0, toggleboxColumn);
        }

        val dataProvider = new CollectionContentsSortableDataProvider(collectionModel);
        val dataTable = new CausewayAjaxDataTable(
                ID_TABLE, columns, dataProvider, collectionModel.getPageSize(), toggleboxColumn);
        addOrReplace(dataTable);
    }

    private MultiselectToggleProvider getMultiselectToggleProvider() {
        Component component = this;
        while(component != null) {
            if(component instanceof MultiselectToggleProvider) {
                return (MultiselectToggleProvider) component;
            }
            component = component.getParent();
        }
        return null;
    }

    private void prependTitleColumn(
            final List<GenericColumn> columns,
            final Variant variant,
            final Wicket wktConfig) {

        val contextBookmark = entityCollectionModel().getParentObject().getBookmark()
                .orElse(null);

        final int maxColumnTitleLength = getModel().getVariant().isParented()
                    ? wktConfig.getMaxTitleLengthInParentedTables()
                    : wktConfig.getMaxTitleLengthInStandaloneTables();

        val opts = ColumnAbbreviationOptions.builder()
            .maxElementTitleLength(columns.size()==0
                            ? wktConfig.getMaxTitleLengthInTablesNotHavingAnyPropertyColumn()
                            : -1 /* don't override */)
            .build();

        columns.add(0, new TitleColumn(variant, contextBookmark, maxColumnTitleLength, opts));
    }

    private void addPropertyColumnsIfRequired(final List<GenericColumn> columns) {

        val collectionModel = getModel();
        val elementTypeSpec = collectionModel.getElementType();
        if(elementTypeSpec == null) {
            return;
        }

        final ManagedObject parentObject = collectionModel.getParentObject();
        val memberIdentifier = collectionModel.getIdentifier();

        // add all ordered columns to the table
        elementTypeSpec.streamAssociationsForColumnRendering(memberIdentifier, parentObject)
        .map(ObjectAssociation::getSpecialization)
        .map(spez->spez.fold(
                this::createSingularColumn,
                this::createPluralColumn))
        .forEach(columns::add);
    }

    private SingularColumn createSingularColumn(final OneToOneAssociation property) {
        val collectionModel = getModel();
        final String parentTypeName = property.getDeclaringType().getLogicalTypeName();

        return new SingularColumn(
                collectionModel.getVariant(),
                Model.of(property.getCanonicalFriendlyName()),
                property.getId(),
                property.getId(),
                parentTypeName,
                property.getCanonicalDescription());
    }

    private PluralColumn createPluralColumn(final OneToManyAssociation collection) {
        val collectionModel = getModel();
        final String parentTypeName = collection.getDeclaringType().getLogicalTypeName();

        return new PluralColumn(
                collectionModel.getVariant(),
                Model.of(collection.getCanonicalFriendlyName()),
                collection.getId(),
                collection.getId(),
                parentTypeName,
                collection.getCanonicalDescription(),
                // future work: can hook up with global config
                RenderOptions.builder().build());
    }

}
