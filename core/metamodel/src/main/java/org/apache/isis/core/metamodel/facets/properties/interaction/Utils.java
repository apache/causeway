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
package org.apache.isis.core.metamodel.facets.properties.interaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.eventbus.PropertyInteractionEvent;

class Utils {
    private Utils(){}
    @SuppressWarnings("unchecked")
    public static <S,T> PropertyInteractionEvent<S,T> newEvent(
            final Class<? extends PropertyInteractionEvent<S, T>> type,
            final S source, 
            final Identifier identifier,
            final T oldValue, 
            final T newValue) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException  {
        final Constructor<?>[] constructors = type.getConstructors();
        for (final Constructor<?> constructor : constructors) {
            final Class<?>[] parameterTypes = constructor.getParameterTypes();
            if(parameterTypes.length != 4) {
                continue;
            }
            if(!parameterTypes[0].isAssignableFrom(source.getClass())) {
                continue;
            }
            if(!parameterTypes[1].isAssignableFrom(Identifier.class)) {
                continue;
            }
            if(oldValue != null && !parameterTypes[2].isAssignableFrom(oldValue.getClass())) {
                continue;
            }
            if(newValue != null && !parameterTypes[3].isAssignableFrom(newValue.getClass())) {
                continue;
            }
            final Object event = constructor.newInstance(source, identifier, oldValue, newValue);
            return (PropertyInteractionEvent<S, T>) event;
        }
        throw new NoSuchMethodException(type.getName()+".<init>(? super " + source.getClass().getName() + ", " + Identifier.class.getName() + ", java.lang.Object, java.lang.Object)");
    }
}