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
package org.apache.isis.core.runtime.system.persistence;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.MetaDataListener;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.config.IsisConfiguration;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.objectstore.jdo.datanucleus.CreateSchemaObjectFromClassMetadata;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPropertiesAware;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoQueryFacet;

public class DataNucleusApplicationComponents implements ApplicationScopedComponent {

    public static final String CLASS_METADATA_LOADED_LISTENER_KEY = "classMetadataLoadedListener";
    static final String CLASS_METADATA_LOADED_LISTENER_DEFAULT = CreateSchemaObjectFromClassMetadata.class.getName();

    ///////////////////////////////////////////////////////////////////////////
    // JRebel support
    ///////////////////////////////////////////////////////////////////////////

    private static DataNucleusApplicationComponents instance;
    
    /**
     * For JRebel plugin
     */
    public static MetaDataManager getMetaDataManager() {
        return instance != null
                ? ((JDOPersistenceManagerFactory)instance.persistenceManagerFactory).getNucleusContext().getMetaDataManager() 
                : null;
    }

    public static void markAsStale() {
        if(instance != null) {
            instance.stale = true;
        }
    }

    private boolean stale = false;
    public boolean isStale() {
        return stale;
    }

    ///////////////////////////////////////////////////////////////////////////

    private final Set<String> persistableClassNameSet;
    private final IsisConfiguration jdoObjectstoreConfig;
    private final Map<String, String> datanucleusProps;
    
    private PersistenceManagerFactory persistenceManagerFactory;

    public DataNucleusApplicationComponents(
            final IsisConfiguration configuration,
            final Map<String, String> datanucleusProps,
            final Set<String> persistableClassNameSet) {

        this.datanucleusProps = datanucleusProps;
        this.persistableClassNameSet = persistableClassNameSet;
        this.jdoObjectstoreConfig = configuration;

        persistenceManagerFactory = createPmfAndSchemaIfRequired(this.persistableClassNameSet, this.datanucleusProps);

        // for JRebel plugin
        instance = this;
    }

    private static boolean isSchemaAwareStoreManager(Map<String,String> datanucleusProps) {

        // we create a throw-away instance of PMF so that we can probe whether DN has
        // been configured with a schema-aware store manager or not.
        final JDOPersistenceManagerFactory probePmf =
                (JDOPersistenceManagerFactory)JDOHelper.getPersistenceManagerFactory(datanucleusProps);
        try {
            final PersistenceNucleusContext nucleusContext = probePmf.getNucleusContext();
            final StoreManager storeManager = nucleusContext.getStoreManager();
            return storeManager instanceof SchemaAwareStoreManager;
        } finally {
            probePmf.close();
        }
    }

    // REF: http://www.datanucleus.org/products/datanucleus/jdo/schema.html
    private PersistenceManagerFactory createPmfAndSchemaIfRequired(final Set<String> persistableClassNameSet, final Map<String, String> datanucleusProps) {

        PersistenceManagerFactory persistenceManagerFactory;
        if(isSchemaAwareStoreManager(datanucleusProps)) {

            // rather than reinvent too much of the wheel, we reuse the same property that DN would check
            // for if it were doing the auto-creation itself (read from isis.properties)
            final boolean createSchema = isSet(datanucleusProps, PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL);

            if(createSchema) {

                // we *don't* use DN's eager loading (autoStart), because doing so means that it attempts to
                // create the table before the schema (for any entities annotated @PersistenceCapable(schema=...)
                //
                // instead, we manually create the schema ourselves
                // (if the configured StoreMgr supports it, and if requested in isis.properties)
                //
                datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL, "false"); // turn off, cos want to do the schema object ourselves...
                datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_SCHEMA, "false");
                datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_TABLES, "true"); // but have DN do everything else...
                datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_COLUMNS, "true");
                datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_CONSTRAINTS, "true");

                persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(datanucleusProps);
                createSchema(persistenceManagerFactory, persistableClassNameSet, datanucleusProps);

            } else {
                persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(datanucleusProps);
            }

        } else {

            // we *DO* use DN's eager loading (autoStart), because it seems that DN requires this (for neo4j at least)
            // otherwise NPEs occur later.

            configureAutoStart(persistableClassNameSet, datanucleusProps);
            persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(this.datanucleusProps);
        }

        return persistenceManagerFactory;

    }

    private void configureAutoStart(final Set<String> persistableClassNameSet, final Map<String, String> datanucleusProps) {
        final String persistableClassNames = Joiner.on(',').join(persistableClassNameSet);

        // ref: http://www.datanucleus.org/products/datanucleus/jdo/autostart.html
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_MECHANISM, "Classes");
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_MODE, "Checked");
        datanucleusProps.put(PropertyNames.PROPERTY_AUTOSTART_CLASSNAMES, persistableClassNames);
    }

    private void createSchema(
            final PersistenceManagerFactory persistenceManagerFactory,
            final Set<String> persistableClassNameSet,
            final Map<String, String> datanucleusProps) {

        JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory) persistenceManagerFactory;
        final PersistenceNucleusContext nucleusContext = jdopmf.getNucleusContext();
        final SchemaAwareStoreManager schemaAwareStoreManager = (SchemaAwareStoreManager)nucleusContext.getStoreManager();

        final MetaDataManager metaDataManager = nucleusContext.getMetaDataManager();

        registerMetadataListener(metaDataManager, datanucleusProps);

        schemaAwareStoreManager.createSchemaForClasses(persistableClassNameSet, asProperties(datanucleusProps));
    }

    private boolean isSet(final Map<String, String> props, final String key) {
        return Boolean.parseBoolean( props.get(key) );
    }

    private void registerMetadataListener(
            final MetaDataManager metaDataManager,
            final Map<String, String> datanucleusProps) {
        final MetaDataListener listener = createMetaDataListener();
        if(listener == null) {
            return;
        }

        if(listener instanceof DataNucleusPropertiesAware) {
            ((DataNucleusPropertiesAware) listener).setDataNucleusProperties(datanucleusProps);
        }


        // and install the listener for any classes that are lazily loaded subsequently
        // (shouldn't be any, this is mostly backwards compatibility with previous design).
        metaDataManager.registerListener(listener);
    }

    private MetaDataListener createMetaDataListener() {
        final String classMetadataListenerClassName = jdoObjectstoreConfig.getString(
                CLASS_METADATA_LOADED_LISTENER_KEY,
                CLASS_METADATA_LOADED_LISTENER_DEFAULT);
        return classMetadataListenerClassName != null
                ? InstanceUtil.createInstance(classMetadataListenerClassName, MetaDataListener.class)
                : null;
    }


    private static Properties asProperties(final Map<String, String> props) {
        final Properties properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    static void catalogNamedQueries(
            Set<String> persistableClassNames, final SpecificationLoader specificationLoader) {
        final Map<String, JdoNamedQuery> namedQueryByName = Maps.newHashMap();
        for (final String persistableClassName: persistableClassNames) {
            final ObjectSpecification spec = specificationLoader.loadSpecification(persistableClassName);
            final JdoQueryFacet facet = spec.getFacet(JdoQueryFacet.class);
            if (facet == null) {
                continue;
            }
            for (final JdoNamedQuery namedQuery : facet.getNamedQueries()) {
                namedQueryByName.put(namedQuery.getName(), namedQuery);
            }
        }
    }

    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return persistenceManagerFactory;
    }

    

}
