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
package org.apache.isis.commons.internal.cdi;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;
import javax.inject.Qualifier;

import org.apache.isis.commons.internal.base._NullSafe;

/**
 * <h1>- internal use only -</h1>
 * <p>
 * Framework internal CDI support.
 * <p>
 * <b>WARNING</b>: Do <b>NOT</b> use any of the classes provided by this package! <br/>
 * These may be changed or removed without notice!
 *
 * @since 2.0.0-M2
 */
public final class _CDI {
    
    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<T> getManagedBean(final Class<T> subType, List<Annotation> qualifiers) {
        if(_NullSafe.isEmpty(qualifiers)) {
            return getManagedBean(subType);
        }
        
        final Annotation[] _qualifiers = qualifiers.toArray(new Annotation[] {});
        
        return cdi()
                .map(cdi->tryGet(()->cdi.select(subType, _qualifiers)))
                .map(instance->tryGet(instance::get));
    }

    /**
     * Obtains a child Instance for the given required type and additional required qualifiers. 
     * @param subType
     * @param qualifiers
     * @return an optional, empty if passed two instances of the same qualifier type, or an 
     * instance of an annotation that is not a qualifier type
     */
    public static <T> Optional<T> getManagedBean(final Class<T> subType) {
        return cdi()
                .map(cdi->tryGet(()->cdi.select(subType)))
                .map(instance->tryGet(instance::get));
    }
    
    /**
     * Get the CDI instance that provides access to the current container. 
     * @return an optional
     */
    public static Optional<CDI<Object>> cdi() {
        try {
            CDI<Object> cdi = CDI.current();
            return Optional.ofNullable(cdi);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Filters the input array into a collection, such that only annotations are retained, 
     * that are valid qualifiers for CDI.
     * @param annotations
     * @return non-null
     */
    public static List<Annotation> filterQualifiers(final Annotation[] annotations) {
        return _NullSafe.stream(annotations)
        .filter(_CDI::isQualifier)
        .collect(Collectors.toList());
    }
    
    /**
     * @param annotation
     * @return whether or not the annotation is a valid qualifier for CDI
     */
    public static boolean isQualifier(Annotation annotation) {
        if(annotation==null) {
            return false;
        }
        return annotation.annotationType().getAnnotationsByType(Qualifier.class).length>0;
    }
    
    // -- HELPER
    
    private _CDI() {}
    
    private static <T> T tryGet(final Supplier<T> supplier) {
        try { 
            return supplier.get();  
        } catch (Exception e) {
            return null;
        }
    }

}
