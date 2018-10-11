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

import java.lang.reflect.Method;
import java.util.Optional;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class ServiceUtil {

    private ServiceUtil() {
    }

    public static String idOfPojo(final Object serviceObject) {
        final Class<?> serviceClass = serviceObject.getClass();
        return explicitelySpecifiedIdOfType(serviceClass, ()->serviceObject)
                .orElseGet(()->normalize(serviceClass));
    }
    
    public static String idOfSpec(final ObjectSpecification serviceSpec) {
        final Class<?> serviceClass = serviceSpec.getCorrespondingClass();
        return explicitelySpecifiedIdOfType(serviceClass, serviceClass::newInstance)
                .orElseGet(()->normalize(serviceClass));
    }
    
    public static String idOfAdapter(final ManagedObject serviceAdapter) {
        return idOfPojo(serviceAdapter.getPojo());
    }
    
    public static Optional<String> getExplicitelySpecifiedIdOfType(final Class<?> serviceClass) {
        return explicitelySpecifiedIdOfType(serviceClass, serviceClass::newInstance);
    }
    
    // -- HELPER

    private static Optional<String> explicitelySpecifiedIdOfType(
            final Class<?> serviceClass, 
            final _Functions.CheckedSupplier<Object> serviceInstanceSupplier) {
        
        final String serviceType = serviceTypeUsingAnnotation(serviceClass);
        if (serviceType != null) {
            return Optional.of(serviceType);
        }
        return serviceTypeUsingIdGetter(serviceClass, serviceInstanceSupplier);
    }
    
    private static String serviceTypeUsingAnnotation(final Class<?> serviceClass) {
        final String serviceType;
        final DomainService domainService = serviceClass.getAnnotation(DomainService.class);
        if(domainService != null) {
            serviceType = domainService.objectType();
            if(!_Strings.isNullOrEmpty(serviceType)) {
                return serviceType;
            }
        }
        return null;
    }

    private static Optional<String> serviceTypeUsingIdGetter(
            final Class<?> serviceClass, 
            final _Functions.CheckedSupplier<Object> serviceInstanceSupplier
            ) {
        
        try {
            final Method m = serviceClass.getMethod("getId");
            return Optional.ofNullable((String) m.invoke(serviceInstanceSupplier.get()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    private static String normalize(Class<?> serviceClass) {
        return serviceClass.getName();
    }

}
