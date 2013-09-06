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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.applib.Identifier;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.lang.MethodExtensions;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.OidMarshaller;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.adapter.version.ConcurrencyException;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.notpersisted.NotPersistedFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.ObjectSpecificationException;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.ObjectNotFoundException;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.core.runtime.persistence.query.PersistenceQueryFindByPattern;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.sql.CollectionMapper;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.Defaults;
import org.apache.isis.objectstore.sql.FieldMappingLookup;
import org.apache.isis.objectstore.sql.IdMapping;
import org.apache.isis.objectstore.sql.ObjectMapping;
import org.apache.isis.objectstore.sql.ObjectMappingLookup;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.Sql;
import org.apache.isis.objectstore.sql.SqlObjectStoreException;
import org.apache.isis.objectstore.sql.TitleMapping;
import org.apache.isis.objectstore.sql.VersionMapping;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

public class AutoMapper extends AbstractAutoMapper implements ObjectMapping, DebuggableWithTitle {

    private static final Logger LOG = LoggerFactory.getLogger(AutoMapper.class);
    private final IdMapping idMapping;
    private final VersionMapping versionMapping;
    private final TitleMapping titleMapping;
    private final boolean useVersioning;

    public AutoMapper(final String className, final String parameterBase, final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup) {
        super(className, parameterBase, lookup, objectMapperLookup);
        idMapping = lookup.createIdMapping();
        versionMapping = lookup.createVersionMapping();
        titleMapping = lookup.createTitleMapping();

        useVersioning = Defaults.useVersioning(specification.getShortIdentifier());

        setUpFieldMappers();
    }

    protected VersionMapping getVersionMapping() {
        return versionMapping;
    }

    protected IdMapping getIdMapping() {
        return idMapping;
    }

    @Override
    public void createTables(final DatabaseConnector connection) {
        if (!connection.hasTable(table)) {
            final StringBuffer sql = new StringBuffer();
            sql.append("create table ");
            sql.append(table);
            sql.append(" (");
            idMapping.appendCreateColumnDefinitions(sql);
            sql.append(", ");
            for (final FieldMapping mapping : fieldMappingByField.values()) {
                mapping.appendColumnDefinitions(sql);
                sql.append(",");
            }
            titleMapping.appendColumnDefinitions(sql);
            sql.append(", ");
            sql.append(versionMapping.appendColumnDefinitions());
            sql.append(")");
            connection.update(sql.toString());
        }
        for (int i = 0; collectionMappers != null && i < collectionMappers.length; i++) {
            if (collectionMappers[i].needsTables(connection)) {
                collectionMappers[i].createTables(connection);
            }
        }
    }

    @Override
    public void createObject(final DatabaseConnector connector, final ObjectAdapter object) {
        final int versionSequence = 1;
        final Version version = createVersion(versionSequence);

        final StringBuffer sql = new StringBuffer();
        sql.append("insert into " + table + " (");
        idMapping.appendColumnNames(sql);
        sql.append(", ");
        final String columnList = columnList(fieldMappingByField);
        if (columnList.length() > 0) {
            sql.append(columnList);
            sql.append(", ");
        }
        titleMapping.appendColumnNames(sql);
        sql.append(", ");
        sql.append(versionMapping.insertColumns());
        sql.append(") values (");
        idMapping.appendInsertValues(connector, sql, object);
        sql.append(", ");
        sql.append(values(connector, object));
        titleMapping.appendInsertValues(connector, sql, object);
        sql.append(", ");
        sql.append(versionMapping.insertValues(connector, version));
        sql.append(") ");

        connector.insert(sql.toString());
        object.setVersion(version);

        for (final CollectionMapper collectionMapper : collectionMappers) {
            collectionMapper.saveInternalCollection(connector, object);
        }
    }

    @Override
    public void destroyObject(final DatabaseConnector connector, final ObjectAdapter adapter) {
        final StringBuffer sql = new StringBuffer();
        sql.append("delete from " + table + " WHERE ");
        final RootOid oid = (RootOid) adapter.getOid();
        idMapping.appendWhereClause(connector, sql, oid);
        sql.append(" AND ");
        sql.append(versionMapping.whereClause(connector, adapter.getVersion()));
        final int updateCount = connector.update(sql.toString());
        if (updateCount == 0) {
            LOG.info("concurrency conflict object " + this + "; no deletion performed");
            throw new ConcurrencyException("", adapter.getOid());
        }
    }

