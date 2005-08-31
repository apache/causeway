package org.nakedobjects.persistence.sql;

import org.nakedobjects.container.configuration.ComponentException;
import org.nakedobjects.container.configuration.ComponentLoader;
import org.nakedobjects.container.configuration.ConfigurationException;
import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.NakedObjectManager;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.ObjectManagerException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.TitleCriteria;
import org.nakedobjects.object.persistence.UnsupportedFindException;

import java.util.Hashtable;

import org.apache.log4j.Logger;


public final class SqlObjectStore implements NakedObjectStore {
    static final String BASE_NAME = "sql-object-store";
    private static final Logger LOG = Logger.getLogger(SqlObjectStore.class);
    private ObjectMapperLookup mapperLookup;
    private DatabaseConnectorPool connectionPool;
    private Hashtable transactionOrientedConnections;

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
        NakedClass cls = mapperLookup.getNakedClassMapper(connection).getNakedClass(connection, name);
        releaseConnectionIfNotInTransaction(connection);
        return cls;
    }
    
    public void createObject(NakedObject object) throws ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
        mapperLookup.getMapper(connection, object).createObject(connection, object);
        releaseConnectionIfNotInTransaction(connection);
    }

    public void destroyObject(NakedObject object) throws ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
        mapperLookup.getMapper(connection, object).destroyObject(connection, object);
        releaseConnectionIfNotInTransaction(connection);
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) throws ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
        NakedObject[] instances = mapperLookup.getMapper(connection, cls).getInstances(connection, cls);
        releaseConnectionIfNotInTransaction(connection);
        return instances;
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) throws ObjectNotFoundException, ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
        NakedObject object = mapperLookup.getMapper(connection, hint).getObject(connection, oid, hint);
        releaseConnectionIfNotInTransaction(connection);
        return object;
    }
    
    public NakedObject[] getInstances(InstancesCriteria criteria) throws ObjectManagerException,
            UnsupportedFindException {

        if(criteria instanceof TitleCriteria) {
	        DatabaseConnector connection = getDatabaseConnector();
	        NakedObjectSpecification spec = criteria.getSpecification();
	        ObjectMapper mapper = mapperLookup.getMapper(connection, spec);
            NakedObject[] instances = mapper.getInstances(connection, spec, ((TitleCriteria) criteria).getRequiredTitle());
	        releaseConnectionIfNotInTransaction(connection);
	        return instances;
        }
        
        throw new UnsupportedFindException();
    }
    
    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) throws ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
        boolean hasInstances = mapperLookup.getMapper(connection, cls).hasInstances(connection, cls);
        releaseConnectionIfNotInTransaction(connection);
        return hasInstances;
    }

    public void init() throws ConfigurationException, ComponentException, ObjectManagerException {
        transactionOrientedConnections = new Hashtable();
        
        DatabaseConnectorFactory connectorFactory = (DatabaseConnectorFactory) ComponentLoader.
        		loadComponent(BASE_NAME + ".connector", DatabaseConnectorFactory.class);
        connectionPool = new DatabaseConnectorPool(connectorFactory);
        
        ObjectMapperFactory mapperFactory = (ObjectMapperFactory) ComponentLoader.
    			loadComponent(BASE_NAME + ".automapper", ObjectMapperFactory.class);
        
        mapperLookup = new ObjectMapperLookup();
        
        mapperLookup.setMapperFactory(mapperFactory, connectionPool);
        mapperLookup.init();
    }

    public String name() {
        return "SQL Object Store";
    }

    public int numberOfInstances(NakedObjectSpecification cls, boolean includedSubclasses) throws ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
        int number = mapperLookup.getMapper(connection, cls).numberOfInstances(connection, cls);
        releaseConnectionIfNotInTransaction(connection);
        return number;
    }

    public void resolveImmediately(NakedObject object) throws ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
       mapperLookup.getMapper(connection, object).resolve(connection, object);
        releaseConnectionIfNotInTransaction(connection);
    }

    public void save(NakedObject object) throws ObjectManagerException {
        DatabaseConnector connection = getDatabaseConnector();
       if (object instanceof InternalCollection) {
            object = ((InternalCollection) object).parent();
            LOG.debug("change to internal collection being persisted through parent");

            // TODO a better plan would be ask the mapper to save the collection
            // - saveCollection(parent, collection)
        }
        mapperLookup.getMapper(connection, object).save(connection, object);
        releaseConnectionIfNotInTransaction(connection);
    }

    public void setObjectManager(NakedObjectManager manager) {}

    public void shutdown() throws ObjectManagerException {
        mapperLookup.shutdown();
        connectionPool.shutdown();
    }

    public void startTransaction() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        DatabaseConnector connector;
        if(transactionOrientedConnections.containsKey(thread)) {
            connector = (DatabaseConnector) transactionOrientedConnections.get(thread);
        } else {
	        connector = connectionPool.acquire();
	        transactionOrientedConnections.put(thread, connector);
        }
        connector.startTransaction();
    }

    private DatabaseConnector getDatabaseConnector() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        if(transactionOrientedConnections.contains(thread)) {
            return (DatabaseConnector) transactionOrientedConnections.get(thread);
        } else {
            return connectionPool.acquire();
        }
    }
    
    private void releaseConnectionIfNotInTransaction(DatabaseConnector connection) {
        Thread thread = Thread.currentThread();
        if(!transactionOrientedConnections.contains(thread)) {
            connectionPool.release(connection);
        } else {
            throw new NakedObjectRuntimeException();
        }
    }

    public void endTransaction() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        DatabaseConnector connector = (DatabaseConnector) transactionOrientedConnections.get(thread);
        connector.endTransaction();
        if(connector.isTransactionComplete()) {
            connector.commit();            
            connectionPool.release(connector);
            transactionOrientedConnections.remove(thread);
        }
   }

    public void abortTransaction() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        DatabaseConnector connector = (DatabaseConnector) transactionOrientedConnections.get(thread);
        connector.rollback();
        connectionPool.release(connector);
        transactionOrientedConnections.remove(thread);
    }
}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2005 Naked Objects Group
 * Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address
 * of Naked Objects Group is Kingsway House, 123 Goldworth Road, Woking GU21
 * 1NR, UK).
 */