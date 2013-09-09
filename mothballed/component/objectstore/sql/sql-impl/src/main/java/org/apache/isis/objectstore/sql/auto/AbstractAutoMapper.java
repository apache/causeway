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
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.exceptions.NotYetImplementedException;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.sql.AbstractMapper;
import org.apache.isis.objectstore.sql.CollectionMapper;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.Defaults;
import org.apache.isis.objectstore.sql.FieldMappingLookup;
import org.apache.isis.objectstore.sql.ObjectMappingLookup;
import org.apache.isis.objectstore.sql.Sql;
import org.apache.isis.objectstore.sql.SqlObjectStoreException;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

public abstract class AbstractAutoMapper extends AbstractMapper {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAutoMapper.class);

    protected final Map<ObjectAssociation, FieldMapping> fieldMappingByField = Maps.newLinkedHashMap();

    protected CollectionMapper collectionMappers[];
    protected String collectionMapperFields[];
    protected boolean dbCreatesId;

    protected ObjectSpecification specification;
    protected String table;

    final String className;
    final String parameterBase;
    final FieldMappingLookup lookup;
    final ObjectMappingLookup objectMappingLookup;

    protected AbstractAutoMapper(final String className, final String parameterBase, final FieldMappingLookup lookup,
        final ObjectMappingLookup objectMappingLookup) {
        this.specification = specificationFor(className);
        this.className = className;
        this.parameterBase = parameterBase;
        this.lookup = lookup;
        this.objectMappingLookup = objectMappingLookup;
    }

    protected AbstractAutoMapper(final FieldMappingLookup lookup, final AbstractAutoMapper abstractAutoMapper,
        final String className) {

        this.specification = getSpecificationLoader().loadSpecification(className);
        this.className = className;

        this.parameterBase = null;
        this.lookup = null;
        this.objectMappingLookup = null;
    }

    private static ObjectSpecification specificationFor(final String className) {
        ObjectSpecification specification = IsisContext.getSpecificationLoader().loadSpecification(className);
        List<OneToOneAssociation> properties = specification.getProperties(Contributed.EXCLUDED);
        if (isNullOrEmpty(properties) && !specification.isAbstract()) {
            throw new SqlObjectStoreException(specification.getFullIdentifier() + " has no fields: " + specification);
        }
        return specification;
    }

    protected void setUpFieldMappers() {
        setUpFieldMappers(lookup, objectMappingLookup, className, parameterBase);
    }

    private void setUpFieldMappers(final FieldMappingLookup lookup, final ObjectMappingLookup objectMappingLookup,
        final String className, final String parameterBase) {
        final IsisConfiguration configParameters = getConfiguration();
        table = configParameters.getString(parameterBase + ".table." + className);
        if (table == null) {
            final String name = getTableNameFromSpecification(specification);
            table = name;
        } else {
            table = Sql.tableIdentifier(table);
        }

        dbCreatesId = configParameters.getBoolean(parameterBase + "db-ids", false);
        if (configParameters.getBoolean(parameterBase + "all-fields", true)) {
            setupFullMapping(lookup, objectMappingLookup, className, configParameters, parameterBase);
        } else {
            // setupSpecifiedMapping(specification, configParameters,
            // parameterBase);
        }

        LOG.info("table mapping: " + table + " (" + columnList(fieldMappingByField) + ")");
    }

    protected String getTableNameFromSpecification(final ObjectSpecification objectSpecification) {
        return Sql.tableIdentifier(Sql.sqlName(Defaults.getTablePrefix() + objectSpecification.getShortIdentifier()));
    }

    protected List<ObjectAssociation> fields = new ArrayList<ObjectAssociation>();

    protected void getExtraFields(final List<ObjectAssociation> fields) {
    }

    private void setupFullMapping(final FieldMappingLookup lookup, final ObjectMappingLookup objectMapperLookup,
        final String className, final IsisConfiguration configParameters, final String parameterBase) {

        fields.addAll(specification.getAssociations(Contributed.EXCLUDED));

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

        final ObjectAssociation[] oneToOneProperties = new ObjectAssociation[simpleFieldCount];
        final ObjectAssociation[] oneToManyProperties = new ObjectAssociation[collectionFieldCount];
        collectionMappers = new CollectionMapper[collectionFieldCount];
        collectionMapperFields = new String[collectionFieldCount];
        final IsisConfiguration subset = getConfiguration().createSubset(parameterBase + ".mapper.");

        for (int i = 0, simpleFieldNo = 0, collectionFieldNo = 0; i < fields.size(); i++) {
            final ObjectAssociation field = fields.get(i);
            if (field.isNotPersisted()) {
                continue;
            } else if (field.isOneToManyAssociation()) {
                oneToManyProperties[collectionFieldNo] = field;

                // TODO: Replace "new ForeignKeyCollectionMapper" with a factory
                // method(?) to allow a different
                // default CollectionMapper

                // TODO: I think the default order should be changed - and I
                // think I (KAM) have dropped support for the
                // original "association-table" implementation. This means the
                // current checks are misleading.
                final String type = subset.getString(field.getId());
                if (type == null || type.equals("association-table")) {
                    // collectionMappers[collectionFieldNo] = new
                    // AutoCollectionMapper(specification,
                    // oneToManyProperties[collectionFieldNo], lookup);
                    // collectionMappers[collectionFieldNo] = new
                    // ForeignKeyCollectionMapper(oneToManyProperties[collectionFieldNo],
                    // parameterBase, lookup,
                    // objectMapperLookup);

                    CollectionMapper collectionMapper = null;

                    // Trying to detect recursion, here.
                    // Let ForeignKeyInChildCollectionMapper find itself when a
                    // field is a collection of the current
                    // field type.
                    if (this instanceof ForeignKeyInChildCollectionMapper) {
                        final ForeignKeyInChildCollectionMapper mc = (ForeignKeyInChildCollectionMapper) this;

                        if (mc.priorField == field) {
                            collectionMapper = mc;
                        }
                    }

                    if (collectionMapper == null) {
                        // TODO: Polymorphism - is it sufficient for the
                        // collectionMapper to handle the subclasses?
                        final ObjectSpecification fieldSpecification = field.getSpecification();
                        if (fieldSpecification.hasSubclasses() || fieldSpecification.isAbstract()) {
                            // PolymorphicForeignKeyInChildCollectionBaseMapper
                            // Or PolymorphicForeignKeyInChildCollectionMapper
                            collectionMapper =
                                new PolymorphicForeignKeyInChildCollectionBaseMapper(
                                    oneToManyProperties[collectionFieldNo], parameterBase, lookup, objectMapperLookup,
                                    this, field);
                        } else {
                            final ForeignKeyInChildCollectionMapper mapper =
                                new ForeignKeyInChildCollectionMapper(oneToManyProperties[collectionFieldNo],
                                    parameterBase, lookup, objectMapperLookup, this, field);
                            mapper.setUpFieldMappers();
                            collectionMapper = mapper;
                        }
                    }

                    collectionMappers[collectionFieldNo] = collectionMapper;
                    collectionMapperFields[collectionFieldNo] = field.getId();

                } else if (type.equals("fk-table")) {
                    final String property = parameterBase + field.getId() + ".element-type";
                    final String elementType = configParameters.getString(property);
                    if (elementType == null) {
                        throw new SqlObjectStoreException("Expected property " + property);
                    }
                    /*
                     * collectionMappers[collectionFieldNo] = new ForeignKeyCollectionMapper(elementType,
                     * oneToManyProperties[collectionFieldNo], parameterBase, lookup, objectMapperLookup);
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

        for (final ObjectAssociation field : oneToOneProperties) {
            if (fieldMappingByField.containsKey(field)) {
                continue;
            }
            final FieldMapping mapping = lookup.createMapping(specification, field);
            fieldMappingByField.put(field, mapping);
        }
    }

    protected String columnList(final Map<ObjectAssociation, FieldMapping> fieldMappingByField) {
        return columnList(fieldMappingByField.values());
    }

    /*
     * private void setupSpecifiedMapping( final ObjectSpecification specification, final IsisConfiguration
     * configParameters, final String parameterBase) { IsisConfiguration columnMappings =
     * IsisContext.getConfiguration().createSubset(parameterBase + "column"); int columnsSize = columnMappings.size();
     * // columnNames = new String[columnsSize]; oneToOneProperties = new ObjectAssociation[columnsSize];
     * 
     * int i = 0; for (Enumeration names = columnMappings.propertyNames(); names.hasMoreElements(); i++) { String
     * columnName = (String) names.nextElement(); String fieldName = columnMappings.getString(columnName);
     * oneToOneProperties[i] = specification.getAssociation(fieldName); // columnNames[i] = columnName; }
     * 
     * IsisConfiguration collectionMappings = IsisContext.getConfiguration().createSubset( parameterBase +
     * "collection"); int collectionsSize = collectionMappings.size(); collectionMappers = new
     * AutoCollectionMapper[collectionsSize]; oneToManyProperties = new ObjectAssociation[collectionsSize];
     * 
     * int j = 0; for (Enumeration names = collectionMappings.propertyNames(); names.hasMoreElements(); j++) { String
     * propertyName = (String) names.nextElement(); String collectionName = collectionMappings.getString(propertyName);
     * String type = collectionMappings.getString(collectionName);
     * 
     * oneToManyProperties[j] = specification.getAssociation(collectionName); if (type.equals("auto")) {
     * collectionMappers[j] = new AutoCollectionMapper(this, specification, oneToManyProperties[j], getLookup()); } else
     * { // TODO use other mappers where necessary // new ReversedAutoAssociationMapper(specification, collectionName,
     * parameterBase);
     * 
     * throw new NotYetImplementedException(); } } }
     */
    protected String columnList(final Collection<FieldMapping> fieldMappings) {
        final StringBuffer sql = new StringBuffer();
        for (final FieldMapping mapping : fieldMappings) {
            if (sql.length() > 0) {
                sql.append(",");
            }
            mapping.appendColumnNames(sql);
        }
        return sql.toString();
    }

    protected ObjectAdapter getAdapter(final ObjectSpecification spec, final Oid oid) {
        final ObjectAdapter adapter = getAdapterManager().getAdapterFor(oid);
        if (adapter != null) {
            return adapter;
        }

        // REVIEW: where the oid is a TypedOid, the following two lines could be replaced by
        // getPersistenceSession().recreatePersistentAdapter(oid)
        // is preferable, since then reuses the PojoRecreator impl defined within SqlPersistorInstaller
        final Object recreatedPojo = spec.createObject();
        return getPersistenceSession().mapRecreatedPojo(oid, recreatedPojo);
    }

    protected FieldMapping fieldMappingFor(final ObjectAssociation field) {
        return fieldMappingByField.get(field);
    }

    @Override
    public boolean needsTables(final DatabaseConnector connection) {
        for (int i = 0; collectionMappers != null && i < collectionMappers.length; i++) {
            if (collectionMappers[i].needsTables(connection)) {
                return true;
            }
        }
        return !connection.hasTable(table);
    }

    protected String values(final DatabaseConnector connector, final ObjectAdapter object) {
        final StringBuffer sql = new StringBuffer();
        for (final FieldMapping mapping : fieldMappingByField.values()) {
            mapping.appendInsertValues(connector, sql, object);
            sql.append(",");
        }
        return sql.toString();
    }

    private static boolean isNullOrEmpty(List<?> list) {
        return list == null || list.size() == 0;
    }

    @Override
    public String toString() {
        return "AbstractAutoMapper [table=" + table + ",noColumns=" + fieldMappingByField.size() + ",specification="
            + specification.getFullIdentifier() + "]";
    }

    protected SpecificationLoaderSpi getSpecificationLoader() {
        return IsisContext.getSpecificationLoader();
    }

    protected IsisConfiguration getConfiguration() {
        return IsisContext.getConfiguration();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

}
