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

import javax.jdo.annotations.NotPersistent;

import org.apache.isis.applib.events.domain.CollectionDomainEvent;

/**
 * Domain semantics for domain object collection.
 */
@Inherited
@Target({ ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Collection {

    /**
     * Indicates that changes to the collection that should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.domain.CollectionDomainEvent}.
     *
     * <p>For example:
     * </p>
     * <pre>
     * public class Order {
     *   public static class OrderLineItems extends CollectionDomainEvent { ... }
     *
     *   &#64;CollectionInteraction(OrderLineItems.class)
     *   public SortedSet&lt;OrderLine&gt; getLineItems() { ...}
     * }
     * </pre>
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends CollectionDomainEvent<?,?>> domainEvent() default CollectionDomainEvent.Default.class;

    // //////////////////////////////////////

    /**
     * Indicates when the collection is not visible to the user.
     */
    Where hidden() default Where.NOT_SPECIFIED;


    // //////////////////////////////////////

    /**
     * Whether the properties of this domain object can be edited, or collections of this object be added to/removed from.
     *
     * <p>
     *     Note that non-editable objects can nevertheless have actions invoked upon them.
     * </p>
     */
    Editing editing() default Editing.NOT_SPECIFIED;

    /**
     * If {@link #editing()} is set to {@link Editing#DISABLED},
     * then the reason to provide to the user as to why this property cannot be edited.
     */
    String editingDisabledReason() default "";


    // //////////////////////////////////////


    /**
     * Indicates whether the collecion should be included or excluded from mementos.
     *
     * <p>
     *     To ensure that the collection is actually not persisted in the objectstore, also annotate with the JDO annotation {@link NotPersistent}.
     * </p>
     */
    MementoSerialization mementoSerialization() default MementoSerialization.NOT_SPECIFIED;



    // //////////////////////////////////////

    /**
     * The type-of the elements held within the collection.
     * @return
     */
    Class<?> typeOf() default Object.class;

}