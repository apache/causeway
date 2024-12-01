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
package org.apache.causeway.core.metamodel.tabular.simple;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.annotation.Where;
import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.tabular.TabularExporter;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._Casts;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.commons.tabular.TabularModel.TabularSheet;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager.BulkLoadRequest;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.causeway.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.causeway.core.metamodel.util.Facets;

import lombok.NonNull;

/**
 * Represents a collection of domain objects (typically entity instances).
 *
 * @since 2.0 {@index}
 */
public record DataTable(
    @NonNull ObjectSpecification elementType,
    @NonNull Can<DataColumn> dataColumns,
    @NonNull Can<DataRow> dataRows,
    @NonNull String tableFriendlyName) implements Serializable {

    // -- CONSTRUCTION

    /**
     * Returns an empty {@link DataTable} for given domain object type,
     * with all properties as columns, excluding mixed-in ones.
     * (For more control on which columns to include,
     * consider {@link #forDomainType(Class, Predicate)} or a constructor that fits.)
     * <p>
     * The table can be populated later on using {@link DataTable#withDataElements(Iterable)} or
     * {@link #withDataElementPojos(Iterable)}.
     */
    public static DataTable forDomainType(
            final @NonNull Class<?> domainType) {
        var elementType = MetaModelContext.instanceElseFail().specForTypeElseFail(domainType);
        return new DataTable(elementType);
    }

    /**
     * Returns an empty {@link DataTable} for given domain object type,
     * with all (including mixed-in) associations as columns,
     * that pass given {@code columnFilter}. If the filter is {@code null} it acts as a pass-through.
     * <p>
     * The table can be populated later on using {@link DataTable#withDataElements(Iterable)} or
     * {@link #withDataElementPojos(Iterable)}.
     */
    public static DataTable forDomainType(
            final @NonNull Class<?> domainType,
            final @Nullable Predicate<ObjectAssociation> columnFilter) {
        var elementType = MetaModelContext.instanceElseFail().specForTypeElseFail(domainType);
        return new DataTable(elementType, columnFilter);
    }

    /**
     * Returns an empty {@link DataTable} for given domain object type,
     * with all properties as columns, excluding mixed-in ones.
     * (For more control on which columns to include, consider a different constructor.)
     * <p>
     * The table can be populated later on using {@link DataTable#withDataElements(Iterable)} or
     * {@link #withDataElementPojos(Iterable)}.
     */
    public DataTable(
            final @NonNull ObjectSpecification elementType) {
        this(elementType,
                elementType.getSingularName(),
                elementType
                    .streamProperties(MixedIn.EXCLUDED)
                    .collect(Can.toCan()),
                Can.empty());
    }

    /**
     * Returns an empty {@link DataTable} for given domain object type,
     * with all (including mixed-in) associations as columns,
     * that pass given {@code columnFilter}. If the filter is {@code null} it acts as a pass-through.
     * <p>
     * The table can be populated later on using {@link DataTable#withDataElements(Iterable)} or
     * {@link #withDataElementPojos(Iterable)}.
     */
    public DataTable(
            final @NonNull ObjectSpecification elementType,
            final @Nullable Predicate<ObjectAssociation> columnFilter) {
        this(elementType,
                elementType.getSingularName(),
                elementType
                    .streamAssociations(MixedIn.INCLUDED)
                    .filter(Optional.ofNullable(columnFilter).orElseGet(_Predicates::alwaysTrue))
                    .collect(Can.toCan()),
                Can.empty());
    }

    /**
     * Returns an empty {@link DataTable} for given domain object type.
     * <p>
     * The table can be populated later on using {@link DataTable#withDataElements(Iterable)} or
     * {@link #withDataElementPojos(Iterable)}.
     */
    public DataTable(
            final @NonNull ObjectSpecification elementType,
            final @NonNull Can<? extends ObjectAssociation> dataColumns) {
        this(elementType, elementType.getSingularName(), dataColumns, Can.empty());
    }

    public DataTable(
            final @NonNull ObjectSpecification elementType,
            final @Nullable String tableFriendlyName,
            final @NonNull Can<? extends ObjectAssociation> dataColumns,
            final @NonNull Can<ManagedObject> dataElements) {
        this(elementType,
            dataColumns.map(DataColumn::new),
            Can.ofIterable(dataElements).map(DataRow::new),
            _Strings.nonEmpty(tableFriendlyName).orElse("Collection")); // fallback to a generic name
    }

    /**
     * Unique within application scope, can act as an id.
     */
    public String getLogicalName() {
        return elementType().logicalTypeName();
    }

    /**
     * Count data rows.
     */
    public int getElementCount() {
        return dataRows().size();
    }

    public Stream<ManagedObject> streamDataElements() {
        return dataRows().stream()
            .map(DataRow::rowElement);
    }

    // -- CONCATENATION (ADD ROWS)

    /**
     * Returns a new table, populated from this and the other table.
     */
    public DataTable withDataElementsFrom(final @Nullable DataTable otherTable) {
        if(otherTable==null) return this;
        { // sanity check
            var thisType = otherTable.elementType().getCorrespondingClass();
            var otherType = this.elementType().getCorrespondingClass();
            _Assert.assertEquals(thisType, otherType, ()->
                    String.format("Other tables's element-type %s must match the this table's element-type %s.",
                            otherType,
                            thisType));
        }
        if(otherTable.dataRows().isNotEmpty()) {
            var mergedDataRows = this.dataRows().addAll(otherTable.dataRows());
            return new DataTable(elementType, dataColumns, mergedDataRows, tableFriendlyName);
        }
        return this;
    }

    // -- POPULATE

    /**
     * Returns a new table instance with the data-elements, which make up the rows of the new table.
     */
    public DataTable withDataElements(final @Nullable Iterable<ManagedObject> dataElements) {
        var newDataRows = Can.ofIterable(dataElements)
            .map(domainObject->new DataRow(domainObject));
        return new DataTable(elementType, dataColumns, newDataRows, tableFriendlyName);
    }
    /**
     * Returns a new table instance with data-elements from given pojos, that are adapted to {@link ManagedObject}(s)..
     * @see #withDataElements(Iterable)
     */
    public DataTable withDataElementPojos(final @Nullable Iterable<?> dataElementPojos) {
        var dataElements = _NullSafe.stream(dataElementPojos)
                .map(objectManager()::adapt)
                .collect(Can.toCan());
        return withDataElements(dataElements);
    }

    /**
     * Returns a new table, populated from the underlying (default) persistence layer.
     * @see #withDataElements(Iterable)
     */
    public DataTable withEntities() {
        var query = Query.allInstances(elementType.getCorrespondingClass());
        return withEntities(query);
    }

    /**
     * Returns a new table, populated from the underlying (default) persistence layer,
     * using given {@link Query} to refine the result.
     * @see #withDataElements(Iterable)
     */
    public DataTable withEntities(final Query<?> query) {
        { // sanity check
            var requestType = query.getResultType();
            var resultType = elementType().getCorrespondingClass();
            _Assert.assertEquals(requestType, resultType, ()->
                    String.format("Query's result-type %s must match the table's element-type %s.",
                            requestType,
                            resultType));
        }
        var queryRequest = new BulkLoadRequest(elementType(), query);
        var allMatching = elementType().getObjectManager().queryObjects(queryRequest);
        return withDataElements(allMatching);
    }

    // -- TRAVERSAL

    public static interface CellVisitor {
        default void onRowEnter(final DataRow row) {}
        default void onRowLeave(final DataRow row) {}
        void onCell(DataColumn column, Can<ManagedObject> cellValues);
    }

    public DataTable visit(final CellVisitor visitor) {
        return visit(visitor, _Predicates.alwaysTrue());
    }
    public DataTable visit(final CellVisitor visitor, final Predicate<DataColumn> columnFilter) {
        var columnsOfInterest = dataColumns().filter(columnFilter);
        if(columnsOfInterest.isNotEmpty()) {
            dataRows().forEach(row->{
                visitor.onRowEnter(row);
                columnsOfInterest.forEach(col->{
                    visitor.onCell(col, row.getCellElements(col, InteractionInitiatedBy.PASS_THROUGH));
                });
                visitor.onRowLeave(row);
            });
        }
        return this;
    }

    // -- EXPORT

    public enum AccessMode {
        /**
         * must be authorized, with transactions, with publishing, with domain events
         */
        USER,
        /**
         * always authorized, no transactions, no publishing, no domain events;
         */
        PASS_THROUGH;
        /**
         * @see #USER
         */
        public boolean isUser() { return this==USER; }
        /**
         * @see #PASS_THROUGH
         */
        public boolean isPassThrough() { return this==PASS_THROUGH; }
    }

    public TabularSheet toTabularSheet(final AccessMode accessMode) {
        return TabularUtil.toTabularSheet(this, accessMode);
    }

    /**
     * Typical use-case:<br>
     * <pre>
     * &#64;Inject TabularExcelExporter excelExporter;
     *
     * Blob exportToBlob(List&lt;MyDomainObject&gt; myDomainObjects) {
     *     var dataTable = DataTable.forDomainType(MyDomainObject.class);
     *     dataTable.setDataElementPojos(myDomainObjects);
     *     return dataTable.exportToBlob(excelExporter, AccessMode.USER);
     * }
     * </pre>
     */
    public Blob exportToBlob(final TabularExporter exporter, final AccessMode accessMode) {
        return exporter.exportToBlob(toTabularSheet(accessMode));
    }

    // -- COLUMN FILTER FACTORIES

    public final static Predicate<ObjectAssociation> columnFilterIncluding(final @NonNull Where whereToInclude) {
        return (final ObjectAssociation assoc) ->
            !Facets.hiddenWhere(assoc)
                .map(where->where.includes(whereToInclude))
                .orElse(false);
    }

    public final static Predicate<ObjectAssociation> columnFilterExcludingMixins() {
        return _Predicates.not(ObjectAssociation::isMixedIn);
    }

    public final static Predicate<ObjectAssociation> columnFilterIncludingEnabledForSnapshot() {
        return (final ObjectAssociation assoc) -> _Casts.castTo(OneToOneAssociation.class, assoc)
                .map(prop->prop.isIncludedWithSnapshots())
                .orElse(false);
    }

    // -- SERIALIZATION PROXY

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    private static class SerializationProxy implements Serializable {
        private static final long serialVersionUID = 1L;

        private final @NonNull Class<?> elementTypeClass;
        private final @NonNull Can<Bookmark> rowElementBookmarks;
        private final @Nullable String tableFriendlyName;
        private final @NonNull Can<String> columnIds;

        private SerializationProxy(final DataTable dataTable) {
            this.elementTypeClass = dataTable.elementType().getCorrespondingClass();
            this.rowElementBookmarks = dataTable.streamDataElements()
                    .map(ManagedObject::getBookmarkElseFail)
                    .collect(Can.toCan());
            this.tableFriendlyName = dataTable.tableFriendlyName();
            this.columnIds = dataTable.dataColumns().map(DataColumn::columnId);
        }

        private Object readResolve() {
            var objectManager = MetaModelContext.instanceElseFail().getObjectManager();
            var elementType = MetaModelContext.instanceElseFail().specForTypeElseFail(elementTypeClass);
            var rowElements = rowElementBookmarks.map(objectManager::loadObjectElseFail);
            var dataTable = new DataTable(elementType,
                tableFriendlyName,
                columnIds
                        .map(columnId->elementType.getAssociationElseFail(columnId, MixedIn.INCLUDED)),
                rowElements);
            return dataTable;
        }
    }

    // -- HELPER

    private ObjectManager objectManager() {
        return elementType().getObjectManager();
    }

}
