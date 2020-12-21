package org.apache.isis.persistence.jdo.integration.services;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jdo.Extent;
import javax.jdo.JDOQLTypedQuery;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.datastore.JDOConnection;
import javax.jdo.query.BooleanExpression;

import org.springframework.stereotype.Service;

import org.apache.isis.applib.FatalException;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.core.metamodel.adapter.oid.ObjectPersistenceException;
import org.apache.isis.persistence.jdo.applib.services.IsisJdoSupport_v3_2;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

@Service
@Named("isisJdoIntegration.DnJdoSupport")
public class DnJdoSupport implements IsisJdoSupport_v3_2 {

    @Inject @Named("transaction-aware-pmf-proxy") 
    private PersistenceManagerFactory pmf;
    
    @Override
    public <T> T refresh(final T domainObject) {
        getJdoPersistenceManager().refresh(domainObject);
        return domainObject;
    }


    @Override
    public void ensureLoaded(final Collection<?> domainObjects) {
        getJdoPersistenceManager().retrieveAll(domainObjects);
    }

    // //////////////////////////////////////


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
            final Extent<?> extent = getJdoPersistenceManager().getExtent(pcClass);
            final List<Object> instances = stream(extent).collect(Collectors.toList());

            try {
                getJdoPersistenceManager().deletePersistentAll(instances);
            } catch (final Exception ex) {
                throw new FatalException(ex);
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

    @Override
    public PersistenceManager getJdoPersistenceManager() {
        return pmf.getPersistenceManager();
    }

}
