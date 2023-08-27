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

import java.lang.reflect.Method;
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
import org.apache.causeway.commons.internal.reflection._Reflect;
import org.apache.causeway.core.metamodel.facetapi.FeatureType;

import lombok.val;
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

    Optional<Method> findMethodExact(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        try {
            return Optional.ofNullable(type.getMethod(methodName, methodTypes));
        } catch (final SecurityException e) {
            return Optional.empty();
        } catch (final NoSuchMethodException e) {
            return Optional.empty();
        }
    }

    Optional<Method> findMethodExact(final Class<?> type, final String methodName) {
        return findMethodExact(type, methodName, _Constants.emptyClasses);
    }

    Method findMethodExactOrFail(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        return findMethodExact(type, methodName, methodTypes)
                .orElseThrow(()->
                _Exceptions.noSuchElement("method '%s' not found in %s", methodName, type));
    }

    Method findMethodExactOrFail(final Class<?> type, final String methodName) {
        return findMethodExactOrFail(type, methodName, _Constants.emptyClasses);
    }

    Can<Method> findMethodsByName(final Class<?> type, final String methodName) {
        val matchingMethods = _ClassCache.getInstance().streamPublicOrDeclaredMethods(type)
                .filter(method->method.getName().equals(methodName))
                .collect(Can.toCan());
        return matchingMethods;
    }

    Method findMethodByNameOrFail(final Class<?> type, final String methodName) {
        val matchingMethods = findMethodsByName(type, methodName);

        //TODO[CAUSEWAY-3556] this logic should be moved to the _ClassCache
        return matchingMethods.isCardinalityMultiple()
                ? _Reflect.mostSpecificMethodOf(matchingMethods).orElseThrow()
                : matchingMethods.getSingletonOrFail();
    }

    Optional<Method> findGetter(final Class<?> declaringClass, final String propertyName) {
        return _Utils.findMethodExact(declaringClass, "get" + _Strings.capitalize(propertyName))
                .or(()->_Utils.findMethodExact(declaringClass, "is" + _Strings.capitalize(propertyName)));
    }

    Method findGetterOrFail(final Class<?> declaringClass, final String propertyName) {
        val getter = findGetter(declaringClass, propertyName)
                    .orElseThrow(()->
                        _Exceptions.noSuchElement("getter '%s' not found in %s", propertyName, declaringClass));
        return getter;
    }

    void assertMethodEquals(final Method a, final Method b) {
        assertEquals(a.getName(), b.getName());
        assertEquals(a.getParameterCount(), b.getParameterCount());
        assertArrayEquals(a.getParameterTypes(), b.getParameterTypes());

        val ownerA = a.getDeclaringClass();
        val ownerB = b.getDeclaringClass();

        assertTrue(ownerA.isAssignableFrom(ownerB)
                || ownerB.isAssignableFrom(ownerA));

    }

}
