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

package org.apache.isis.core.commons.lang;

import java.lang.reflect.Method;

public class MethodUtils {

    private MethodUtils() {
    }

    public static Method getMethod(final Object object, final String methodName, final Class<?>... parameterClass) throws NoSuchMethodException {
        return getMethod(object.getClass(), methodName, parameterClass);
    }

    public static Method getMethod(final Object object, final String methodName) throws NoSuchMethodException {
        return getMethod(object.getClass(), methodName, new Class[0]);
    }

    public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterClass) throws NoSuchMethodException {
        return clazz.getMethod(methodName, parameterClass);
    }

    public static Method findMethodElseNull(final Class<?> clazz, final String methodName, final Class<?>... parameterClass) {
        try {
            return clazz.getMethod(methodName, parameterClass);
        } catch (final NoSuchMethodException e) {
            return null;
        }
    }

    public static Method findMethodElseNull(final Class<?> clazz, final String[] candidateMethodNames, final Class<?>... parameterClass) {
        for (final String candidateMethodName : candidateMethodNames) {
            final Method method = findMethodElseNull(clazz, candidateMethodName, parameterClass);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

}
