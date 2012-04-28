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

package org.apache.isis.core.metamodel.services;

import java.util.List;

import org.apache.isis.applib.DomainObjectContainer;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.commons.components.SessionScopedComponent;
import org.apache.isis.core.metamodel.runtimecontext.DependencyInjector;

/**
 * The repository of services, also able to inject into any object.
 * 
 * <p>
 * The {@link #getContainer() domain object container} is always injected but is
 * not a {@link #getRegisteredServices() registered service}.
 * 
 * <p>
 * Can be considered a mutable SPI to the {@link DependencyInjector} immutable API.
 */
public interface ServicesInjector extends SessionScopedComponent, Injectable, DependencyInjector {

    // ///////////////////////////////////////////////////////////////////////////
    // Container
    // ///////////////////////////////////////////////////////////////////////////

    DomainObjectContainer getContainer();

    /**
     * Container to inject.
     * 
     * <p>
     * This itself is injected.
     */
    public void setContainer(final DomainObjectContainer container);

    // ///////////////////////////////////////////////////////////////////////////
    // Services
    // ///////////////////////////////////////////////////////////////////////////

    /**
     * Services to be injected.
     * 
     * <p>
     * Should automatically inject all services into each other (though calling
     * {@link #open()} will also do this).
     * 
     * @param services
     */
    void setServices(List<Object> services);

    /**
     * All registered services, as an immutable {@link List}.
     * 
     * <p>
     * Does not include the {@link #getContainer() container}.
     */
    List<Object> getRegisteredServices();

}
