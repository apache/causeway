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

import org.apache.isis.applib.annotations.Programmatic;
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
public abstract class AbstractSpecification<T> implements Specification {

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
            .filter(AbstractSpecification::isSatisfiesSafelyMethod)
            .findFirst()
            .orElse(null);

            if(methodFound!=null) {
                return methodFound.getParameterTypes()[0];
            }

        }

        throw new Error("Cannot determine correct type for satisfiesSafely() method.");
    }

    private static boolean isSatisfiesSafelyMethod(final Method method) {
        return method.getName().equals("satisfiesSafely") && method.getParameterTypes().length == 1 && !method.isSynthetic();
    }

    private final Class<?> expectedType;
    private final Nullability nullability;
    private final TypeChecking typeChecking;

    protected AbstractSpecification() {
        this(Nullability.IGNORE_IF_NULL, TypeChecking.IGNORE_INCORRECT_TYPE);
    }

    protected AbstractSpecification(final Nullability nullability, final TypeChecking typeChecking) {
        this.expectedType = findExpectedType(getClass());
        this.nullability = nullability;
        this.typeChecking = typeChecking;
    }

    /**
     * Checks not null and is correct type, and delegates to
     * {@link #satisfiesSafely(Object)}.
     */
    @Programmatic
    @Override
    @SuppressWarnings({ "unchecked" })
    public final String satisfies(final Object obj) {
        if (obj == null) {
            return nullability == Nullability.IGNORE_IF_NULL ? null : "Cannot be null";
        }
        if (!expectedType.isInstance(obj)) {
            return typeChecking == TypeChecking.IGNORE_INCORRECT_TYPE ? null : "Incorrect type";
        }
        final T objAsT = (T) obj;
        return satisfiesSafely(objAsT);
    }

    /**
     * If <tt>null</tt> then satisfied, otherwise is reason why the
     * specification is not satisfied.
     *
     * <p>
     * Subclasses should implement this. The item will already have been checked
     * for the specific type and will never be null.
     */
    @Programmatic
    public abstract String satisfiesSafely(T obj);

}
