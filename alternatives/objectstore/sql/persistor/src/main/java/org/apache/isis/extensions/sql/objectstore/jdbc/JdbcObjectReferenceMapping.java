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

import org.apache.isis.metamodel.adapter.ObjectAdapter;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.extensions.sql.objectstore.IdMappingAbstract;
import org.apache.isis.extensions.sql.objectstore.Results;
import org.apache.isis.extensions.sql.objectstore.Sql;
import org.apache.isis.extensions.sql.objectstore.SqlObjectStoreException;
import org.apache.isis.extensions.sql.objectstore.mapping.ObjectReferenceMapping;


public class JdbcObjectReferenceMapping extends IdMappingAbstract implements ObjectReferenceMapping {
    private ObjectSpecification specification;

    public JdbcObjectReferenceMapping(String columnName, ObjectSpecification specification) {
        this.specification = specification;
        String idColumn = Sql.sqlName("fk_" + columnName);
        setColumn(idColumn);
    }

     public void appendUpdateValues(StringBuffer sql, ObjectAdapter object) {
        sql.append(getColumn());
        if (object == null) {
            sql.append("= NULL ");
        } else {
            sql.append("=");
            sql.append(primaryKey(object.getOid()));
        }
    }

    public ObjectAdapter initializeField(Results rs) {
        Oid oid = recreateOid(rs, specification);
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

