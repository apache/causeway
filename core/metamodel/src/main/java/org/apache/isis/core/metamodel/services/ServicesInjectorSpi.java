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

import org.apache.isis.core.commons.components.ApplicationScopedComponent;
import org.apache.isis.core.commons.components.Injectable;
import org.apache.isis.core.metamodel.runtimecontext.ServicesInjector;

/**
 * The repository of services, also able to inject into any object.
 * 
 * <p>
 * Can be considered a mutable SPI to the {@link org.apache.isis.core.metamodel.runtimecontext.ServicesInjector} immutable API.
 */
public interface ServicesInjectorSpi extends ApplicationScopedComponent, Injectable, ServicesInjector {


    /**
     * Services to be injected.
     */
    void setServices(List<Object> services);


    /**
     * Update an individual service.
     *
     * <p>
     * There should already be a service {@link #getRegisteredServices() registered} of the specified type.
     *
     * @return <tt>true</tt> if a service of the specified type was found and updated, <tt>false</tt> otherwise.
     * @param originalService
     * @param replacementService
     */
    <T> void replaceService(T originalService, T replacementService);
}
