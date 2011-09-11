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

package org.apache.isis.runtimes.dflt.objectstores.sql.auto;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.CollectionMapper;
import org.apache.isis.runtimes.dflt.objectstores.sql.DatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.FieldMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.IdMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.ObjectMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.ObjectMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.Results;
import org.apache.isis.runtimes.dflt.objectstores.sql.Sql;
import org.apache.isis.runtimes.dflt.objectstores.sql.VersionMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.FieldMapping;
import org.apache.isis.runtimes.dflt.objectstores.sql.mapping.ObjectReferenceMapping;
import org.apache.isis.runtimes.dflt.runtime.persistence.PersistorUtil;

public class CombinedCollectionMapper extends AbstractAutoMapper implements CollectionMapper {
    private static final Logger LOG = Logger.getLogger(CombinedCollectionMapper.class);
    private final ObjectAssociation field;
    private final IdMapping idMapping;
    private final VersionMapping versionMapping;
    private final ObjectReferenceMapping foreignKeyMapping;
    private String foreignKeyName;
    private String columnName;
    private final ObjectMappingLookup objectMapperLookup2;

    private ObjectMapping originalMapping = null;
    private final boolean isAbstract;

    public CombinedCollectionMapper(final ObjectAssociation objectAssociation, final String parameterBase,
        final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup) {
        super(objectAssociation.getSpecification().getFullIdentifier(), parameterBase, lookup, objectMapperLookup);

        this.field = objectAssociation;

        isAbstract = field.getSpecification().isAbstract();

        objectMapperLookup2 = objectMapperLookup;

        idMapping = lookup.createIdMapping();
        versionMapping = lookup.createVersionMapping();

        setColumnName(determineColumnName(objectAssociation));
        foreignKeyName = Sql.sqlName("fk_" + getColumnName());

        foreignKeyName = Sql.identifier(foreignKeyName);
        foreignKeyMapping = lookup.createMapping(columnName, objectAssociation.getSpecification());
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

    @Override
    public boolean needsTables(final DatabaseConnector connection) {
        return isAbstract || !connection.hasColumn(table, foreignKeyName);
    }

    @Override
    public void startup(final DatabaseConnector connector, final FieldMappingLookup lookup) {
        if (originalMapping == null) {
            originalMapping = objectMapperLookup.getMapping(field.getSpecification(), null);
        }
        originalMapping.startup(connector, objectMapperLookup2);
        super.startup(connector, lookup);
    }

    @Override
    public void createTables(final DatabaseConnector connection) {
        if (isAbstract) {
            return;
        }
        final StringBuffer sql = new StringBuffer();
        sql.append("alter table ");
        sql.append(table);
        sql.append(" add ");
        foreignKeyMapping.appendColumnDefinitions(sql);
        connection.update(sql.toString());
    }

    @Override
    public void loadInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent,
        final boolean makeResolved) {

        if (isAbstract) { // hasSubClasses, too?
            // TODO: Polymorphism: loadInternalCollection must load the instance from all the possible child tables.
            LOG.debug("Is Abstract");
            return;
        }

        final ObjectAdapter collection = field.get(parent);
        if (collection.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            LOG.debug("loading internal collection " + field);
            PersistorUtil.start(collection, ResolveState.RESOLVING);

            final StringBuffer sql = new StringBuffer();
            sql.append("select ");
            idMapping.appendColumnNames(sql);

            sql.append(", ");
            final String columnList = columnList();
            if (columnList.length() > 0) {
                sql.append(columnList);
                sql.append(", ");
            }
            sql.append(versionMapping.appendSelectColumns());
            sql.append(" from ");
            sql.append(table);
            sql.append(" where ");
            foreignKeyMapping.appendUpdateValues(connector, sql, parent);

            final Results rs = connector.select(sql.toString());
            final List<ObjectAdapter> list = new ArrayList<ObjectAdapter>();
            while (rs.next()) {
                final Oid oid = idMapping.recreateOid(rs, specification);
                final ObjectAdapter element = getAdapter(specification, oid);
                loadFields(element, rs, makeResolved);
                LOG.debug("  element  " + element.getOid());
                list.add(element);
            }
            final CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
            collectionFacet.init(collection, list.toArray(new ObjectAdapter[list.size()]));
            rs.close();
            PersistorUtil.end(collection);

            // TODO: Need to finalise this behaviour. At the moment, all collections will get infinitely resolved. I
            // don't think this is desirable. Sub-collections should be left "Partially Resolved".
            if (makeResolved) {
                for (ObjectAdapter field : list) {
                    // final ObjectMapping mapping = objectMappingLookup.getMapping(field, connector);
                    if (field.getSpecification().isOfType(parent.getSpecification())) {
                        loadInternalCollection(connector, field, false);
                    }
                }
            }
        }
    }

    protected void loadFields(final ObjectAdapter object, final Results rs, final boolean makeResolved) {
        if (object.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            PersistorUtil.start(object, ResolveState.RESOLVING);
            for (final FieldMapping mapping : fieldMappings) {
                mapping.initializeField(object, rs);
            }
            object.setOptimisticLock(versionMapping.getLock(rs));
            if (makeResolved) {
                PersistorUtil.end(object);
            }
        }
    }

    @Override
    public void saveInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        final ObjectAdapter collection = field.get(parent);
        LOG.debug("Saving internal collection " + collection);

        final CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
        final Iterable<ObjectAdapter> elements = collectionFacet.iterable(collection);

        // TODO What is needed to allow a collection update (add/remove) to mark the collection as dirty?
        // checkIfDirty(collection);

        if (elements.iterator().hasNext() == false) {
            return;
        }

        if (isAbstract) {
            // TODO: Polymorphism: saveInternalCollection must save instance into the appropriate child tables.
            LOG.debug("Is Abstract");
            return;
        }

        // Delete collection parent
        // TODO: for polymorphism, must delete from all? appropriate child tables.
        final StringBuffer sql = new StringBuffer();
        sql.append("update ");
        sql.append(table);
        sql.append(" set ");
        sql.append(foreignKeyName);
        sql.append(" = NULL where ");
        foreignKeyMapping.appendUpdateValues(connector, sql, parent);
        connector.update(sql.toString());

        // Reinstall collection parent
        // TODO: : for polymorphism, must load from all appropriate child tables.
        final StringBuffer update = new StringBuffer();
        update.append("update ");
        update.append(table);
        update.append(" set ");

        foreignKeyMapping.appendUpdateValues(connector, update, parent);
        update.append(" where ");

        idMapping.appendColumnNames(update);

        update.append(" IN (");

        int count = 0;
        for (final ObjectAdapter element : elements) {
            if (count++ > 0) {
                update.append(",");
            }
            idMapping.appendObjectId(connector, update, element.getOid());
        }
        update.append(")");
        if (count > 0) {
            connector.insert(update.toString());
        }
    }

    protected void checkIfDirty(final ObjectAdapter collection) {
        // Test: is dirty?
        ObjectSpecification collectionSpecification = collection.getSpecification();
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
