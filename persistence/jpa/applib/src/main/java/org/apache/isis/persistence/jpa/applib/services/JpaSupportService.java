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
package org.apache.isis.persistence.jpa.applib.services;

import javax.persistence.EntityManager;

import org.apache.isis.commons.functional.Result;

import lombok.NonNull;

/**
 * Provides access to the current interaction's {@link EntityManager}(s) 
 * 
 * @since 2.0 {@index}
 */
public interface JpaSupportService {

    /**
     * Optionally returns the current interaction's {@link EntityManager}, 
     * that is bound to given {@code entityClass},
     * based on whether an open interaction is available and a persistence layer 
     * is configured in support of JPA.
     * @param entityType - (non-null)
     * @throws NullPointerException when given {@code entityType} is {@code null}
     */
    Result<EntityManager> getEntityManager(@NonNull Class<?> entityType);
    
    /**
     * Returns the current interaction's {@link EntityManager} that is managing the given domain type 
     * {@code entityType}.
     * @param entityType - (non-null)
     * 
     * @throws NullPointerException when given {@code entityType} is {@literal null}
     * @throws IllegalStateException when no open interaction is available or when the given type is not 
     * JPA managed or no unique {@link EntityManager} managing this type can be resolved.
     */
    default EntityManager getEntityManagerElseFail(final @NonNull Class<?> entityType) {
        
        return getEntityManager(entityType)
                .presentElseThrow(cause->new IllegalStateException(
                        String.format(
                        "Current thread either has no open interaction"
                        + " or no unique EntityManager managing type %s can be resolved.", entityType), cause));
    }
    
}
