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
package org.apache.isis.applib.services.inject;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import org.springframework.beans.factory.InjectionPoint;

import org.apache.isis.commons.internal.context._Context;

import lombok.val;

/**
 * Resolves injection points using the ServiceRegistry.
 * <p>
 * Implementation must be thread-safe.
 * </p>
 * 
 * @since 2.0.0-M3
 */
public interface ServiceInjector {

    <T> T injectServicesInto(final T domainObject, Consumer<InjectionPoint> onNotResolvable);
    
    default <T> T injectServicesInto(final T domainObject) {
        return injectServicesInto(domainObject, injectionPoint->{
            
            val injectionPointName = injectionPoint.toString();
            val requiredType = injectionPoint.getDeclaredType();
            val msg = String
                    .format("Could not resolve injection point [%s] in target '%s' of required type '%s'",
                    injectionPointName,        
                    domainObject.getClass().getName(),
                    requiredType);
            throw new NoSuchElementException(msg);
        });
            
    }
   
    
}
