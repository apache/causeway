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
package org.apache.isis.core.config.beans;

import org.apache.isis.core.commons.internal.base._Strings;
import org.apache.isis.core.commons.internal.context._Context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.val;

@RequiredArgsConstructor(staticName = "of")
final class TypeMetaData {

    /**
     * Fully qualified name of the underlying class.
     */
    @Getter private final String className;
    
    /**
     * As proposed by IoC, before any overrides.
     */
    @Getter private final String proposedBeanName;
    
    /**
     * Name override, applied only if not empty. 
     */
    @Getter @Setter
    private String beanNameOverride;
    
    /**
     * Whether this type should be made available to resolve injection points.  
     */
    @Getter @Setter
    private boolean injectable = true;
    
    @Getter(lazy=true)
    private final ClassOrFailure underlyingClassOrFailure = resolveClass();
    
    public String getEffectiveBeanName() {
        return _Strings.isNullOrEmpty(beanNameOverride)
                ? proposedBeanName 
                        : beanNameOverride;
    }
    
    // -- HELPER
    
    /**
     * Holds either the class or the failure string when attempting to load by name. 
     */
    @Value(staticConstructor = "of")
    final static class ClassOrFailure {
        Class<?> underlyingClass;
        String failure;
        public boolean isFailure() {
            return underlyingClass==null;
        }
    }
    
    /**
     * @return the underlying class of this TypeMetaData
     */
    private ClassOrFailure resolveClass() {
        try {
            return ClassOrFailure.of(_Context.loadClass(className), null);
        } catch (ClassNotFoundException e) {
            val msg = String.format("Failed to load class for name '%s', throwing %s", className, e);
            return ClassOrFailure.of(null, msg);
        }
    }


}
