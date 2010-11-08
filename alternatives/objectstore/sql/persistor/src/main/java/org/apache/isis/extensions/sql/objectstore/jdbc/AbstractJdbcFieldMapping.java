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


package org.apache.isis.extensions.sql.objectstore.jdbc;

import org.apache.isis.core.commons.debug.DebugString;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.feature.ObjectAssociation;
import org.apache.isis.core.metamodel.spec.feature.OneToOneAssociation;
import org.apache.isis.extensions.sql.objectstore.Results;
import org.apache.isis.extensions.sql.objectstore.Sql;
import org.apache.isis.extensions.sql.objectstore.mapping.FieldMapping;


public abstract class AbstractJdbcFieldMapping implements FieldMapping {
    private String columnName;
    private final ObjectAssociation field;
        
    public AbstractJdbcFieldMapping(ObjectAssociation field) {
        this.field = field;
        columnName = Sql.sqlFieldName(field.getId());
    }

    public void appendColumnDefinitions(StringBuffer sql) {
        sql.append(columnName);
        sql.append(" ");
        sql.append(columnType());
    }

    public void appendColumnNames(StringBuffer sql) {
        sql.append(columnName);
    }

    public void appendInsertValues(StringBuffer sql, ObjectAdapter object) {
        ObjectAdapter fieldValue = field.get(object);
        if (fieldValue == null) {
            sql.append("NULL");
        } else {
            sql.append(valueAsDBString(fieldValue));
        }
    }

    public void appendUpdateValues(StringBuffer sql, ObjectAdapter object) {
        appendEqualsClause(sql, object, "=");
    }

    public void appendWhereClause(StringBuffer sql, ObjectAdapter object) {
        appendEqualsClause(sql, object, "=");
    }

    private void appendEqualsClause(StringBuffer sql, ObjectAdapter object, String condition) {
        sql.append(Sql.sqlFieldName(field.getId()));
        sql.append(condition);
        ObjectAdapter fieldValue = field.get(object);
        sql.append(valueAsDBString(fieldValue));
    }
    
    public void initializeField(ObjectAdapter object, Results rs) {
        String columnName = Sql.sqlFieldName(field.getId());
        String encodedValue = (String) rs.getString(columnName);
        ObjectAdapter restoredValue;
        if (encodedValue == null) {
            restoredValue = null;
        } else {
            restoredValue = setFromDBColumn(encodedValue, field);
            
        }
        ((OneToOneAssociation) field).initAssociation(object, restoredValue);
    }
    
    public void debugData(DebugString debug) {
        debug.appendln(field.getId(), columnName + "/" + columnType());
    }

    protected abstract String columnType();

    protected abstract String valueAsDBString(ObjectAdapter value);

    protected abstract ObjectAdapter setFromDBColumn(String encodeValue, ObjectAssociation field);

}

