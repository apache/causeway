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


package org.apache.isis.extensions.sql.objectstore.auto;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.isis.extensions.sql.objectstore.AbstractMapper;
import org.apache.isis.extensions.sql.objectstore.CollectionMapper;
import org.apache.isis.extensions.sql.objectstore.DatabaseConnector;
import org.apache.isis.extensions.sql.objectstore.FieldMappingLookup;
import org.apache.isis.extensions.sql.objectstore.IdMapping;
import org.apache.isis.extensions.sql.objectstore.Results;
import org.apache.isis.extensions.sql.objectstore.Sql;
import org.apache.isis.extensions.sql.objectstore.jdbc.JdbcObjectReferenceMapping;
import org.apache.isis.extensions.sql.objectstore.mapping.ObjectReferenceMapping;
import org.apache.log4j.Logger;


public class AutoCollectionMapper extends AbstractMapper implements CollectionMapper {
    private static final Logger LOG = Logger.getLogger(AutoCollectionMapper.class);
    private String tableName;
    private ObjectAssociation field;
    private ObjectReferenceMapping elementMapping;
    private IdMapping idMapping;

    public AutoCollectionMapper(
            final ObjectSpecification specification,
            final ObjectAssociation field,
            final FieldMappingLookup lookup) {
        this.field = field;

        ObjectSpecification spec = field.getFacet(TypeOfFacet.class).valueSpec();
        idMapping = lookup.createIdMapping();
        elementMapping = lookup.createMapping(spec);

        String className = specification.getShortName();
        String columnName = field.getId();
        tableName = Sql.sqlName(className) + "_" + asSqlName(columnName);
        tableName = Sql.identifier(tableName);
    }

    public void createTables(final DatabaseConnector connector) {
        if (!connector.hasTable(tableName)) {
            StringBuffer sql = new StringBuffer();
            sql.append("create table ");
            sql.append(tableName);
            sql.append(" (");

            idMapping.appendColumnDefinitions(sql);
            sql.append(", ");
            elementMapping.appendColumnDefinitions(sql);

            sql.append(")");

            connector.update(sql.toString());
        }
    }

    public void loadInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        ObjectAdapter collection = (ObjectAdapter) field.get(parent);
        if (collection.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            LOG.debug("loading internal collection " + field);
            collection.changeState(ResolveState.RESOLVING);
            
            StringBuffer sql = new StringBuffer();
            sql.append("select ");
            idMapping.appendColumnNames(sql);
            sql.append(", ");
            elementMapping.appendColumnNames(sql);
            sql.append(" from ");
            sql.append(tableName);

            Results rs = connector.select(sql.toString());
            List<ObjectAdapter> list = new ArrayList<ObjectAdapter>();
            while (rs.next()) {
                ObjectAdapter element = ((JdbcObjectReferenceMapping) elementMapping).initializeField(rs);
                LOG.debug("  element  " + element.getOid());
                list.add(element);
            }
            CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
            collectionFacet.init(collection, list.toArray(new ObjectAdapter[list.size()]));
            rs.close();
            PersistorUtil.end(collection);
        }
    }

    public boolean needsTables(final DatabaseConnector connector) {
        return !connector.hasTable(tableName);
    }

    public void saveInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        ObjectAdapter collection = (ObjectAdapter) field.get(parent);
        LOG.debug("saving internal collection " + collection);

        StringBuffer sql = new StringBuffer();
        sql.append("delete from ");
        sql.append(tableName);
        sql.append(" where ");
        idMapping.appendWhereClause(connector, sql, parent.getOid());
        connector.update(sql.toString());

        sql = new StringBuffer();
        sql.append("insert into ");
        sql.append(tableName);
        sql.append(" (");
        idMapping.appendColumnNames(sql);
        sql.append(", ");
        elementMapping.appendColumnNames(sql);
        sql.append(" ) values (");
        idMapping.appendInsertValues(connector, sql, parent);
        sql.append(", ");

        CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
        for (ObjectAdapter element : collectionFacet.iterable(collection)) {
            StringBuffer values = new StringBuffer();
            elementMapping.appendInsertValues(connector, values, element);
            connector.update(sql.toString() + values + ")");
        }
    }
     
    public void debugData(DebugString debug) {
        debug.appendln(field.getName(), "collection");
        debug.indent();
        debug.appendln("Table", tableName);
        debug.appendln("ID mapping", idMapping);
        debug.appendln("Element mapping", elementMapping);
        debug.unindent();
    }

}
