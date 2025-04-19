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
package org.apache.causeway.persistence.jpa.eclipselink.metamodel;

import java.lang.reflect.Method;

import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.SingularAttribute;

import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.mappings.DatabaseMapping;

import org.apache.causeway.commons.internal.base._Lazy;
import org.apache.causeway.commons.internal.context._Context;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
class _EclipseLinkInternals {
    
    @SneakyThrows
    ClassDescriptor getClassDescriptor(final EntityType<?> entityType) {
        return (ClassDescriptor) managedTypeImplGetDescriptor.get().invoke(entityType, new Object[0]);
    }
    
    @SneakyThrows
    DatabaseMapping getDatabaseMapping(final SingularAttribute<?, ?> sa) {
        return (DatabaseMapping) attributeImplGetMappingLazy.get().invoke(sa, new Object[0]);
    }
    
    // -- HELPER
    
    private _Lazy<Method> managedTypeImplGetDescriptor = _Lazy.threadSafe(()->{
        var x = _Context.loadClass("org.eclipse.persistence.internal.jpa.metamodel.ManagedTypeImpl");
        return x.getMethod("getDescriptor", new Class[0]);
    });
    
    private _Lazy<Method> attributeImplGetMappingLazy = _Lazy.threadSafe(()->{
        var x = _Context.loadClass("org.eclipse.persistence.internal.jpa.metamodel.AttributeImpl");
        return x.getMethod("getMapping", new Class[0]);
    }); 
    
}
