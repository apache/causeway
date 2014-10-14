/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.metamodel.spec;

import java.lang.reflect.Method;
import java.util.Map;
import com.google.common.collect.Maps;

public abstract class SpecificationLoaderAbstract implements SpecificationLoader {

    @Override
    public void injectInto(final Object candidate) {
        if (SpecificationLoaderAware.class.isAssignableFrom(candidate.getClass())) {
            final SpecificationLoaderAware cast = SpecificationLoaderAware.class.cast(candidate);
            cast.setSpecificationLookup(this);
        }
    }

    //region > isInjectorMethodFor

    private final Map<Method, Map<Class<?>, Boolean>> isInjectorMethod = Maps.newConcurrentMap();

    public boolean isInjectorMethodFor(Method method, final Class<?> serviceClass) {
        Map<Class<?>, Boolean> classBooleanMap = isInjectorMethod.get(method);
        if(classBooleanMap == null) {
            synchronized (isInjectorMethod) {
                classBooleanMap = Maps.newConcurrentMap();
                isInjectorMethod.put(method, classBooleanMap);
            }
        }
        Boolean result = classBooleanMap.get(serviceClass);
        if(result == null) {
            result = determineIsInjectorMethodFor(method, serviceClass);
            classBooleanMap.put(serviceClass, result);
        }
        return result;
    }

    private static boolean determineIsInjectorMethodFor(Method method, Class<?> serviceClass) {
        final String methodName = method.getName();
        if (methodName.startsWith("set") || methodName.startsWith("inject")) {
            final Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] != Object.class && parameterTypes[0].isAssignableFrom(serviceClass)) {
                return true;
            }
        }
        return false;
    }
    //endregion

}
