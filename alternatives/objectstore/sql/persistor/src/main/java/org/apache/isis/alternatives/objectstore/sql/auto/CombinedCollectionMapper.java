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


package org.apache.isis.alternatives.objectstore.sql.auto;

import java.util.ArrayList;
import java.util.List;

import org.apache.isis.alternatives.objectstore.sql.CollectionMapper;
import org.apache.isis.alternatives.objectstore.sql.DatabaseConnector;
import org.apache.isis.alternatives.objectstore.sql.FieldMappingLookup;
import org.apache.isis.alternatives.objectstore.sql.IdMapping;
import org.apache.isis.alternatives.objectstore.sql.ObjectMapping;
import org.apache.isis.alternatives.objectstore.sql.ObjectMappingLookup;
import org.apache.isis.alternatives.objectstore.sql.Results;
import org.apache.isis.alternatives.objectstore.sql.Sql;
import org.apache.isis.alternatives.objectstore.sql.VersionMapping;
import org.apache.isis.alternatives.objectstore.sql.mapping.FieldMapping;
import org.apache.isis.alternatives.objectstore.sql.mapping.ObjectReferenceMapping;
import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ResolveState;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.persistence.PersistorUtil;
import org.apache.log4j.Logger;

public class CombinedCollectionMapper extends AbstractAutoMapper implements CollectionMapper {
    private static final Logger LOG = Logger.getLogger(CombinedCollectionMapper.class);
    private final ObjectAssociation field;
    private final IdMapping idMapping;
    private final VersionMapping versionMapping;
    private ObjectReferenceMapping foreignKeyMapping;
    private String foreignKeyName;
    private String columnName;
    private ObjectMapping originalMapping;
    private final ObjectMappingLookup objectMapperLookup2;

	public CombinedCollectionMapper(
            ObjectAssociation objectAssociation,
            String parameterBase,
            FieldMappingLookup lookup, 
            ObjectMappingLookup objectMapperLookup) {
        super(objectAssociation.getSpecification().getFullIdentifier(), parameterBase, lookup, objectMapperLookup);
        this.field = objectAssociation;
		
        objectMapperLookup2 = objectMapperLookup;
        
        idMapping = lookup.createIdMapping();
        versionMapping = lookup.createVersionMapping();

        originalMapping = objectMapperLookup.getMapping(objectAssociation.getSpecification(), null);
        
        setColumnName(determineColumnName(objectAssociation));
    	foreignKeyName = Sql.sqlName("fk_" + getColumnName());
        
        foreignKeyName = Sql.identifier(foreignKeyName);
        foreignKeyMapping = lookup.createMapping(columnName, objectAssociation.getSpecification()); 
    }

	protected String determineColumnName(ObjectAssociation objectAssociation){
    	return  objectAssociation.getSpecification().getShortIdentifier();
	}
	
    public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
    
    public boolean needsTables(DatabaseConnector connection) {
        return !connection.hasColumn(table, foreignKeyName);
    }

    public void startup(DatabaseConnector connector, FieldMappingLookup lookup) {
        originalMapping.startup(connector, objectMapperLookup2);
        super.startup(connector, lookup);
    }
    
    public void createTables(DatabaseConnector connection) {
        StringBuffer sql = new StringBuffer();
        sql.append("alter table ");
        sql.append(table);
        sql.append(" add column ");
        foreignKeyMapping.appendColumnDefinitions(sql); 
        connection.update(sql.toString());
    }

    public void loadInternalCollection(DatabaseConnector connector, ObjectAdapter parent) {
        ObjectAdapter collection = (ObjectAdapter) field.get(parent);
        if (collection.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            LOG.debug("loading internal collection " + field);
            PersistorUtil.start(collection, ResolveState.RESOLVING);

            StringBuffer sql = new StringBuffer();
            sql.append("select ");
            idMapping.appendColumnNames(sql);
            
            sql.append(", ");
            String columnList = columnList();
            if (columnList.length() > 0){
            	sql.append(columnList);
            	sql.append(", ");
            }
            sql.append(versionMapping.appendSelectColumns());
            sql.append(" from ");
            sql.append(table);
            sql.append(" where ");
            foreignKeyMapping.appendUpdateValues(connector, sql, parent);
            
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
        if (object.getResolveState().canChangeTo(ResolveState.RESOLVING)) {
            PersistorUtil.start(object, ResolveState.RESOLVING);
            for (FieldMapping mapping  : fieldMappings) {
                mapping.initializeField(object, rs);
            }
            object.setOptimisticLock(versionMapping.getLock(rs));
            PersistorUtil.end(object);
        }
    }

    public void saveInternalCollection(DatabaseConnector connector, ObjectAdapter parent) {
        ObjectAdapter collection = field.get(parent);
        LOG.debug("Saving internal collection " + collection);
        
        StringBuffer sql = new StringBuffer();
        sql.append("update ");
        sql.append(table);
        sql.append(" set ");
        sql.append(foreignKeyName);
        sql.append(" = NULL where ");
        foreignKeyMapping.appendUpdateValues(connector, sql, parent);
        connector.update(sql.toString()); 
         
        sql = new StringBuffer();
        sql.append("update ");
        sql.append(table);
        sql.append(" set ");
        
        CollectionFacet collectionFacet = collection.getSpecification().getFacet(CollectionFacet.class);
        for (ObjectAdapter element : collectionFacet.iterable(collection)) {
            StringBuffer update = new StringBuffer(sql);
            foreignKeyMapping.appendUpdateValues(connector, update, parent);
            update.append(" where ");
            idMapping.appendWhereClause(connector, update, element.getOid());
            connector.insert(update.toString());
        }
    }
    
    public void debugData(DebugString debug) {
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


