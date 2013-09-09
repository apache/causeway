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

package org.apache.isis.objectstore.sql;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.debug.DebugBuilder;
import org.apache.isis.core.commons.debug.DebuggableWithTitle;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.factory.InstanceCreationException;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.spec.feature.Contributed;
import org.apache.isis.core.runtime.persistence.ObjectPersistenceException;
import org.apache.isis.core.runtime.system.context.IsisContext;

public class ObjectMappingLookup implements DebuggableWithTitle {
    private static final Logger LOG = LoggerFactory.getLogger(ObjectMappingLookup.class);
    private DatabaseConnectorPool connectionPool;
    private final Map<ObjectSpecification, ObjectMapping> mappings = new HashMap<ObjectSpecification, ObjectMapping>();
    private ObjectMappingFactory objectMappingFactory;
    private FieldMappingLookup fieldMappingLookup;

    public ObjectMapping getMapping(final ObjectSpecification spec, final DatabaseConnector connection) {
    	String fullName = spec.getFullIdentifier();
        ObjectMapping mapping = mappings.get(spec);
        if (mapping == null) {
            final String propertiesBase = SqlObjectStore.BASE_NAME + ".automapper.default";
            mapping = objectMappingFactory.createMapper(fullName, propertiesBase, fieldMappingLookup, this);
            add(spec, mapping, connection);
        }
        LOG.debug("  mapper for " + spec.getSingularName() + " -> " + mapping);
        if (mapping == null) {
            throw new IsisException("No mapper for " + spec + " (no default mapper)");
        }
        return mapping;
    }

    public ObjectMapping getMapping(final ObjectAdapter object, final DatabaseConnector connection) {
        return getMapping(object.getSpecification(), connection);
    }

    public void setConnectionPool(final DatabaseConnectorPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    // / ???
    public void setObjectMappingFactory(final ObjectMappingFactory mapperFactory) {
        this.objectMappingFactory = mapperFactory;
    }

    public void setValueMappingLookup(final FieldMappingLookup fieldMappingLookup) {
        this.fieldMappingLookup = fieldMappingLookup;
    }

    private void add(final String className, final ObjectMapping mapper) {
        final ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(className);
        if (spec.getProperties(Contributed.EXCLUDED).size() == 0) {
            throw new SqlObjectStoreException(spec.getFullIdentifier() + " has no fields to persist: " + spec);
        }
        add(spec, mapper, null);
    }

    public void add(final ObjectSpecification specification, final ObjectMapping mapper, DatabaseConnector connection) {
        LOG.debug("add mapper " + mapper + " for " + specification);
        if (connection == null) {
            connection = connectionPool.acquire();
        }
        mapper.startup(connection, this);
        connectionPool.release(connection);
        mappings.put(specification, mapper);
    }

    public void init() {
        fieldMappingLookup.init();

        final String prefix = SqlObjectStore.BASE_NAME + ".mapper.";
        final IsisConfiguration subset = IsisContext.getConfiguration().createSubset(prefix);
        for (final String className : subset) {
            final String value = subset.getString(className);

            if (value.startsWith("auto.")) {
                final String propertiesBase = SqlObjectStore.BASE_NAME + ".automapper." + value.substring(5) + ".";
                add(className, objectMappingFactory.createMapper(className, propertiesBase, fieldMappingLookup, this));
            } else if (value.trim().equals("auto")) {
                final String propertiesBase = SqlObjectStore.BASE_NAME + ".automapper.default";
                add(className, objectMappingFactory.createMapper(className, propertiesBase, fieldMappingLookup, this));
            } else {
                LOG.debug("mapper " + className + "=" + value);

                try {
                    add(className, InstanceUtil.createInstance(value, ObjectMapping.class));
                } catch (final ObjectPersistenceException ex) {
                    throw new InstanceCreationException("Failed to set up mapper for " + className, ex);
                }
            }
        }
    }

    public void shutdown() {
        for (final ObjectMapping mapping : mappings.values()) {
            try {
                mapping.shutdown();
            } catch (final ObjectPersistenceException ex) {
                LOG.error("Shutdown mapper " + mapping, ex);
            }
        }
    }

    @Override
    public void debugData(final DebugBuilder debug) {
        debug.appendln("field mapping lookup", fieldMappingLookup);
        debug.appendln("object mapping factory", objectMappingFactory);
        debug.appendTitle("Mappings");
        int i = 1;
        for (final ObjectSpecification specification : mappings.keySet()) {
            debug.appendln(i++ + ". " + specification.getShortIdentifier());
            final ObjectMapping mapper = mappings.get(specification);
            debug.indent();
            debug.append(mapper);
            debug.unindent();
        }
    }

    @Override
    public String debugTitle() {
        return "Object Mapping Lookup";
    }

}
