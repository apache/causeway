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

package org.apache.isis.objectstore.sql;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.adapter.oid.RootOid;
import org.apache.isis.core.metamodel.adapter.oid.RootOidDefault;
import org.apache.isis.core.metamodel.adapter.oid.TypedOid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;

public class IdMappingAbstract {
    private String column;

    protected void setColumn(final String column) {
        this.column = Sql.identifier(column);
    }

    protected String getColumn() {
        return column;
    }

    public void appendWhereClause(final DatabaseConnector connector, final StringBuffer sql, final RootOid oid) {
        sql.append(column);
        sql.append(" = ");
        appendObjectId(connector, sql, oid);
    }

    public void appendObjectId(final DatabaseConnector connector, final StringBuffer sql, final RootOid oid) {
        sql.append("?");
        connector.addToQueryValues(primaryKey(oid));
    }

    public void appendCreateColumnDefinitions(final StringBuffer sql) {
        sql.append(column);
        sql.append(" ");
        sql.append(Defaults.TYPE_PK() + " NOT NULL PRIMARY KEY");
    }

    public void appendColumnDefinitions(final StringBuffer sql) {
        sql.append(column);
        sql.append(" ");
        sql.append(Defaults.TYPE_PK());
    }

    public void appendColumnNames(final StringBuffer sql) {
        sql.append(column);
    }

    public void appendInsertValues(final DatabaseConnector connector, final StringBuffer sql, final ObjectAdapter object) {
        if (object == null) {
            sql.append("NULL");
        } else {
            appendObjectId(connector, sql, (RootOid) object.getOid());
            // sql.append(connector.addToQueryValues(primaryKeyAsObject(object.getOid())));
        }
    }

    public String primaryKey(final RootOid oid) {
        return oid.getIdentifier();
    }

    public TypedOid recreateOid(final Results rs, final ObjectSpecification specification) {
        final Object object = rs.getObject(column);
        if (object == null) {
            return null;
        }
        final int id = ((Integer) object).intValue();
        return new RootOidDefault(specification.getSpecId(), "" + id, Oid.State.PERSISTENT);
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

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

}
