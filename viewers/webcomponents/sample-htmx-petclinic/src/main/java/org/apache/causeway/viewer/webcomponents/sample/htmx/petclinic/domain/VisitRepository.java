/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.causeway.viewer.webcomponents.sample.htmx.petclinic.domain;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

@Repository
public class VisitRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Visit findById(final String id) {
        return entityManager.find(Visit.class, id);
    }

    public List<Visit> findByPetOwner(final PetOwner owner) {
        return entityManager.createQuery(
                        "select v from Visit v where v.pet.petOwner = :owner order by v.visitAt desc", Visit.class)
                .setParameter("owner", owner)
                .getResultList();
    }

    public List<Visit> findByVisitAtAfter(final LocalDateTime visitAt) {
        return entityManager.createQuery(
                        "select v from Visit v where v.visitAt >= :visitAt order by v.visitAt", Visit.class)
                .setParameter("visitAt", visitAt)
                .getResultList();
    }

    public Visit persist(final Visit visit) {
        entityManager.persist(visit);
        entityManager.flush();
        return visit;
    }
}
