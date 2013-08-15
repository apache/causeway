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
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.adapter.version.Version;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.objectstore.sql.CollectionMapper;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.FieldMappingLookup;
import org.apache.isis.objectstore.sql.ObjectMappingLookup;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.VersionMapping;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;
import org.apache.isis.objectstore.sql.mapping.ObjectReferenceMapping;

/**
 * used where there is a one to many association, and the elements are only
 * known to parent
 */
public class ReversedAutoAssociationMapper extends AbstractAutoMapper implements CollectionMapper {
    private static final Logger LOG = LoggerFactory.getLogger(ReversedAutoAssociationMapper.class);
    private final ObjectAssociation field;
    private final ObjectReferenceMapping idMapping;
    private final VersionMapping versionMapping;

    public ReversedAutoAssociationMapper(final String elemenType, final ObjectAssociation field, final String parameterBase, final FieldMappingLookup lookup, final ObjectMappingLookup objectLookup) {
        super(elemenType, parameterBase, lookup, objectLookup);

        this.field = field;

        idMapping = lookup.createMapping(field.getSpecification());
        versionMapping = lookup.createVersionMapping();

        setUpFieldMappers();
    }

    @Override
    public void createTables(final DatabaseConnector connection) {
        if (!connection.hasTable(table)) {
            final StringBuffer sql = new StringBuffer();
            sql.append("create table ");
            sql.append(table);
            sql.append(" (");
            idMapping.appendColumnDefinitions(sql);
            sql.append(", ");
            for (final FieldMapping mapping : fieldMappingByField.values()) {
                mapping.appendColumnDefinitions(sql);
                sql.append(",");
            }
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
    public void loadInternalCollection(final DatabaseConnector connector, final ObjectAdapter parentAdapter) {
        final ObjectAdapter collectionAdapter = field.get(parentAdapter);
        if (!collectionAdapter.canTransitionToResolving()) {
            return;
        } 
        if(LOG.isDebugEnabled()) {
            LOG.debug("loading internal collection " + field);
        }
        
        try {
            // added, since was missing (presumably an error given similarity with other 'Mapper' impls?)
            PersistorUtil.startResolving(collectionAdapter);
            
            final StringBuffer sql = new StringBuffer();
            sql.append("select ");
            idMapping.appendColumnNames(sql);
            sql.append(", ");
            sql.append(columnList(fieldMappingByField));
            sql.append(" from ");
            sql.append(table);
            sql.append(" where ");
            idMapping.appendUpdateValues(connector, sql, parentAdapter);
            
            final Results rs = connector.select(sql.toString());
            final List<ObjectAdapter> list = new ArrayList<ObjectAdapter>();
            while (rs.next()) {
                final Oid oid = idMapping.recreateOid(rs, specification);
                final ObjectAdapter element = getAdapter(specification, oid);
                loadFields(element, rs);
                if(LOG.isDebugEnabled()) {
                    LOG.debug("  element  " + element.getOid());
                }
                list.add(element);
            }
            final CollectionFacet collectionFacet = collectionAdapter.getSpecification().getFacet(CollectionFacet.class);
            collectionFacet.init(collectionAdapter, list.toArray(new ObjectAdapter[list.size()]));
            rs.close();
        } finally {
            PersistorUtil.toEndState(collectionAdapter);
        }

    }

    protected void loadFields(final ObjectAdapter object, final Results rs) {
        try {
            PersistorUtil.startResolving(object);
            for (final FieldMapping mapping : fieldMappingByField.values()) {
                mapping.initializeField(object, rs);
            }
            /*
             * for (int i = 0; i < oneToManyProperties.length; i++) { /* Need to set
             * up collection to be a ghost before we access as below
             */
            // CollectionAdapter collection = (CollectionAdapter)
            /*
             * oneToManyProperties[i].get(object); }
             */
            
            object.setVersion(versionMapping.getLock(rs));
            
        } finally {
            PersistorUtil.toEndState(object);
        }
    }

    @Override
    public void saveInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        final ObjectAdapter collection = field.get(parent);
        LOG.debug("Saving internal collection " + collection);

        deleteAllElments(connector, parent);
        reinsertElements(connector, parent, collection);
    }

    private void reinsertElements(final DatabaseConnector connector, final ObjectAdapter parent, final ObjectAdapter collection) {
        final StringBuffer sql = new StringBuffer();
        sql.append("insert into " + table + " (");
        idMapping.appendColumnNames(sql);
        sql.append(", ");
        final String columnList = columnList(fieldMappingByField);
        if (columnList.length() > 0) {
            sql.append(columnList);
            sql.append(", ");
        }
        sql.append(versionMapping.insertColumns());
        sql.append(") values (");
        idMapping.appendInsertValues(connector, sql, parent);
        sql.append(", ");

        final CollectionFacet collectionFacet = field.getFacet(CollectionFacet.class);
        for (final ObjectAdapter element : collectionFacet.iterable(collection)) {
            final StringBuffer insert = new StringBuffer(sql);
            insert.append(values(connector, element));
            final Version version = SerialNumberVersion.create(0, "", new Date());
            insert.append(versionMapping.insertValues(connector, version));
            insert.append(") ");

            connector.insert(insert.toString());
            element.setVersion(version);
        }
    }

    private void deleteAllElments(final DatabaseConnector connector, final ObjectAdapter parent) {
        final StringBuffer sql = new StringBuffer();
        sql.append("delete from ");
        sql.append(table);
        sql.append(" where ");
        idMapping.appendUpdateValues(connector, sql, parent);
        connector.update(sql.toString());
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln(field.getName(), "collection");
        debug.indent();
        debug.appendln("ID mapping", idMapping);
        debug.appendln("Version mapping", versionMapping);
        debug.unindent();
    }

}
