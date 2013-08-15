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

package org.apache.isis.objectstore.sql.auto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.objectstore.sql.CollectionMapper;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.FieldMappingLookup;
import org.apache.isis.objectstore.sql.IdMapping;
import org.apache.isis.objectstore.sql.IdMappingAbstract;
import org.apache.isis.objectstore.sql.ObjectMapping;
import org.apache.isis.objectstore.sql.ObjectMappingLookup;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.Sql;
import org.apache.isis.objectstore.sql.VersionMapping;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;
import org.apache.isis.objectstore.sql.mapping.ObjectReferenceMapping;

/**
 * Stores 1-to-many collections by creating a foreign-key column in the table for the incoming objectAssociation class.
 * This assumes this the class is only ever in 1 collection parent.
 * 
 * @version $Rev$ $Date$
 */
public class ForeignKeyCollectionMapper extends AbstractAutoMapper implements CollectionMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ForeignKeyCollectionMapper.class);
    private final ObjectAssociation field;
    private final IdMapping idMapping;
    private final VersionMapping versionMapping;
    private final ObjectReferenceMapping foreignKeyMapping;
    private String foreignKeyName;
    private String columnName;
    private final ObjectMappingLookup objectMapperLookup2;

    private ObjectMapping originalMapping = null;

    public ForeignKeyCollectionMapper(final ObjectAssociation objectAssociation, final String parameterBase,
        final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup) {
        super(objectAssociation.getSpecification().getFullIdentifier(), parameterBase, lookup, objectMapperLookup);

        this.field = objectAssociation;

        objectMapperLookup2 = objectMapperLookup;

        idMapping = lookup.createIdMapping();
        versionMapping = lookup.createVersionMapping();

        setColumnName(determineColumnName(objectAssociation));
        foreignKeyName = Sql.sqlName("fk_" + getColumnName());

        foreignKeyName = Sql.identifier(foreignKeyName);
        foreignKeyMapping = lookup.createMapping(columnName, specification);
    }

    protected ForeignKeyCollectionMapper(final FieldMappingLookup lookup, final AbstractAutoMapper abstractAutoMapper,
        final ObjectAssociation field) {
        super(lookup, abstractAutoMapper, field.getSpecification().getFullIdentifier());

        this.field = field;
        objectMapperLookup2 = null;

        idMapping = lookup.createIdMapping();
        versionMapping = lookup.createVersionMapping();

        setColumnName(determineColumnName(field));
        foreignKeyName = Sql.sqlName("fk_" + getColumnName());

        foreignKeyName = Sql.identifier(foreignKeyName);
        foreignKeyMapping = lookup.createMapping(columnName, specification);
    }

    protected String determineColumnName(final ObjectAssociation objectAssociation) {
        return objectAssociation.getSpecification().getShortIdentifier();
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(final String columnName) {
        this.columnName = columnName;
    }

    protected VersionMapping getVersionMapping() {
        return versionMapping;
    }

    protected ObjectReferenceMapping getForeignKeyMapping() {
        return foreignKeyMapping;
    }

    protected String getForeignKeyName() {
        return foreignKeyName;
    }

    @Override
    public void startup(final DatabaseConnector connector) {
        if (originalMapping == null) {
            originalMapping = objectMappingLookup.getMapping(specification, null);
        }
        originalMapping.startup(connector, objectMapperLookup2);
        super.startup(connector);
    }

    @Override
    public boolean needsTables(final DatabaseConnector connection) {
        return !connection.hasColumn(table, foreignKeyName);
    }

    @Override
    public void createTables(final DatabaseConnector connection) {
        if (connection.hasTable(table)) {
            final StringBuffer sql = new StringBuffer();
            sql.append("alter table ");
            sql.append(table);
            sql.append(" add ");
            appendColumnDefinitions(sql);
            connection.update(sql.toString());
        } else {
            final StringBuffer sql = new StringBuffer();
            sql.append("create table ");
            sql.append(table);
            sql.append(" (");
            idMapping.appendCreateColumnDefinitions(sql);
            sql.append(", ");

            appendColumnDefinitions(sql);

            // for (final FieldMapping mapping : fieldMappings) {
            // mapping.appendColumnDefinitions(sql);
            // sql.append(",");
            // }
            // sql.append(versionMapping.appendColumnDefinitions());
            sql.append(")");
            connection.update(sql.toString());
        }
    }

    public IdMappingAbstract getIdMapping() {
        return idMapping;
    }

    protected void appendCollectionUpdateColumnsToNull(final StringBuffer sql) {
        sql.append(foreignKeyName + "=NULL ");
    }

    protected void appendCollectionWhereValues(final DatabaseConnector connector, final ObjectAdapter parent,
        final StringBuffer sql) {
        foreignKeyMapping.appendUpdateValues(connector, sql, parent);
    }

    protected void appendCollectionUpdateValues(final DatabaseConnector connector, final ObjectAdapter parent,
        final StringBuffer sql) {
        appendCollectionWhereValues(connector, parent, sql);
    }

    protected void appendColumnDefinitions(final StringBuffer sql) {
        foreignKeyMapping.appendColumnDefinitions(sql);
    }

    @Override
    public void loadInternalCollection(final DatabaseConnector connector, final ObjectAdapter parentAdapter) {

        final ObjectAdapter collectionAdapter = field.get(parentAdapter);
        if (!collectionAdapter.canTransitionToResolving()) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("loading internal collection " + field);
        }
        final List<ObjectAdapter> list = new ArrayList<ObjectAdapter>();
        try {
            PersistorUtil.startResolving(collectionAdapter);

            loadCollectionIntoList(connector, parentAdapter, table, specification, getIdMapping(), fieldMappingByField,
                versionMapping, list);

            final CollectionFacet collectionFacet =
                collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
            collectionFacet.init(collectionAdapter, list.toArray(new ObjectAdapter[list.size()]));

        } finally {
            PersistorUtil.toEndState(collectionAdapter);
        }

        // TODO: Need to finalise this behaviour. At the moment, all
        // collections will get infinitely resolved. I
        // don't think this is desirable.
        for (final ObjectAdapter field : list) {
            // final ObjectMapping mapping =
            // objectMappingLookup.getMapping(field, connector);
            if (field.getSpecification().isOfType(parentAdapter.getSpecification())) {
                loadInternalCollection(connector, field);
            }
        }
    }

    protected void loadCollectionIntoList(final DatabaseConnector connector, final ObjectAdapter parent,
        final String table, final ObjectSpecification specification, final IdMappingAbstract idMappingAbstract,
        final Map<ObjectAssociation, FieldMapping> fieldMappingByField, final VersionMapping versionMapping,
        final List<ObjectAdapter> list) {

        final StringBuffer sql = new StringBuffer();
        sql.append("select ");
        idMappingAbstract.appendColumnNames(sql);

        sql.append(", ");
        final String columnList = columnList(fieldMappingByField);
        if (columnList.length() > 0) {
            sql.append(columnList);
            sql.append(", ");
        }
        sql.append(versionMapping.appendColumnNames());
        sql.append(" from ");
        sql.append(table);
        sql.append(" where ");
        appendCollectionWhereValues(connector, parent, sql);

        final Results rs = connector.select(sql.toString());
        while (rs.next()) {
            final Oid oid = idMappingAbstract.recreateOid(rs, specification);
            final ObjectAdapter element = getAdapter(specification, oid);
            loadFields(element, rs, fieldMappingByField);
            LOG.debug("  element  " + element.getOid());
            list.add(element);
        }
        rs.close();
    }

    protected void loadFields(final ObjectAdapter adapter, final Results rs,
        final Map<ObjectAssociation, FieldMapping> fieldMappingByField) {
        if (!adapter.canTransitionToResolving()) {
            return;
        }

        try {
            PersistorUtil.startResolving(adapter);
            for (final FieldMapping mapping : fieldMappingByField.values()) {
                mapping.initializeField(adapter, rs);
            }
            adapter.setVersion(versionMapping.getLock(rs));
        } finally {
            PersistorUtil.toEndState(adapter);
        }
    }

    /**
     * Override this in the Polymorphic case to return just the elements that are appropriate for the subclass currently
     * being handled.
     * 
     * @param collection
     * @return those elements that ought to be used.
     */
    protected Iterator<ObjectAdapter> getElementsForCollectionAsIterator(final ObjectAdapter collection) {
        final CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
        final Iterable<ObjectAdapter> elements = collectionFacet.iterable(collection);
        return elements.iterator();
    }

    @Override
    public void saveInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        final ObjectAdapter collection = field.get(parent);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Saving internal collection " + collection);
        }

        final Iterator<ObjectAdapter> elements = getElementsForCollectionAsIterator(collection);

        // TODO What is needed to allow a collection update (add/remove) to mark
        // the collection as dirty?
        // checkIfDirty(collection);

        if (elements.hasNext() == false) {
            return;
        }

        clearCollectionParent(connector, parent);

        resetCollectionParent(connector, parent, elements);
    }

    protected void clearCollectionParent(final DatabaseConnector connector, final ObjectAdapter parent) {
        // Delete collection parent
        final StringBuffer sql = new StringBuffer();
        sql.append("update ");
        sql.append(table);
        sql.append(" set ");
        appendCollectionUpdateColumnsToNull(sql);
        sql.append(" where ");
        appendCollectionWhereValues(connector, parent, sql);
        connector.update(sql.toString());
    }

    protected void resetCollectionParent(final DatabaseConnector connector, final ObjectAdapter parent,
        final Iterator<ObjectAdapter> elements) {
        // Reinstall collection parent
        final StringBuffer update = new StringBuffer();
        update.append("update ");
        update.append(table);
        update.append(" set ");
        appendCollectionUpdateValues(connector, parent, update);
        update.append(" where ");

        idMapping.appendColumnNames(update);

        update.append(" IN (");

        int count = 0;
        for (final Iterator<ObjectAdapter> iterator = elements; iterator.hasNext();) {
            final ObjectAdapter element = iterator.next();

            if (count++ > 0) {
                update.append(",");
            }
            final RootOid elementOid = (RootOid) element.getOid();
            idMapping.appendObjectId(connector, update, elementOid);
        }
        update.append(")");
        if (count > 0) {
            connector.insert(update.toString());
        }
    }

    protected void checkIfDirty(final ObjectAdapter collection) {
        // Test: is dirty?
        final ObjectSpecification collectionSpecification = collection.getSpecification();
        if (collectionSpecification.isDirty(collection)) {
            LOG.debug(collection.getOid() + " is dirty");
        } else {
            LOG.debug(collection.getOid() + " is clean");
        }

        final CollectionFacet collectionFacetD = collection.getSpecification().getFacet(CollectionFacet.class);
        for (final ObjectAdapter element : collectionFacetD.iterable(collection)) {
            if (collectionSpecification.isDirty(element)) {
                LOG.debug(element.getOid() + " is dirty");
            } else {
                LOG.debug(element.getOid() + " is clean");
            }
        }
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln(field.getName(), "collection");
        debug.indent();
        debug.appendln("Foreign key name", foreignKeyName);
        debug.appendln("Foreign key mapping", foreignKeyMapping);
        debug.appendln("ID mapping", idMapping);
        debug.appendln("Version mapping", versionMapping);
        debug.appendln("Original mapping", originalMapping);
        debug.unindent();
    }

}
