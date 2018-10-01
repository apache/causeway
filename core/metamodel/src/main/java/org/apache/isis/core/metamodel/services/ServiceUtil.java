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
import java.util.function.Supplier;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.functions._Functions;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

public final class ServiceUtil {

    private ServiceUtil() {
    }

    public static String idOfType(final Class<?> serviceClass) {
        
        final String serviceType = serviceTypeUsingAnnotation(serviceClass);
        if (serviceType != null) {
            return serviceType;
        }

        return serviceTypeUsingIdGetterOrElseGet(serviceClass, ()->serviceClass.newInstance(), ()->null);
    }

    public static String idOfPojo(final Object serviceObject) {
        final Class<?> serviceClass = serviceObject.getClass();
        final String serviceType = serviceTypeUsingAnnotation(serviceClass);
        if(serviceType != null) {
            return serviceType;
        }

        return serviceTypeUsingIdGetterOrElseGet(serviceClass, ()->serviceObject, ()->normalize(serviceClass));
    }
    
    public static String idOfSpec(final ObjectSpecification serviceSpec) {
        return idOfType(serviceSpec.getCorrespondingClass());
    }
    
    public static String idOfAdapter(final ManagedObject adapter) {
        return idOfPojo(adapter.getPojo());
    }
    
    // -- HELPER

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

    private static String serviceTypeUsingIdGetterOrElseGet(
            final Class<?> serviceClass, 
            final _Functions.CheckedSupplier<Object> objectSupplier,
            final Supplier<String> orElse
            ) {
        
        try {
            final Method m = serviceClass.getMethod("getId");
            return (String) m.invoke(objectSupplier.get());
        } catch (Exception e) {
            return orElse.get();
        }
    }
    
    private static String normalize(Class<?> serviceClass) {
        return serviceClass.getName();
    }

}
