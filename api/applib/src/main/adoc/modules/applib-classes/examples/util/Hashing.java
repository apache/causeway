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

import org.apache.isis.commons.internal.collections._Lists;

/**
 * Fluent Object Hash Code Composition.
 *
 * @param <T>
 * @since 2.0
 *
 */
public class Hashing<T> {

    public static <T> Hashing<T> hashing(Function<? super T, ?> getter){
        return new Hashing<>(getter);
    }

    private final List<Function<? super T, ?>> getters = _Lists.newArrayList();

    private Hashing(Function<? super T, ?> getter) {
        getters.add(getter);
    }

    public Hashing<T> thenHashing(Function<? super T, ?> getter){
        Objects.requireNonNull(getter);
        getters.add(getter);
        return this;
    }

    public int hashCode(T object){
        if(object==null) {
            return 0;
        }
        int result = 1;
        for(Function<? super T, ?> getter : getters) {
            final Object element = getter.apply(object);
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }
        return result;
    }



}
