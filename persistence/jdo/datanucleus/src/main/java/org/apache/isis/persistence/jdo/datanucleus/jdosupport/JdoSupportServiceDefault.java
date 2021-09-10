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
package org.apache.isis.persistence.jdo.datanucleus.jdosupport;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.Extent;
import javax.jdo.JDOQLTypedQuery;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.query.BooleanExpression;

import org.datanucleus.store.rdbms.RDBMSPropertyNames;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.PriorityPrecedence;
import org.apache.isis.applib.exceptions.UnrecoverableException;
import org.apache.isis.applib.exceptions.unrecoverable.ObjectPersistenceException;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.persistence.jdo.applib.services.JdoSupportService;
import org.apache.isis.persistence.jdo.spring.integration.TransactionAwarePersistenceManagerFactoryProxy;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import lombok.val;

@Service
@Named("isis.persistence.jdo.JdoSupportServiceDefault")
@Priority(PriorityPrecedence.MIDPOINT)
@Qualifier("DN5")
public class JdoSupportServiceDefault implements JdoSupportService {

    @Inject private TransactionAwarePersistenceManagerFactoryProxy pmf;
    @Inject private MetaModelContext mmc;

    @Override
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return pmf.getPersistenceManagerFactory();
    }

    @Override
    public <T> T refresh(final T domainObject) {
        val objectManager = mmc.getObjectManager();
        val adapter = mmc.getObjectManager().adapt(domainObject);
        objectManager.refreshObject(adapter);
        return domainObject;
    }

    @Override
    public void ensureLoaded(final Collection<?> domainObjects) {
        getPersistenceManager().retrieveAll(domainObjects);
    }

    // //////////////////////////////////////


    @Override
    public List<Map<String, Object>> executeSql(final String sql) {
        final JDOConnection dataStoreConnection = getPersistenceManager().getDataStoreConnection();
        try {
            final Object connectionObj = dataStoreConnection.getNativeConnection();
            if(!(connectionObj instanceof java.sql.Connection)) {
                return null;
            }
            final java.sql.Connection connection = (java.sql.Connection) connectionObj;
            return executeSql(connection, sql);
        } finally {
            dataStoreConnection.close();
        }
    }


    @Override
    public Integer executeUpdate(final String sql) {
        final JDOConnection dataStoreConnection = getPersistenceManager().getDataStoreConnection();
        try {
            final Object connectionObj = dataStoreConnection.getNativeConnection();
            if(!(connectionObj instanceof java.sql.Connection)) {
                return null;
            }
            final java.sql.Connection connection = (java.sql.Connection) connectionObj;
            return executeUpdate(connection, sql);
        } finally {
            dataStoreConnection.close();
        }
    }

    private static List<Map<String, Object>> executeSql(final java.sql.Connection connection, final String sql) {
        final List<Map<String,Object>> rows = _Lists.newArrayList();

        try(Statement statement = connection.createStatement()) {
            try(final ResultSet rs = statement.executeQuery(sql)) {

                final ResultSetMetaData rsmd = rs.getMetaData();
                while(rs.next()) {
                    final Map<String,Object> row = _Maps.newLinkedHashMap();
                    final int columnCount = rsmd.getColumnCount();
                    for(int i=0; i<columnCount; i++) {
                        final Object val = rs.getObject(i+1);
                        row.put(rsmd.getColumnName(i+1), val);
                    }
                    rows.add(row);
                }
            }

        } catch (final SQLException ex) {
            throw new ObjectPersistenceException("Failed to executeSql: " + sql, ex);
        }

        return rows;
    }

    private static int executeUpdate(final java.sql.Connection connection, final String sql) {

        try(Statement statement = connection.createStatement()){
            return statement.executeUpdate(sql);

        } catch (final SQLException ex) {
            throw new ObjectPersistenceException("Failed to executeSql: " + sql, ex);
        }
    }

    // //////////////////////////////////////


    @Override
    public void deleteAll(final Class<?>... pcClasses) {
        for (final Class<?> pcClass : pcClasses) {
            final Extent<?> extent = getPersistenceManager().getExtent(pcClass);
            final List<Object> instances = stream(extent).collect(Collectors.toList());

            try {
                getPersistenceManager().deletePersistentAll(instances);
            } catch (final Exception ex) {
                throw new UnrecoverableException(ex);
            }
        }
    }

    // //////////////////////////////////////


    @Override
    public <T> List<T> executeQuery(final Class<T> cls, final BooleanExpression filter) {
        JDOQLTypedQuery<T> query = newTypesafeQuery(cls);
        if(filter!=null) {
            query = query.filter(filter);
        }
        return executeListAndClose(query);
    }


    @Override
    public <T> T executeQueryUnique(final Class<T> cls, final BooleanExpression filter) {
        JDOQLTypedQuery<T> query = newTypesafeQuery(cls);
        if(filter!=null) {
            query = query.filter(filter);
        }
        return executeUniqueAndClose(query);
    }


    @Override
    public <T> JDOQLTypedQuery<T> newTypesafeQuery(Class<T> cls) {
        return getPersistenceManager().newJDOQLTypedQuery(cls);
    }

    private static <T> List<T> executeListAndClose(final JDOQLTypedQuery<T> query) {
        try {
            final List<T> elements = query.executeList();
            final List<T> list = _Lists.newArrayList(elements);
            return list;
        } finally {
            query.closeAll();
        }
    }

    private static <T> T executeUniqueAndClose(final JDOQLTypedQuery<T> query) {
        try {
            final T result = query.executeUnique();
            return result;
        } finally {
            query.closeAll();
        }
    }

    @Override
    public void disableMultivaluedFetch(JDOQLTypedQuery<?> query) {
        query.extension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
    }

    @Override
    public void disableMultivaluedFetch(Query<?> query) {
        query.addExtension(RDBMSPropertyNames.PROPERTY_RDBMS_QUERY_MULTIVALUED_FETCH, "none");
    }


}
