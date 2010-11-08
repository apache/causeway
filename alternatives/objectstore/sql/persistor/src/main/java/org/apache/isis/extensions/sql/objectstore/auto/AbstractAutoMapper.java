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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.config.IsisConfiguration;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.extensions.sql.objectstore.AbstractMapper;
import org.apache.isis.extensions.sql.objectstore.CollectionMapper;
import org.apache.isis.extensions.sql.objectstore.DatabaseConnector;
import org.apache.isis.extensions.sql.objectstore.FieldMappingLookup;
import org.apache.isis.extensions.sql.objectstore.ObjectMappingLookup;
import org.apache.isis.extensions.sql.objectstore.Sql;
import org.apache.isis.extensions.sql.objectstore.SqlObjectStoreException;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMapping;
import org.apache.isis.runtime.context.IsisContext;
import org.apache.isis.runtime.persistence.adaptermanager.AdapterManager;


public abstract class AbstractAutoMapper extends AbstractMapper {
    private static final Logger LOG = Logger.getLogger(AbstractAutoMapper.class);
    protected CollectionMapper collectionMappers[];
    protected boolean dbCreatesId;

    protected ObjectSpecification specification;
    protected String table;
    protected List<FieldMapping> fieldMappings = new ArrayList<FieldMapping>();
    protected Map<ObjectAssociation, FieldMapping> fieldMappingLookup = new HashMap<ObjectAssociation, FieldMapping>();
    
    protected AbstractAutoMapper(final String className, final String parameterBase, FieldMappingLookup lookup, ObjectMappingLookup objectMapperLookup) {
        specification = IsisContext.getSpecificationLoader().loadSpecification(className);
        if (specification.getPropertyList() == null || specification.getPropertyList().size() == 0) {
            throw new SqlObjectStoreException(specification.getFullName() + " has no fields: " + specification);
        }
        setUpFieldMappers(lookup, objectMapperLookup, className, parameterBase);
    }

    private void setUpFieldMappers(FieldMappingLookup lookup, ObjectMappingLookup objectMapperLookup, final String className, final String parameterBase) {
        IsisConfiguration configParameters = IsisContext.getConfiguration();
        table = configParameters.getString(parameterBase + "table");
        if (table == null) {
            String name = className.substring(className.lastIndexOf('.') + 1).toUpperCase();
            table = Sql.sqlName(name);
        }

        dbCreatesId = configParameters.getBoolean(parameterBase + "db-ids", false);
        if (configParameters.getBoolean(parameterBase + "all-fields", true)) {
            setupFullMapping(lookup, objectMapperLookup, className, configParameters, parameterBase);
        } else {
   //         setupSpecifiedMapping(specification, configParameters, parameterBase);
        }
        
        table = Sql.identifier(table);

        LOG.info("table mapping: " + table + " (" + columnList() + ")");
    }

    private void setupFullMapping(
            final FieldMappingLookup lookup,
            final ObjectMappingLookup objectMapperLookup, 
            final String className,
            final IsisConfiguration configParameters,
            final String parameterBase) {
        List<? extends ObjectAssociation> fields = specification.getAssociationList();

        int simpleFieldCount = 0;
        int collectionFieldCount = 0;
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).isNotPersisted()) {
                continue;
            } else if (fields.get(i).isOneToManyAssociation()) {
                collectionFieldCount++;
            } else {
                simpleFieldCount++;
            }
        }

        ObjectAssociation[] oneToOneProperties = new ObjectAssociation[simpleFieldCount];
        ObjectAssociation[] oneToManyProperties = new ObjectAssociation[collectionFieldCount];
        collectionMappers = new CollectionMapper[collectionFieldCount];
        // Properties collectionMappings = configParameters.getPropertiesStrippingPrefix(parameterBase +
        // "collection");
        IsisConfiguration subset = IsisContext.getConfiguration().createSubset(parameterBase + ".mapper.");

        for (int i = 0, simpleFieldNo = 0, collectionFieldNo = 0; i < fields.size(); i++) {
            ObjectAssociation field = fields.get(i);
            if (field.isNotPersisted()) {
                continue;
            } else if (field.isOneToManyAssociation()) {
                oneToManyProperties[collectionFieldNo] = field;

                // TODO: Replace "new CombinedCollectionMapper" with a factory method(?) to allow a different
                // default CollectionMapper
                String type = subset.getString(field.getId());
                if (type == null || type.equals("association-table")) {
//                    collectionMappers[collectionFieldNo] = new AutoCollectionMapper(specification,
//                            oneToManyProperties[collectionFieldNo], lookup);
                    //collectionMappers[collectionFieldNo] = new CombinedCollectionMapper(oneToManyProperties[collectionFieldNo], parameterBase, lookup, objectMapperLookup);
                    collectionMappers[collectionFieldNo] = new MultiColumnCombinedCollectionMapper(oneToManyProperties[collectionFieldNo], parameterBase, lookup, objectMapperLookup);

                } else if (type.equals("fk-table")) {
                    String property = parameterBase + field.getId() + ".element-type";
                    String elementType = configParameters.getString(property);
                    if (elementType == null) {
                        throw new SqlObjectStoreException("Expected property " + property);
                    }
                    /*
                    collectionMappers[collectionFieldNo] = new CombinedCollectionMapper(elementType,
                            oneToManyProperties[collectionFieldNo], parameterBase, lookup, objectMapperLookup);
*/
                } else {
                    // TODO use other mappers where necessary
                    throw new NotYetImplementedException("for " + type);
                }

                collectionFieldNo++;
            } else if (field.isOneToOneAssociation()) {
                oneToOneProperties[simpleFieldNo] = field;
                simpleFieldNo++;
            } else {
                oneToOneProperties[simpleFieldNo] = field;
                simpleFieldNo++;
            }
        }

        for (int f = 0; f < oneToOneProperties.length; f++) {
            ObjectAssociation field = oneToOneProperties[f];
            FieldMapping mapping = lookup.createMapping(field);
            fieldMappings.add(mapping);
            fieldMappingLookup.put(field, mapping);
        }

    }
