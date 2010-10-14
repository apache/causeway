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


package org.apache.isis.extensions.sql.objectstore;

import java.util.Date;
import java.util.Hashtable;

import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.sql.objectstore.SqlOid.State;


public abstract class AbstractMapper {
    private FieldMappingLookup objectMapperLookup;
    private Hashtable<String, PrimaryKeyMapper> keyMapping = new Hashtable<String, PrimaryKeyMapper>();

    public abstract void createTables(final DatabaseConnector connector);

    protected boolean needsTables(final DatabaseConnector connector) {
        return false;
    }

    public final void shutdown() {}

    public void startup(final DatabaseConnector connector, final FieldMappingLookup objectMapperLookup) {
        this.objectMapperLookup = objectMapperLookup;
        if (needsTables(connector)) {
            createTables(connector);
        }
    }

    protected FieldMappingLookup getFieldMappingLookup() {
        return objectMapperLookup;
    }

    // TODO remove
    protected Oid recreateOid(final Results rs, final ObjectSpecification cls, final String column) {
        PrimaryKey key;
        if (keyMapping.containsKey(column)) {
            key = keyMapping.get(column).generateKey(rs, column);
        } else {
            Object object = rs.getObject(column);
            if (object == null) {
                return null;
            } else {
                int id = ((Integer) object).intValue();
                key = new IntegerPrimaryKey(id);
            }
        }
        Oid oid = new SqlOid(cls.getFullName(), key, State.PERSISTENT);
        return oid;
    }

    // TODO remove
    protected void addPrimaryKeyMapper(final String columnName, final PrimaryKeyMapper mapper) {
        keyMapping.put(columnName, mapper);
    }

    protected String asSqlName(final String name) {
        return Sql.sqlName(name);
    }

    // TODO remove
    protected SerialNumberVersion createVersion(final long versionSequence) {
        return new SerialNumberVersion(versionSequence, "", new Date());
    }
}
