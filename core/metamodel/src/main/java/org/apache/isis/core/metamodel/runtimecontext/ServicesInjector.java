/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.runtimecontext;

import java.util.List;
import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.exceprecog.ExceptionRecognizer;
import org.apache.isis.applib.services.publish.PublishingService;
import org.apache.isis.core.commons.components.Injectable;

public interface ServicesInjector extends Injectable {

    /**
     * All registered services, as an immutable {@link List}.
     */
    List<Object> getRegisteredServices();

    /**
     * Provided by the <tt>ServicesInjectorDefault</tt> when used by framework.
     * 
     * <p>
     * Called in multiple places from metamodel and facets.
     */
    void injectServicesInto(final Object domainObject);

    /**
     * As per {@link #injectServicesInto(Object)}, but for all objects in the
     * list.
     */
    void injectServicesInto(List<Object> objects);

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
    <T> T lookupService(Class<T> serviceClass);
    
    /**
     * Returns all domain services implementing the requested type, in the order
     * that they were registered in <tt>isis.properties</tt>.
     * 
     * <p>
     * Typically there will only ever be one domain service implementing a given type,
     * (eg {@link PublishingService}), but for some services there can be more than one
     * (eg {@link ExceptionRecognizer}).
     * 
     * @see #lookupService(Class)
     */
    @Programmatic
    <T> List<T> lookupServices(Class<T> serviceClass);
}
