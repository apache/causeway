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
package org.apache.isis.testing.fixtures.applib.personas;

import org.apache.isis.applib.services.registry.ServiceRegistry;

/**
 * Intended for persona enums to implement, to obtain an instance of the corresponding top-level entity representing
 * the persona.
 *
 * @see PersonaWithBuilderScript
 *
 * @since 2.x {@index}
 */
public interface PersonaWithFinder<T> {

    /**
     * Looks up the top-level domain entity representing the persona, with the provided {@link ServiceRegistry}
     * parameter providing access to all domain services available.
     *
     * <p>
     *     Typically the implementation looks up the appropriate domain-specific repository domain service (or could
     *     just use the generic {@link org.apache.isis.applib.services.repository.RepositoryService} in order to
     *     find by key.
     * </p>
     *
     * @param serviceRegistry
     */
    T findUsing(final ServiceRegistry serviceRegistry);

}

