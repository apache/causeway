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

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;

public interface ServiceRegistry {

    @Programmatic
    <T> T injectServicesInto(final T domainObject);


    /**
     * @return Stream of all currently registered service instances.
     */
    @Programmatic
    Stream<Object> streamServices();

    @Programmatic
    <T> Stream<T> streamServices(Class<T> serviceClass);
    
    /**
     * Returns the first registered domain service implementing the requested type.
     *
     * <p>
     * Typically there will only ever be one domain service implementing a given type,
     * (eg {@link PublishingService}), but for some services there can be more than one
     * (eg {@link ExceptionRecognizer}).
     *
     * @see #lookupServices(Class)
     */
    @Programmatic
    public default <T> Optional<T> lookupService(final Class<T> serviceClass) {
        return streamServices(serviceClass)
                .findFirst();
    }

    @Programmatic
    public default <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return streamServices(serviceClass)
                .findFirst()
                .orElseThrow(()->
                    new IllegalStateException("Could not locate service of type '" + serviceClass + "'"));
    }
    
}
