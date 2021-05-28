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
package org.apache.isis.testdomain.persistence.jpa.entitylifecycle;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.isis.applib.services.repository.EntityState;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.config.presets.IsisPresets;
import org.apache.isis.core.metamodel.objectmanager.ObjectManager;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ManagedObjects;
import org.apache.isis.testdomain.conf.Configuration_usingJpa;
import org.apache.isis.testdomain.jpa.entities.JpaEntityGeneratedLongId;

import lombok.val;

@SpringBootTest(
        classes = {
                Configuration_usingJpa.class,
        },
        properties = {
        })
@Transactional
@TestPropertySource(IsisPresets.UseLog4j2Test)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class JpaGeneratedLongIdEntityLifecycleTest {

    @Inject private RepositoryService repository;
    @Inject private ObjectManager objectManager;

    private static AtomicReference<ManagedObject> entityRef;

    @BeforeAll
    static void beforeAll() {
        entityRef = new AtomicReference<>();
    }

    @AfterAll
    static void afterAll() {
        entityRef = null;
    }

    @Test @Order(0) @Commit
    void cleanup_justInCase() {
        // cleanup just in case
        repository.removeAll(JpaEntityGeneratedLongId.class);
    }

    @Test @Order(1) @Commit
    void detached_shouldBeProperlyDetected() {

        val entity = objectManager.adapt(
                repository.detachedEntity(new JpaEntityGeneratedLongId("test")));

        assertTrue(entity.getSpecification().isEntity());
        assertEquals(
                EntityState.PERSISTABLE_DETACHED,
                ManagedObjects.EntityUtil.getEntityState(entity));

        entityRef.set(entity);
    }

    @Test @Order(2) @Commit
    void attached_shouldBeProperlyDetected() {

        val entity = entityRef.get();

        repository.persist(entity.getPojo());

        assertEquals(
                EntityState.PERSISTABLE_ATTACHED,
                ManagedObjects.EntityUtil.getEntityState(entity));
        assertEquals(1, repository.allInstances(JpaEntityGeneratedLongId.class).size());

    }

    @Test @Order(3) @Commit
    void removed_shouldBeProperlyDetected() {

        // expected post-condition (after persist, and having entered a new transaction)
        assertEquals(
                EntityState.PERSISTABLE_DETACHED,
                ManagedObjects.EntityUtil.getEntityState(entityRef.get()));

        val id = ((JpaEntityGeneratedLongId)entityRef.get().getPojo()).getId();

        val entity = objectManager.adapt(
                repository.firstMatch(
                        JpaEntityGeneratedLongId.class,
                        entity->Objects.equals(entity.getId(), id))
                .orElseThrow(_Exceptions::noSuchElement));

        // expected pre-condition (before removal)
        assertEquals(
                EntityState.PERSISTABLE_ATTACHED,
                ManagedObjects.EntityUtil.getEntityState(entity));

        repository.remove(entity.getPojo());

     // expected post-condition (after removal)
        assertEquals(
                EntityState.PERSISTABLE_DESTROYED,
                ManagedObjects.EntityUtil.getEntityState(entity));

        entityRef.set(entity);
    }

    @Test @Order(4) @Commit
    void postCondition_shouldBe_anEmptyRepository() {

        val entity = entityRef.get();

        assertEquals(
                EntityState.PERSISTABLE_DESTROYED,
                ManagedObjects.EntityUtil.getEntityState(entity));
        assertEquals(0, repository.allInstances(JpaEntityGeneratedLongId.class).size());

    }


}
