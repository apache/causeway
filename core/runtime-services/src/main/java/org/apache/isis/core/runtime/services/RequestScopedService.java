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

package org.apache.isis.core.runtime.services;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.inject.ServiceInjector;

/**
 * Interface in support of request-scoped domain services (is implemented by the Javassist proxy).
 *
 * <p>
 *     Do NOT implement directly (will cause the javassist generation to fail).  Instead the request-scoped service
 *     can provide a (conventional) <code>@PostConstruct</code> and <code>@PreDestroy</code> method.
 * </p>
 */
public interface RequestScopedService {

    /**
     * Indicates to the proxy that a new request is starting, so should instantiate a new instance of the underlying
     * service and bind to the thread, and inject into that service using the provided {@link ServiceInjector}.
     *
     * <p>
     *     This is done before the <code>@PostConstruct</code>, see {@link #__isis_postConstruct()}.
     * </p>
     */
    @Programmatic
    public void __isis_startRequest(ServiceInjector injector);

    /**
     * Indicates to the proxy that <code>@PostConstruct</code> should be called on
     * underlying instance for current thread.
     *
     * <p>
     *     This is done after the request has started, see {@link #__isis_startRequest(ServiceInjector)}.
     * </p>
     */
    @Programmatic
    public void __isis_postConstruct();

    /**
     * Indicates to the proxy that <code>@PreDestroy</code> should be called on
     * underlying instance for current thread.
     *
     * <p>
     *     This is done prior to the request ending, see {@link #__isis_endRequest()}.
     * </p>
     */
    @Programmatic
    public void __isis_preDestroy();

    /**
     * Indicates to the proxy that request is ending, so should clean up and remove the
     * underlying instance for current thread.
     *
     * <p>
     *     This is done after the <code>@PreDestroy</code>, see {@link #__isis_preDestroy()}.
     * </p>
     */
    @Programmatic
    public void __isis_endRequest();

}