    @Override
    public Vector<ObjectAdapter> getInstances(final DatabaseConnector connector, final ObjectSpecification spec, 
            final long startIndex, final long rowCount) {
        final StringBuffer sql = createSelectStatement();
        final Vector<ObjectAdapter> instances = new Vector<ObjectAdapter>();
        loadInstancesToVector(connector, spec, completeSelectStatement(sql, startIndex, rowCount), instances);
        return instances;
    }

    @Override
    public Vector<ObjectAdapter> getInstances(final DatabaseConnector connector, final ObjectSpecification spec, 
            final PersistenceQueryFindByPattern query) {
        final Vector<ObjectAdapter> instances = new Vector<ObjectAdapter>();

        final StringBuffer sql = createSelectStatement();
        int initialLength = 0;

        int foundFields = 0;
        final ObjectAdapter pattern = query.getPattern();

        // for all fields in the query.getPattern, build a SQL select clause for
        // this spec.
        final Object o = pattern.getObject();
        final ObjectSpecification patternSpec = pattern.getSpecification();
        final List<ObjectAssociation> patternAssociations = patternSpec.getAssociations(Contributed.EXCLUDED);
        for (final ObjectAssociation patternAssoc : patternAssociations) {
            final Method method;
            final Identifier identifier = patternAssoc.getIdentifier();
            final String memberName = identifier.getMemberName();
            final String methodName = memberName.substring(0, 1).toUpperCase() + memberName.substring(1);

            try {
                if (true) {
                    final ObjectAdapter field = patternAssoc.get(pattern);
                    if (field != null) {
                        final String id = patternAssoc.getId();
                        try {
                            final ObjectAssociation oa = spec.getAssociation(id);
                            final NotPersistedFacet fc = oa.getFacet(NotPersistedFacet.class);
                            if (fc != null) {
                                continue;
                            }
                        } catch (final ObjectSpecificationException e) {
                            // this is OK
                        }

                        if (foundFields == 0) {
                            sql.append(" WHERE ");
                            initialLength = sql.length();
                        }

                        if (sql.length() > initialLength) {
                            sql.append(" AND ");
                        }

                        final FieldMapping fieldMapping = fieldMappingFor(patternAssoc);
                        if (fieldMapping != null) {
                            fieldMapping.appendWhereClause(connector, sql, pattern);
                        } else {
                            // Have to use getXXX method if the fieldMapping is
                            // null..
                            final ObjectSpecification specification = patternAssoc.getSpecification();

                            method = o.getClass().getMethod("get" + methodName, (Class<?>[]) null);
                            final Object res = MethodExtensions.invoke(method, o);

                            if (specification.isValue()) {
                                // If the property (memberName) is a value type,
                                // use the value.
                                final String fieldName = Sql.sqlFieldName(identifier.getMemberName());
                                sql.append(fieldName + "=?");
                                connector.addToQueryValues(res);
                            } else {
                                throw new SqlObjectStoreException("Unhandled combination!");
                            }
                        }
                        foundFields++;
                    }
                }
            } catch (final SecurityException e) {
                LOG.debug(e.getMessage());
            } catch (final NoSuchMethodException e) {
                LOG.info("Unable to invode method: get" + methodName + " in getInstances");
                LOG.debug(e.getMessage());
            }
        }
        // if (foundFields > 0) {
        loadInstancesToVector(connector, spec, completeSelectStatement(sql, query.getStart(), query.getCount()), instances);
        // }
        return instances;
    }

    @Override
    public Vector<ObjectAdapter> getInstances(final DatabaseConnector connector, final ObjectSpecification spec, 
            final String title, final long startIndex, final long rowCount) {
        final Vector<ObjectAdapter> instances = new Vector<ObjectAdapter>();

        final StringBuffer sql = createSelectStatement();
        sql.append(" WHERE ");
        titleMapping.appendWhereClause(sql, title);
        loadInstancesToVector(connector, spec, completeSelectStatement(sql, startIndex, rowCount), instances);
        return instances;
    }

