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


package org.apache.isis.commons.filters;


public final class Filters {

    private Filters() {}

    public static <T> Filter<T> and(final Filter<T> f1, final Filter<T> f2) {
        return new AbstractFilter<T>() {
            @Override
            public boolean accept(final T f) {
                return f1.accept(f) && f2.accept(f);
            }
        };
    }

    public static <T> Filter<T> or(final Filter<T> f1, final Filter<T> f2) {
        return new AbstractFilter<T>() {
            @Override
            public boolean accept(final T f) {
                return f1.accept(f) || f2.accept(f);
            }
        };
    }

    public static <T> Filter<T> not(final Filter<T> f1) {
        return new AbstractFilter<T>() {
            @Override
            public boolean accept(final T f) {
                return !f1.accept(f);
            }
        };
    }

    public static <T> Filter<T> any() {
        return new AbstractFilter<T>() {

            @Override
            public boolean accept(final T t) {
                return true;
            }
        };
    }

    public static <T> Filter<T> none() {
        return new AbstractFilter<T>() {
            @Override
            public boolean accept(final T f) {
                return false;
            }
        };
    }

}
