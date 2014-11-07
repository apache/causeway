/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.applib.util;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;


public class ObjectContracts {

    @SuppressWarnings("unchecked")
    public static <T> int compare(T p, T q, String propertyNames) {
        ComparisonChain chain = ComparisonChain.start();
        for (final Clause clause : iterable(propertyNames)) {
            final Comparable<T> propertyValue = (Comparable<T>) clause.getValueOf(p);
            final Comparable<T> propertyValue2 = (Comparable<T>) clause.getValueOf(q);
            chain = chain.compare(propertyValue, propertyValue2, clause.getDirection().getOrdering());
        }
        return chain.result();
    }

    public static <T> Comparator<T> compareBy(final String propertyNames){
        return new Comparator<T>() {
            @Override
            public int compare(T p, T q) {
                return ObjectContracts.compare(p, q, propertyNames);
            }
        };
    }


    public static String toString(Object p, String propertyNames) {
        return new ObjectContracts().toStringOf(p, propertyNames);
    }
    
    public static int hashCode(Object obj, String propertyNames) {
        final List<Object> propertyValues = Lists.newArrayList();
        for (final Clause clause : iterable(propertyNames)) {
            final Object propertyValue = clause.getValueOf(obj);
            if(propertyValue != null) {
                propertyValues.add(propertyValue);
            }
        }
        return Objects.hashCode(propertyValues.toArray());
    }

    public static boolean equals(Object p, Object q, String propertyNames) {
        if(p==null && q==null) {
            return true;
        }
        if(p==null || q==null) {
            return false;
        }
        if(p.getClass() != q.getClass()) {
            return false;
        }
        for (final Clause clause : iterable(propertyNames)) {
            final Object pValue = clause.getValueOf(p);
            final Object qValue = clause.getValueOf(q);
            if(!Objects.equal(pValue,qValue)) {
                return false;
            }
        }
        return true;
    }
    // //////////////////////////////////////
    
    private static Iterable<Clause> iterable(String propertyNames) {
        return Iterables.transform(Splitter.on(',').split(propertyNames), new Function<String,Clause>() {
            @Override
            public Clause apply(String input) {
                return Clause.parse(input);
            }
        });
    }

    // //////////////////////////////////////

    public interface ToStringEvaluator {
        boolean canEvaluate(Object o);
        String evaluate(Object o);
    }
    
    // //////////////////////////////////////

    private final List<ToStringEvaluator> evaluators = Lists.newArrayList();

    public ObjectContracts with(ToStringEvaluator evaluator) {
        evaluators.add(evaluator);
        return this;
    }

    public String toStringOf(Object p, String propertyNames) {
        final ToStringHelper stringHelper = Objects.toStringHelper(p);
        for (final Clause clause : iterable(propertyNames)) {
            stringHelper.add(clause.getPropertyName(), asString(clause, p));
        }
        return stringHelper.toString();
    }

    private String asString(final Clause clause, Object p) {
        final Object value = clause.getValueOf(p);
        if(value == null) {
            return null;
        }
        for (ToStringEvaluator evaluator : evaluators) {
            if(evaluator.canEvaluate(value)) {
                return evaluator.evaluate(value);
            }
        }
        return value.toString();
    }



    
}
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
