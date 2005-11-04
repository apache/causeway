package org.nakedobjects.persistence.sql;

import org.nakedobjects.object.InternalCollection;
import org.nakedobjects.object.NakedClass;
import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.NakedObjectSpecification;
import org.nakedobjects.object.NakedObjects;
import org.nakedobjects.object.ObjectPerstsistenceException;
import org.nakedobjects.persistence.sql.auto.AutoMapper;
import org.nakedobjects.utility.NakedObjectConfiguration;
import org.nakedobjects.utility.NakedObjectRuntimeException;
import org.nakedobjects.utility.NotImplementedException;
import org.nakedobjects.utility.configuration.ComponentException;
import org.nakedobjects.utility.configuration.ComponentLoader;
import org.nakedobjects.utility.configuration.PropertiesConfiguration;
import org.nakedobjects.utility.configuration.ConfigurationException;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;


public class ObjectMapperLookup {
    private static final Logger LOG = Logger.getLogger(ObjectMapperLookup.class);
    private final Hashtable mappers = new Hashtable();
    private ObjectMapperFactory mapperFactory;
    private DatabaseConnectorPool connectionPool;

    public ObjectMapper getMapper(DatabaseConnector connection, NakedObjectSpecification cls) throws SqlObjectStoreException {
        ObjectMapper mapper = (ObjectMapper) mappers.get(cls);
        if (mapper == null) {
            AutoMapper autoMapper = (AutoMapper) mapperFactory.createMapper(cls.getFullName(), SqlObjectStore.BASE_NAME
                    + ".automapper.default");
            // DatabaseConnector connection = connectionPool.acquire();
            autoMapper.startup(connection, this);
            if (autoMapper.needsTables(connection)) {
                autoMapper.createTables(connection);
            }
            mapper = autoMapper;
            // connectionPool.release(connection);
        }
        LOG.debug("  mapper for " + cls.getSingularName() + " -> " + mapper);
        if (mapper == null) {
            throw new NakedObjectRuntimeException("No mapper for " + cls + " (no default mapper)");
        }
        return mapper;
    }

    public ObjectMapper getMapper(DatabaseConnector connection, NakedObject object) throws SqlObjectStoreException {
        if (object instanceof InternalCollection) {
            object = ((InternalCollection) object).parent();
            return getMapper(connection, object.getSpecification());
        } else {
            return getMapper(connection, object.getSpecification());
        }
    }

    public ObjectMapper getMapper(Object oid) {
        throw new NotImplementedException("" + oid);
    }

    public void setConnectionPool(DatabaseConnectorPool connectionPool) {
        this.connectionPool = connectionPool;
    }
    
    public void setMapperFactory(ObjectMapperFactory mapperFactory) {
        this.mapperFactory = mapperFactory;
    }
    
    private void add(String className, ObjectMapper mapper) throws SqlObjectStoreException {
        NakedObjectSpecification cls = NakedObjects.getSpecificationLoader().loadSpecification(className);
        add(cls, mapper);
    }

    private void add(NakedObjectSpecification cls, ObjectMapper mapper) throws SqlObjectStoreException {
        LOG.debug("add mapper " + mapper + " for " + cls);
        DatabaseConnector connection = connectionPool.acquire();
        mapper.startup(connection, this);
        connectionPool.release(connection);
        mappers.put(cls, mapper);
    }

    public void init() {
        String BASE_NAME = SqlObjectStore.BASE_NAME;

     //   Properties properties = NakedObjects.getConfiguration().getPropertiesStrippingPrefix(BASE_NAME + ".mapper.");

        NakedObjectConfiguration subset = NakedObjects.getConfiguration().getConfigurationSubsetStrippingPrefix(BASE_NAME + ".mapper.");
        
        if (properties.size() == 0) {
            throw new ConfigurationException("No mappers defined");

        }

        Enumeration e = properties.keys();
        while (e.hasMoreElements()) {
            String className = (String) e.nextElement();
            String value = properties.getProperty(className);

            if (value.startsWith("auto.")) {
                add(className, mapperFactory.createMapper(className, BASE_NAME + ".automapper." + value.substring(5) + "."));
            } else if (value.trim().equals("auto")) {
                add(className, mapperFactory.createMapper(className, BASE_NAME + ".automapper.default"));
            } else {
	            LOG.debug("mapper " + className + "=" + value);
	
	            try {
	                add(className, (ObjectMapper) ComponentLoader.loadNamedComponent(value, ObjectMapper.class));
	            } catch (ObjectPerstsistenceException ex) {
	                throw new ComponentException("Failed to set up mapper for " + className, ex);
	            }
            }
        }
    }

    public void shutdown() {
        Enumeration e = mappers.elements();
        while (e.hasMoreElements()) {
            ObjectMapper mapper = (ObjectMapper) e.nextElement();
            try {
                mapper.shutdown();
            } catch (ObjectPerstsistenceException ex) {
                LOG.error("Shutdown mapper " + mapper, ex);
            }
        }
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
