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
package org.apache.isis.config.registry;

import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.Value;
import lombok.val;

@Value(staticConstructor="of")
public class TypeMetaData {

    /**
     * Fully qualified name of the underlying class.
     */
    String className;

    //    /**
    //     * Fully qualified class names of all annotation types that are present on the underlying class.
    //     */
    //    Set<String> annotationTypes;

    //    public boolean hasSingletonAnnotation() {
    //        return annotationTypes.contains(singletonAnnotation);
    //    }
    //    
    //    public boolean hasRequestScopedAnnotation() {
    //        return annotationTypes.contains(requestScopedAnnotation);
    //    }
    //    
    //    public boolean hasDomainServiceAnnotation() {
    //        return annotationTypes.contains(domainServiceAnnotation);
    //    }
    //    
    //    public boolean hasDomainObjectAnnotation() {
    //        return annotationTypes.contains(domainObjectAnnotation);
    //    }
    //    
    //    public boolean hasMixinAnnotation() {
    //        return annotationTypes.contains(mixinAnnotation);
    //    }
    //    
    //    public boolean hasViewModelAnnotation() {
    //        return annotationTypes.contains(viewModelAnnotation);
    //    }

    /**
     * @return the underlying class of this TypeMetaData
     */
    public Class<?> getUnderlyingClass() {
        try {
            return _Context.loadClass(className);
        } catch (ClassNotFoundException e) {
            val msg = String.format("Failed to load class for name '%s'", className);
            throw _Exceptions.unrecoverable(msg, e);
        }
    }

    //    private final static String singletonAnnotation = 
    //    		javax.inject.Singleton.class.getName();
    //    private final static String requestScopedAnnotation = 
    //    		javax.enterprise.context.RequestScoped.class.getName();
    //    private final static String domainServiceAnnotation = 
    //            org.apache.isis.applib.annotation.DomainService.class.getName();
    //    private final static String domainObjectAnnotation = 
    //            org.apache.isis.applib.annotation.DomainObject.class.getName();
    //    private final static String mixinAnnotation = 
    //            org.apache.isis.applib.annotation.Mixin.class.getName();
    //    private final static String viewModelAnnotation = 
    //            org.apache.isis.applib.annotation.ViewModel.class.getName();




}
