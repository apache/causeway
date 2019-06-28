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
package org.apache.isis.jdo.datanucleus.valuetypes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.mapping.MappingManager;
import org.datanucleus.store.rdbms.mapping.java.SingleFieldMultiMapping;
import org.datanucleus.store.rdbms.table.Column;
import org.datanucleus.store.rdbms.table.Table;
import org.apache.isis.applib.value.Money;

public class IsisMoneyMapping extends SingleFieldMultiMapping {

    public IsisMoneyMapping() {

    }

    @Override
    public Class<?> getJavaType() {

        return org.apache.isis.applib.value.Money.class;
    }

    @Override
    public void initialize(final AbstractMemberMetaData mmd, final Table container, final ClassLoaderResolver clr) {

        super.initialize(mmd, container, clr);
        addColumns();
    }

    @Override
    public void initialize(final RDBMSStoreManager storeMgr, final String type) {

        super.initialize(storeMgr, type);

        addColumns();
    }

    private void addColumns() {

        // amount
        addColumns(ClassNameConstants.JAVA_LANG_LONG);

        // currency
        addColumnWithLength(ClassNameConstants.JAVA_LANG_STRING, 3);
    }

    public void addColumnWithLength(final String typeName, final int columnLength) {

        final MappingManager mgr = getStoreManager().getMappingManager();
        Column column = null;
        if (table != null) {
            column = mgr.createColumn(this, typeName, getNumberOfColumnMappings());
            /* TODO metaData.setJdbcType("NCHAR") */
            column.setColumnMetaData(column.getColumnMetaData().setLength(columnLength));
        }
        mgr.createColumnMapping(this, column, typeName);
    }

    @Override
    public Object getValueForColumnMapping(final NucleusContext nucleusCtx, final int index, final Object value) {

        final Money m = ((Money) value);
        switch (index) {
        case 0:
            return m.longValue();
        case 1:
            return m.getCurrency();
        }
        throw new IndexOutOfBoundsException("Wrong index: " + index);
    }

    @Override
    public void setObject(final ExecutionContext ec, final PreparedStatement preparedStmt, final int[] exprIndex,
            final Object value) {

        if (value instanceof Money) {
            final Money m = ((Money) value);
            getColumnMapping(0).setLong(preparedStmt, exprIndex[0], m.longValue());
            getColumnMapping(1).setString(preparedStmt, exprIndex[1], m.getCurrency());
        } else {
            getColumnMapping(0).setLong(preparedStmt, exprIndex[0], 0l);
            getColumnMapping(1).setString(preparedStmt, exprIndex[1], null);
        }
    }

    @Override
    public Object getObject(final ExecutionContext ec, final ResultSet resultSet, final int[] exprIndex) {

        try {
            // Check for null entries
            if (getColumnMapping(0).getObject(resultSet, exprIndex[0]) == null
                    || getColumnMapping(1).getObject(resultSet, exprIndex[1]) == null) {
                return null;
            }
        } catch (final Exception e) {
            // Do nothing
        }

        final long amount = getColumnMapping(0).getLong(resultSet, exprIndex[0]);
        final String currency = getColumnMapping(1).getString(resultSet, exprIndex[1]);
        if (currency == null) {
            return null;
        }
        return new Money(((Long) amount).doubleValue() / 100, currency);
    }

}
