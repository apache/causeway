package org.nakedobjects.persistence.sql2;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectManager;
import org.nakedobjects.object.NakedObjectStore;
import org.nakedobjects.object.ObjectNotFoundException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.UnsupportedFindException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.persistence.sql2.mysql.AutoMapper;
import org.nakedobjects.utility.ComponentException;
import org.nakedobjects.utility.ComponentLoader;
import org.nakedobjects.utility.ConfigurationException;
import org.nakedobjects.utility.ConfigurationParameters;

import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;


public class ObjectStore implements NakedObjectStore {
    private static final String BASE_NAME = "sql-object-store-2";
    private static final Logger LOG = Logger.getLogger(ObjectStore.class);
    private LoadedObjects loaded = new LoadedObjects();
    private ObjectMapperFactory factory;

    public void abortTransaction() {}

    public NakedClass getNakedClass(String name) throws ObjectNotFoundException, ObjectStoreException {
        return factory.getNakedClassMapper().getNakedClass(name);
    }
    
    public void createNakedClass(NakedClass cls) throws ObjectStoreException {
        factory.getNakedClassMapper().createNakedClass(cls);
    }
    
    public void createObject(NakedObject object) throws ObjectStoreException {
        factory.getMapper(object).createObject(object);
    }

    public void destroyObject(NakedObject object) throws ObjectStoreException {
        factory.getMapper(object).destroyObject(object);
    }

    public void endTransaction() {}

    public String getDebugData() {
        return null;
    }

    public String getDebugTitle() {
        return null;
    }

    public Vector getInstances(NakedClass cls, boolean includeSubclasses) throws ObjectStoreException {
        return factory.getMapper(cls).getInstances(cls);
    }

    public Vector getInstances(NakedClass cls, String pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        return factory.getMapper(cls).getInstances(cls, pattern);
    }

    public Vector getInstances(NakedObject pattern, boolean includeSubclasses) throws ObjectStoreException, UnsupportedFindException {
        return factory.getMapper(pattern).getInstances(pattern);
    }

    public NakedObject getObject(Object oid, NakedClass hint) throws ObjectNotFoundException, ObjectStoreException {
        return factory.getMapper(hint).getObject(oid, hint);
    }

    public LoadedObjects getLoadedObjects() {
        return loaded;
    }
    
    public boolean hasInstances(NakedClass cls, boolean includeSubclasses) throws ObjectStoreException {
        return factory.getMapper(cls).hasInstances(cls);
    }

    public void init() throws ConfigurationException, ComponentException, ObjectStoreException {
        factory = new ObjectMapperFactory(loaded);
        factory.init();
        
        try {
            factory.setDefault((ObjectMapper) ComponentLoader.loadComponent(BASE_NAME + ".default-mapper",
                    ObjectMapper.class));
        } catch (ObjectStoreException e) {
            throw new ComponentException("Failed to set up default mapper", e);
        }
        
        try {
            factory.setNakedClassMapper((NakedClassMapper) ComponentLoader.loadComponent(BASE_NAME + ".class-mapper",
                    NakedClassMapper.class));
        } catch (ObjectStoreException e) {
            throw new ComponentException("Failed to set up class mapper", e);
        }

        Properties properties = ConfigurationParameters.getInstance().getPropertySubset(BASE_NAME + ".mapper");
        Enumeration e = properties.keys();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            String value = properties.getProperty(key);

            if(value.startsWith("auto.")) {
            	factory.add(key, new AutoMapper(key, BASE_NAME + ".automapper." + value.substring(5) + "."));
            } else  if(value.trim().equals("auto")) {
            	factory.add(key, new AutoMapper(key, BASE_NAME + ".automapper.default"));
            } else {
	            LOG.debug("mapper " + key + "=" + value);
	
	            try {
	                factory.add(key, (ObjectMapper) ComponentLoader.loadNamedComponent(value, ObjectMapper.class));
	            } catch (ObjectStoreException ex) {
	                throw new ComponentException("Failed to set up mapper for " + key, ex);
	            }
            }
        }
        
        
    }

    public String name() {
        return "SQL Object Store II";
    }

    public int numberOfInstances(NakedClass cls, boolean includedSubclasses) throws ObjectStoreException {
        return factory.getMapper(cls).numberOfInstances(cls);
    }

    public void resolve(NakedObject object) throws ObjectStoreException {
        factory.getMapper(object).resolve(object);
    }

    public void save(NakedObject object) throws ObjectStoreException {
        if (object instanceof InternalCollection) {
            object = ((InternalCollection) object).forParent();
            LOG.debug("change to internal collection being persisted through parent");

            // TODO a better plan would be ask the mapper to save the collection
            // - saveCollection(parent, collection)
        }
        factory.getMapper(object).save(object);
    }

    public void setObjectManager(NakedObjectManager manager) {}

    public void shutdown() throws ObjectStoreException {
        factory.shutdown();
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