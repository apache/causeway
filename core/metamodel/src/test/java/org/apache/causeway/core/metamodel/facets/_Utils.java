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
package org.apache.causeway.core.metamodel.facets;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.collections.ImmutableEnumSet;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Strings;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.reflection._ClassCache;
import org.apache.causeway.commons.internal.reflection._GenericResolver;
import org.apache.causeway.commons.internal.reflection._GenericResolver.ResolvedMethod;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;

import lombok.experimental.UtilityClass;

@UtilityClass
class _Utils {

    DomainEventHelper domainEventHelper() {
        return DomainEventHelper.ofEventService(null);
    }

    boolean contains(final Class<?>[] array, final Class<?> val) {
        for (final Class<?> element : array) {
            if (element == val) {
                return true;
            }
        }
        return false;
    }

    boolean contains(final ImmutableEnumSet<FeatureType> featureTypes, final FeatureType featureType) {
        if(featureTypes==null || featureType==null) {
            return false;
        }
        return featureTypes.contains(featureType);
    }

    Optional<ResolvedMethod> findMethodExact(final Class<?> type, final String methodName, final Class<?>[] parameterTypes) {
        try {
            return _GenericResolver.resolveMethod(type.getMethod(methodName, parameterTypes), type);
        } catch (NoSuchMethodException | SecurityException e) {
            return Optional.empty();
        }
    }

    Optional<ResolvedMethod> findMethodExact(final Class<?> type, final String methodName) {
        return findMethodExact(type, methodName, _Constants.emptyClasses);
    }

    ResolvedMethod findMethodExactOrFail(final Class<?> type, final String methodName, final Class<?>[] paramTypes) {
        return findMethodExact(type, methodName, paramTypes)
                .orElseThrow(()->
                _Exceptions.noSuchElement("method '%s' not found in %s", methodName, type));
    }

    ResolvedMethod findMethodExactOrFail(final Class<?> type, final String methodName) {
        return findMethodExactOrFail(type, methodName, _Constants.emptyClasses);
    }

    Can<ResolvedMethod> findMethodsByName(final Class<?> type, final String methodName) {
        var matchingMethods = _ClassCache.getInstance().streamResolvedMethods(type)
                .filter(method->method.name().equals(methodName))
                .collect(Can.toCan());
        return matchingMethods;
    }

    ResolvedMethod findMethodByNameOrFail(final Class<?> type, final String methodName) {
        return _ClassCache.getInstance().findMethodUniquelyByNameOrFail(type, methodName);
    }

    Optional<ResolvedMethod> findGetter(final Class<?> declaringClass, final String propertyName) {
        return _Utils.findMethodExact(declaringClass, "get" + _Strings.capitalize(propertyName))
                .or(()->_Utils.findMethodExact(declaringClass, "is" + _Strings.capitalize(propertyName)));
    }

    ResolvedMethod findGetterOrFail(final Class<?> declaringClass, final String propertyName) {
        var getter = findGetter(declaringClass, propertyName)
                    .orElseThrow(()->
                        _Exceptions.noSuchElement("getter '%s' not found in %s", propertyName, declaringClass));
        return getter;
    }

    void assertMethodEquals(final ResolvedMethod a, final ResolvedMethod b) {
        assertEquals(a.name(), b.name());
        assertEquals(a.paramCount(), b.paramCount());
        assertArrayEquals(a.paramTypes(), b.paramTypes());

        var ownerA = a.method().getDeclaringClass();
        var ownerB = b.method().getDeclaringClass();

        assertTrue(ownerA.isAssignableFrom(ownerB)
                || ownerB.isAssignableFrom(ownerA));

    }

}
