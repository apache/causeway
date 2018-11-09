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

package org.apache.isis.applib.services.jdosupport;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.jdo.Extent;
import javax.jdo.JDOQLTypedQuery;
import javax.jdo.PersistenceManager;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.query.BooleanExpression;

import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.NatureOfService;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.concurrency.ConcurrencyChecking;
import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.core.runtime.system.session.IsisSessionFactory;


/**
 * This service provides a number of utility methods to supplement/support the capabilities of the JDO Objectstore.
 *
 * <p>
 * This implementation has no UI and there are no other implementations of the service API, and so it is annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  Because it is implemented in the core, this means
 * that it is automatically registered and available for use; no further configuration is required.
 */
@DomainService(
        nature = NatureOfService.DOMAIN,
        menuOrder = "" + Integer.MAX_VALUE
        )
public class IsisJdoSupportDN5 implements IsisJdoSupport_v3_2 {

    @Programmatic
    @Override
    public <T> T refresh(final T domainObject) {
        final ObjectAdapter adapter = getPersistenceSession().adapterFor(domainObject);
        getPersistenceSession().refreshRoot(adapter);
        return domainObject;
    }

    @Programmatic
    @Override
    public void ensureLoaded(final Collection<?> domainObjects) {
        getPersistenceSession().getPersistenceManager().retrieveAll(domainObjects);
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public List<Map<String, Object>> executeSql(final String sql) {
        final JDOConnection dataStoreConnection = getJdoPersistenceManager().getDataStoreConnection();
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

    @Programmatic
    @Override
    public Integer executeUpdate(final String sql) {
        final JDOConnection dataStoreConnection = getJdoPersistenceManager().getDataStoreConnection();
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
            final ResultSet rs = statement.executeQuery(sql);
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

    @Programmatic
    @Override
    public void deleteAll(final Class<?>... pcClasses) {
        for (final Class<?> pcClass : pcClasses) {
            final Extent<?> extent = getJdoPersistenceManager().getExtent(pcClass);
            final List<Object> instances = stream(extent).collect(Collectors.toList());

            // temporarily disable concurrency checking while this method is performed
            try {
                ConcurrencyChecking.executeWithConcurrencyCheckingDisabled((Callable<Void>) () -> {
                    getJdoPersistenceManager().deletePersistentAll(instances);
                    return null;
                });
            } catch (final Exception ex) {
                throw new FatalException(ex);
            }
        }
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public <T> List<T> executeQuery(final Class<T> cls, final BooleanExpression filter) {
        JDOQLTypedQuery<T> query = newTypesafeQuery(cls);
        if(filter!=null) {
            query = query.filter(filter);
        }
        return executeListAndClose(query);
    }

    @Programmatic
    @Override
    public <T> T executeQueryUnique(final Class<T> cls, final BooleanExpression filter) {
        JDOQLTypedQuery<T> query = newTypesafeQuery(cls);
        if(filter!=null) {
            query = query.filter(filter);
        }
        return executeUniqueAndClose(query);
    }

    @Programmatic
    @Override
    public <T> JDOQLTypedQuery<T> newTypesafeQuery(Class<T> cls) {
        return getJdoPersistenceManager().newJDOQLTypedQuery(cls);
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

    // //////////////////////////////////////

    @javax.inject.Inject
    IsisSessionFactory isisSessionFactory;

    protected PersistenceSession getPersistenceSession() {
        return isisSessionFactory.getCurrentSession().getPersistenceSession();
    }

    protected ServicesInjector getServicesInjector() {
        return isisSessionFactory.getServicesInjector();
    }

    @Programmatic
    @Override
    public PersistenceManager getJdoPersistenceManager() {
        return getPersistenceSession().getPersistenceManager();
    }
}
