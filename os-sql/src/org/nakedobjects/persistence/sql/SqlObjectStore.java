package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.persistence.CreateObjectCommand;
import org.nakedobjects.object.persistence.DestroyObjectCommand;
import org.nakedobjects.object.persistence.InstancesCriteria;
import org.nakedobjects.object.persistence.NakedObjectStore;
import org.nakedobjects.object.persistence.ObjectNotFoundException;
import org.nakedobjects.object.persistence.Oid;
import org.nakedobjects.object.persistence.PersistenceCommand;
import org.nakedobjects.object.persistence.SaveObjectCommand;
import org.nakedobjects.object.persistence.TitleCriteria;
import org.nakedobjects.object.persistence.UnsupportedFindException;
import org.nakedobjects.object.reflect.NakedObjectField;
import org.nakedobjects.utility.UnexpectedCallException;

import java.util.Hashtable;

import org.apache.log4j.Logger;


public final class SqlObjectStore implements NakedObjectStore {
    static final String BASE_NAME = "sql-object-store";
    private static final Logger LOG = Logger.getLogger(SqlObjectStore.class);
    private DatabaseConnectorPool connectionPool;
    private ObjectMapperLookup mapperLookup;
    private Hashtable transactionOrientedConnections;

    public void abortTransaction() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        DatabaseConnector connector = (DatabaseConnector) transactionOrientedConnections.get(thread);
        connector.rollback();
        connectionPool.release(connector);
        transactionOrientedConnections.remove(thread);
    }

    public CreateObjectCommand createCreateObjectCommand(final NakedObject object) {
        return new CreateObjectCommand() {
            public void execute() {
                LOG.debug("  create object " + object);
                DatabaseConnector connection = getDatabaseConnector();
                mapperLookup.getMapper(connection, object).createObject(connection, object);
                releaseConnectionIfNotInTransaction(connection);
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "CreateObjectCommand [object=" + object + "]";
            }
        };
    }

    public DestroyObjectCommand createDestroyObjectCommand(final NakedObject object) {
        return new DestroyObjectCommand() {
            public void execute() {
                LOG.debug("  destroy object " + object);
                DatabaseConnector connection = getDatabaseConnector();
                mapperLookup.getMapper(connection, object).destroyObject(connection, object);
                releaseConnectionIfNotInTransaction(connection);
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "DestroyObjectCommand [object=" + object + "]";
            }
        };
    }

    public SaveObjectCommand createSaveObjectCommand(final NakedObject object) {
        return new SaveObjectCommand() {
            public void execute() {
                LOG.debug("  save object " + object);
                DatabaseConnector connection = getDatabaseConnector();
                if (object instanceof InternalCollection) {
                    NakedObject parent = ((InternalCollection) object).parent();
                    LOG.debug("change to internal collection being persisted through parent");

                    // TODO a better plan would be ask the mapper to save the collection
                    // - saveCollection(parent, collection)
	                mapperLookup.getMapper(connection, parent).save(connection, parent);
	                releaseConnectionIfNotInTransaction(connection);
	                
                } else {
                    mapperLookup.getMapper(connection, object).save(connection, object);
	                releaseConnectionIfNotInTransaction(connection);
                }
            }

            public NakedObject onObject() {
                return object;
            }

            public String toString() {
                return "SaveObjectCommand [object=" + object + "]";
            }

        };
    }

    public void endTransaction() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        DatabaseConnector connector = (DatabaseConnector) transactionOrientedConnections.get(thread);
        connector.endTransaction();
        if (connector.isTransactionComplete()) {
            connector.commit();
            connectionPool.release(connector);
            transactionOrientedConnections.remove(thread);
        }
    }

    private DatabaseConnector getDatabaseConnector() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        if (transactionOrientedConnections.contains(thread)) {
            return (DatabaseConnector) transactionOrientedConnections.get(thread);
        } else {
            return connectionPool.acquire();
        }
    }

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public NakedObject[] getInstances(InstancesCriteria criteria) {
        if (criteria instanceof TitleCriteria) {
            DatabaseConnector connection = getDatabaseConnector();
            NakedObjectSpecification spec = criteria.getSpecification();
            ObjectMapper mapper = mapperLookup.getMapper(connection, spec);
            NakedObject[] instances = mapper.getInstances(connection, spec, ((TitleCriteria) criteria).getRequiredTitle());
            releaseConnectionIfNotInTransaction(connection);
            return instances;
        }

        throw new UnsupportedFindException();
    }

    public NakedObject[] getInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        DatabaseConnector connection = getDatabaseConnector();
        NakedObject[] instances = mapperLookup.getMapper(connection, cls).getInstances(connection, cls);
        releaseConnectionIfNotInTransaction(connection);
        return instances;
    }

    public NakedClass getNakedClass(String name) {
  /*      DatabaseConnector connection = getDatabaseConnector();
        NakedClass cls = mapperLookup.getNakedClassMapper(connection).getNakedClass(connection, name);
        releaseConnectionIfNotInTransaction(connection);
        return cls;
        
        */
        
        throw new ObjectNotFoundException();
    }

    public NakedObject getObject(Oid oid, NakedObjectSpecification hint) {
        DatabaseConnector connection = getDatabaseConnector();
        NakedObject object = mapperLookup.getMapper(connection, hint).getObject(connection, oid, hint);
        releaseConnectionIfNotInTransaction(connection);
        return object;
    }

    public boolean hasInstances(NakedObjectSpecification cls, boolean includeSubclasses) {
        DatabaseConnector connection = getDatabaseConnector();
        boolean hasInstances = mapperLookup.getMapper(connection, cls).hasInstances(connection, cls);
        releaseConnectionIfNotInTransaction(connection);
        return hasInstances;
    }

    public void init() {
        transactionOrientedConnections = new Hashtable();

 /*       DatabaseConnectorFactory connectorFactory = (DatabaseConnectorFactory) ComponentLoader.loadComponent(BASE_NAME
                + ".connector", DatabaseConnectorFactory.class);
        connectionPool = new DatabaseConnectorPool(connectorFactory);

        ObjectMapperFactory mapperFactory = (ObjectMapperFactory) ComponentLoader.loadComponent(BASE_NAME + ".automapper",
                ObjectMapperFactory.class);

        mapperLookup = new ObjectMapperLookup();

        mapperLookup.setMapperFactory(mapperFactory, connectionPool);
*/        mapperLookup.init();
    }

    public String name() {
        return "SQL Object Store";
    }

    public int numberOfInstances(NakedObjectSpecification cls, boolean includedSubclasses)  {
        DatabaseConnector connection = getDatabaseConnector();
        int number = mapperLookup.getMapper(connection, cls).numberOfInstances(connection, cls);
        releaseConnectionIfNotInTransaction(connection);
        return number;
    }

    private void releaseConnectionIfNotInTransaction(DatabaseConnector connection) {
        Thread thread = Thread.currentThread();
        if (!transactionOrientedConnections.contains(thread)) {
            connectionPool.release(connection);
        } else {
            throw new NakedObjectRuntimeException();
        }
    }

    public void reset() {}

    public void resolveEagerly(NakedObject object, NakedObjectField field) {
        if(field.isCollection()) {
            DatabaseConnector connection = getDatabaseConnector();
            NakedObjectSpecification spec = object.getSpecification();
            mapperLookup.getMapper(connection, spec).resolveCollection(connection, object, field);
        } else {
            throw new UnexpectedCallException();
        }
    }

    public void resolveImmediately(NakedObject object) {
        DatabaseConnector connection = getDatabaseConnector();
        mapperLookup.getMapper(connection, object).resolve(connection, object);
        releaseConnectionIfNotInTransaction(connection);
    }

    public void runTransaction(PersistenceCommand[] commands) {
        for (int i = 0; i < commands.length; i++) {
            PersistenceCommand command = commands[i];
            command.execute();
        }
    }

    public void setMapperLookup(ObjectMapperLookup mapperLookup) {
        this.mapperLookup = mapperLookup;
    }
    
    public void setConnectionPool(DatabaseConnectorPool connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    public void shutdown() {
        mapperLookup.shutdown();
        connectionPool.shutdown();
    }

    public void startTransaction() throws SqlObjectStoreException {
        Thread thread = Thread.currentThread();
        DatabaseConnector connector;
        if (transactionOrientedConnections.containsKey(thread)) {
            connector = (DatabaseConnector) transactionOrientedConnections.get(thread);
        } else {
            connector = connectionPool.acquire();
            transactionOrientedConnections.put(thread, connector);
        }
        connector.startTransaction();
    }

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business objects directly to the user.
 * Copyright (C) 2000 - 2005 Naked Objects Group Ltd
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 2 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted via www.nakedobjects.org (the registered address of Naked Objects Group is
 * Kingsway House, 123 Goldworth Road, Woking GU21 1NR, UK).
 */