/*
    private void setupSpecifiedMapping(
            final ObjectSpecification specification,
            final IsisConfiguration configParameters,
            final String parameterBase) {
        IsisConfiguration columnMappings = IsisContext.getConfiguration().createSubset(parameterBase + "column");
        int columnsSize = columnMappings.size();
        // columnNames = new String[columnsSize];
        oneToOneProperties = new ObjectAssociation[columnsSize];

        int i = 0;
        for (Enumeration names = columnMappings.propertyNames(); names.hasMoreElements(); i++) {
            String columnName = (String) names.nextElement();
            String fieldName = columnMappings.getString(columnName);
            oneToOneProperties[i] = specification.getAssociation(fieldName);
            // columnNames[i] = columnName;
        }

        IsisConfiguration collectionMappings = IsisContext.getConfiguration().createSubset(
                parameterBase + "collection");
        int collectionsSize = collectionMappings.size();
        collectionMappers = new AutoCollectionMapper[collectionsSize];
        oneToManyProperties = new ObjectAssociation[collectionsSize];

        int j = 0;
        for (Enumeration names = collectionMappings.propertyNames(); names.hasMoreElements(); j++) {
            String propertyName = (String) names.nextElement();
            String collectionName = collectionMappings.getString(propertyName);
            String type = collectionMappings.getString(collectionName);

            oneToManyProperties[j] = specification.getAssociation(collectionName);
            if (type.equals("auto")) {
                collectionMappers[j] = new AutoCollectionMapper(this, specification, oneToManyProperties[j], getLookup());
            } else {
                // TODO use other mappers where necessary
                // new ReversedAutoAssociationMapper(specification, collectionName, parameterBase);

                throw new NotYetImplementedException();
            }
        }
    }
*/
    protected String columnList() {
        StringBuffer sql = new StringBuffer();
        for (FieldMapping mapping : fieldMappings) {
            if (sql.length() > 0) {
                sql.append(",");
            }
            mapping.appendColumnNames(sql);
        }
        return sql.toString();
    }

    protected ObjectAdapter getAdapter(final ObjectSpecification specification, final Oid oid) {
        AdapterManager objectLoader = IsisContext.getPersistenceSession().getAdapterManager();
        ObjectAdapter adapter = objectLoader.getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        } else {
            return IsisContext.getPersistenceSession().recreateAdapter(oid, specification);
        }
    }

    protected FieldMapping fieldMappingFor(ObjectAssociation field) {
        return fieldMappingLookup.get(field);
    }

    public boolean needsTables(final DatabaseConnector connection) {
        for (int i = 0; collectionMappers != null && i < collectionMappers.length; i++) {
            if (collectionMappers[i].needsTables(connection)) {
                return true;
            }
        }
        return !connection.hasTable(table);
    }

    public String toString() {
        return "AbstractAutoMapper [table=" + table + ",noColumns=" + fieldMappings.size() + ",specification="
                + specification.getFullName() + "]";
    }

    protected String values(final ObjectAdapter object) {
        StringBuffer sql = new StringBuffer();
        for (FieldMapping mapping : fieldMappings) {
            mapping.appendInsertValues(sql, object);
            sql.append(",");
        }
        return sql.toString();
    }
}