    @Override
    public ObjectAdapter getObject(final DatabaseConnector connector, final TypedOid typedOid) {
        final StringBuffer sql = createSelectStatement();
        sql.append(" WHERE ");
        idMapping.appendWhereClause(connector, sql, (RootOid) typedOid);
        final Results rs = connector.select(completeSelectStatement(sql, 0, 0));
        final ObjectSpecification objectSpec = getSpecificationLoader().lookupBySpecId(typedOid.getObjectSpecId());
        if (rs.next()) {
            return loadMappedObject(connector, objectSpec, rs);
        } else {
            throw new ObjectNotFoundException("No object with with " + typedOid + " in table " + table);
        }
    }

    @Override
    public boolean hasInstances(final DatabaseConnector connector, final ObjectSpecification cls) {
        final String statement = "select count(*) from " + table;
        final int instances = connector.count(statement);
        return instances > 0;
    }

    private StringBuffer createSelectStatement() {
        final StringBuffer sql = new StringBuffer();
        sql.append("select ");
        idMapping.appendColumnNames(sql);
        sql.append(", ");
        final String columnList = columnList(fieldMappingByField);
        if (columnList.length() > 0) {
            sql.append(columnList);
            sql.append(", ");
        }
        sql.append(versionMapping.insertColumns());
        sql.append(" from " + table);
        return sql;
    } /*
       * if (whereClause != null) { sql.append(" WHERE ");
       * sql.append(whereClause); } else if (whereClause != null) {
       * sql.append(" WHERE "); idMapping.appendWhereClause(sql, oid); }
       */

    private String completeSelectStatement(final StringBuffer sql, final long startIndex, final long rowCount) {
        sql.append(" order by ");
        idMapping.appendColumnNames(sql);

        if ((startIndex != 0) || (rowCount != 0)) {
            sql.append(" ");
            sql.append(Defaults.getLimitsClause(startIndex, rowCount));
        }

        return sql.toString();
    }

    protected void loadFields(final ObjectAdapter adapter, final Results rs) {
        PersistorUtil.startResolving(adapter);
        try {
            for (final FieldMapping mapping : fieldMappingByField.values()) {
                mapping.initializeField(adapter, rs);
            }
            /*
             * for (int i = 0; i < oneToManyProperties.length; i++) { /* Need to
             * set up collection to be a ghost before we access as below
             */
            // CollectionAdapter collection = (CollectionAdapter)
            /*
             * oneToManyProperties[i].get(object); }
             */
            adapter.setVersion(versionMapping.getLock(rs));
        } finally {
            PersistorUtil.toEndState(adapter);
        }
    }

    // KAM
    private void loadCollections(final DatabaseConnector connector, final ObjectAdapter instance) {

        for (final CollectionMapper mapper : collectionMappers) {
            mapper.loadInternalCollection(connector, instance);
        }
    }

    private void loadInstancesToVector(final DatabaseConnector connector, final ObjectSpecification cls, final String selectStatment, final Vector<ObjectAdapter> instances) {
        LOG.debug("loading instances from SQL " + table);

        try {
            final Results rs = connector.select(selectStatment);
            final int maxInstances = Defaults.getMaxInstances();
            for (int count = 0; rs.next() && count < maxInstances; count++) {
                final ObjectAdapter instance = loadMappedObject(connector, cls, rs);
                LOG.debug("  instance  " + instance);
                instances.addElement(instance);
            }
            rs.close();
        } catch (final SqlObjectStoreException e) {
            // Invalid SELECT means no object found.. don't worry about it,
            // here.
        }
    }

    private ObjectAdapter loadMappedObject(final DatabaseConnector connector, final ObjectSpecification cls, final Results rs) {
        final Oid oid = idMapping.recreateOid(rs, specification);
        final ObjectAdapter adapter = getAdapter(cls, oid);

        if (adapter.canTransitionToResolving()) {
            loadFields(adapter, rs);
            loadCollections(connector, adapter); // KAM
        }
        return adapter;
    }

