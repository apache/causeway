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
package org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.CssResourceReference;

import org.apache.causeway.core.config.CausewayConfiguration.Viewer.Wicket;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToManyAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.tabular.DataTableInteractive;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModel.Variant;
import org.apache.causeway.viewer.wicket.model.models.coll.CollectionModelParented;
import org.apache.causeway.viewer.wicket.ui.components.collection.count.CollectionCountProvider;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.ActionColumn;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.ColumnAbbreviationOptions;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.GenericColumn;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.PluralColumn;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.PluralColumn.RenderOptions;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.SingularColumn;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.TitleColumn;
import org.apache.causeway.viewer.wicket.ui.components.collection.present.ajaxtable.columns.ToggleboxColumn;
import org.apache.causeway.viewer.wicket.ui.components.table.CausewayAjaxDataTable;
import org.apache.causeway.viewer.wicket.ui.components.table.DataTableWithPagesAndFilter;
import org.apache.causeway.viewer.wicket.ui.components.table.filter.FilterToolbar;
import org.apache.causeway.viewer.wicket.ui.panels.PanelAbstract;
import org.apache.causeway.viewer.wicket.ui.util.Wkt;
import org.apache.causeway.viewer.wicket.ui.util.WktComponents;

/**
 * {@link PanelAbstract Panel} that represents a {@link CollectionModel
 * collection of entity}s rendered using {@link AjaxFallbackDefaultDataTable}.
 */
