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

import org.apache.isis.applib.events.domain.CollectionDomainEvent;

/**
 * Domain semantics for domain object collection.
 *
 * @see Action
 * @see Property
 * @see DomainObject
 * @see CollectionLayout
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
@Domain.Include // meta annotation, in support of meta-model validation
public @interface Collection {

    /**
     * Indicates that changes to the collection that should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.domain.CollectionDomainEvent}.
     *
     * <p>
     *     Subscribers of this event can interact with the business rule
     *     checking (hide, disable, validate) and its modification (before and
     *     after).
     * </p>
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
     *
     * @see Action#domainEvent()
     * @see Property#domainEvent()
     * @see DomainObject#collectionDomainEvent()
     */
    Class<? extends CollectionDomainEvent<?,?>> domainEvent()
            default CollectionDomainEvent.Default.class;

    /**
     * Indicates when the collection is not visible to the user.
     *
     * @see Action#hidden()
     * @see Property#hidden()
     * @see Collection#hidden()
     */
    Where hidden()
            default Where.NOT_SPECIFIED;

    /**
     * The type-of the elements held within the collection.
     *
     * <p>
     *     This is only provided as a fallback; usually the framework can infer
     *     the element type of the collection from the collection method's
     *     generic type.
     * </p>
     *
     * @see Action#typeOf()
     */
    Class<?> typeOf()
            default void.class; // represents unspecified

}
