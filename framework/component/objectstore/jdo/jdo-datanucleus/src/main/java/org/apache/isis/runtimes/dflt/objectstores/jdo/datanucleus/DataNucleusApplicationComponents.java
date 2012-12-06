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
package org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.datanucleus.NucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.store.schema.SchemaAwareStoreManager;

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.FrameworkSynchronizer;
import org.apache.isis.runtimes.dflt.objectstores.jdo.datanucleus.persistence.IsisLifecycleListener;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.embeddedonly.JdoEmbeddedOnlyFacet;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.persistencecapable.JdoPersistenceCapableFacet;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.query.JdoNamedQuery;
import org.apache.isis.runtimes.dflt.objectstores.jdo.metamodel.facets.object.query.JdoQueryFacet;

public class DataNucleusApplicationComponents implements ApplicationScopedComponent {

    private final PersistenceManagerFactory persistenceManagerFactory;
    private final Map<String, JdoNamedQuery> namedQueryByName;
    
    private final IsisLifecycleListener lifecycleListener;
    private final FrameworkSynchronizer synchronizer;


    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public DataNucleusApplicationComponents(final Map<String, String> props, final Collection<ObjectSpecification> objectSpecs) {
        persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(props);
        
        final Set<String> classesToBePersisted = catalogClassesToBePersisted(objectSpecs);

        createSchema(props, classesToBePersisted);

        namedQueryByName = Collections.unmodifiableMap(catalogNamedQueries(objectSpecs));

        synchronizer = new FrameworkSynchronizer();
        lifecycleListener = new IsisLifecycleListener(synchronizer);
    }
    
    private void createSchema(final Map<String, String> props, final Set<String> classesToBePersisted) {
        final JDOPersistenceManagerFactory jdopmf = (JDOPersistenceManagerFactory)persistenceManagerFactory;
        final NucleusContext nucleusContext = jdopmf.getNucleusContext();
        final SchemaAwareStoreManager storeManager = (SchemaAwareStoreManager) nucleusContext.getStoreManager();
        storeManager.createSchema(classesToBePersisted, asProperties(props));
    }

    private static Set<String> catalogClassesToBePersisted(Collection<ObjectSpecification> objectSpecs) {
        Set<String> classNames = Sets.newTreeSet();
        for (final ObjectSpecification spec : objectSpecs) {
            if(spec.containsFacet(JdoPersistenceCapableFacet.class) || spec.containsFacet(JdoEmbeddedOnlyFacet.class)) {
                classNames.add(spec.getFullIdentifier());
            }
        }
        return Collections.unmodifiableSet(classNames);
    }

    private static Properties asProperties(Map<String, String> props) {
        Properties properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    private static Map<String, JdoNamedQuery> catalogNamedQueries(Collection<ObjectSpecification> objectSpecs) {
        final Map<String, JdoNamedQuery> namedQueryByName = Maps.newHashMap();
        for (final ObjectSpecification spec : objectSpecs) {
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
