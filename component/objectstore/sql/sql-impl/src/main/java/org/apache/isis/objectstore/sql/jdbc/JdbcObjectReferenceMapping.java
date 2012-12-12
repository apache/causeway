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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.objectstore.sql.DatabaseConnector;
import org.apache.isis.objectstore.sql.IdMappingAbstract;
import org.apache.isis.objectstore.sql.Results;
import org.apache.isis.objectstore.sql.Sql;
import org.apache.isis.objectstore.sql.SqlObjectStoreException;
import org.apache.isis.objectstore.sql.mapping.ObjectReferenceMapping;

public class JdbcObjectReferenceMapping extends IdMappingAbstract implements ObjectReferenceMapping {
    private final ObjectSpecification specification;

    public JdbcObjectReferenceMapping(final String columnName, final ObjectSpecification specification) {
        this.specification = specification;
        final String idColumn = Sql.sqlName("fk_" + columnName);
        setColumn(idColumn);
    }

    @Override
    public void appendUpdateValues(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter adapter) {
        sql.append(getColumn());
        if (adapter == null) {
            sql.append("= NULL ");
        } else {
            sql.append("= ?");
            // sql.append(primaryKey(object.getOid()));
            final RootOid oid = (RootOid) adapter.getOid();
            connector.addToQueryValues(primaryKey(oid));
        }
    }

    public ObjectAdapter initializeField(final Results rs) {
        final Oid oid = recreateOid(rs, specification);
        if (oid != null) {
            if (specification.isAbstract()) {
                throw new SqlObjectStoreException("NOT DEALING WITH POLYMORPHIC ASSOCIATIONS");
            } else {
                return getAdapter(specification, oid);
            }
        } else {
            return null;
        }
    }

}
