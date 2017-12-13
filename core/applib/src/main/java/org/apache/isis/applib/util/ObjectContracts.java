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

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;


public class ObjectContracts {

    //region > compare

    /**
     * Evaluates which of p and q is first.
     *
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     * @param propertyNames - the property name or names, CSV format.  If multiple properties, use the {@link #compare(Object, Object, String...) varargs} overloaded version of this method.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> int compare(final T p, final T q, final String propertyNames) {
        final Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
        return compare(p, q, propertyNamesIter);
    }

    /**
     * Evaluates which of p and q is first.
     *
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> int compare(final T p, final T q, final String... propertyNames) {
        final Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
        return compare(p, q, propertyNamesIter);
    }

    private static <T> int compare(final T p, final T q, final Iterable<String> propertyNamesIter) {
        final Iterable<Clause> clauses = clausesFor(propertyNamesIter);
        ComparisonChain chain = ComparisonChain.start();
        for (final Clause clause : clauses) {
            final Comparable<T> propertyValueOfP = (Comparable<T>) clause.getValueOf(p);
            final Comparable<T> propertyValueOfQ = (Comparable<T>) clause.getValueOf(q);
            chain = chain.compare(propertyValueOfP, propertyValueOfQ, clause.getDirection().getOrdering());
        }
        return chain.result();
    }
    //endregion

    //region > compareBy
    /**
     * Returns a {@link Comparator} to evaluate objects by their property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     * @param propertyNames - the property name or names, CSV format.  If multiple properties, use the {@link #compareBy(String...)} varargs} overloaded version of this method.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> compareBy(final String propertyNames){
        return new Comparator<T>() {
            @Override
            public int compare(T p, T q) {
                return ObjectContracts.compare(p, q, propertyNames);
            }
        };
    }
    /**
     * Returns a {@link Comparator} to evaluate objects by their property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> compareBy(final String... propertyNames){
        return new Comparator<T>() {
            @Override
            public int compare(T p, T q) {
                return ObjectContracts.compare(p, q, propertyNames);
            }
        };
    }
    //endregion

    //region > toString

    /**
     * Returns a string representation of the object consisting of the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     * @param propertyNames - the property name or names, CSV format.  If multiple properties, use the {@link #toString(Object, String...)} varargs} overloaded version of this method.
     */
    @Deprecated
    public static String toString(Object p, String propertyNames) {
        return new ObjectContracts().toStringOf(p, propertyNames);
    }
    /**
     * Returns a string representation of the object consisting of the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     */
    @Deprecated
    public static String toString(Object p, String... propertyNames) {
        return new ObjectContracts().toStringOf(p, propertyNames);
    }
    //endregion

    //region > hashCode
    /**
     * Returns the hashCode for the object using the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     * @param propertyNames - the property name or names, CSV format.  If multiple properties, use the {@link #hashCode(Object, String...)} varargs} overloaded version of this method.
     */
    @Deprecated
    public static int hashCode(Object obj, String propertyNames) {
        final Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
        return hashCode(obj, propertyNamesIter);
    }

    /**
     * Returns the hashCode for the object using the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     */
    @Deprecated
    public static int hashCode(Object obj, String... propertyNames) {
        final Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
        return hashCode(obj, propertyNamesIter);
    }

    private static int hashCode(final Object obj, final Iterable<String> propertyNamesIter) {
        final List<Object> propertyValues = Lists.newArrayList();
        for (final Clause clause : clausesFor(propertyNamesIter)) {
            final Object propertyValue = clause.getValueOf(obj);
            if(propertyValue != null) {
                propertyValues.add(propertyValue);
            }
        }
        return Objects.hashCode(propertyValues.toArray());
    }
    //endregion

    //region > equals

    /**
     * Returns whether two objects are equal, considering just the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     * @param propertyNames - the property name or names, CSV format.  If multiple properties, use the {@link #equals(Object, Object, String...)} varargs} overloaded version of this method.
     */
    @Deprecated
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
        final Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
        return equals(p, q, propertyNamesIter);
    }

    /**
     * Returns whether two objects are equal, considering just the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     */
    @Deprecated
    public static boolean equals(Object p, Object q, String... propertyNames) {
        if(p==null && q==null) {
            return true;
        }
        if(p==null || q==null) {
            return false;
        }
        if(p.getClass() != q.getClass()) {
            return false;
        }
        final Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
        return equals(p, q, propertyNamesIter);
    }

    private static boolean equals(final Object p, final Object q, final Iterable<String> propertyNamesIter) {
        final Iterable<Clause> clauses = clausesFor(propertyNamesIter);
        for (final Clause clause : clauses) {
            final Object pValue = clause.getValueOf(p);
            final Object qValue = clause.getValueOf(q);
            if(!Objects.equal(pValue, qValue)) {
                return false;
            }
        }
        return true;
    }
    //endregion

    //region > helpers
    private static Iterable<Clause> clausesFor(final Iterable<String> iterable) {
        return Iterables.transform(iterable, new Function<String, Clause>() {
            @Override
            public Clause apply(String input) {
                return Clause.parse(input);
            }
        });
    }

    private static Iterable<String> csvToIterable(final String propertyNames) {
        return Splitter.on(',').split(propertyNames);
    }

    private static List<String> varargsToIterable(final String[] iterable) {
        return Arrays.asList(iterable);
    }
    //endregion

    //region > toStringOf

    public interface ToStringEvaluator {
        boolean canEvaluate(Object o);
        String evaluate(Object o);
    }
    
    private final List<ToStringEvaluator> evaluators = Lists.newArrayList();

    public ObjectContracts with(ToStringEvaluator evaluator) {
        evaluators.add(evaluator);
        return this;
    }

    /**
     * Returns a string representation of two objects, considering just the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     * @param propertyNames - the property name or names, CSV format.  If multiple properties, use the {@link #toString(Object, String...)} varargs} overloaded version of this method.
     */
    @Deprecated
    public String toStringOf(Object p, String propertyNames) {
        final Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
        return toStringOf(p, propertyNamesIter);
    }

    /**
     * Returns a string representation of two objects, considering just the specified property name(s).
     * @deprecated - please be aware that this utility heavily uses reflection.  We don't actually intend to deprecate this method (it's useful while prototyping), but we wanted to bring this to your attention!
     */
    @Deprecated
    public String toStringOf(Object p, String... propertyNames) {
        final Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
        return toStringOf(p, propertyNamesIter);
    }

    private String toStringOf(final Object p, final Iterable<String> propertyNamesIter) {
        final ToStringHelper stringHelper = Objects.toStringHelper(p);
        for (final Clause clause : clausesFor(propertyNamesIter)) {
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

    //endregion


}
