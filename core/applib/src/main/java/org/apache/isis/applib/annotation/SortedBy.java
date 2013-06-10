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

package org.apache.isis.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Comparator;
import java.util.SortedSet;

/**
 * Indicates that the elements in a ({@link SortedSet}) collection should be sorted according to a different order than the
 * natural sort order.
 * 
 * <p>
 * Whenever there is a collection of type {@link SortedSet}, the domain entity referenced 
 * is expected to implement {@link Comparable}, ie to have a natural ordering.  In effect tis
 * means that all domain entities should provide a natural ordering.
 * 
 * <p>
 * However, in some circumstances the ordering of collection may be different to the entity's
 * natural ordering.  For example, the entity may represent an interval of time sorted by its
 * <i>startDate</i> ascending, but the collection may wish to sort by <i>startDate</i>.
 * 
 * <p>
 * The purpose of this annotation is to provide a {@link Comparator} such that the collection
 * may be sorted in an order more suitable to the context.
 * 
 * 
 * <p>
 * Supported by Wicket viewer; check documentation for other viewers.
 */
@Inherited
@Target( ElementType.METHOD )
@Retention(RetentionPolicy.RUNTIME)
public @interface SortedBy {

    /**
     * The comparator to use to sort this collection; must be a {@link Comparator} able to
     * compare the types.
     */
    @SuppressWarnings("rawtypes")
    Class value() default Comparator.class;
}
