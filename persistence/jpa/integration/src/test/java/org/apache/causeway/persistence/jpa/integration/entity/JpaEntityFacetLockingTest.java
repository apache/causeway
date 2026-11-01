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

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.OptimisticLockException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.apache.causeway.applib.services.bookmark.Bookmark;
import org.apache.causeway.applib.services.bookmark.IdStringifier;
import org.apache.causeway.core.config.metamodel.facets.DomainObjectConfigOptions.LockingPolicy;
import org.apache.causeway.core.metamodel.facetapi.FacetHolder;
import org.apache.causeway.core.config.observation.CausewayObservationIntegration;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.Observation;
import org.apache.causeway.core.metamodel.facets.object.entity.EntityFacet.PrimaryKeyType;
import org.apache.causeway.core.metamodel.facets.object.locking.LockingFacet;

class JpaEntityFacetLockingTest {
    private final EntityManager em = mock(EntityManager.class);
    private final FacetHolder holder = mock(FacetHolder.class);
    private final LockingFacet policy = mock(LockingFacet.class);
    private JpaEntityFacet facet;
    private final ObservationRegistry registry = ObservationRegistry.create();
    private boolean observing;
    private final Bookmark bookmark = Bookmark.forLogicalTypeNameAndIdentifier("test.Entity", "42");

    @BeforeEach
    void setUp() {
        registry.observationConfig().observationHandler(new ObservationHandler<Observation.Context>() {
            @Override public boolean supportsContext(Observation.Context context) { return true; }
            @Override public void onStart(Observation.Context context) { observing = true; }
            @Override public void onStop(Observation.Context context) { observing = false; }
        });
        facet = mock(JpaEntityFacet.class, CALLS_REAL_METHODS);
        ReflectionTestUtils.setField(facet, "observationProvider",
                new CausewayObservationIntegration(registry).provider(JpaEntityFacet.class));
        doReturn(em).when(facet).getEntityManager();
        doReturn(holder).when(facet).facetHolder();
        @SuppressWarnings("unchecked")
        final IdStringifier<Long> stringifier = mock(IdStringifier.class);
        when(stringifier.destring(Object.class, "42")).thenReturn(42L);
        ReflectionTestUtils.setField(facet, "entityClass", Object.class);
        ReflectionTestUtils.setField(facet, "primaryKeyType", new PrimaryKeyType<>(Object.class, stringifier, Long.class));
        when(holder.lookupFacet(LockingFacet.class)).thenReturn(java.util.Optional.of(policy));
    }

    @Test
    void pessimisticLoadConvertsKeyAndLocks() {
        when(policy.getPolicy()).thenReturn(LockingPolicy.PESSIMISTIC);
        final Object entity = new Object();
        when(em.find(Object.class, 42L, LockModeType.PESSIMISTIC_WRITE)).thenAnswer(invocation -> {
            assertTrue(observing, "locking find must remain observed");
            return entity;
        });
        assertSame(entity, facet.fetchByBookmark(bookmark).orElseThrow());
        verify(em).find(Object.class, 42L, LockModeType.PESSIMISTIC_WRITE);
        verifyNoMoreInteractions(em);
    }

    @Test
    void optimisticLoadPreservesOrdinaryFind() {
        when(policy.getPolicy()).thenReturn(LockingPolicy.OPTIMISTIC);
        final Object entity = new Object();
        when(em.find(Object.class, 42L)).thenAnswer(invocation -> {
            assertTrue(observing, "ordinary find must remain observed");
            return entity;
        });
        assertEquals(entity, facet.fetchByBookmark(bookmark).orElseThrow());
        verify(em).find(Object.class, 42L);
        verifyNoMoreInteractions(em);
    }

    @Test
    void missingEntityReturnsEmpty() {
        when(policy.getPolicy()).thenReturn(LockingPolicy.PESSIMISTIC);
        assertTrue(facet.fetchByBookmark(bookmark).isEmpty());
    }

    @Test
    void preservesProviderFailure() {
        when(policy.getPolicy()).thenReturn(LockingPolicy.PESSIMISTIC);
        final PessimisticLockException failure = new PessimisticLockException("locked");
        when(em.find(Object.class, 42L, LockModeType.PESSIMISTIC_WRITE)).thenThrow(failure);
        assertSame(failure, assertThrows(PessimisticLockException.class, () -> facet.fetchByBookmark(bookmark)));
        assertTrue(!observing, "observation must stop on provider failure");
        verify(em).find(Object.class, 42L, LockModeType.PESSIMISTIC_WRITE);
        verifyNoMoreInteractions(em);
    }
    @Test
    void preservesProviderVersionFailure() {
        when(policy.getPolicy()).thenReturn(LockingPolicy.PESSIMISTIC);
        final OptimisticLockException failure = new OptimisticLockException("stale version");
        when(em.find(Object.class, 42L, LockModeType.PESSIMISTIC_WRITE)).thenThrow(failure);
        assertSame(failure, assertThrows(OptimisticLockException.class, () -> facet.fetchByBookmark(bookmark)));
        assertTrue(!observing, "observation must stop on provider failure");
        verify(em).find(Object.class, 42L, LockModeType.PESSIMISTIC_WRITE);
        verifyNoMoreInteractions(em);
    }

}