class CollectionContentsAsAjaxTablePanel
extends PanelAbstract<DataTableInteractive, CollectionModel>
implements CollectionCountProvider {

    private static final long serialVersionUID = 1L;
    private static final String ID_TABLE = "table";
    private static final String ID_TABLE_FILTER_BAR = "table-filter-bar";

    private static final CssResourceReference TABLE_CSS =
            new CssResourceReference(CollectionContentsAsAjaxTablePanel.class, "CollectionContentsAsAjaxTablePanel.css");

    public CollectionContentsAsAjaxTablePanel(final String id, final CollectionModel model) {
        super(id, model);
    }

    @Override
    public Integer getCount() {
        return getModel().getDataTableModel().getFilteredElementCount();
    }

    // supports partial page updates (AJAX)
    @Override
    protected void onConfigure() {
        super.onConfigure();
        buildGui();
        Wkt.ajaxEnable(this);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(CssHeaderItem.forReference(TABLE_CSS));
    }

    @Override
    protected void onModelChanged() {
    }

    // -- HELPER

    private CollectionModel collectionModel() {
        return getModel();
    }

    private DataTableInteractive dataTableInteractive() {
        return getModelObject();
    }

    private void buildGui() {
        var collectionModel = collectionModel();

        if(collectionModel.isHidden()) {
            WktComponents.permanentlyHide(this, ID_TABLE);
            WktComponents.permanentlyHide(this, ID_TABLE_FILTER_BAR);
            return;
        }

        var elementType = Objects.requireNonNull(collectionModel.getElementType(),
                ()->"invalid: CollectionModel without element type");

        var columns = new ArrayList<GenericColumn>();
        // first create property columns, so we know how many columns there are
        addPropertyColumnsIfRequired(columns);
        // prepend title column, which may have distinct rendering hints,
        // based on whether there are any property columns or not
        prependTitleColumn(
            elementType,
            collectionModel.getVariant(),
            getWicketViewerSettings(),
            columns);

        // last append action column
        addActionsColumnIfRequired(elementType, columns);

        var dataProvider = new CollectionContentsSortableDataProvider(collectionModel);
        var dataTable = new CausewayAjaxDataTable(
                ID_TABLE, columns, dataProvider, collectionModel.getPageSize());
        addOrReplace(dataTable);

        // prepend toggle-box column (left most), if enabled
        createToggleboxColumn(dataTable)
            .ifPresent(toggleboxColumn->columns.add(0, toggleboxColumn));

        addFilterToolbar(dataTable);
    }

    // -- MULTI SELECTION SUPPORT

    private Optional<ToggleboxColumn> createToggleboxColumn(final DataTableWithPagesAndFilter<?, ?> dataTable) {
        if(collectionModel() instanceof CollectionModelParented collModel) {
            var collMetaModel = collModel.getMetaModel();
            return collMetaModel.hasAssociatedActionsWithChoicesFromThisCollection()
                ? Optional.of(new ToggleboxColumn(collModel.getElementType(), dataTable))
                : Optional.empty();
        }
        return Optional.empty();
    }

    /**
     * If table quick search is supported, adds a search bar on top of the table component.
     * @param placeholderText
     */
    private void addFilterToolbar(
            final CausewayAjaxDataTable dataTableComponent) {
        Wkt.addIfElseHide(dataTableInteractive().isSearchSupported(),
            this, ID_TABLE_FILTER_BAR,
            id -> new FilterToolbar(id, dataTableComponent));
    }

    private void prependTitleColumn(
            final ObjectSpecification elementType,
            final Variant variant,
            final Wicket wktConfig,
            final List<GenericColumn> columns) {

        var contextBookmark = collectionModel().getParentObject().getBookmark()
            .orElse(null);

        final int maxColumnTitleLength = getModel().getVariant().isParented()
            ? wktConfig.maxTitleLengthInParentedTables()
            : wktConfig.maxTitleLengthInStandaloneTables();

        var opts = new ColumnAbbreviationOptions(columns.size()==0
            ? wktConfig.maxTitleLengthInTablesNotHavingAnyPropertyColumn()
            : -1 /* don't override */);

        columns.add(0, new TitleColumn(elementType, variant, contextBookmark, maxColumnTitleLength, opts));
    }

    private void addPropertyColumnsIfRequired(
            final List<GenericColumn> columns) {

        var collectionModel = getModel();
        var elementType = collectionModel.getElementType();
        if(elementType == null) return;

        final ManagedObject parentObject = collectionModel.getParentObject();
        var memberIdentifier = collectionModel.getIdentifier();

        // add all ordered columns to the table
        elementType.streamAssociationsForColumnRendering(memberIdentifier, parentObject)
            .map(ObjectAssociation::getSpecialization)
            .map(spez->spez.fold(
                    this::createSingularColumn,
                    this::createPluralColumn))
            .forEach(columns::add);
    }

    private SingularColumn createSingularColumn(
            final OneToOneAssociation property) {
        var collectionModel = getModel();
        final String parentTypeName = property.getDeclaringType().logicalTypeName();
        final Optional<String> sortability = property.getElementType().isComparableOrOrdered()
            ? Optional.of(property.getId())
            : Optional.empty();

        return new SingularColumn(
            collectionModel.getElementType(),
            collectionModel.getVariant(),
            Model.of(property.getCanonicalFriendlyName()),
            sortability,
            property.getId(),
            parentTypeName,
            property.getCanonicalDescription());
    }

    private PluralColumn createPluralColumn(
            final OneToManyAssociation collection) {
        var collectionModel = getModel();
        final String parentTypeName = collection.getDeclaringType().logicalTypeName();

        return new PluralColumn(
            collectionModel.getElementType(),
            collectionModel.getVariant(),
            Model.of(collection.getCanonicalFriendlyName()),
            collection.getId(),
            parentTypeName,
            collection.getCanonicalDescription(),
            // future work: can hook up with global config
            new RenderOptions(50, 5, true));
    }

    private void addActionsColumnIfRequired(
            final ObjectSpecification elementType,
            final List<GenericColumn> columns) {
        var collectionModel = getModel();
        var memberIdentifier = collectionModel.getIdentifier();
        ActionColumn.create(memberIdentifier, elementType, collectionModel.getVariant()).ifPresent(columns::add);
    }

}
