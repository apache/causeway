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

/**
 * Layout hints for collections.
 *
 * @see ActionLayout
 * @see PropertyLayout
 * @see DomainObjectLayout
 * @see Collection
 *
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@DomainObject(nature=Nature.MIXIN, mixinMethod = "coll") // meta annotation, only applies at class level
public @interface CollectionLayout {

    /**
     * Indicates the css class that a collection should have, to
     * allow more targeted styling in <code>application.css</code>.
     *
     * @see ActionLayout#cssClass()
     * @see PropertyLayout#cssClass()
     * @see ParameterLayout#cssClass()
     * @see DomainObjectLayout#cssClass()
     */
    String cssClass()
            default "";

    /**
     * Indicates which view is used by default to render the collection.
     *
     * <p>
     *     This is a simple string; every available implementation is required
     *     to use a different string.
     * </p>
     * <p>
     *     The default is &quot;table&quot;.  Extensions provide also:
     *     &quot;excel&quot; (download as Excel spreadsheet), &quot;calendar&quot;
     *     and &quot;map&quot;.
     * </p>
     */
    String defaultView()
            default "";

    /**
     * Description of this collection, eg to be rendered in a tooltip.
     *
     * @see ActionLayout#describedAs()
     * @see ParameterLayout#describedAs()
     * @see PropertyLayout#describedAs()
     * @see DomainObjectLayout#describedAs()
     */
    String describedAs()
            default "";

    /**
     * Indicates where in the UI the collection should <i>not</i>not be visible.
     *
     * <p>
     *      Only {@link Where#NOWHERE NOWHERE} or {@link Where#EVERYWHERE EVERYWHERE}/{@link Where#ANYWHERE ANYWHERE}
     *      apply for collections.
     * </p>
     */
    Where hidden()
            default Where.NOT_SPECIFIED;

    /**
     * Name of this collection (overriding the name derived from its name in
     * code).
     *
     * <p>
     *     A typical use case is if the desired name is a reserved Java
     *     keyword, such as `default` or `package`.
     * </p>
     *
     * @see ActionLayout#named()
     * @see ParameterLayout#named()
     * @see PropertyLayout#named()
     * @see DomainObjectLayout#named()
     * @see DomainServiceLayout#named()
     * @see CollectionLayout#namedEscaped()
     */
    String named()
            default "";

    /**
     * A flag indicating whether the value of {@linkplain #named()} should be HTML escaped or not.
     *
     * @see CollectionLayout#named()
     */
    boolean namedEscaped()
            default true;

    /**
     * The page size for instances of this class when rendered within
     * a table.
     *
     * <p>
     * If annotated on a collection, then the page size refers to
     * parented collections (eg <tt>Order#lineItems</tt>).
     *
     * <p>
     * If annotated on a type, then the page size refers to standalone
     * collections (eg as returned from a repository query).
     *
     * @see DomainObjectLayout#paged()
     */
    int paged()
            default -1;

    /**
     * Indicates that the elements in a ({@link java.util.SortedSet}) collection should be sorted according to a different order than the
     * natural sort order, as defined by the specified{@link java.util.Comparator}.
     *
     * <p>
     * Whenever there is a collection of type {@link java.util.SortedSet}, the domain entity referenced
     * is expected to implement {@link Comparable}, ie to have a natural ordering.  In effect tis
     * means that all domain entities should provide a natural ordering.
     *
     * <p>
     * However, in some circumstances the ordering of collection may be different to the entity's
     * natural ordering.  For example, the entity may represent an interval of time sorted by its
     * <i>startDate</i> ascending, but the collection may wish to sort by <i>startDate</i>.
     *
     * <p>
     * The purpose of this annotation is to provide a {@link java.util.Comparator} such that the collection
     * may be sorted in an order more suitable to the context.
     */
    @SuppressWarnings("rawtypes")
    Class sortedBy()
            default Comparator.class;

}
