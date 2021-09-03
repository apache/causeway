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

package org.apache.isis.applib.spec;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.i18n.TranslatableString;
import org.apache.isis.commons.internal.base._Casts;
import org.apache.isis.commons.internal.reflection._ClassCache;

import lombok.val;

/**
 * Adapter to make it easy to write {@link Specification}s.
 *
 * <p>
 * Provides two main features:
 * <ul>
 * <li>first, is type-safe (with invalid type being either ignored or
 * constituting a failure), and
 * <li>second, checks for nulls (with a null either being ignore or again
 * constituting a failure)
 * </ul>
 *
 * <p>
 * Implementation note: inspired by (borrowed code from) Hamcrest's
 * <tt>TypeSafeMatcher</tt>.
 *
 * @since 1.x {@index}
 */
public abstract class AbstractSpecification2<T> implements Specification2 {

    public enum TypeChecking {
        ENSURE_CORRECT_TYPE, IGNORE_INCORRECT_TYPE,
    }

    public enum Nullability {
        ENSURE_NOT_NULL, IGNORE_IF_NULL
    }

    private static Class<?> findExpectedType(final Class<?> fromClass) {

        val classCache = _ClassCache.getInstance();

        for (Class<?> c = fromClass; c != Object.class; c = c.getSuperclass()) {

            val methodFound = classCache
            .streamDeclaredMethods(c)
            .filter(AbstractSpecification2::isSatisfiesTranslatableSafelyMethod)
            .findFirst()
            .orElse(null);

            if(methodFound!=null) {
                return methodFound.getParameterTypes()[0];
            }

        }

        throw new Error("Cannot determine correct type for satisfiesSafely() method.");
    }

    private static boolean isSatisfiesTranslatableSafelyMethod(final Method method) {
        return method.getName().equals("satisfiesTranslatableSafely") && method.getParameterTypes().length == 1 && !method.isSynthetic();
    }

    private final Class<?> expectedType;
    private final Nullability nullability;
    private final TypeChecking typeChecking;

    protected AbstractSpecification2() {
        this(Nullability.IGNORE_IF_NULL, TypeChecking.IGNORE_INCORRECT_TYPE);
    }

    protected AbstractSpecification2(final Nullability nullability, final TypeChecking typeChecking) {
        this.expectedType = findExpectedType(getClass());
        this.nullability = nullability;
        this.typeChecking = typeChecking;
    }

    @Programmatic
    @Override
    public final String satisfies(final Object obj) {
        // unused because satisfiesTranslatable will be called instead.
        return null;
    }

    /**
     * Checks not null and is correct type, and delegates to
     * {@link #satisfiesTranslatableSafely(Object)}.
     */
    @Programmatic
    @Override
    public TranslatableString satisfiesTranslatable(final Object obj) {
        if (obj == null) {
            return nullability == Nullability.IGNORE_IF_NULL
                    ? null
                            : TranslatableString.tr("Cannot be null");
        }
        if (!expectedType.isInstance(obj)) {
            return typeChecking == TypeChecking.IGNORE_INCORRECT_TYPE
                    ? null
                            : TranslatableString.tr("Incorrect type");
        }
        final T objAsT = _Casts.uncheckedCast(obj);
        return satisfiesTranslatableSafely(objAsT);

    }

    @Programmatic
    public abstract TranslatableString satisfiesTranslatableSafely(T obj);

}
