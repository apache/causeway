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
package org.apache.isis.persistence.jdo.objectadapter;

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

import org.apache.isis.commons.exceptions.IsisException;
import org.apache.isis.metamodel.spec.ObjectSpecification;
import org.apache.isis.runtime.system.context.session.RuntimeContext;

import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * package private mixin for ObjectAdapterContext
 * <p>
 * Responsibility: creates new domain object instances  
 * </p> 
 * @since 2.0
 */
@Deprecated // use ObjectManager.createObject instead
@RequiredArgsConstructor
class ObjectAdapterContext_DependencyInjection {

    private final RuntimeContext runtimeContext;

    Object instantiateAndInjectServices(ObjectSpecification spec) {

        val type = spec.getCorrespondingClass();
        if (type.isArray()) {
            return Array.newInstance(type.getComponentType(), 0);
        }

        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IsisException("Cannot create an instance of an abstract class: " + type);
        }

        final Object newInstance;
        try {
            newInstance = type.newInstance();
        } catch (final IllegalAccessException | InstantiationException e) {
            throw new IsisException("Failed to create instance of type " + spec.getFullIdentifier(), e);
        }

        runtimeContext.getServiceInjector().injectServicesInto(newInstance);
        return newInstance;

    }

}