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
package org.apache.causeway.persistence.jpa.applib.services;

import jakarta.persistence.EntityManager;

import org.apache.causeway.commons.functional.Try;

import org.jspecify.annotations.NonNull;

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
    Try<EntityManager> getEntityManager(@NonNull Class<?> entityType);

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
                .mapFailure(cause->failureFor(entityType, cause))
                .getValue()
                .orElseThrow(()->failureFor(entityType));
    }

    void executeUpdate(String sql);
    
    // -- HELPER

    private static IllegalStateException failureFor(final @NonNull Class<?> entityType, final Throwable cause) {
        return new IllegalStateException(failureMessage(entityType), cause);
    }

    private static IllegalStateException failureFor(final @NonNull Class<?> entityType) {
        return new IllegalStateException(failureMessage(entityType));
    }

    private static String failureMessage(final @NonNull Class<?> entityType) {
        return String.format(
                "Current thread either has no open interaction"
                + " or no unique EntityManager managing type %s can be resolved.", entityType);
    }

}
