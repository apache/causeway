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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.DomainService;
import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.core.commons.exceptions.IsisException;

public final class ServiceUtil {

    private ServiceUtil() {
    }

    public static String id(final Class<?> serviceClass) {
        final String serviceType = serviceTypeOf(serviceClass);
        if (serviceType != null) {
            return serviceType;
        }

        try {
            Object object = serviceClass.newInstance();
            return getIdOf(object);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            return null;
        }
    }

    public static String id(final Object object) {
        final Class<?> serviceClass = object.getClass();
        final String serviceType = serviceTypeOf(serviceClass);
        if(serviceType != null) {
            return serviceType;
        }

        try {
            return getIdOf(object);
        } catch (final NoSuchMethodException e) {
            return fqcnOf(serviceClass);
        }
    }

    private static String serviceTypeOf(final Class<?> serviceClass) {
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

    private static String getIdOf(final Object object) throws NoSuchMethodException {
        try {
            final Class<?> objectClass = object.getClass();
            final Method m = objectClass.getMethod("getId");
            return (String) m.invoke(object);
        } catch (final SecurityException | IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
            throw new IsisException(e);
        }
    }

    private static String fqcnOf(final Class<?> serviceClass) {
        return serviceClass.getName();
    }

}
