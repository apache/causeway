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

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.Ordering;

class Clause {
    private static Pattern pattern = Pattern.compile("\\W*(\\w+)\\W*(asc|asc nullsFirst|asc nullsLast|desc|desc nullsFirst|desc nullsLast)?\\W*");
    enum Direction {
        ASC {
            @Override
            public Comparator<Comparable<?>> getOrdering() {
                return Ordering.natural().nullsFirst();
            }
        },
        ASC_NULLS_LAST {
            @Override
            public Comparator<Comparable<?>> getOrdering() {
                return Ordering.natural().nullsLast();
            }
        },
        DESC {
            @Override
            public Comparator<Comparable<?>> getOrdering() {
                return Ordering.natural().nullsLast().reverse();
            }
        },
        DESC_NULLS_LAST {
            @Override
            public Comparator<Comparable<?>> getOrdering() {
                return Ordering.natural().nullsFirst().reverse();
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
    private String propertyName;
    private Direction direction;
    static Clause parse(String input) {
        final Matcher matcher = pattern.matcher(input);
        if(!matcher.matches()) {
            return null;
        }
        return new Clause(matcher.group(1), Direction.valueOfElseAsc(matcher.group(2)));
    }
    Clause(String propertyName, Direction direction) {
        this.propertyName = propertyName;
        this.direction = direction;
    }
    String getPropertyName() {
        return propertyName;
    }
    Direction getDirection() {
        return direction;
    }
    public Object getValueOf(Object obj) {
        if(obj == null) {
            return null;
        }
        final String methodNameSuffix = upperFirst(propertyName);
        final String getMethodName = "get" + methodNameSuffix;
        try {
            final Method getterMethod = obj.getClass().getMethod(getMethodName);
            return getterMethod.invoke(obj);
        } catch (NoSuchMethodException e) {
            final String isMethodName = "is" + methodNameSuffix;
            try {
                final Method getterMethod = obj.getClass().getMethod(isMethodName);
                return getterMethod.invoke(obj);
            } catch (NoSuchMethodException ex) {
                throw new IllegalArgumentException("No such method ' " + getMethodName + "' or '" + isMethodName + "'", e);
            } catch (Exception e1) {
                // some other reason; for example, a JDOUserException if the object has been deleted and interaction with its properties is not permitted.
                throw new RuntimeException(e1);
            }
        } catch (Exception e) {
            // some other reason; for example, a JDOUserException if the object has been deleted and interaction with its properties is not permitted.
            throw new RuntimeException(e);
        }
    }
    private static String upperFirst(final String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
