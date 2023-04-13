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
package org.apache.causeway.viewer.thymeflux.test.config;

import java.util.Map;

import jakarta.persistence.Cache;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.metamodel.Metamodel;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymefluxConfig_headlessPersistence {

    @Bean
    EntityManagerFactory entityManagerFactory() {
        return new EntityManagerFactory() {
            @Override
            public <T> T unwrap(final Class<T> cls) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public boolean isOpen() {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public Map<String, Object> getProperties() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public PersistenceUnitUtil getPersistenceUnitUtil() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Metamodel getMetamodel() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public CriteriaBuilder getCriteriaBuilder() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public Cache getCache() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public EntityManager createEntityManager(final SynchronizationType synchronizationType, final Map map) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public EntityManager createEntityManager(final SynchronizationType synchronizationType) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public EntityManager createEntityManager(final Map map) {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public EntityManager createEntityManager() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public void close() {
                // TODO Auto-generated method stub
            }

            @Override
            public void addNamedQuery(final String name, final Query query) {
                // TODO Auto-generated method stub
            }

            @Override
            public <T> void addNamedEntityGraph(final String graphName, final EntityGraph<T> entityGraph) {
                // TODO Auto-generated method stub
            }
        };
    }

}
