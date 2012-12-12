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

import org.apache.isis.applib.ApplicationException;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.Sql;

public abstract class AbstractJdbcMultiFieldMapping extends AbstractJdbcFieldMapping {
    private final int columnCount;
    private final String[] types;
    private final String[] columnNames;
    private final AdapterManager adapterManager;

    /**
     * 
     * @param field
     *            the field object association.
     * @param columnCount
     *            the number of columns required to store this field. See the
     *            abstract methods ,
     *            {@link AbstractJdbcFieldMapping#preparedStatementObject(int i, ObjectAdapter fieldValue)}
     *            ,
     *            {@link AbstractJdbcFieldMapping#getObjectFromResults(Results results)}
     *            ,
     * @param types
     *            the list of SQL data types, 1 per columnCount, to represent
     *            the value type.
     */
    public AbstractJdbcMultiFieldMapping(final ObjectAssociation field, final int columnCount, final String... types) {
        super(field);
        this.columnCount = columnCount;

        this.types = new String[columnCount];
        for (int i = 0; i < types.length; i++) {
            this.types[i] = types[i];
        }

        final String fieldName = field.getId();
        columnNames = new String[columnCount];
        columnNames[0] = Sql.sqlFieldName(fieldName + "1");
        columnNames[1] = Sql.sqlFieldName(fieldName + "2");

        adapterManager = IsisContext.getPersistenceSession().getAdapterManager();

    }

    @Override
    public void appendColumnDefinitions(final StringBuffer sql) {
        for (int i = 0; i < columnCount; i++) {
            sql.append(columnName(i) + " " + columnType(i));
            if (i < columnCount - 1) {
                sql.append(", ");
            }
        }
    }

    @Override
    public void appendColumnNames(final StringBuffer sql) {
        for (int i = 0; i < columnCount; i++) {
            sql.append(columnName(i));
            if (i < columnCount - 1) {
                sql.append(", ");
            }
        }
    }

    @Override
    public void appendInsertValues(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        final ObjectAdapter fieldValue = field.get(object);
        final Object o = (fieldValue == null) ? null : fieldValue.getObject();

        for (int i = 0; i < columnCount; i++) {
            if (fieldValue == null) {
                sql.append("NULL");
            } else {
                sql.append("?");
                if (i < columnCount - 1) {
                    sql.append(", ");
                }

                connector.addToQueryValues(preparedStatementObject(i, o));
            }
        }
    }

    @Override
    public void appendUpdateValues(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        final ObjectAdapter fieldValue = field.get(object);
        final Object o = (fieldValue == null) ? null : fieldValue.getObject();
        for (int i = 0; i < columnCount; i++) {
            appendEqualsClause(connector, i, sql, o, "=");
        }
    }

    @Override
    public void appendWhereClause(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        appendUpdateValues(connector, sql, object);
    }

    private void appendEqualsClause(final DatabaseConnector connector, final int index, final StringBuffer sql, final Object object, final String condition) {

        final Object oPart = preparedStatementObject(index, object);

        sql.append(columnName(index) + condition + "?");
        if (index < columnCount - 1) {
            sql.append(", ");
        }

        connector.addToQueryValues(oPart);
    }

    @Override
    public ObjectAdapter setFromDBColumn(final Results results, final String columnName, final ObjectAssociation field) {
        ObjectAdapter restoredValue;
        final Object objectValue = getObjectFromResults(results);
        restoredValue = adapterManager.adapterFor(objectValue); // NOTE: If this
                                                                // fails, then
                                                                // fetch back
                                                                // the
                                                                // declaration
                                                                // from the
                                                                // constructor
                                                                // to here.
        return restoredValue;
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        for (int i = 0; i < columnCount; i++) {
            debug.appendln(field.getId(), columnName(i) + "/" + columnType(i));
        }
    }

    @Override
    protected String columnType() {
        throw new ApplicationException("Should never be called");
    }

    @Override
    protected Object preparedStatementObject(final ObjectAdapter value) {
        throw new ApplicationException("Should never be called");
    }

    protected String columnType(final int index) {
        return types[index];
    }

    protected String columnName(final int index) {
        return columnNames[index];
    }

    /**
     * Return an object suitable for passing to the SQL prepared statement
     * constructor, to handle field "index". Will be called "columnCount" times.
     * 
     * @param index
     *            0 based index
     * @param fieldObject
     *            the value type currently being
     * @return a JDBC-compatible object.
     */
    protected abstract Object preparedStatementObject(int index, Object o);

    /**
     * Return an applib object represented by the results set.
     * 
     * @param results
     *            the current record row from the underlying table
     * @return a fully initialised value object.
     */
    protected abstract Object getObjectFromResults(Results results);
}
