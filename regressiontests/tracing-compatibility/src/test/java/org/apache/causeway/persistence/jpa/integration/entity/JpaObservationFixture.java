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
package org.apache.causeway.persistence.jpa.integration.entity;

import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

import org.springframework.data.jpa.repository.JpaContext;

import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityOrmMetadata;
import org.apache.causeway.core.metamodel.services.idstringifier.IdStringifierLookupService;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

/** Real JPA facet and observation policy, with a controlled persistence backend. */
public final class JpaObservationFixture {
    public static class Entity {
        @Override public String toString() { return "sentinel-object-title"; }
    }

    public static EntityFacet create(CausewayObservationIntegration integration, Runnable jdbc) {
        var em = mock(EntityManager.class, RETURNS_DEEP_STUBS);
        var jpa = mock(JpaContext.class);
        when(jpa.getEntityManagerByManagedType(Entity.class)).thenReturn(em);
        var metadata = mock(EntityOrmMetadata.class);
        doReturn(String.class).when(metadata).primaryKeyClass();
        var orm = mock(OrmMetadataProvider.class);
        when(orm.ormMetadataFor(em, Entity.class)).thenReturn(metadata);
        var ids = mock(IdStringifierLookupService.class);
        var pk = mock(EntityFacet.PrimaryKeyType.class);
        doReturn(pk).when(ids).primaryKeyTypeFor(Entity.class, String.class);
        when(pk.destring(anyString())).thenAnswer(call -> call.getArgument(0));
        var dependencies = Map.of("jpaContext", jpa, "ormMetadataProvider", orm,
                "idStringifierLookupService", ids, "observationIntegration", integration);
        var injector = mock(ServiceInjector.class);
        doAnswer(call -> {
            Object facet = call.getArgument(0);
            for (var entry : dependencies.entrySet()) {
                var field = JpaEntityFacet.class.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(facet, entry.getValue());
            }
            return facet;
        }).when(injector).injectServicesInto(any());
        doAnswer(call -> { jdbc.run(); return null; }).when(em).persist(any());
        var query = mock(TypedQuery.class);
        when(query.getResultStream()).thenAnswer(call -> Stream.empty());
        when(em.createNamedQuery(anyString(), eq(Entity.class))).thenReturn(query);
        when(em.createQuery(any(jakarta.persistence.criteria.CriteriaQuery.class))).thenReturn(query);
        var spec = mock(ObjectSpecification.class);
        return new JpaEntityFacet(FacetHolder.forTesting(mock(MetaModelContext.class)), Entity.class) {
            @Override public ServiceInjector getServiceInjector() { return injector; }
            @Override public ObjectSpecification getEntitySpecification() { return spec; }
        };
    }
}
