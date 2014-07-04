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

package org.apache.isis.objectstore.jdo.datanucleus.service.support;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.datastore.JDOConnection;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.isis.applib.FatalException;
import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager;
import org.apache.isis.core.metamodel.adapter.mgr.AdapterManager.ConcurrencyChecking;
import org.apache.isis.core.metamodel.services.ServicesInjectorSpi;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.runtime.system.persistence.PersistenceSession;
import org.apache.isis.objectstore.jdo.applib.service.support.IsisJdoSupport;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusObjectStore;

/**
 * This service provdes a number of utility methods to supplement/support the capabilities of the JDO Objectstore.
 *
 * <p>
 * This implementation has no UI and there are no other implementations of the service API, and so it is annotated
 * with {@link org.apache.isis.applib.annotation.DomainService}.  Because it is implemented in the core, this means
 * that it is automatically registered and available for use; no further configuration is required.
 */
@DomainService
@Hidden
public class IsisJdoSupportImpl implements IsisJdoSupport {
    
    @Programmatic
    @Override
    public <T> T refresh(T domainObject) {
        DataNucleusObjectStore objectStore = getObjectStore();
        ObjectAdapter adapter = getAdapterManager().adapterFor(domainObject);
        objectStore.refreshRoot(adapter);
        return domainObject;
    }

    @Programmatic
    @Override
    public void ensureLoaded(Collection<?> domainObjects) {
        getObjectStore().getPersistenceManager().retrieveAll(domainObjects);
    }

    // //////////////////////////////////////

    @Programmatic
    @Override
    public List<Map<String, Object>> executeSql(String sql) {
        final JDOConnection dataStoreConnection = getJdoPersistenceManager().getDataStoreConnection();
        try {
            final Object connectionObj = dataStoreConnection.getNativeConnection();
            if(!(connectionObj instanceof java.sql.Connection)) {
                return null;
            } 
            java.sql.Connection connection = (java.sql.Connection) connectionObj;
            return executeSql(connection, sql);
        } finally {
            dataStoreConnection.close();
        }
    }

    @Programmatic
    @Override
    public Integer executeUpdate(String sql) {
        final JDOConnection dataStoreConnection = getJdoPersistenceManager().getDataStoreConnection();
        try {
            final Object connectionObj = dataStoreConnection.getNativeConnection();
            if(!(connectionObj instanceof java.sql.Connection)) {
                return null;
            } 
            java.sql.Connection connection = (java.sql.Connection) connectionObj;
            return executeUpdate(connection, sql);
        } finally {
            dataStoreConnection.close();
        }
    }

    private static List<Map<String, Object>> executeSql(java.sql.Connection connection, String sql) {
        final List<Map<String,Object>> rows = Lists.newArrayList();

        Statement statement = null;
        try {
            statement = connection.createStatement();
            final ResultSet rs = statement.executeQuery(sql);
            final ResultSetMetaData rsmd = rs.getMetaData();
            while(rs.next()) {
                Map<String,Object> row = Maps.newLinkedHashMap(); 
                final int columnCount = rsmd.getColumnCount();
                for(int i=0; i<columnCount; i++) {
                    final Object val = rs.getObject(i+1);
                    row.put(rsmd.getColumnName(i+1), val);
                }
                rows.add(row);
            }
            
        } catch (SQLException ex) {
            throw new ObjectPersistenceException("Failed to executeSql: " + sql, ex);
        } finally {
            closeSafely(statement);
        }

        return rows;
    }

    private static int executeUpdate(java.sql.Connection connection, String sql) {
        
        Statement statement = null;
        try {
            statement = connection.createStatement();
            return statement.executeUpdate(sql);
            
        } catch (SQLException ex) {
            throw new ObjectPersistenceException("Failed to executeSql: " + sql, ex);
        } finally {
            closeSafely(statement);
        }
    }

    private static void closeSafely(Statement statement) {
        if(statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                // ignore
            }
        }
    }
    
    // //////////////////////////////////////

    @Programmatic
    @Override
    public void deleteAll(Class<?>... pcClasses) {
        for (Class<?> pcClass : pcClasses) {
            final Extent<?> extent = getJdoPersistenceManager().getExtent(pcClass);
            final List<Object> instances = Lists.newArrayList(extent.iterator());
            
            // temporarily disable concurrency checking while this method is performed
            try {
                ConcurrencyChecking.executeWithConcurrencyCheckingDisabled(new Callable<Void>(){
                    @Override
                    public Void call() {
                        getJdoPersistenceManager().deletePersistentAll(instances);
                        return null;
                    }
                });
            } catch (final Exception ex) {
                throw new FatalException(ex);
            }
        }
    }

    // //////////////////////////////////////
    
    
    protected DataNucleusObjectStore getObjectStore() {
        return (DataNucleusObjectStore) getPersistenceSession().getObjectStore();
    }

    protected AdapterManager getAdapterManager() {
        return getPersistenceSession().getAdapterManager();
    }

    protected PersistenceSession getPersistenceSession() {
        return IsisContext.getPersistenceSession();
    }

    protected ServicesInjectorSpi getServicesInjector() {
        return getPersistenceSession().getServicesInjector();
    }

    @Programmatic
    @Override
    public PersistenceManager getJdoPersistenceManager() {
        return getObjectStore().getPersistenceManager();
    }





}
