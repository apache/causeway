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
package org.apache.causeway.testing.unittestsupport.applib.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

/**
 * @since 2.0 {@index}
 */
public class InjectUtils {

    public static <T> Predicate<Field> withTypeAssignableFrom(final Class<T> type) {
        return (Field input)-> input != null && input.getType().isAssignableFrom(type);
    }

    public static <T> Predicate<Method> withReturnTypeAssignableFrom(final Class<T> type) {
        return (Method input) -> input != null && input.getReturnType().isAssignableFrom(type);
    }

    public static Predicate<Method> withParametersAssignableFrom(final Class<?>... types) {
        return (Method input) -> {
            if (input != null) {
                Class<?>[] parameterTypes = input.getParameterTypes();
                if (parameterTypes.length == types.length) {
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (!parameterTypes[i].isAssignableFrom(types[i])) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        };
    }

}
