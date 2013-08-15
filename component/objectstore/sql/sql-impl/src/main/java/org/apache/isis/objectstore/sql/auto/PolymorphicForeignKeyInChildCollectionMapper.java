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
package org.apache.isis.objectstore.sql.auto;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.FieldMappingLookup;
import org.apache.isis.objectstore.sql.IdMappingAbstract;
import org.apache.isis.objectstore.sql.ObjectMapping;
import org.apache.isis.objectstore.sql.ObjectMappingLookup;
import org.apache.isis.objectstore.sql.VersionMapping;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

/**
 * Used to map 1-to-many collections by creating, in the child table, 1 column
 * per parent collection. The column is named by combining the final part of the
 * parent class name and the collection variable name.
 * 
 * You have a choice between this class and
 * {@link PolymorphicForeignKeyInChildCollectionBaseMapper}
 * 
 * @author Kevin
 */
public class PolymorphicForeignKeyInChildCollectionMapper extends ForeignKeyInChildCollectionMapper {

    private static final Logger LOG = LoggerFactory.getLogger(PolymorphicForeignKeyInChildCollectionMapper.class);

    private final ObjectAssociation baseField;
    private final List<String> tables;
    private final List<ObjectSpecification> tableSpecifications;
    private final List<ObjectMapping> subClassMappers;

    // For iterating through the subclasses
    private ObjectSpecification currentTableSpecification;
    private Iterator<ObjectAdapter> currentIterator;
    private List<ObjectAdapter> currentCollection;
    private int currentIndexStart;
    private int currentIndex;

    // Store for passing on to other mappers
    final String parameterBase;
    final FieldMappingLookup lookup;
    final ObjectMappingLookup objectMapperLookup;
    final String fieldClassName;

    public PolymorphicForeignKeyInChildCollectionMapper(final ObjectAssociation objectAssociation, final String parameterBase, final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup, final AbstractAutoMapper abstractAutoMapper, final ObjectAssociation field) {

        super(lookup, abstractAutoMapper, field);

        fieldClassName = className;

        baseField = objectAssociation;
        tables = new ArrayList<String>();
        tableSpecifications = new ArrayList<ObjectSpecification>();
        subClassMappers = new ArrayList<ObjectMapping>();

        // Capture for use in creating subclass mappers.
        this.parameterBase = parameterBase;
        this.lookup = lookup;
        this.objectMapperLookup = objectMapperLookup;

        addSubSpecificationsToTable(specification);
    }

    protected void addSubSpecificationsToTable(final ObjectSpecification objectSpecification) {
        if (objectSpecification.isAbstract() == false) {
            final String tableNameFromSpecification = getTableNameFromSpecification(objectSpecification);
            tables.add(tableNameFromSpecification);
            tableSpecifications.add(objectSpecification);

            final ObjectMapping autoMapper = objectMapperLookup.getMapping(objectSpecification, null);
            subClassMappers.add(autoMapper);
        }
        if (objectSpecification.hasSubclasses()) {
            for (final ObjectSpecification subSpecification : objectSpecification.subclasses()) {
                addSubSpecificationsToTable(subSpecification);
            }
        }
    }

    @Override
    public boolean needsTables(final DatabaseConnector connection) {
        for (final String subTableName : tables) {
            table = subTableName;
            if (super.needsTables(connection)) {
                // Stop on first table that is needed.
                return true;
            }
        }
        return false;
    }

    @Override
    public void createTables(final DatabaseConnector connection) {
        for (final String subTableName : tables) {
            table = subTableName;
            if (super.needsTables(connection)) {
                super.createTables(connection);
            }
        }
    }

    @Override
    protected Iterator<ObjectAdapter> getElementsForCollectionAsIterator(final ObjectAdapter collection) {
        return currentIterator;
    }

    @Override
    public void saveInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent) {
        final ObjectAdapter collection = baseField.get(parent);
        LOG.debug("Saving polymorphic internal collection " + collection);

        currentCollection = new ArrayList<ObjectAdapter>();
        currentIterator = super.getElementsForCollectionAsIterator(collection);
        for (; currentIterator.hasNext();) {
            final ObjectAdapter item = currentIterator.next();
            currentCollection.add(item);
        }

        for (int i = 0; i < tables.size(); i++) {
            currentTableSpecification = tableSpecifications.get(i);
            currentIndex = 0;
            currentIndexStart = 0;

            currentIterator = new Iterator<ObjectAdapter>() {
                @Override
                public boolean hasNext() {
                    for (int i = currentIndexStart; i < currentCollection.size(); i++) {
                        final ObjectAdapter thisObjectAdapter = currentCollection.get(i);
                        if (thisObjectAdapter.getSpecification().isOfType(currentTableSpecification)) {
                            currentIndexStart = currentIndex = i;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public ObjectAdapter next() {
                    currentIndexStart = currentIndex + 1;
                    return currentCollection.get(currentIndex);
                }

                @Override
                public void remove() {
                }
            };

            // Provide replacement table and column definitions here
            table = tables.get(i);
            super.saveInternalCollection(connector, parent);
        }
    }

    @Override
    protected void loadCollectionIntoList(final DatabaseConnector connector, final ObjectAdapter parent, final String table, final ObjectSpecification specification, final IdMappingAbstract idMappingAbstract, final Map<ObjectAssociation, FieldMapping> fieldMappingByField, final VersionMapping versionMapping,
            final List<ObjectAdapter> superList) {
        final List<ObjectAdapter> list = Lists.newArrayList();
        
        for (int i = 0; i < tables.size(); i++) {
            currentTableSpecification = tableSpecifications.get(i);
            final AutoMapper mapper = (AutoMapper) subClassMappers.get(i);
            final String mapperTable = tables.get(i);

            super.loadCollectionIntoList(connector, parent, mapperTable, currentTableSpecification, mapper.getIdMapping(), mapper.fieldMappingByField, mapper.getVersionMapping(), list);

            superList.addAll(list);
            list.clear();
        }

    }

}
