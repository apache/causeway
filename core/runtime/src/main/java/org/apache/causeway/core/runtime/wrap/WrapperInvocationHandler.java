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
package org.apache.causeway.core.runtime.wrap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jspecify.annotations.NonNull;

import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.applib.services.wrapper.events.InteractionEvent;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.core.metamodel.context.MetaModelContext;

public interface WrapperInvocationHandler extends InvocationHandler {
    
    Context context();
    
    default Method equalsMethod() { return context().equalsMethod(); }
    default Method hashCodeMethod() { return context().hashCodeMethod(); }
    default Method toStringMethod() { return context().toStringMethod(); }
    
    public record Context(
            Object delegate,
            WrapperFactory wrapperFactory,
            SyncControl syncControl,

            Method equalsMethod,
            Method hashCodeMethod,
            Method toStringMethod) {
        
        public static Context of(
                final @NonNull MetaModelContext metaModelContext,
                final @NonNull Object pojo,
                final SyncControl syncControl) {

            var pojoClass = pojo.getClass();
            try {
                var equalsMethod = pojoClass.getMethod("equals", _Constants.classesOfObject);
                var hashCodeMethod = pojoClass.getMethod("hashCode", _Constants.emptyClasses);
                var toStringMethod = pojoClass.getMethod("toString", _Constants.emptyClasses);
                
                return new WrapperInvocationHandler
                        .Context(pojo, metaModelContext.getWrapperFactory(), 
                                syncControl, equalsMethod, hashCodeMethod, toStringMethod);
                
            } catch (final NoSuchMethodException e) {
                // ///CLOVER:OFF
                throw new RuntimeException("An Object method could not be found: " + e.getMessage());
                // ///CLOVER:ON
            }
        }
        
        public Object invoke(final Method method, final Object[] args)
                throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return method.invoke(delegate(), args);
        }
        
        public boolean isObjectMethod(final Method method) {
            return toStringMethod().equals(method) 
                    || hashCodeMethod().equals(method) 
                    || equalsMethod().equals(method);
        }
        
        public InteractionEvent notifyListeners(final InteractionEvent interactionEvent) {
            wrapperFactory().notifyListeners(interactionEvent);
            return interactionEvent;
        }
        
    }
    
}
