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

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.value.Blob;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.base._NullSafe;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.functions._Predicates;
import org.apache.causeway.core.metamodel.consent.InteractionInitiatedBy;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectBulkLoader;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;
import org.apache.causeway.core.metamodel.spec.feature.MixedIn;
import org.apache.causeway.core.metamodel.spec.feature.ObjectAssociation;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

/**
 * Represents a collection of domain objects (typically entity instances).
 *
 * @since 2.0 {@index}
 */
public class DataTable implements Serializable {
    private static final long serialVersionUID = 1L;

    // -- CONSTRUCTION

    @Getter private final @NonNull ObjectSpecification elementType;
    @Getter private final @NonNull Can<DataColumn> dataColumns;
    @Getter private @NonNull Can<DataRow> dataRows;
    @Getter private @NonNull String tableFriendlyName;

    /**
     * Returns an empty {@link DataTable} for given domain object type,
     * with all properties as columns, excluding mixed-in ones.
     * (For more control on which columns to include,
     * consider {@link #forDomainType(Class, Predicate)} or a constructor that fits.)
     * <p>
     * The table can be populated later on using {@link DataTable#setDataElements(Iterable)} or
     * {@link #setDataElementPojos(Iterable)}.
     */
    public static DataTable forDomainType(
            final @NonNull Class<?> domainType) {
        val elementType = MetaModelContext.instanceElseFail().specForTypeElseFail(domainType);
        return new DataTable(elementType);
    }

    /**
     * Returns an empty {@link DataTable} for given domain object type,
     * with all (including mixed-in) associations as columns,
     * that pass given {@code columnFilter}. If the filter is {@code null} it acts as a pass-through.
     * <p>
     * The table can be populated later on using {@link DataTable#setDataElements(Iterable)} or
     * {@link #setDataElementPojos(Iterable)}.
     */
    public static DataTable forDomainType(
            final @NonNull Class<?> domainType,
            final @Nullable Predicate<ObjectAssociation> columnFilter) {
        val elementType = MetaModelContext.instanceElseFail().specForTypeElseFail(domainType);
        return new DataTable(elementType, columnFilter);
    }

    /**
     * Returns an empty {@link DataTable} for given domain object type,
     * with all properties as columns, excluding mixed-in ones.
     * (For more control on which columns to include, consider a different constructor.)
     * <p>
     * The table can be populated later on using {@link DataTable#setDataElements(Iterable)} or
     * {@link #setDataElementPojos(Iterable)}.
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
     * The table can be populated later on using {@link DataTable#setDataElements(Iterable)} or
     * {@link #setDataElementPojos(Iterable)}.
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
     * The table can be populated later on using {@link DataTable#setDataElements(Iterable)} or
     * {@link #setDataElementPojos(Iterable)}.
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

        this.tableFriendlyName = _Strings.nonEmpty(tableFriendlyName)
                .orElse("Collection"); // fallback to a generic name

        this.elementType = elementType;

        this.dataColumns = dataColumns
                .map(assoc->new DataColumn(this, assoc));

        setDataElements(dataElements);
    }

    /**
     * Unique within application scope, can act as an id.
     */
    public String getLogicalName() {
        return getElementType().getLogicalTypeName();
    }

    /**
     * Count data rows.
     */
    public int getElementCount() {
        return dataRows.size();
    }

    public Stream<ManagedObject> streamDataElements() {
        return dataRows.stream()
            .map(DataRow::getRowElement);
    }

    // -- CONCATENATION (ADD ROWS)

    /**
     * Adds all data-elements from the other table to this table.
     */
    public DataTable addDataElementsFrom(final @Nullable DataTable otherTable) {
        if(otherTable==null) return this;
        { // sanity check
            val thisType = otherTable.getElementType().getCorrespondingClass();
            val otherType = this.getElementType().getCorrespondingClass();
            _Assert.assertEquals(thisType, otherType, ()->
                    String.format("Other tables's element-type %s must match the this table's element-type %s.",
                            otherType,
                            thisType));
        }
        if(otherTable.dataRows.isNotEmpty()) {
            this.dataRows = this.dataRows.addAll(otherTable.dataRows);
        }
        return this;
    }