    @Override
    public void resolve(final DatabaseConnector connector, final ObjectAdapter object) {
        LOG.debug("loading data from SQL " + table + " for " + object);
        final StringBuffer sql = new StringBuffer();
        sql.append("select ");
        sql.append(columnList(fieldMappingByField));
        sql.append(",");
        sql.append(versionMapping.appendColumnNames());
        sql.append(" from " + table + " WHERE ");
        final RootOid oid = (RootOid) object.getOid();
        idMapping.appendWhereClause(connector, sql, oid);

        final Results rs = connector.select(sql.toString());
        if (rs.next()) {
            loadFields(object, rs);
            rs.close();

            for (final CollectionMapper collectionMapper : collectionMappers) {
                collectionMapper.loadInternalCollection(connector, object);
            }
        } else {
            rs.close();
            throw new SqlObjectStoreException("Unable to load data from " + table + " with id " + object.getOid().enString(getOidMarshaller()));
        }
    }

    @Override
    public void resolveCollection(final DatabaseConnector connector, final ObjectAdapter object, final ObjectAssociation field) {
        if (collectionMappers.length > 0) {
            final DatabaseConnector secondConnector = connector.getConnectionPool().acquire();
            for (final CollectionMapper collectionMapper : collectionMappers) {
                collectionMapper.loadInternalCollection(secondConnector, object);
            }
            connector.getConnectionPool().release(secondConnector);
        }
    }

    @Override
    public void startup(final DatabaseConnector connector, final ObjectMappingLookup objectMapperLookup) {
        if (needsTables(connector)) {
            createTables(connector);
        }
    }

    @Override
    public void save(final DatabaseConnector connector, final ObjectAdapter adapter) {
        final Version version = adapter.getVersion();
        final long nextSequence;
        if (useVersioning) {
            nextSequence = version.getSequence() + 1;
        } else {
            nextSequence = version.getSequence();
        }

        final StringBuffer sql = new StringBuffer();
        sql.append("UPDATE " + table + " SET ");
        for (final FieldMapping mapping : fieldMappingByField.values()) {
            mapping.appendUpdateValues(connector, sql, adapter);
            sql.append(", ");
        }
        sql.append(versionMapping.updateAssigment(connector, nextSequence));
        sql.append(", ");
        titleMapping.appendUpdateAssignment(connector, sql, adapter);
        sql.append(" WHERE ");
        final RootOid oid = (RootOid) adapter.getOid();
        idMapping.appendWhereClause(connector, sql, oid);
        if (useVersioning) {
            sql.append(" AND ");
            sql.append(versionMapping.whereClause(connector, adapter.getVersion()));
        }

        final int updateCount = connector.update(sql.toString());
        if (updateCount == 0) {
            LOG.info("concurrency conflict object " + this + "; no update performed");
            throw new ConcurrencyException("", adapter.getOid());
        } else {
            adapter.setVersion(createVersion(nextSequence));
        }

        // TODO update collections - change only when needed rather than
        // reinserting from scratch
        for (final CollectionMapper collectionMapper : collectionMappers) {
            collectionMapper.saveInternalCollection(connector, adapter);
        }
    }

    @Override
    public boolean saveCollection(final DatabaseConnector connection, final ObjectAdapter parent, final String fieldName) {
        int i = 0;
        for (final String collectionFieldName : collectionMapperFields) {
            if (collectionFieldName.equals(fieldName)) {
                final CollectionMapper fieldMapper = collectionMappers[i];
                fieldMapper.saveInternalCollection(connection, parent);
                return true;
            }
            i++;
        }
        return false;
    }

    // //////////////////////////////////////////////////////////////
    // debugging, toString
    // //////////////////////////////////////////////////////////////

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln("ID mapping", idMapping);
        debug.appendln("ID mapping", versionMapping);
        debug.appendln("ID mapping", titleMapping);
        for (final FieldMapping mapping : fieldMappingByField.values()) {
            mapping.debugData(debug);
        }
        for (final CollectionMapper collectionMapper : collectionMappers) {
            collectionMapper.debugData(debug);
        }

    }

    @Override
    public String debugTitle() {
        return toString();
    }

    @Override
    public String toString() {
        return "AutoMapper [table=" + table + ",id=" + idMapping + ",noColumns=" + fieldMappingByField.size() + ",specification=" + specification.getFullIdentifier() + "]";
    }

    // //////////////////////////////////////////////////////////////
    // dependencies (from context)
    // //////////////////////////////////////////////////////////////

    protected OidMarshaller getOidMarshaller() {
        return IsisContext.getOidMarshaller();
    }

}
