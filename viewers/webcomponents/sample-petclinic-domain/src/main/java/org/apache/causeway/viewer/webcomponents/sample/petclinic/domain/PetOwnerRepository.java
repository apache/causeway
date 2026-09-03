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
package org.apache.causeway.viewer.webcomponents.sample.petclinic.domain;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;

@Repository
public class PetOwnerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public PetOwner findById(final String id) {
        return entityManager.find(PetOwner.class, id);
    }

    public PetOwner findByName(final String name) {
        return entityManager.createQuery(
                        "select p from PetOwner p where lower(p.name) = lower(:name)", PetOwner.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    public List<PetOwner> findByNameContaining(final String name) {
        return entityManager.createQuery(
                        "select p from PetOwner p where lower(p.name) like lower(:name) order by p.name", PetOwner.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    public List<PetOwner> findAll() {
        return entityManager.createQuery("select p from PetOwner p order by p.name", PetOwner.class)
                .getResultList();
    }

    public long count() {
        return entityManager.createQuery("select count(p) from PetOwner p", Long.class).getSingleResult();
    }

    public PetOwner persist(final PetOwner owner) {
        entityManager.persist(owner);
        entityManager.flush();
        return owner;
    }
}
