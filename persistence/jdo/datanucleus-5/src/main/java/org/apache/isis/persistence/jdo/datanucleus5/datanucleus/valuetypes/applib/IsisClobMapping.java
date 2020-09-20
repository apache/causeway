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
package org.apache.isis.persistence.jdo.datanucleus5.datanucleus.valuetypes.applib;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.mapping.java.SingleFieldMultiMapping;
import org.datanucleus.store.rdbms.table.Table;

import org.apache.isis.applib.value.Clob;

public class IsisClobMapping extends SingleFieldMultiMapping {

    public IsisClobMapping() {
    }

    @Override
    public Class<?> getJavaType() {
        return org.apache.isis.applib.value.Clob.class;
    }

    @Override
    public void initialize(AbstractMemberMetaData mmd, Table container, ClassLoaderResolver clr)
    {
        super.initialize(mmd, container, clr);
        addColumns();
    }

    @Override
    public void initialize(RDBMSStoreManager storeMgr, String type)
    {
        super.initialize(storeMgr, type);
        addColumns();
    }

    protected void addColumns()
    {
        addColumns(ClassNameConstants.JAVA_LANG_STRING); // name
        addColumns(ClassNameConstants.JAVA_LANG_STRING); // mime type
        addColumns(ClassNameConstants.JAVA_LANG_STRING); // chars
    }

    @Override
    public Object getValueForColumnMapping(NucleusContext nucleusCtx, int index, Object value)
    {
        Clob clob = ((Clob)value);
        switch (index) {
        case 0: return clob.getName();
        case 1: return clob.getMimeType().getBaseType();
        case 2: return clob.getChars();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setObject(ExecutionContext ec, PreparedStatement preparedStmt, int[] exprIndex, Object value)
    {
        Clob clob = ((Clob)value);
        if (clob == null) {
            getColumnMapping(0).setObject(preparedStmt, exprIndex[0], null);
            getColumnMapping(1).setObject(preparedStmt, exprIndex[1], null);
            getColumnMapping(2).setObject(preparedStmt, exprIndex[2], null);
        } else {
            getColumnMapping(0).setString(preparedStmt, exprIndex[0], clob.getName());
            getColumnMapping(1).setString(preparedStmt, exprIndex[1], clob.getMimeType().getBaseType());
            getColumnMapping(2).setObject(preparedStmt, exprIndex[2], clob.getChars().toString());
        }
    }

    @Override
    public Object getObject(ExecutionContext ec, ResultSet resultSet, int[] exprIndex)
    {
        try
        {
            // Check for null entries
            if (getColumnMapping(0).getObject(resultSet, exprIndex[0]) == null)
            {
                return null;
            }
        }
        catch (Exception e)
        {
            // Do nothing
        }

        final String name = getColumnMapping(0).getString(resultSet, exprIndex[0]);
        final String mimeTypeBase = getColumnMapping(1).getString(resultSet, exprIndex[1]);
        final String str = getColumnMapping(2).getString(resultSet, exprIndex[2]);
        if(name == null || mimeTypeBase == null || str == null) {
            return null;
        }
        return new Clob(name, mimeTypeBase, str.toCharArray());
    }
}
