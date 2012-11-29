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
package org.apache.isis.core.commons.lang;

import java.util.Collection;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public final class Types {
    
    private Types(){}

    public static <T> Collection<T> filtered(final List<Object> candidates, final Class<T> type) {
        return Collections2.transform(
                    Collections2.filter(candidates, Types.isOfType(type)),
                Types.castTo(type));
    }

    public static final <T> Predicate<Object> isOfType(final Class<T> type) {
        return new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return type.isAssignableFrom(input.getClass());
            }
        };
    }

    public static <T> Function<Object, T> castTo(final Class<T> type) {
        return new Function<Object, T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T apply(final Object input) {
                return (T) input;
            }
        };
    }


}
