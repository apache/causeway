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

import java.util.Optional;

import javax.persistence.EntityManager;

import org.apache.isis.commons.internal.exceptions._Exceptions;

/**
 * Provides access to the current interaction's {@link EntityManager} 
 * 
 * @since 2.0 {@index}
 */
public interface JpaSupportService {

    /**
     * Optionally returns the current interaction's {@link EntityManager},
     * based on whether an open interaction is available and a persistence layer 
     * is configured in support of JPA.
     */
    Optional<EntityManager> getEntityManager();
    
    default EntityManager getEntityManagerElseFail() {
        return getEntityManager()
                .orElseThrow(()->_Exceptions.illegalState(
                        "Current thread either has no open interaction or"
                        + " no persistence layer "
                        + " in support of JPA is configured."));
    }
    
}
