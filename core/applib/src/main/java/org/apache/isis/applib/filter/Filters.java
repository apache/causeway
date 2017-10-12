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

package org.apache.isis.applib.filter;

import com.google.common.base.Predicates;

/**
 * @deprecated - use {@link com.google.common.base.Predicate} and {@link Predicates} instead.
 */
@Deprecated
public final class Filters {

    private Filters() {
    }

    public static <T> Predicate<T> and(final Predicate<T>... predicates) {
        return new Predicate<T>() {
            @Override
            public boolean apply(final T f) {
                for(final Predicate<T> predicate : predicates) {
                    if(!predicate.apply(f)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static <T> Predicate<T> or(final Predicate<T>... predicates) {
        return new Predicate<T>() {
            @Override
            public boolean apply(final T f) {
                for(final Predicate<T> predicate : predicates) {
                    if(predicate.apply(f)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static <T> Predicate<T> not(final Predicate<T> f1) {
        return new Predicate<T>() {
            @Override
            public boolean apply(final T f) {
                return !f1.apply(f);
            }
        };
    }

    public static <T> Predicate<T> any() {
        return new Predicate<T>() {
            @Override
            public boolean apply(final T t) {
                return true;
            }
        };
    }

    public final static <T> Predicate<T> anyOfType(final Class<T> clazz) {
        return any();
    }

    public static <T> Predicate<T> none() {
        return new Predicate<T>() {
            @Override
            public boolean apply(final T f) {
                return false;
            }
        };
    }

    public static <T> Predicate<T> noneOfType(final Class<T> clazz) {
        return none();
    }

    public static <T> com.google.common.base.Predicate asPredicate(final Predicate<T> predicate) {
        return new com.google.common.base.Predicate<T>() {
            @Override
            public boolean apply(T candidate) {
                return predicate.apply(candidate);
            }
        };
    }

}
