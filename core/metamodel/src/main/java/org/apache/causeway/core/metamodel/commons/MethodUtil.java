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
package org.apache.causeway.core.metamodel.commons;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.semantics.AccessorSemantics;
import org.apache.causeway.commons.semantics.CollectionSemantics;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MethodUtil {

    public static boolean isNotStatic(final ResolvedMethod method) {
        return !isStatic(method);
    }

    public static boolean isStatic(final ResolvedMethod method) {
        final int modifiers = method.method().getModifiers();
        return Modifier.isStatic(modifiers);
    }

    public static boolean isPublic(final ResolvedMethod method) {
        final int modifiers = method.method().getModifiers();
        return Modifier.isPublic(modifiers);
    }

    public static boolean isNoArg(final ResolvedMethod method) {
        return method.paramCount() == 0;
    }

    public static boolean isVoid(final ResolvedMethod method) {
        var returnType = method.returnType();
        return returnType.equals(void.class)
                    || returnType.equals(Void.class);
    }

    public static boolean isNotVoid(final ResolvedMethod method) {
        return !isVoid(method);
    }

    public static boolean isScalar(final ResolvedMethod method) {
        return isNotVoid(method)
                    && CollectionSemantics.valueOf(method.returnType())
                        .isEmpty();
    }

    @UtilityClass
    public static class Predicates {

        public static Predicate<ResolvedMethod> paramCount(final int n) {
            return method -> method.paramCount() == n;
        }

        public static Predicate<ResolvedMethod> matchParamTypes(
                final int paramIndexOffset,
                final Can<Class<?>> matchingParamTypes) {
            return method -> {
                // check params (if required)

                if(matchingParamTypes.isEmpty()) {
                    return true;
                }

                if(method.paramCount()<(paramIndexOffset+matchingParamTypes.size())) {
                    return false;
                }

                final Class<?>[] parameterTypes = method.paramTypes();

                for (int c = 0; c < matchingParamTypes.size(); c++) {
                    var left = parameterTypes[paramIndexOffset + c];
                    var right = matchingParamTypes.getElseFail(paramIndexOffset);

                    if(!Objects.equals(left, right)) {
                        return false;
                    }
                }

                return true;

            };
        }

        /**
         * @return whether the method under test matches the given signature
         */
        public static Predicate<ResolvedMethod> signature(
                final String methodName,
                final Class<?> returnType,
                final Class<?>[] paramTypes) {

            return method -> {

                if (!isPublic(method)) {
                    return false;
                }

                if (isStatic(method)) {
                    return false;
                }

                // check for name
                if (!method.name().equals(methodName)) {
                    return false;
                }

                // check for return type
                if (returnType != null && returnType != method.returnType()) {
                    return false;
                }

                // check params (if required)
                if (paramTypes != null) {
                    final Class<?>[] parameterTypes = method.paramTypes();
                    if (paramTypes.length != parameterTypes.length) {
                        return false;
                    }

                    for (int c = 0; c < paramTypes.length; c++) {
                        if ((paramTypes[c] != null) && (paramTypes[c] != parameterTypes[c])) {
                            return false;
                        }
                    }
                }

                return true;
            };

        }

        /**
         * @return whether the method under test matches the given constraints
         */
        public static Predicate<ResolvedMethod> prefixed(
                final String prefix, final Class<?> returnType, final CanBeVoid canBeVoid, final int paramCount) {

            return method -> {
                if (MethodUtil.isStatic(method)) {
                    return false;
                }
                if(!method.name().startsWith(prefix)) {
                    return false;
                }
                if(method.paramCount() != paramCount) {
                    return false;
                }
                var type = method.returnType();
                if(!ClassExtensions.isCompatibleAsReturnType(returnType, canBeVoid, type)) {
                    return false;
                }

                return true;
            };

        }

        public static Predicate<ResolvedMethod> booleanGetter() {
            return AccessorSemantics::isBooleanGetter;
        }

        public static Predicate<ResolvedMethod> nonBooleanGetter(final Class<?> returnType) {
            return method->AccessorSemantics.isNonBooleanGetter(method, returnType);
        }

        public static Predicate<ResolvedMethod> supportedNonScalarMethodReturnType() {
            return method->
                AccessorSemantics.isNonBooleanGetter(method, Iterable.class)
                && CollectionSemantics.valueOf(method.returnType())
                    .isPresent();
        }

    }

}
