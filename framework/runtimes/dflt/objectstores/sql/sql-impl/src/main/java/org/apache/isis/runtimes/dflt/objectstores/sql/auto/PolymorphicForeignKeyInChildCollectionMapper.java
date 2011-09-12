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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.runtimes.dflt.objectstores.sql.DatabaseConnector;
import org.apache.isis.runtimes.dflt.objectstores.sql.FieldMappingLookup;
import org.apache.isis.runtimes.dflt.objectstores.sql.ObjectMappingLookup;

/**
 * Used to map 1-to-many collections by creating, in the child table, 1 column per parent collection. The column is
 * named by combining the final part of the parent class name and the collection variable name.
 * 
 * @author Kevin
 */
public class PolymorphicForeignKeyInChildCollectionMapper extends ForeignKeyInChildCollectionMapper {

    private static final Logger LOG = Logger.getLogger(PolymorphicForeignKeyInChildCollectionMapper.class);

    // TODO: Fields have subclasses. Must add FK_* column to *all* non-abstract subclasses.
    private final ObjectAssociation baseField;
    private final List<String> tables;
    private final List<ObjectSpecification> tableSpecifications;
    // private final String classColumnName;

    // For iterating through the subclasses
    private ObjectSpecification currentTableSpecification;
    private Iterator<ObjectAdapter> currentIterator;
    private List<ObjectAdapter> currentCollection;
    private int currentIndexStart;
    private int currentIndex;

    public PolymorphicForeignKeyInChildCollectionMapper(final ObjectAssociation objectAssociation,
        final String parameterBase, final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup,
        AbstractAutoMapper abstractAutoMapper, ObjectAssociation field) {
        super(objectAssociation, parameterBase, lookup, objectMapperLookup, abstractAutoMapper, field);

        this.baseField = objectAssociation;
        // classColumnName = Sql.identifier(getForeignKeyName() + "_cls");

        tables = new ArrayList<String>();
        tableSpecifications = new ArrayList<ObjectSpecification>();
        addSubSpecificationsToTable(specification);

    }

    protected void addSubSpecificationsToTable(ObjectSpecification objectSpecification) {
        for (ObjectSpecification subSpecification : objectSpecification.subclasses()) {
            if (subSpecification.isAbstract() == false) {
                final String tableNameFromSpecification = getTableNameFromSpecification(subSpecification);
                tables.add(tableNameFromSpecification);
                tableSpecifications.add(subSpecification);
            }
            if (subSpecification.hasSubclasses()) {
                addSubSpecificationsToTable(subSpecification);
            }
        }
    }

    @Override
    public boolean needsTables(DatabaseConnector connection) {
        for (String subTableName : tables) {
            table = subTableName;
            if (super.needsTables(connection)) { // || !connection.hasColumn(table, classColumnName)) {
                // Stop on first table that is needed.
                return true;
            }
        }
        return false;
    }

    @Override
    public void createTables(final DatabaseConnector connection) {
        for (String subTableName : tables) {
            table = subTableName;
            if (super.needsTables(connection)) {
                super.createTables(connection);
            }

            // if (!connection.hasColumn(table, classColumnName)) {
            // // I couldn't combine this in one operation with appendColumnDefinitions
            // final StringBuffer sql = new StringBuffer();
            // sql.append("alter table ");
            // sql.append(table);
            // sql.append(" add ");
            //
            // sql.append(classColumnName);
            // sql.append(" ");
            // sql.append(JdbcConnector.TYPE_LONG_STRING());
            //
            // connection.update(sql.toString());
            // }
        }
    }

    @Override
    protected void appendCollectionUpdateColumnsToNull(StringBuffer sql) {
        super.appendCollectionUpdateColumnsToNull(sql);
        // sql.append("," + classColumnName + "=NULL");
    }

    @Override
    protected void appendCollectionUpdateValues(final DatabaseConnector connector, final ObjectAdapter parent,
        final StringBuffer sql) {
        super.appendCollectionUpdateValues(connector, parent, sql);
        // sql.append("," + classColumnName + "=?");
        // connector.addToQueryValues(currentTableSpecification.getFullIdentifier());
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
            ObjectAdapter item = currentIterator.next();
            currentCollection.add(item);
        }

        // TODO: Polymorphism: save instances to appropriate subclass tables
        for (int i = 0; i < tables.size(); i++) {
            currentTableSpecification = tableSpecifications.get(i);
            currentIndex = 0;
            currentIndexStart = 0;

            currentIterator = new Iterator<ObjectAdapter>() {
                @Override
                public boolean hasNext() {
                    for (int i = currentIndexStart; i < currentCollection.size(); i++) {
                        ObjectAdapter thisObjectAdapter = currentCollection.get(i);
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
    public void loadInternalCollection(final DatabaseConnector connector, final ObjectAdapter parent,
        final boolean makeResolved) {
        final ObjectAdapter collection = baseField.get(parent);
        LOG.debug("loading polymorphic internal collection " + collection);
        // TODO: Polymorphism: load instances from all subclass tables
        super.loadInternalCollection(connector, parent, makeResolved);
    }

    @Override
    protected void loadCollectionIntoList(final DatabaseConnector connector, final ObjectAdapter parent,
        final boolean makeResolved, final List<ObjectAdapter> superList) {
        // TODO: Polymorphism: save instances to appropriate subclass tables
        final List<ObjectAdapter> list = new ArrayList<ObjectAdapter>();
        for (int i = 0; i < tables.size(); i++) {
            currentTableSpecification = tableSpecifications.get(i);
            specification = currentTableSpecification;

            table = tables.get(i);
            super.loadCollectionIntoList(connector, parent, makeResolved, list);

            superList.addAll(list);
            list.clear();
        }

    }

}
