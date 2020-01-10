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
package org.apache.isis.applib.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.apache.isis.core.commons.internal.base._Casts;
import org.apache.isis.core.commons.internal.collections._Lists;

/**
 * Fluent Object Equality Composition.
 *
 * @param <T>
 * @since 2.0
 */
public class Equality<T> {

    public static <T> Equality<T> checkEquals(Function<? super T, ?> getter) {
        Objects.requireNonNull(getter);
        return new Equality<>(getter);
    }

    private final List<Function<? super T, ?>> getters = _Lists.newArrayList();

    private Equality(Function<? super T, ?> getter) {
        getters.add(getter);
    }

    public Equality<T> thenCheckEquals(Function<? super T, ?> getter){
        Objects.requireNonNull(getter);
        getters.add(getter);
        return this;
    }

    public boolean equals(T target, Object other){
        if(target==null && other==null) {
            return true;
        }
        if(target==null || other==null) {
            return false;
        }
        if(target.getClass() != other.getClass()) {
            return false;
        }
        final T o = _Casts.uncheckedCast(other);

        for(Function<? super T, ?> getter : getters) {
            if(!Objects.equals(getter.apply(target), getter.apply(o)))
                return false;
        }

        return true;
    }



}
