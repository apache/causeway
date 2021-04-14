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
package org.apache.isis.persistence.jpa.applib.integration;

import javax.inject.Inject;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;
import javax.persistence.PreUpdate;

import org.apache.isis.applib.services.inject.ServiceInjector;

import lombok.extern.log4j.Log4j2;

/**
 * EntityListener class for listing with the {@link javax.persistence.EntityListeners} annotation, to
 * support injection point resolving for entities.
 * <p>
 * Instances of this class are not managed by Spring, but by the persistence layer.
 * <p>
 * The particular persistence layer implementation in use needs to be configured,
 * with a BeanManager, that is able to resolve injection points for this EntityListener.
 *
 * @since 2.0 {@index}
 */
@Log4j2
public class JpaEntityInjectionPointResolver {

    @Inject // not managed by Spring (directly)
    private ServiceInjector serviceInjector;

    @PrePersist
    @PreUpdate
    @PreRemove
    private void beforeAnyUpdate(Object entityPojo) {
        log.debug("beforeAnyUpdate: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
    }

    @PostPersist
    @PostUpdate
    @PostRemove
    private void afterAnyUpdate(Object entityPojo) {
        log.debug("afterAnyUpdate: {}", entityPojo);
    }

    @PostLoad
    private void afterLoad(Object entityPojo) {
        log.debug("afterLoad: {}", entityPojo);
        serviceInjector.injectServicesInto(entityPojo);
    }

}
