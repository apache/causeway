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
package org.apache.causeway.persistence.jpa.integration.services;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import org.springframework.data.jpa.repository.JpaContext;

import org.apache.causeway.commons.functional.Try;
import org.apache.causeway.persistence.jpa.applib.services.JpaSupportService;

import org.jspecify.annotations.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor(onConstructor_ = {@Inject})
@Slf4j
public class JpaSupportServiceUsingSpring implements JpaSupportService {

    private final JpaContext jpaContextSpring;

    @Override
    public Try<EntityManager> getEntityManager(final @NonNull Class<?> entityClass) {
        try {
            var em = jpaContextSpring.getEntityManagerByManagedType(entityClass);
            return Try.success(em);
        } catch (Exception e) {
            log.error(String.format("failed to get an EntityManager for entity type %s", entityClass), e);
            return Try.failure(e);
        }
    }

}
