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

/**
 * 
 */
package org.apache.isis.runtimes.dflt.objectstores.sql.auto;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.DatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.FieldMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.IdMappingAbstract;
import org.apache.isis.runtimes.dflt.objectstores.sql.ObjectMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.ObjectMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.Sql;
import org.apache.isis.runtimes.dflt.objectstores.sql.VersionMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.jdbc.JdbcPolymorphicObjectReferenceMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;

/**
 * Used to map 1-to-many collections by creating, in the collection child table (which may be an interface or abstract
 * class), 2 columns per parent collection. The first column is the class type, the second is the entity ID. The columns
 * are named by combining the final part of the parent class name and the collection variable name.
 * 
 * You have a choice between this class and {@link PolymorphicForeignKeyInChildCollectionMapper}
 * 
 * @author Kevin
 */
public class PolymorphicForeignKeyInChildCollectionBaseMapper extends ForeignKeyInChildCollectionMapper {

    private static final Logger LOG = Logger.getLogger(PolymorphicForeignKeyInChildCollectionBaseMapper.class);

    private final String classColumnName;
    private final String itemIdColumnName;

    public PolymorphicForeignKeyInChildCollectionBaseMapper(final ObjectAssociation objectAssociation,
        final String parameterBase, final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup,
        final AbstractAutoMapper abstractAutoMapper, final ObjectAssociation field) {

        super(objectAssociation, parameterBase, lookup, objectMapperLookup, abstractAutoMapper, field);

        classColumnName = Sql.identifier(Sql.sqlName(getForeignKeyName() + "_cls"));
        itemIdColumnName = Sql.identifier("item_id");
    }

    @Override
    public boolean needsTables(final DatabaseConnector connection) {
        return super.needsTables(connection) || !connection.hasColumn(table, classColumnName);
    }

    @Override
    public void createTables(final DatabaseConnector connection) {
        if (super.needsTables(connection)) {
            super.createTables(connection);
        }

        if (!connection.hasColumn(table, classColumnName)) {
            addColumn(connection, classColumnName, JdbcConnector.TYPE_LONG_STRING());
            addColumn(connection, itemIdColumnName, JdbcConnector.TYPE_PK());
        }
    }

    protected void addColumn(final DatabaseConnector connection, String columnName, String columnType) {
        final StringBuffer sql = new StringBuffer();
        sql.append("alter table ");
        sql.append(table);
        sql.append(" add ");
        sql.append(columnName);
        sql.append(" ");
        sql.append(columnType);
        connection.update(sql.toString());
    }

    @Override
    protected void appendCollectionUpdateColumnsToNull(StringBuffer sql) {
        super.appendCollectionUpdateColumnsToNull(sql);
        sql.append(", " + classColumnName + "=NULL ");
    }

    @Override
    protected void appendCollectionUpdateValues(final DatabaseConnector connector, final ObjectAdapter parent,
        final StringBuffer sql) {
        super.appendCollectionUpdateValues(connector, parent, sql);
    }

    @Override
    protected void appendColumnDefinitions(final StringBuffer sql) {
        super.appendColumnDefinitions(sql);
    }

    @Override
    protected void clearCollectionParent(final DatabaseConnector connector, final ObjectAdapter parent) {
        // Delete collection parent
        final StringBuffer sql = new StringBuffer();
        sql.append("DELETE FROM ");
        sql.append(table);
        sql.append(" WHERE ");
        appendCollectionWhereValues(connector, parent, sql);
        connector.update(sql.toString());
    }

    @Override
    protected void resetCollectionParent(final DatabaseConnector connector, final ObjectAdapter parent,
        final Iterator<ObjectAdapter> elements) {
        LOG.debug("Saving polymorphic list");

        // TODO: Continue appending "insert" IDs while the element specification is the same. When it changes, commit
        // current list and start again.

        ObjectSpecification elementSpecification;
        while (elements.hasNext()) {
            ObjectAdapter thisAdapter = elements.next();
            elementSpecification = thisAdapter.getSpecification();

            // Reinstall collection parent
            final StringBuffer update = new StringBuffer();
            update.append("INSERT INTO ");
            update.append(table);
            update.append(" (");
            // list of column names
            super.getIdMapping().appendColumnNames(update);
            update.append("," + getForeignKeyName());
            update.append(", " + itemIdColumnName);
            update.append("," + classColumnName);
            update.append(") VALUES (");

            // TODO: I'm not sure about reusing the item's own Id as ID for this table..
            // PK_ID column
            getIdMapping().appendObjectId(connector, update, thisAdapter.getOid());
            update.append(",");

            // Foreign key ID column
            getForeignKeyMapping().appendInsertValues(connector, update, parent);
            update.append(",");

            // item Id column
            getIdMapping().appendObjectId(connector, update, thisAdapter.getOid());

            // Class name column
            update.append(",?)");
            connector.addToQueryValues(elementSpecification.getFullIdentifier());

            connector.insert(update.toString());
        }
    }

    @Override
    public IdMappingAbstract getIdMapping() {
        return new JdbcPolymorphicObjectReferenceMapping(itemIdColumnName);
    }

    @Override
    protected void loadCollectionIntoList(final DatabaseConnector connector, final ObjectAdapter parent,
        final boolean makeResolved, final String table, ObjectSpecification specification,
        final IdMappingAbstract idMappingAbstract, final List<FieldMapping> fieldMappings,
        final VersionMapping versionMapping, final List<ObjectAdapter> list) {
        LOG.debug("Loading polymorphic list");

        final StringBuffer sql = new StringBuffer();
        sql.append("select ");
        super.getIdMapping().appendColumnNames(sql);

        sql.append("," + getForeignKeyName());
        sql.append("," + classColumnName);
        sql.append("," + itemIdColumnName);

        sql.append(" from ");
        sql.append(table);
        sql.append(" where ");
        appendCollectionWhereValues(connector, parent, sql);

        final Results rs = connector.select(sql.toString());

        SpecificationLoader reflector = IsisContext.getSpecificationLoader();
        final JdbcPolymorphicObjectReferenceMapping idMapping =
            (JdbcPolymorphicObjectReferenceMapping) idMappingAbstract;

        while (rs.next()) {
            ObjectSpecification itemSpecification = reflector.loadSpecification(rs.getString(classColumnName));
            idMapping.setObjectSpecification(itemSpecification);

            // Load new recordSet for the actual class
            ObjectMapping itemMapper = objectMapperLookup.getMapping(itemSpecification, connector);
            final Oid oid = idMapping.recreateOid(rs, itemSpecification);
            final ObjectAdapter loadedObject = itemMapper.getObject(connector, oid, itemSpecification);

            LOG.debug("  element  " + loadedObject.getOid());

            list.add(loadedObject);
        }
        rs.close();

    }
}
