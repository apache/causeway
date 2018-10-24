/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.isis.core.webapp.jee;

import java.util.Map;

import javax.persistence.Cache;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.Query;
import javax.persistence.SynchronizationType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.ProviderUtil;
import javax.resource.NotSupportedException;

/**
 * Implements a PersistenceProvider that does nothing. (no longer required)
 * <p>
 * Note: the axon framework on JEE required at least a dummy persistence unit. 
 * That was before release of axon-framework 3.4.
 * This required that the {@code web.xml} includes a {@code persistence-context-ref} entry as follows:
 *
 * <pre>{@code
 * <persistence-context-ref>
 *     <persistence-context-ref-name>org.axonframework.common.jpa.ContainerManagedEntityManagerProvider/entityManager</persistence-context-ref-name>
 *     <persistence-unit-name>noop</persistence-unit-name>
 * </persistence-context-ref>
 * }
 * </pre>
 * </p>
 * <p>
 * A {@code META_INF/persistence.xml} that declares the 'noop' persistence-unit
 * is bundled with this module.
 * </p>
 *
 * @since 2.0.0-M1
 *
 */
@SuppressWarnings("rawtypes")
public class PersistenceUnitNoopProvider implements javax.persistence.spi.PersistenceProvider{

    @Override
    public EntityManagerFactory createEntityManagerFactory(String emName, Map map) {
        return noopEntityManagerFactory();
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map map) {
        return noopEntityManagerFactory();
    }

    @Override
    public void generateSchema(PersistenceUnitInfo info, Map map) {
        throw notSupported();
    }

    @Override
    public boolean generateSchema(String persistenceUnitName, Map map) {
        throw notSupported();
    }

    @Override
    public ProviderUtil getProviderUtil() {
        throw notSupported();
    }

    // -- HELPER

    private static RuntimeException notSupported() {
        return new RuntimeException(
                new NotSupportedException("This PersistenceProvider is just a dummy."));
    }

    private EntityManagerFactory noopEntityManagerFactory() {
        return new EntityManagerFactory() {
            @Override public EntityManager createEntityManager() {	return null; }
            @Override public EntityManager createEntityManager(Map map) { return null; }
            @Override public EntityManager createEntityManager(SynchronizationType synchronizationType) { return null;	}
            @Override public EntityManager createEntityManager(SynchronizationType synchronizationType, Map map) { return null;	}
            @Override public CriteriaBuilder getCriteriaBuilder() { return null; }
            @Override public Metamodel getMetamodel() { return null; }
            @Override public boolean isOpen() {	return false; }
            @Override public void close() {	}
            @Override public Map<String, Object> getProperties() { return null;	}
            @Override public Cache getCache() { return null; }
            @Override public PersistenceUnitUtil getPersistenceUnitUtil() {	return null; }
            @Override public void addNamedQuery(String name, Query query) {	}
            @Override public <T> T unwrap(Class<T> cls) { return null; }
            @Override public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) { }
        };
    }

}
