package org.nakedobjects.persistence.sql2;

import org.nakedobjects.object.LoadedObjects;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedClassManager;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectRuntimeException;
import org.nakedobjects.object.ObjectStoreException;
import org.nakedobjects.object.collection.InternalCollection;
import org.nakedobjects.utility.Assert;
import org.nakedobjects.utility.NotImplementedException;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;


public class ObjectMapperFactory {
    private static final Logger LOG = Logger.getLogger(ObjectMapperFactory.class);
    private static final NakedClass nakedClass = NakedClassManager.getInstance().getNakedClass(NakedClass.class.getName());
    private final Hashtable mappers = new Hashtable();
    private final LoadedObjects loadedObjects;
    private ObjectMapper defaulMapper;
    
    public ObjectMapperFactory(LoadedObjects loadedObjects) {
        Assert.assertNotNull(loadedObjects);
        this.loadedObjects = loadedObjects;
    }
    
    public ObjectMapper getMapper(NakedClass cls) {
        ObjectMapper mapper = (ObjectMapper) mappers.get(cls);
        if (mapper == null) {
            mapper = defaulMapper;
        }
        LOG.debug("  mapper for " + cls.getSingularName() + " -> " + mapper);
        if(mapper == null) {
            throw new NakedObjectRuntimeException("No mapper for " + cls + " (no default mapper)");
        }
        return mapper;
    }

    public ObjectMapper getMapper(NakedObject object) {
        if (object instanceof InternalCollection) {
            object = ((InternalCollection) object).forParent();
            return getMapper(object.getNakedClass());
        } else {
            return getMapper(object.getNakedClass());
        }
    }

    public ObjectMapper getMapper(Object oid) {
        throw new NotImplementedException("" + oid);
    }

    public void setDefault(ObjectMapper mapper) throws ObjectStoreException {
        LOG.debug("set default mapper " + mapper);
        defaulMapper = mapper;
        defaulMapper.startup(loadedObjects);
    }

    public void setNakedClassMapper(NakedClassMapper mapper) throws ObjectStoreException {
        LOG.debug("set naked class mapper " + mapper);
		add(nakedClass, mapper);
    }

    public void add(String className, ObjectMapper mapper) throws ObjectStoreException {
        NakedClass cls = NakedClassManager.getInstance().getNakedClass(className);
        add(cls, mapper);
    }

    private void add(NakedClass cls, ObjectMapper mapper) throws ObjectStoreException {
		LOG.debug("add mapper " + mapper + " for " + cls);
        mapper.startup(loadedObjects);
        mappers.put(cls, mapper);
	}

	public NakedClassMapper getNakedClassMapper() {
        return (NakedClassMapper) getMapper(nakedClass);
    }

    public void init() throws ObjectStoreException {
    }

    public void shutdown() {
        try {
            if(defaulMapper != null) {
                defaulMapper.shutdown();
            }
        } catch (ObjectStoreException e) {
           LOG.error("Shutdown default mapper", e);
        }
        Enumeration e = mappers.elements();
        while (e.hasMoreElements()) {
        	ObjectMapper mapper = (ObjectMapper) e.nextElement();
            try {
                mapper.shutdown();
            } catch (ObjectStoreException ex) {
                LOG.error("Shutdown mapper " + mapper, ex);
            }
        }    
    }
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