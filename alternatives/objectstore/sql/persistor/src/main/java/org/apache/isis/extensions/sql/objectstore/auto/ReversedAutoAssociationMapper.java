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
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.ResolveState;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.sql.objectstore.CollectionMapper;
import org.apache.isis.extensions.sql.objectstore.DatabaseConnector;
import org.apache.isis.extensions.sql.objectstore.FieldMappingLookup;
import org.apache.isis.extensions.sql.objectstore.ObjectMappingLookup;
import org.apache.isis.extensions.sql.objectstore.Results;
import org.apache.isis.extensions.sql.objectstore.VersionMapping;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMapping;
import org.apache.isis.extensions.sql.objectstore.mapping.ObjectReferenceMapping;
import org.apache.isis.runtime.persistence.PersistorUtil;


/** used where there is a one to many association, and the elements are only known to parent */
public class ReversedAutoAssociationMapper extends AbstractAutoMapper implements CollectionMapper {
    private static final Logger LOG = Logger.getLogger(ReversedAutoAssociationMapper.class);
    private ObjectAssociation field;
    private final ObjectReferenceMapping idMapping;
    private final VersionMapping versionMapping;


    public ReversedAutoAssociationMapper(
            final String elemenType,
            final ObjectAssociation field,
            final String parameterBase,
            final FieldMappingLookup lookup,
            final ObjectMappingLookup objectLookup) {
        super(elemenType, parameterBase, lookup, objectLookup);

        this.field = field;

        idMapping = lookup.createMapping(field.getSpecification());
        versionMapping = lookup.createVersionMapping();  
    }
    
    public void createTables(final DatabaseConnector connection) {
        if (!connection.hasTable(table)) {
            StringBuffer sql = new StringBuffer();
            sql.append("create table ");
            sql.append(table);
            sql.append(" (");
            idMapping.appendColumnDefinitions(sql);
            sql.append(", ");
            for (FieldMapping mapping : fieldMappings) {
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


    public void loadInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        ObjectAdapter collection = (ObjectAdapter) field.get(parent);
        if (collection.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            LOG.debug("loading internal collection " + field);

            StringBuffer sql = new StringBuffer();
            sql.append("select ");
            idMapping.appendColumnNames(sql);
            sql.append(", ");
            sql.append(columnList());
            sql.append(" from ");
            sql.append(table);
            sql.append(" where ");
            idMapping.appendUpdateValues(sql, parent);
            
            Results rs = connector.select(sql.toString());
            List<ObjectAdapter> list = new ArrayList<ObjectAdapter>();
            while (rs.next()) {
                Oid oid = idMapping.recreateOid(rs,  specification);
                ObjectAdapter element = getAdapter(specification, oid);
                loadFields(element, rs);
                LOG.debug("  element  " + element.getOid());
                list.add(element);
            }
            CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
            collectionFacet.init(collection, list.toArray(new ObjectAdapter[list.size()]));
            rs.close();
            PersistorUtil.end(collection);
        }
    }
    
    protected void loadFields(final ObjectAdapter object, final Results rs) {
        PersistorUtil.start(object, ResolveState.RESOLVING);
        for (FieldMapping mapping  : fieldMappings) {
            mapping.initializeField(object, rs);
        }
/*
        for (int i = 0; i < oneToManyProperties.length; i++) {
            /*
             * Need to set up collection to be a ghost before we access as below
             */
            // CollectionAdapter collection = (CollectionAdapter)
   /*         oneToManyProperties[i].get(object);
        }
*/
        

        object.setOptimisticLock(versionMapping.getLock(rs));
        PersistorUtil.end(object);

    }

    public void saveInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        ObjectAdapter collection = field.get(parent);
        LOG.debug("Saving internal collection " + collection);
        
        deleteAllElments(connector, parent);
        reinsertElements(connector, parent, collection);
    }

    private void reinsertElements(final DatabaseConnector connector, final ObjectAdapter parent, ObjectAdapter collection) {
        StringBuffer sql = new StringBuffer();
        sql.append("insert into " + table + " (");
        idMapping.appendColumnNames(sql);
        sql.append(", ");
        sql.append(columnList());
        sql.append(", ");
        sql.append(versionMapping.insertColumns());
        sql.append(") values (" );
        idMapping.appendInsertValues(sql, parent);
        sql.append(", ");
        
        CollectionFacet collectionFacet = field.getFacet(CollectionFacet.class);
        for (ObjectAdapter element : collectionFacet.iterable(collection)) {
            StringBuffer insert = new StringBuffer(sql);
            insert.append(values(element));
            SerialNumberVersion version = new SerialNumberVersion(0, "", new Date());
            insert.append(versionMapping.insertValues(version));
            insert.append(") " );
            
            connector.insert(insert.toString());
            element.setOptimisticLock(version);
        }
    }

    private void deleteAllElments(final DatabaseConnector connector, final ObjectAdapter parent) {
        StringBuffer sql = new StringBuffer();
        sql.append("delete from ");
        sql.append(table);
        sql.append(" where ");
        idMapping.appendUpdateValues(sql, parent);
        connector.update(sql.toString());
    }

 
    public void debugData(DebugString debug) {
        debug.appendln(field.getName(), "collection");
        debug.indent();
        debug.appendln("ID mapping", idMapping);
        debug.appendln("Version mapping", versionMapping);
       debug.unindent();
    }

}
