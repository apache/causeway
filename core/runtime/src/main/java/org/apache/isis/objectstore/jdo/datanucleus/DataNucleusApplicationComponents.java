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
package org.apache.isis.objectstore.jdo.datanucleus;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import org.datanucleus.NucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.metadata.MetaDataManager;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.objectstore.jdo.datanucleus.persistence.IsisLifecycleListener;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.objectstore.jdo.metamodel.facets.object.query.JdoQueryFacet;

public class DataNucleusApplicationComponents implements ApplicationScopedComponent {


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
    //
    ///////////////////////////////////////////////////////////////////////////
    
    private final Set<String> persistableClassNameSet;
    private final Map<String, String> props;
    
    private final IsisLifecycleListener lifecycleListener;
    private final FrameworkSynchronizer synchronizer;
    
    private Map<String, JdoNamedQuery> namedQueryByName;
    private PersistenceManagerFactory persistenceManagerFactory;

    public DataNucleusApplicationComponents(
            final Map<String, String> props, 
            final Set<String> persistableClassNameSet) {
    
        this.props = props;
        this.persistableClassNameSet = persistableClassNameSet;

        this.synchronizer = new FrameworkSynchronizer();
        this.lifecycleListener = new IsisLifecycleListener(synchronizer);

        init(props, persistableClassNameSet);
        
        // for JRebel plugin
        instance = this;
    }

    private void init(final Map<String, String> props, final Set<String> persistableClassNameSet) {
        final String persistableClassNames = Joiner.on(',').join(persistableClassNameSet);
        
        props.put("datanucleus.autoStartClassNames", persistableClassNames);
        persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(props);

        final boolean createSchema = Boolean.parseBoolean( props.get("datanucleus.autoCreateSchema") );
        if(createSchema) {
            createSchema(props, persistableClassNameSet);
        }

        namedQueryByName = catalogNamedQueries(persistableClassNameSet);

    }
    
    public PersistenceManagerFactory getPersistenceManagerFactory() {
        return persistenceManagerFactory;
    }

    private void createSchema(final Map<String, String> props, final Set<String> classesToBePersisted) {
        final JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory)persistenceManagerFactory;
        final NucleusContext nucleusContext = jdopmf.getNucleusContext();
        final StoreManager storeManager = nucleusContext.getStoreManager();
        if (storeManager instanceof SchemaAwareStoreManager) {
            ((SchemaAwareStoreManager)storeManager).createSchema(classesToBePersisted, asProperties(props));
		}
    }


    private static Properties asProperties(Map<String, String> props) {
        Properties properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    private static Map<String, JdoNamedQuery> catalogNamedQueries(Set<String> persistableClassNames) {
        final Map<String, JdoNamedQuery> namedQueryByName = Maps.newHashMap();
        for (final String persistableClassName: persistableClassNames) {
            final ObjectSpecification spec = IsisContext.getSpecificationLoader().loadSpecification(persistableClassName);
            final JdoQueryFacet facet = spec.getFacet(JdoQueryFacet.class);
            if (facet == null) {
                continue;
            }
            for (final JdoNamedQuery namedQuery : facet.getNamedQueries()) {
                namedQueryByName.put(namedQuery.getName(), namedQuery);
            }
        }
        return namedQueryByName;
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void init() {
    }

    @Override
    public void shutdown() {
    }


    ///////////////////////////////////////////////////////////////////////////
    // FrameworkSynchronizer
    ///////////////////////////////////////////////////////////////////////////

    public FrameworkSynchronizer getFrameworkSynchronizer() {
        return synchronizer;
    }


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    
    public PersistenceManager createPersistenceManager() {
        PersistenceManager persistenceManager = persistenceManagerFactory.getPersistenceManager();
        
        persistenceManager.addInstanceLifecycleListener(lifecycleListener, (Class[])null);
        return persistenceManager;
    }

    public JdoNamedQuery getNamedQuery(String queryName) {
        return namedQueryByName.get(queryName);
    }

    
    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public void suspendListener() {
        lifecycleListener.setSuspended(true);
    }

    public void resumeListener() {
        lifecycleListener.setSuspended(false);
    }

}
