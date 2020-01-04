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

package org.apache.isis.metamodel.specloader;

import java.lang.reflect.Method;

import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.metamodel.spec.InjectorMethodEvaluator;

import lombok.extern.log4j.Log4j2;

@Service
@Named("isisMetaModel.InjectorMethodEvaluatorDefault")
@Order(OrderPrecedence.HIGH)
@Primary
@Qualifier("Default")
@Log4j2
public final class InjectorMethodEvaluatorDefault implements InjectorMethodEvaluator {

    //TODO[2112] cleanup comment
    //    private final Map<Method, Map<Class<?>, Boolean>> isInjectorMethod = _Maps.newConcurrentHashMap();
    //
    //    private boolean isInjectorMethodFor(
    //            final Method method,
    //            final Class<?> serviceClass) {
    //
    //        // there's no need to synchronize this access.
    //        // if there were a race condition, then at worst a result of the determineXxx method might be discard
    //        // (but it would end up being calculated next time around)
    //
    //        Map<Class<?>, Boolean> classBooleanMap = isInjectorMethod.get(method);
    //        if(classBooleanMap == null) {
    //            classBooleanMap = _Maps.newConcurrentHashMap();
    //            isInjectorMethod.put(method, classBooleanMap);
    //        }
    //        Boolean result = classBooleanMap.get(serviceClass);
    //        if(result == null) {
    //            result = determineIsInjectorMethodFor(method, serviceClass);
    //            classBooleanMap.put(serviceClass, result);
    //        }
    //        return result;
    //    }
    //
    //    private static boolean determineIsInjectorMethodFor(
    //            final Method method,
    //            final Class<?> serviceClass) {
    //        final String methodName = method.getName();
    //        if (methodName.startsWith("set") || methodName.startsWith("inject")) {
    //            final Class<?>[] parameterTypes = method.getParameterTypes();
    //            if (parameterTypes.length == 1 && parameterTypes[0] != Object.class && parameterTypes[0].isAssignableFrom(serviceClass)) {
    //                return true;
    //            }
    //        }
    //        return false;
    //    }

    @Override
    public Class<?> getTypeToBeInjected(Method setter) {
        final String methodName = setter.getName();
        if (methodName.startsWith("set") || methodName.startsWith("inject")) {
            final Class<?>[] parameterTypes = setter.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] != Object.class) { 
                return parameterTypes[0];
            }
        }
        return null;
    }


}
