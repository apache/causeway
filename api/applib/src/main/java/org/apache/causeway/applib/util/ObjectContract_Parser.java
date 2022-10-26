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
package org.apache.causeway.applib.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.lang.Nullable;

import org.apache.causeway.applib.util.ObjectContracts.ObjectContract;
import org.apache.causeway.commons.internal._Constants;
import org.apache.causeway.commons.internal.base._Strings;

/**
 * Package private parser for ObjectContract.<br/><br/>
 *
 * Not public API! Use ObjectContracts.parse(String) instead.
 *
 *
 * @since 2.0
 */
class ObjectContract_Parser<T> {

    /**
     * Parsing stringified property name lists like <pre>"invoice desc, productCode, quantity"</pre>.
     * @param cls
     * @param propertyNames
     */
    @SuppressWarnings("unchecked")
    public static <T> ObjectContract<T> parse(Class<T> cls, final @Nullable String propertyNames) {

        Objects.requireNonNull(cls);

        final List<Clause<T>> clauses = _Strings.splitThenStream(propertyNames, ",")
                .map(String::trim)
                .filter(p->!p.isEmpty())
                .map(p->Clause.parse(cls, p))
                .collect(Collectors.toList());

        ObjectContract<T> contract = ObjectContract.empty(cls);

        if(clauses.isEmpty()) {
            return contract;
        }

        for(Clause<T> clause : clauses) {
            @SuppressWarnings("rawtypes")
            final Function valueExtractor = x->clause.extractValue((T)x);

            contract = contract.thenUse(
                    clause.propertyName,
                    valueExtractor,
                    clause.direction.getOrdering() );
        }

        return contract;
    }

    private static class Clause<T> {

        private static Pattern pattern =
                Pattern.compile("\\W*(\\w+)\\W*(asc|asc nullsFirst|asc nullsLast|desc|desc nullsFirst|desc nullsLast)?\\W*");

        private enum Direction {
            ASC {
                @Override @SuppressWarnings({ "unchecked", "rawtypes" })
                public Comparator<Comparable<?>> getOrdering() {
                    // legacy of Ordering.natural().nullsFirst();
                    return Comparator.nullsFirst(Comparator.<Comparable>naturalOrder());
                }
            },
            ASC_NULLS_LAST {
                @Override @SuppressWarnings({ "unchecked", "rawtypes" })
                public Comparator<Comparable<?>> getOrdering() {
                    // legacy of Ordering.natural().nullsLast();
                    return Comparator.nullsLast(Comparator.<Comparable>naturalOrder());
                }
            },
            DESC {
                @Override
                public Comparator<Comparable<?>> getOrdering() {
                    // legacy of Ordering.natural().nullsLast().reverse();
                    return ASC_NULLS_LAST.getOrdering().reversed();
                }
            },
            DESC_NULLS_LAST {
                @Override
                public Comparator<Comparable<?>> getOrdering() {
                    // legacy of Ordering.natural().nullsFirst().reverse();
                    return ASC.getOrdering().reversed();
                }
            };

            public abstract Comparator<Comparable<?>> getOrdering();

            public static Direction valueOfElseAsc(String str) {
                if("asc".equals(str)) return ASC;
                if("asc nullsFirst".equals(str)) return ASC;
                if("asc nullsLast".equals(str)) return ASC_NULLS_LAST;
                if("desc".equals(str)) return DESC;
                if("desc nullsFirst".equals(str)) return DESC;
                if("desc nullsLast".equals(str)) return DESC_NULLS_LAST;
                return ASC;
            }
        }

        private final Class<T> objectClass;
        private final String propertyName;
        private final Direction direction;
        private final Method getterMethod;

        private static <X> Clause<X> parse(Class<X> cls, String input) {
            final Matcher matcher = pattern.matcher(input);
            if(!matcher.matches()) {
                return null;
            }
            return new Clause<>(cls, matcher.group(1), Direction.valueOfElseAsc(matcher.group(2)));
        }

        private Clause(Class<T> cls, String propertyName, Direction direction) {
            this.objectClass = cls;
            this.propertyName = propertyName;
            this.direction = direction;
            this.getterMethod = findGetter();
        }

        public Method findGetter() {
            final String methodNameSuffix = _Strings.capitalize(propertyName);
            final String getMethodName = "get" + methodNameSuffix;
            try {
                return objectClass.getMethod(getMethodName);
            } catch (NoSuchMethodException e) {
                final String isMethodName = "is" + methodNameSuffix;
                try {
                    return objectClass.getMethod(isMethodName);
                } catch (NoSuchMethodException ex) {
                    throw new IllegalArgumentException("No such method ' " +
                            getMethodName + "' or '" + isMethodName + "'", e);
                }
            }
        }

        public Object extractValue(T obj) {
            try {
                return getterMethod.invoke(obj, _Constants.emptyObjects);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

    }


}
