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
import java.util.stream.Collectors;

import javax.enterprise.inject.Vetoed;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;

import org.datanucleus.PersistenceNucleusContext;
import org.datanucleus.PropertyNames;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.MetaDataListener;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.apache.isis.commons.internal.base._NullSafe;
import org.apache.isis.commons.internal.collections._Maps;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.factory.InstanceUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.jdo.datanucleus.CreateSchemaObjectFromClassMetadata;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusLifeCycleHelper;
import org.apache.isis.objectstore.jdo.datanucleus.DataNucleusPropertiesAware;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoQueryFacet;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

@Vetoed
public class DataNucleusApplicationComponents5 implements ApplicationScopedComponent {

    public static final String CLASS_METADATA_LOADED_LISTENER_KEY = "classMetadataLoadedListener";
    static final String CLASS_METADATA_LOADED_LISTENER_DEFAULT = CreateSchemaObjectFromClassMetadata.class.getName();

    ///////////////////////////////////////////////////////////////////////////
    // JRebel support
    ///////////////////////////////////////////////////////////////////////////

    private static DataNucleusApplicationComponents5 instance;

    /**
     * For JRebel plugin
     */
    public static MetaDataManager getMetaDataManager() {
        return instance != null
                ? ((JDOPersistenceManagerFactory)instance.persistenceManagerFactory)
                        .getNucleusContext().getMetaDataManager()
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

    public DataNucleusApplicationComponents5(
            final IsisConfiguration configuration,
            final Map<String, String> datanucleusProps,
            final Set<String> persistableClassNameSet) {

        this.datanucleusProps = datanucleusProps;
        this.persistableClassNameSet = persistableClassNameSet;
        this.jdoObjectstoreConfig = configuration;

        persistenceManagerFactory = createPmfAndSchemaIfRequired(
                this.persistableClassNameSet, this.datanucleusProps);

        // for JRebel plugin
        instance = this;
    }

    /**
     * Marks the end of DataNucleus' life-cycle. Purges any state associated with DN.
     * Subsequent calls have no effect.
     *
     * @since 2.0.0
     */
    public void shutdown() {
        instance = null;
        if(persistenceManagerFactory != null) {
            DataNucleusLifeCycleHelper.cleanUp(persistenceManagerFactory);
            persistenceManagerFactory = null;
        }
    }

    static PersistenceManagerFactory newPersistenceManagerFactory(Map<String, String> datanucleusProps) {
        return JDOHelper.getPersistenceManagerFactory(datanucleusProps, IsisContext.getClassLoader());
    }

    // REF: http://www.datanucleus.org/products/datanucleus/jdo/schema.html
    private PersistenceManagerFactory createPmfAndSchemaIfRequired(
            final Set<String> persistableClassNameSet, 
            final Map<String, String> datanucleusProps) {

        final DNStoreManagerType dnStoreManagerType = DNStoreManagerType.typeOf(datanucleusProps);
        
        PersistenceManagerFactory persistenceManagerFactory;

        if(dnStoreManagerType.isSchemaAware()) {

            // rather than reinvent too much of the wheel, we reuse the same property that DN would check
            // for if it were doing the auto-creation itself (read from isis.properties)
            final boolean createSchema = isSet(datanucleusProps, PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL);

            if(createSchema) {
                
                configureAutoCreateSchema(datanucleusProps);
                persistenceManagerFactory = newPersistenceManagerFactory(datanucleusProps);
                createSchema(persistenceManagerFactory, persistableClassNameSet, datanucleusProps);

            } else {
                persistenceManagerFactory = newPersistenceManagerFactory(datanucleusProps);
            }

        } else {

            // we *DO* use DN's eager loading (autoStart), because it seems that DN requires this (for neo4j at least)
            // otherwise NPEs occur later.

            configureAutoStart(persistableClassNameSet, datanucleusProps);
            persistenceManagerFactory = newPersistenceManagerFactory(this.datanucleusProps);
        }

        return persistenceManagerFactory;

    }

    private void configureAutoCreateSchema(final Map<String, String> datanucleusProps) {
        // we *don't* use DN's eager loading (autoStart), because doing so means that it attempts to
        // create the table before the schema (for any entities annotated @PersistenceCapable(schema=...)
        //
        // instead, we manually create the schema ourselves
        // (if the configured StoreMgr supports it, and if requested in isis.properties)
        //
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_ALL, "false"); // turn off, cos want to do the schema object ourselves...
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_DATABASE, "false");
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_TABLES, "true"); // but have DN do everything else...
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_COLUMNS, "true");
        datanucleusProps.put(PropertyNames.PROPERTY_SCHEMA_AUTOCREATE_CONSTRAINTS, "true");
    }
    
    private void configureAutoStart(
            final Set<String> persistableClassNameSet, 
            final Map<String, String> datanucleusProps) {
        
        final String persistableClassNames =
                stream(persistableClassNameSet).collect(Collectors.joining(","));

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
        final SchemaAwareStoreManager schemaAwareStoreManager = 
                (SchemaAwareStoreManager)nucleusContext.getStoreManager();

        final MetaDataManager metaDataManager = nucleusContext.getMetaDataManager();

        registerMetadataListener(metaDataManager, datanucleusProps);

        if(_NullSafe.isEmpty(persistableClassNameSet)) {
        	return; // skip
        }
        
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
        final Map<String, JdoNamedQuery> namedQueryByName = _Maps.newHashMap();
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
