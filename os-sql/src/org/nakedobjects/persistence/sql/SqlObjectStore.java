package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ComponentLoader;
import org.nakedobjects.utility.ConfigurationException;

import java.util.Vector;

import org.apache.log4j.Logger;


public final class SqlObjectStore implements NakedObjectStore {
    static final String BASE_NAME = "sql-object-store";
    private static final Logger LOG = Logger.getLogger(SqlObjectStore.class);
    private LoadedObjects loaded = new LoadedObjects();
    private ObjectMapperLookup mapperLookup;
    private DatabaseConnectorPool connectorPool;

    public void abortTransaction() {}

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        return mapperLookup.getNakedClassMapper().getNakedClass(name);
    }
    
    public void createNakedClass(NakedClass cls) throws ObjectStoreException {
        mapperLookup.getNakedClassMapper().createNakedClass(cls);
    }
    
    public void createObject(NakedObject object) throws ObjectStoreException {
        mapperLookup.getMapper(object).createObject(object);
    }

    public void destroyObject(NakedObject object) throws ObjectStoreException {
        mapperLookup.getMapper(object).destroyObject(object);
    }

    public void endTransaction() {}

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public Vector getInstances(NakedClass cls, boolean includeSubclasses) throws ObjectStoreException {
        return mapperLookup.getMapper(cls).getInstances(cls);
    }

    public Vector getInstances(NakedClass cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        return mapperLookup.getMapper(cls).getInstances(cls, pattern);
    }

    public Vector getInstances(NakedObject pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        return mapperLookup.getMapper(pattern).getInstances(pattern);
    }

    public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException {
        return mapperLookup.getMapper(hint).getObject(oid, hint);
    }

    public LoadedObjects getLoadedObjects() {
        return loaded;
    }
    
    public boolean hasInstances(NakedClass cls, boolean includeSubclasses) throws ObjectStoreException {
        return mapperLookup.getMapper(cls).hasInstances(cls);
    }

    public void init() throws ConfigurationException, ComponentException, ObjectStoreException {
        DatabaseConnectorFactory connectorFactory = (DatabaseConnectorFactory) ComponentLoader.
        		loadComponent(BASE_NAME + ".connector", DatabaseConnectorFactory.class);
        connectorPool = new DatabaseConnectorPool(connectorFactory);
        
        ObjectMapperFactory mapperFactory = (ObjectMapperFactory) ComponentLoader.
    			loadComponent(BASE_NAME + ".automapper", ObjectMapperFactory.class);
        
        Connection connection = new Connection(connectorPool);
        mapperLookup = new ObjectMapperLookup(loaded, connection);
        
        mapperLookup.setMapperFactory(mapperFactory, connectorPool);
        mapperLookup.init();
    }

    public String name() {
        return "SQL Object Store";
    }

    public int numberOfInstances(NakedClass cls, boolean includedSubclasses) throws ObjectStoreException {
        return mapperLookup.getMapper(cls).numberOfInstances(cls);
    }

    public void resolve(NakedObject object) throws ObjectStoreException {
        mapperLookup.getMapper(object).resolve(object);
    }

    public void save(NakedObject object) throws ObjectStoreException {
        if (object instanceof InternalCollection) {
            object = ((InternalCollection) object).forParent();
            LOG.debug("change to internal collection being persisted through parent");

            // TODO a better plan would be ask the mapper to save the collection
            // - saveCollection(parent, collection)
        }
        mapperLookup.getMapper(object).save(object);
    }

    public void setObjectManager(NakedObjectManager manager) {}

    public void shutdown() throws ObjectStoreException {
        mapperLookup.shutdown();
        connectorPool.shutdown();
    }

    public void startTransaction() {}

}

/*
 * Naked Objects - a framework that exposes behaviourally complete business
 * objects directly to the user. Copyright (C) 2000 - 2004 Naked Objects Group
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