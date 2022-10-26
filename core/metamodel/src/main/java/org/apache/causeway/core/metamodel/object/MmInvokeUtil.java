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
package org.apache.causeway.core.metamodel.object;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;

import lombok.val;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmInvokeUtil {

    /** PAT ... Parameters as Tuple */
    public static Object invokeWithPAT(
            final Constructor<?> patConstructor,
            final Method method,
            final ManagedObject adapter,
            final Can<ManagedObject> pendingArguments,
            final List<Object> additionalArguments) {

        val pat = CanonicalInvoker.construct(patConstructor, MmUnwrapUtil.multipleAsArray(pendingArguments));
        val paramPojos = _Arrays.combineWithExplicitType(Object.class, pat, additionalArguments.toArray());
        return CanonicalInvoker.invoke(method, MmUnwrapUtil.single(adapter), paramPojos);
    }

    /** PAT ... Parameters as Tuple */
    public static Object invokeWithPAT(
            final Constructor<?> ppmConstructor,
            final Method method,
            final ManagedObject adapter,
            final Can<ManagedObject> argumentAdapters) {
        return invokeWithPAT(ppmConstructor, method, adapter, argumentAdapters, Collections.emptyList());
    }

    public static void invokeAll(final Iterable<Method> methods, final ManagedObject adapter) {
        CanonicalInvoker.invokeAll(methods, MmUnwrapUtil.single(adapter));
    }

    public static Object invoke(final Method method, final ManagedObject adapter) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtil.single(adapter));
    }

    public static Object invoke(final Method method, final ManagedObject adapter, final Object arg0) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtil.single(adapter), new Object[] {arg0});
    }

    public static Object invoke(final Method method, final ManagedObject adapter, final Can<ManagedObject> argumentAdapters) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtil.single(adapter), MmUnwrapUtil.multipleAsArray(argumentAdapters));
    }

    public static Object invoke(final Method method, final ManagedObject adapter, final ManagedObject arg0Adapter) {
        return invoke(method, adapter, MmUnwrapUtil.single(arg0Adapter));
    }

    public static Object invoke(final Method method, final ManagedObject adapter, final ManagedObject[] argumentAdapters) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtil.single(adapter), MmUnwrapUtil.multipleAsArray(argumentAdapters));
    }

    /**
     * Invokes the method, adjusting arguments as required to make them fit the method's parameters.
     * <p>
     * That is:
     * <ul>
     * <li>if the method declares parameters but arguments are missing, then will provide 'null' defaults for these.</li>
     * </ul>
     */
    public static Object invokeAutofit(final Method method, final ManagedObject adapter) {
        return invoke(method, adapter, new ManagedObject[method.getParameterTypes().length]);
    }

    /**
     * Invokes the method, adjusting arguments as required to make them fit the method's parameters.
     * <p>
     * That is:
     * <ul>
     * <li>if the method declares parameters but arguments are missing, then will provide 'null' defaults for these.</li>
     * <li>if the method does not declare all parameters for arguments, then truncates arguments.</li>
     * <li>any {@code additionalArgValues} must also fit at the end of the resulting parameter list</li>
     * </ul>
     */
    public static Object invokeAutofit(
            final Method method,
            final ManagedObject target,
            final Can<? extends ManagedObject> pendingArgs,
            final List<Object> additionalArgValues) {

        val argArray = adjust(method, pendingArgs, additionalArgValues);

        return CanonicalInvoker.invoke(method, MmUnwrapUtil.single(target), argArray);
    }

    /**
     * same as {@link #invokeAutofit(Method, ManagedObject, Can, List)} w/o additionalArgValues
     */
    public static Object invokeAutofit(
            final Method method,
            final ManagedObject target,
            final Can<? extends ManagedObject> pendingArgs) {

        return invokeAutofit(method, target, pendingArgs, Collections.emptyList());
    }

    private static Object[] adjust(
            final Method method,
            final Can<? extends ManagedObject> pendingArgs,
            final List<Object> additionalArgValues) {

        val parameterTypes = method.getParameterTypes();
        val paramCount = parameterTypes.length;
        val additionalArgCount = additionalArgValues.size();
        val pendingArgsToConsiderCount = paramCount - additionalArgCount;

        val argIterator = argIteratorFrom(pendingArgs);
        val adjusted = new Object[paramCount];
        for(int i=0; i<pendingArgsToConsiderCount; i++) {

            val paramType = parameterTypes[i];
            val arg = argIterator.hasNext() ? MmUnwrapUtil.single(argIterator.next()) : null;

            adjusted[i] = honorPrimitiveDefaults(paramType, arg);
        }

        // add the additional parameter values (if any)
        int paramIndex = pendingArgsToConsiderCount;
        for(val additionalArg : additionalArgValues) {
            val paramType = parameterTypes[paramIndex];
            adjusted[paramIndex] = honorPrimitiveDefaults(paramType, additionalArg);
            ++paramIndex;
        }

        return adjusted;

    }

    private static Iterator<? extends ManagedObject> argIteratorFrom(final Can<? extends ManagedObject> pendingArgs) {
        return pendingArgs!=null ? pendingArgs.iterator() : Collections.emptyIterator();
    }

    private static Object honorPrimitiveDefaults(
            final Class<?> expectedType,
            final @Nullable Object value) {

        if(value == null && expectedType.isPrimitive()) {
            return ClassExtensions.toDefault(expectedType);
        }
        return value;
    }


}