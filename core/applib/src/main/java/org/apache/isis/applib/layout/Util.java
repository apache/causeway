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
package org.apache.isis.applib.layout;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

class Util {

    private Util(){}

    static <F,T> Predicate<F> is(final Class<T> cls) {
        return new Predicate<F>() {
            @Override public boolean apply(@Nullable final F from) {
                return cls.isAssignableFrom(from.getClass());
            }
        };
    }

    static <F, T extends F> CastFunction<F, T> cast(final Class<T> cls) {
        return new CastFunction<>();
    }

    private static class CastFunction<F, T extends F> implements Function<F, T> {
        @Override
        public final T apply(final F from) {
            return (T) from;
        }
    }

}
