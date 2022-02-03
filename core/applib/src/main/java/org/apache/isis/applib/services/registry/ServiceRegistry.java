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

package org.apache.isis.applib.services.registry;

import org.apache.isis.applib.annotation.Programmatic;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface ServiceRegistry {

    @Deprecated
    @Programmatic
    <T> T injectServicesInto(final T domainObject);

    @Programmatic
    <T> Optional<T> lookupService(Class<T> service);

    @Deprecated
    @Programmatic
    <T> Iterable<T> lookupServices(Class<T> service);


    /**
     * Looks up a domain service of the requested type (same as
     * {@link #lookupService(Class)}) but throws a
     * {@link NoSuchElementException} if there are no such instances.
     *
     * @param serviceClass
     * @param <T>
     */
    @Programmatic
    default <T> T lookupServiceElseFail(final Class<T> serviceClass) {

        return lookupService(serviceClass)
                .orElseThrow(()->
                        new NoSuchElementException("Could not locate service of type '" + serviceClass + "'"));

        // ...
    }

}
