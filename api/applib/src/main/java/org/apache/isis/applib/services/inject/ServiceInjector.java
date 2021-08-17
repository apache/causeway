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
package org.apache.isis.applib.services.inject;

import org.springframework.lang.Nullable;

/**
 * Resolves injection points using the
 * {@link org.apache.isis.applib.services.registry.ServiceRegistry} (in other
 * words provides a domain service instance to all fields and setters that are
 * annotated with {@link javax.inject.Inject}).
 *
 * @since 1.x extended in 2.0 {@index}
 */
public interface ServiceInjector {

    /**
     * Injects domain services into the object.
     *
     * @param domainObject
     * @param <T>
     * @return domainObject with injection points resolved
     */
    <T> T injectServicesInto(final @Nullable T domainObject);


}
