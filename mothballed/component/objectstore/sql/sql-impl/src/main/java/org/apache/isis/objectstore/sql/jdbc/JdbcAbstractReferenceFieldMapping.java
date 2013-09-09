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

package org.apache.isis.objectstore.sql.jdbc;

import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderSpi;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.sql.AbstractFieldMappingFactory;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.Defaults;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.Sql;
import org.apache.isis.objectstore.sql.mapping.FieldMapping;

/**
 * Provides support for persisting abstract classes and interfaces.
 * 
 * Provides two columns: the first is the standard property field. The second is
 * initialised only when the field is "saved", and contains the actual classname
 * of the persisted concrete class.
 * 
 * 
 * @version $Rev$ $Date$
 */
public class JdbcAbstractReferenceFieldMapping extends JdbcObjectReferenceFieldMapping {
    private final String classnameColumn;
    private final String dataType;

    public static class Factory extends AbstractFieldMappingFactory {

        public Factory(final String type) {
            super();
        }

        @Override
        public FieldMapping createFieldMapping(final ObjectSpecification object, final ObjectAssociation field) {
            final String dataType = getTypeOverride(object, field, Defaults.TYPE_LONG_STRING());
            return new JdbcAbstractReferenceFieldMapping(field, dataType);
        }
    }

    public JdbcAbstractReferenceFieldMapping(final ObjectAssociation field, final String dataType) {
        super(field);
        this.dataType = dataType;
        classnameColumn = Sql.identifier(getColumn() + "_cls");
    }

    @Override
    public void appendColumnDefinitions(final StringBuffer sql) {
        super.appendColumnDefinitions(sql);

        sql.append(", ");
        sql.append(classnameColumn);
        sql.append(" ");
        sql.append(dataType);
    }

    @Override
    public void appendCreateColumnDefinitions(final StringBuffer sql) {
        super.appendCreateColumnDefinitions(sql);
        sql.append(classnameColumn);
        sql.append(" ");
        sql.append(dataType);
    }

    @Override
    public void appendColumnNames(final StringBuffer sql) {
        super.appendColumnNames(sql);
        sql.append(", ");
        sql.append(classnameColumn);
    }

    @Override
    public void appendWhereClause(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        super.appendWhereClause(connector, sql, object);
    }

    @Override
    public void appendInsertValues(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        super.appendInsertValues(connector, sql, object);
        sql.append(",?");

        final ObjectAdapter objectAdapter = field.get(object);
        if (objectAdapter != null) {
            connector.addToQueryValues(objectAdapter.getSpecification().getFullIdentifier());
        } else {
            connector.addToQueryValues(null);
        }
    }

    @Override
    public void appendUpdateValues(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        super.appendUpdateValues(connector, sql, object);

        sql.append(",");
        sql.append(classnameColumn);
        sql.append(" = ?");

        final ObjectAdapter objectAdapter = field.get(object);
        if (objectAdapter != null) {
            connector.addToQueryValues(objectAdapter.getSpecification().getFullIdentifier());
        } else {
            connector.addToQueryValues(null);
        }
    }

    @Override
    public void initializeField(final ObjectAdapter object, final Results rs) {
        final String className = rs.getString(classnameColumn);
        if (className != null) {
            final ObjectSpecification specification = getReflector().loadSpecification(className);

            final Oid oid = recreateOid(rs, specification);

            final ObjectAdapter reference = getAdapter(specification, oid);
            ((OneToOneAssociation) field).initAssociation(object, reference);
        }
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln(field.getId(), getColumn());
    }

    private SpecificationLoaderSpi getReflector() {
        return IsisContext.getSpecificationLoader();
    }
}
