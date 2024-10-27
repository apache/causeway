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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.internal.collections._Arrays;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedConstructor;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.commons.internal.reflection._MethodFacades.MethodFacade;
import org.apache.causeway.core.metamodel.commons.CanonicalInvoker;
import org.apache.causeway.core.metamodel.commons.ClassExtensions;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class MmInvokeUtils {

    /** PAT ... Parameters as Tuple */
    public static Object invokeWithPAT(
            final ResolvedConstructor patConstructor,
            final ResolvedMethod method,
            final ManagedObject adapter,
            final Can<ManagedObject> pendingArguments,
            final List<Object> additionalArguments) {

        var pat = CanonicalInvoker.construct(patConstructor.constructor(), MmUnwrapUtils.multipleAsArray(pendingArguments));
        var paramPojos = _Arrays.combineWithExplicitType(Object.class, pat, additionalArguments.toArray());
        return CanonicalInvoker.invoke(method.method(), MmUnwrapUtils.single(adapter), paramPojos);
    }

    /** PAT ... Parameters as Tuple */
    public static Object invokeWithPAT(
            final ResolvedConstructor patConstructor,
            final ResolvedMethod method,
            final ManagedObject adapter,
            final Can<ManagedObject> argumentAdapters) {
        return invokeWithPAT(patConstructor, method, adapter, argumentAdapters, Collections.emptyList());
    }

    public static void invokeAll(final Iterable<Method> methods, final ManagedObject adapter) {
        CanonicalInvoker.invokeAll(methods, MmUnwrapUtils.single(adapter));
    }

    public static Object invokeAutofit(
            final Optional<ResolvedConstructor> patConstructor,
            final MethodFacade methodFacade, final ManagedObject owningAdapter, final Can<ManagedObject> pendingArgs) {
        return patConstructor.isPresent()
                ? invokeWithPAT(patConstructor.get(),
                        methodFacade.asMethodForIntrospection(),
                        owningAdapter, pendingArgs)
                : invokeAutofit(methodFacade.asMethodElseFail().method(),
                        owningAdapter, pendingArgs);
    }

    public static Object invokeNoAutofit(
            final Optional<ResolvedConstructor> patConstructor,
            final MethodFacade methodFacade, final ManagedObject owningAdapter, final Can<ManagedObject> pendingArgs) {
        return patConstructor.isPresent()
                ? invokeWithPAT(patConstructor.get(),
                        methodFacade.asMethodForIntrospection(),
                        owningAdapter, pendingArgs)
                : invokeWithArgs(methodFacade.asMethodElseFail().method(),
                        owningAdapter, pendingArgs);
    }

    public static Object invokeWithSearchArg(
            final Optional<ResolvedConstructor> patConstructor,
            final MethodFacade methodFacade, final ManagedObject owningAdapter, final Can<ManagedObject> pendingArgs, final String searchArg) {
        final Object collectionOrArray = patConstructor.isPresent()
                ? invokeWithPAT(
                        patConstructor.get(),
                        methodFacade.asMethodForIntrospection(),
                        owningAdapter, pendingArgs,
                        Collections.singletonList(searchArg))
                : invokeAutofit(
                        methodFacade.asMethodElseFail().method(),
                        owningAdapter, pendingArgs,
                        Collections.singletonList(searchArg));
        return collectionOrArray;
    }

    public static Object invokeNoArg(final Method method, final ManagedObject adapter) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtils.single(adapter));
    }

    public static Object invokeWithSingleArgPojo(final Method method, final ManagedObject adapter, final Object arg0) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtils.single(adapter), new Object[] {arg0});
    }

    public static Object invokeWithArgs(final Method method, final ManagedObject adapter, final Can<ManagedObject> argumentAdapters) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtils.single(adapter), MmUnwrapUtils.multipleAsArray(argumentAdapters));
    }

    public static Object invokeWithSingleArg(final Method method, final ManagedObject adapter, final ManagedObject arg0Adapter) {
        return invokeWithSingleArgPojo(method, adapter, MmUnwrapUtils.single(arg0Adapter));
    }

    public static Object invokeWithArgArray(final Method method, final ManagedObject adapter, final ManagedObject[] argumentAdapters) {
        return CanonicalInvoker.invoke(method, MmUnwrapUtils.single(adapter), MmUnwrapUtils.multipleAsArray(argumentAdapters));
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
        return invokeWithArgArray(method, adapter, new ManagedObject[method.getParameterTypes().length]);
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

        var argArray = adjust(method, pendingArgs, additionalArgValues);

        return CanonicalInvoker.invoke(method, MmUnwrapUtils.single(target), argArray);
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

        var parameterTypes = method.getParameterTypes();
        var paramCount = parameterTypes.length;
        var additionalArgCount = additionalArgValues.size();
        var pendingArgsToConsiderCount = paramCount - additionalArgCount;

        var argIterator = argIteratorFrom(pendingArgs);
        var adjusted = new Object[paramCount];
        for(int i=0; i<pendingArgsToConsiderCount; i++) {

            var paramType = parameterTypes[i];
            var arg = argIterator.hasNext() ? MmUnwrapUtils.single(argIterator.next()) : null;

            adjusted[i] = honorPrimitiveDefaults(paramType, arg);
        }

        // add the additional parameter values (if any)
        int paramIndex = pendingArgsToConsiderCount;
        for(var additionalArg : additionalArgValues) {
            var paramType = parameterTypes[paramIndex];
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