    // -- POPULATE

    /**
     * Sets the data-elements of this table, which make up the rows of this table.
     */
    public DataTable setDataElements(final @Nullable Iterable<ManagedObject> dataElements) {
        this.dataRows = Can.ofIterable(dataElements)
                .map(domainObject->new DataRow(this, domainObject));
        return this;
    }
    /**
     * Sets the data-elements of this table from given pojos, that are adapted to {@link ManagedObject}(s).
     * @see #setDataElements(Iterable)
     */
    public void setDataElementPojos(final @Nullable Iterable<?> dataElementPojos) {
        var dataElements = _NullSafe.stream(dataElementPojos)
                .map(objectManager()::adapt)
                .collect(Can.toCan());
        setDataElements(dataElements);
    }

    /**
     * Populates this table from the underlying (default) persistence layer.
     * @see #setDataElements(Iterable)
     */
    public DataTable populateEntities() {
        val query = Query.allInstances(elementType.getCorrespondingClass());
        return populateEntities(query);
    }

    /**
     * Populates this table from the underlying (default) persistence layer,
     * using given {@link Query} to refine the result.
     * @see #setDataElements(Iterable)
     */
    public DataTable populateEntities(final Query<?> query) {
        { // sanity check
            val requestType = query.getResultType();
            val resultType = getElementType().getCorrespondingClass();
            _Assert.assertEquals(requestType, resultType, ()->
                    String.format("Query's result-type %s must match the table's element-type %s.",
                            requestType,
                            resultType));
        }
        val queryRequest = ObjectBulkLoader.Request.of(getElementType(), query);
        val allMatching = getElementType().getObjectManager().queryObjects(queryRequest);
        return setDataElements(allMatching);
    }

    // -- TRAVERSAL

    public static interface CellVisitor {
        default void onRowEnter(final DataRow row) {};
        default void onRowLeave(final DataRow row) {};
        void onCell(DataColumn column, Can<ManagedObject> cellValues);
    }

    public DataTable visit(final CellVisitor visitor) {
        return visit(visitor, _Predicates.alwaysTrue());
    }
    public DataTable visit(final CellVisitor visitor, final Predicate<DataColumn> columnFilter) {
        var columnsOfInterest = getDataColumns().filter(columnFilter);
        if(columnsOfInterest.isNotEmpty()) {
            getDataRows().forEach(row->{
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

    /**
     *
     * Typical use-case:<br>
     * <pre>{@code
     * @Inject CollectionContentsAsExcelExporter excelExporter;
     *
     * Blob exportToExcel(List&lt;MyDomainObject&gt; myDomainObjects) {
     *     var dataTable = DataTable.forDomainType(MyDomainObject.class);
     *     dataTable.setDataElementPojos(myDomainObjects);
     *     return dataTable.exportToBlob(excelExporter);
     * }
     * }</pre>
     */
    public Blob exportToBlob(final CollectionContentsExporter exporter) {
        return exporter.exportToBlob(this, tableFriendlyName);
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

        private SerializationProxy(final DataTable dataTable) {
            this.elementTypeClass = dataTable.getElementType().getCorrespondingClass();
            this.rowElementBookmarks = dataTable.streamDataElements()
                    .map(ManagedObject::getBookmarkElseFail)
                    .collect(Can.toCan());
            this.tableFriendlyName = dataTable.getTableFriendlyName();
        }

        private Object readResolve() {
            var objectManager = MetaModelContext.instanceElseFail().getObjectManager();
            var dataTable = DataTable.forDomainType(elementTypeClass);
            var rowElements = rowElementBookmarks.map(objectManager::loadObjectElseFail);
            dataTable.setDataElements(rowElements);
            dataTable.tableFriendlyName = tableFriendlyName;
            return dataTable;
        }
    }

    // -- HELPER

    private ObjectManager objectManager() {
        return getElementType().getObjectManager();
    }

}
