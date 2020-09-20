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
import java.sql.SQLException;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ClassNameConstants;
import org.datanucleus.ExecutionContext;
import org.datanucleus.NucleusContext;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.rdbms.RDBMSStoreManager;
import org.datanucleus.store.rdbms.mapping.java.SingleFieldMultiMapping;
import org.datanucleus.store.rdbms.table.Table;

import org.apache.isis.applib.value.Blob;

public class IsisBlobMapping extends SingleFieldMultiMapping {

    public IsisBlobMapping() {
    }

    @Override
    public Class<?> getJavaType() {
        return org.apache.isis.applib.value.Blob.class;
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
        // this mapping type isn't supported out-of-the-box by DN, but the ByteArayBlobRdbmsMapping that we register supports it
        addColumns(ClassNameConstants.BYTE_ARRAY); // bytes
    }


    @Override
    public Object getValueForColumnMapping(NucleusContext nucleusCtx, int index, Object value)
    {
        Blob blob = ((Blob)value);
        switch (index) {
        case 0: return blob.getName();
        case 1: return blob.getMimeType().getBaseType();
        case 2: return blob.getBytes();
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setObject(ExecutionContext ec, PreparedStatement preparedStmt, int[] exprIndex, Object value)
    {
        Blob blob = ((Blob)value);
        if (blob == null) {
            getColumnMapping(0).setString(preparedStmt, exprIndex[0], null);
            getColumnMapping(1).setString(preparedStmt, exprIndex[1], null);

            // using:
            // getDatastoreMapping(2).setObject(preparedStmt, exprIndex[2], null);
            // fails for PostgreSQL, as interprets as a reference to an oid (pointer to offline blob)
            // rather than a bytea (inline blob)
            try {
                preparedStmt.setBytes(exprIndex[2], null);
            } catch (SQLException e) {
                // ignore
            }
        } else {
            getColumnMapping(0).setString(preparedStmt, exprIndex[0], blob.getName());
            getColumnMapping(1).setString(preparedStmt, exprIndex[1], blob.getMimeType().getBaseType());
            getColumnMapping(2).setObject(preparedStmt, exprIndex[2], blob.getBytes());
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
        final byte[] bytes = (byte[]) getColumnMapping(2).getObject(resultSet, exprIndex[2]);
        if(name == null || mimeTypeBase == null || bytes == null) {
            return null;
        }
        return new Blob(name, mimeTypeBase, bytes);
    }


}
