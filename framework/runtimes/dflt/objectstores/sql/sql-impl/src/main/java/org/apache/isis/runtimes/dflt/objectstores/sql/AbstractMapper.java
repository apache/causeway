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

package org.apache.isis.runtimes.dflt.objectstores.sql;

import java.util.Date;
import java.util.Hashtable;

import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.version.SerialNumberVersion;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.sql.SqlOid.State;

public abstract class AbstractMapper {
    private FieldMappingLookup fieldMappingLookup;
    private final Hashtable<String, PrimaryKeyMapper> keyMapping = new Hashtable<String, PrimaryKeyMapper>();

    public abstract void createTables(final DatabaseConnector connector);

    protected boolean needsTables(final DatabaseConnector connector) {
        return false;
    }

    public final void shutdown() {
    }

    public void startup(final DatabaseConnector connector, final FieldMappingLookup fieldMappingLookup) {
        this.fieldMappingLookup = fieldMappingLookup;
        if (needsTables(connector)) {
            createTables(connector);
        }
    }

    protected FieldMappingLookup getFieldMappingLookup() {
        return fieldMappingLookup;
    }

    // TODO remove
    protected Oid recreateOid(final Results rs, final ObjectSpecification cls, final String column) {
        PrimaryKey key;
        if (keyMapping.containsKey(column)) {
            key = keyMapping.get(column).generateKey(rs, column);
        } else {
            final Object object = rs.getObject(column);
            if (object == null) {
                return null;
            } else {
                final int id = ((Integer) object).intValue();
                key = new IntegerPrimaryKey(id);
            }
        }
        final Oid oid = new SqlOid(cls.getFullIdentifier(), key, State.PERSISTENT);
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
