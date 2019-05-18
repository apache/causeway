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
package org.apache.isis.commons.internal.spring;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import org.apache.isis.commons.ioc.BeanAdapter;
import org.apache.isis.commons.ioc.BeanSort;
import org.apache.isis.commons.ioc.LifecycleContext;
import org.apache.isis.core.commons.collections.Bin;

import lombok.Value;
import lombok.val;

@Value(staticConstructor="of")
final class BeanAdapterSpring implements BeanAdapter {

    private final String id;
    private final LifecycleContext lifecycleContext;
    private final Class<?> beanClass;
    private final ObjectProvider<?> beanProvider;
    private final BeanSort managedObjectSort;
    
    @Override
    public Bin<?> getInstance() {
//[2112] debug        
//        if(beanClass.getName().contains("MessageService")) {
//            beanProvider.stream()
//            .sorted(AnnotationAwareOrderComparator.INSTANCE)  
//            .forEach(x->System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!! " + x));
//        }
        val allMatchingBeans = beanProvider.stream(); 
        return Bin.ofStream(allMatchingBeans);
    }
    
    @Override
    public boolean isCandidateFor(Class<?> requiredType) {
        return beanProvider.stream()
        .map(Object::getClass)
        .anyMatch(type->requiredType.isAssignableFrom(type));
    }
    
    
    
}
