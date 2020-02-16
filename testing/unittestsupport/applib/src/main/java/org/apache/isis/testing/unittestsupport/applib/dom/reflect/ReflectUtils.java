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
package org.apache.isis.testing.unittestsupport.applib.dom.reflect;

import org.apache.isis.core.commons.internal.reflection._Reflect;

import lombok.val;

public class ReflectUtils {
    private ReflectUtils() {
    }

    public static void inject(
            final Object target,
            final String fieldName,
            final Object toInject) {

        try {
            val field = target.getClass().getDeclaredField(fieldName);
            
            _Reflect.setFieldOn(field, target, toInject);
            
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void inject(
            final Object target,
            final Object toInject) {

        final String clsName = toInject.getClass().getSimpleName();
        final String fieldName = Character.toLowerCase(clsName.charAt(0)) + clsName.substring(1);
        inject(target, fieldName, toInject);
    }
}
