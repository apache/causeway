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
import java.util.Iterator;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

/**
 * This is a copy of ObjectContracts in isis' applib, enhanced to deal with "cross-type comparisons", eg
 * polymorphic associations such as task#object).
 */
public class ObjectContracts2 {
    private final List<ObjectContracts.ToStringEvaluator> evaluators = Lists.newArrayList();

    public ObjectContracts2() {
    }

    /** @deprecated */
    @Deprecated
    public static <T> int compare(T p, T q, String propertyNames) {
        Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
        return compare(p, q, propertyNamesIter);
    }

    /** @deprecated */
    @Deprecated
    public static <T> int compare(T p, T q, String... propertyNames) {
        Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
        return compare(p, q, (Iterable)propertyNamesIter);
    }

    private static <T> int compare(T p, T q, Iterable<String> propertyNamesIter) {
        if(p == null) { return -1;}
        if(q == null) { return +1;}
        if(p.getClass() != q.getClass()) {
            // just sort on the class type
            return Ordering.natural().onResultOf(new Function<Object, String>() {
                @Override public String apply(final Object o) {
                    return o.getClass().getSimpleName();
                }
            }).compare(p, q);
        }

        Iterable<Clause> clauses = clausesFor(propertyNamesIter);
        ComparisonChain chain = ComparisonChain.start();

        Clause clause;
        Comparable propertyValueOfP;
        Comparable propertyValueOfQ;
        for(Iterator var5 = clauses.iterator(); var5.hasNext(); chain = chain.compare(propertyValueOfP, propertyValueOfQ, clause.getDirection().getOrdering())) {
            clause = (Clause)var5.next();
            propertyValueOfP = (Comparable)clause.getValueOf(p);
            propertyValueOfQ = (Comparable)clause.getValueOf(q);
        }

        return chain.result();
    }

    /** @deprecated */
    @Deprecated
    public static <T> Comparator<T> compareBy(final String propertyNames) {
        return new Comparator<T>() {
            public int compare(T p, T q) {
                return ObjectContracts.compare(p, q, propertyNames);
            }
        };
    }

    /** @deprecated */
    @Deprecated
    public static <T> Comparator<T> compareBy(final String... propertyNames) {
        return new Comparator<T>() {
            public int compare(T p, T q) {
                return ObjectContracts.compare(p, q, propertyNames);
            }
        };
    }

    /** @deprecated */
    @Deprecated
    public static String toString(Object p, String propertyNames) {
        return (new ObjectContracts()).toStringOf(p, propertyNames);
    }

    /** @deprecated */
    @Deprecated
    public static String toString(Object p, String... propertyNames) {
        return (new ObjectContracts()).toStringOf(p, propertyNames);
    }

    /** @deprecated */
    @Deprecated
    public static int hashCode(Object obj, String propertyNames) {
        Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
        return hashCode(obj, propertyNamesIter);
    }

    /** @deprecated */
    @Deprecated
    public static int hashCode(Object obj, String... propertyNames) {
        Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
        return hashCode(obj, (Iterable)propertyNamesIter);
    }

    private static int hashCode(Object obj, Iterable<String> propertyNamesIter) {
        List<Object> propertyValues = Lists.newArrayList();
        Iterator var3 = clausesFor(propertyNamesIter).iterator();

        while(var3.hasNext()) {
            Clause clause = (Clause)var3.next();
            Object propertyValue = clause.getValueOf(obj);
            if (propertyValue != null) {
                propertyValues.add(propertyValue);
            }
        }

        return Objects.hashCode(propertyValues.toArray());
    }

    /** @deprecated */
    @Deprecated
    public static boolean equals(Object p, Object q, String propertyNames) {
        if (p == null && q == null) {
            return true;
        } else if (p != null && q != null) {
            if (p.getClass() != q.getClass()) {
                return false;
            } else {
                Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
                return equals(p, q, propertyNamesIter);
            }
        } else {
            return false;
        }
    }

    /** @deprecated */
    @Deprecated
    public static boolean equals(Object p, Object q, String... propertyNames) {
        if (p == null && q == null) {
            return true;
        } else if (p != null && q != null) {
            if (p.getClass() != q.getClass()) {
                return false;
            } else {
                Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
                return equals(p, q, (Iterable)propertyNamesIter);
            }
        } else {
            return false;
        }
    }

    private static boolean equals(Object p, Object q, Iterable<String> propertyNamesIter) {
        Iterable<Clause> clauses = clausesFor(propertyNamesIter);
        Iterator var4 = clauses.iterator();

        Object pValue;
        Object qValue;
        do {
            if (!var4.hasNext()) {
                return true;
            }

            Clause clause = (Clause)var4.next();
            pValue = clause.getValueOf(p);
            qValue = clause.getValueOf(q);
        } while(Objects.equal(pValue, qValue));

        return false;
    }

    private static Iterable<Clause> clausesFor(Iterable<String> iterable) {
        return Iterables.transform(iterable, new Function<String, Clause>() {
            public Clause apply(String input) {
                return Clause.parse(input);
            }
        });
    }

    private static Iterable<String> csvToIterable(String propertyNames) {
        return Splitter.on(',').split(propertyNames);
    }

    private static List<String> varargsToIterable(String[] iterable) {
        return Arrays.asList(iterable);
    }

    public ObjectContracts2 with(ObjectContracts.ToStringEvaluator evaluator) {
        this.evaluators.add(evaluator);
        return this;
    }

    /** @deprecated */
    @Deprecated
    public String toStringOf(Object p, String propertyNames) {
        Iterable<String> propertyNamesIter = csvToIterable(propertyNames);
        return this.toStringOf(p, propertyNamesIter);
    }

    /** @deprecated */
    @Deprecated
    public String toStringOf(Object p, String... propertyNames) {
        Iterable<String> propertyNamesIter = varargsToIterable(propertyNames);
        return this.toStringOf(p, (Iterable)propertyNamesIter);
    }

    private String toStringOf(Object p, Iterable<String> propertyNamesIter) {
        Objects.ToStringHelper stringHelper = Objects.toStringHelper(p);
        Iterator var4 = clausesFor(propertyNamesIter).iterator();

        while(var4.hasNext()) {
            Clause clause = (Clause)var4.next();
            stringHelper.add(clause.getPropertyName(), this.asString(clause, p));
        }

        return stringHelper.toString();
    }

    private String asString(Clause clause, Object p) {
        Object value = clause.getValueOf(p);
        if (value == null) {
            return null;
        } else {
            Iterator var4 = this.evaluators.iterator();

            ObjectContracts.ToStringEvaluator evaluator;
            do {
                if (!var4.hasNext()) {
                    return value.toString();
                }

                evaluator = (ObjectContracts.ToStringEvaluator)var4.next();
            } while(!evaluator.canEvaluate(value));

            return evaluator.evaluate(value);
        }
    }

    public interface ToStringEvaluator {
        boolean canEvaluate(Object var1);

        String evaluate(Object var1);
    }
}

