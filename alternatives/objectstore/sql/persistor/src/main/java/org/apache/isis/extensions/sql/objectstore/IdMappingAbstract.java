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

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.oid.Oid;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.context.IsisContext;
import org.apache.isis.core.runtime.persistence.adaptermanager.AdapterManager;
import org.apache.isis.core.runtime.persistence.oidgenerator.simple.SerialOid;
import org.apache.isis.extensions.sql.objectstore.SqlOid.State;

public class IdMappingAbstract {
	private String column;

	protected void setColumn(String column) {
		this.column = Sql.identifier(column);
	}

	protected String getColumn() {
		return column;
	}

	public void appendWhereClause(DatabaseConnector connector,
			StringBuffer sql, Oid oid) {
		sql.append(column);
		connector.addToQueryValues(primaryKeyAsObject(oid));
		sql.append(" = ?");
		// String id = primaryKey(oid);
		// sql.append(id);
	}

	public void appendCreateColumnDefinitions(StringBuffer sql) {
		sql.append(column);
		sql.append(" ");
		sql.append("INTEGER NOT NULL PRIMARY KEY");
	}

	public void appendColumnDefinitions(StringBuffer sql) {
		sql.append(column);
		sql.append(" ");
		sql.append("INTEGER");
	}

	public void appendColumnNames(StringBuffer sql) {
		sql.append(column);
	}

	public void appendInsertValues(DatabaseConnector connector,
			StringBuffer sql, ObjectAdapter object) {
		if (object == null) {
			sql.append(connector.addToQueryValues(null));
		} else {
			sql.append(connector.addToQueryValues(primaryKeyAsObject(object
					.getOid())));
		}
	}

	/*
	 * This doesn't have to be an Int, it should be any object.
	 */
	public Object primaryKeyAsObject(final Oid oid) {
		if (oid instanceof SqlOid) {
			PrimaryKey pk = ((SqlOid) oid).getPrimaryKey();
			return pk.naturalValue();
		} else
			return ((SerialOid) oid).getSerialNo();
	}

	public String primaryKey(final Oid oid) {
		if (oid instanceof SqlOid)
			return "" + ((SqlOid) oid).getPrimaryKey().stringValue() + "";
		else
			return "" + ((SerialOid) oid).getSerialNo();
	}

	public Oid recreateOid(final Results rs,
			final ObjectSpecification specification) {
		PrimaryKey key;
		Object object = rs.getObject(column);
		if (object == null) {
			return null;
		} else {
			int id = ((Integer) object).intValue();
			key = new IntegerPrimaryKey(id);
		}
		Oid oid = new SqlOid(specification.getFullName(), key, State.PERSISTENT);
		return oid;
	}

	protected ObjectAdapter getAdapter(final ObjectSpecification specification,
			final Oid oid) {
		AdapterManager objectLoader = IsisContext.getPersistenceSession()
				.getAdapterManager();
		ObjectAdapter adapter = objectLoader.getAdapterFor(oid);
		if (adapter != null) {
			return adapter;
		} else {
			return IsisContext.getPersistenceSession().recreateAdapter(oid,
					specification);
		}
	}

}
