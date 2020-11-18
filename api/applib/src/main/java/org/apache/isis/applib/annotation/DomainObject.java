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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.apache.isis.applib.events.domain.ActionDomainEvent;
import org.apache.isis.applib.events.domain.CollectionDomainEvent;
import org.apache.isis.applib.events.domain.PropertyDomainEvent;
import org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent;
import org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent;
import org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent;
import org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent;

/**
 * Domain semantics for domain objects (entities and view models; for services see {@link org.apache.isis.applib.annotation.DomainService}).
 * 
 * @apiNote Meta annotation {@link Component} allows for the Spring framework to pick up (discover) the 
 * annotated type. 
 * For more details see <code>org.apache.isis.core.config.beans.IsisBeanFactoryPostProcessorForSpring</code>
 */
// tag::refguide[]
@Inherited
@Target({
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Component @Scope("prototype")
// tag::refguide-lifecycle-events[]
// tag::refguide-domain-events[]
public @interface DomainObject {

    // end::refguide[]
    // ...

    // end::refguide-lifecycle-events[]
    // end::refguide-domain-events[]
    /**
     * Whether the entity should be audited (note: does not apply to view models or other recreatable objects.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.audit.EntityAuditListener} is
     * registered with the framework.
     * </p>
     */
    // tag::refguide[]
    Auditing auditing()                             // <.>
            default Auditing.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * The class of the domain service that provides an <code>autoComplete(String)</code> method.
     *
     * <p>
     * It is sufficient to specify an interface rather than a concrete type.
     */
    // tag::refguide[]
    Class<?> autoCompleteRepository()               // <.>
            default Object.class;

    // end::refguide[]
    /**
     * The method (despite its name, not necessarily an action) to use in order to perform the auto-complete search
     * (defaults to &quot;autoComplete&quot;).
     *
     * <p>
     * The method is required to accept a single string parameter, and must return a list of the domain type.
     */
    // tag::refguide[]
    String autoCompleteAction()                     // <.>
            default "autoComplete";

    // end::refguide[]
    /**
     * Indicates that the class has a bounded, or finite, set of instances.
     *
     * <p>
     *     Takes precedence over auto-complete.
     * </p>
     *
     * <p>
     *     Note: this replaces bounded=true|false prior to v2.x
     * </p>
     *
     */
    // tag::refguide[]
    Bounding bounding()                             // <.>
            default Bounding.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * Whether the properties of this domain object can be edited, or collections of this object be added to/removed from.
     *
     * <p>
     *     Note that non-editable objects can nevertheless have actions invoked upon them.
     * </p>
     */
    // tag::refguide[]
    Editing editing()                               // <.>
            default Editing.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * If {@link #editing()} is set to {@link Editing#DISABLED},
     * then the reason to provide to the user as to why the object's properties cannot be edited/collections modified.
     */
    // tag::refguide[]
    String editingDisabledReason()                  // <.>
            default "Disabled";

    // end::refguide[]
    /**
     * Equivalent to {@link Mixin#method()}.
     *
     * <p>
     *     Applicable only if {@link #nature()} is {@link Nature#MIXIN}.
     * </p>
     */
    // tag::refguide[]
    String mixinMethod()                            // <.>
            default Mixin.DEFAULT_METHOD_NAME;

    // end::refguide[]
    /**
     * The nature of this domain object.
     */
    // tag::refguide[]
    Nature nature()                                 // <.>
            default Nature.NOT_SPECIFIED;

    // end::refguide[]
    /**
     * Provides a unique abbreviation for the object type, eg &quot;customer.Customer&quot; for Customer.
     *
     * <p>
     * This value, if specified, is used in the serialized form of the object's OID.  An OID is
     * used by the framework to unique identify an object over time (same concept as a URN).
     * </p>
     */
    // tag::refguide[]
    String objectType()                             // <.>
            default "";

    // end::refguide[]
    /**
     * Whether changes to the object should be published.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.publish.ExecutionListener} is
     * registered with the framework.
     * </p>
     */
    // tag::refguide[]
    Publishing publishing()                         // <.>
            default Publishing.NOT_SPECIFIED;

    // end::refguide[]

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-lifecycle-events[]
    Class<? extends ObjectCreatedEvent<?>>
            createdLifecycleEvent()                         // <.>
            default ObjectCreatedEvent.Default.class;

    // end::refguide-lifecycle-events[]
    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-lifecycle-events[]
    Class<? extends ObjectPersistingEvent<?>>
            persistingLifecycleEvent()                      // <.>
            default ObjectPersistingEvent.Default.class;

    // end::refguide-lifecycle-events[]
    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-lifecycle-events[]
    Class<? extends ObjectPersistedEvent<?>>
            persistedLifecycleEvent()                       // <.>
            default ObjectPersistedEvent.Default.class;

    // end::refguide-lifecycle-events[]
    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-lifecycle-events[]
    Class<? extends ObjectLoadedEvent<?>>
            loadedLifecycleEvent()                          // <.>
            default ObjectLoadedEvent.Default.class;

    // end::refguide-lifecycle-events[]
    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-lifecycle-events[]
    Class<? extends ObjectUpdatingEvent<?>>
            updatingLifecycleEvent()                        // <.>
            default ObjectUpdatingEvent.Default.class;

    // end::refguide-lifecycle-events[]
    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-lifecycle-events[]
    Class<? extends ObjectUpdatedEvent<?>>
            updatedLifecycleEvent()                         // <.>
            default ObjectUpdatedEvent.Default.class;

    // end::refguide-lifecycle-events[]
    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    // tag::refguide-lifecycle-events[]
    Class<? extends ObjectRemovingEvent<?>>
            removingLifecycleEvent()                        // <.>
            default ObjectRemovingEvent.Default.class;

    // end::refguide-lifecycle-events[]
    /**
     * Indicates that an invocation of <i>any</i> action of the domain object (that do not themselves specify their own
     * <tt>&#64;Action(domainEvent=...)</tt> should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using the specified custom
     * (subclass of) {@link ActionDomainEvent}.
     *
     * <p>For example:
     * </p>
     *
     * <pre>
     * &#64;DomainObject(actionDomainEvent=SomeObject.GenericActionDomainEvent.class)
     * public class SomeObject{
     *     public static class GenericActionDomainEvent extends ActionDomainEvent&lt;Object&gt; { ... }
     *
     *     public void changeStartDate(final Date startDate) { ...}
     *     ...
     * }
     * </pre>
     *
     * <p>
     *     This will result in all actions as a more specific type to use) to emit this event.
     * </p>
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * It must also use <tt>Object</tt> as its generic type.  This is to allow mixins to also emit the same event.
     * </p>
     */
    // tag::refguide-domain-events[]
    Class<? extends ActionDomainEvent<?>>
            actionDomainEvent()                             // <.>
            default ActionDomainEvent.Default.class;

    // end::refguide-domain-events[]
    /**
     * Indicates that changes to <i>any</i> property of the domain object (that do not themselves specify their own
     * <tt>&#64;Property(domainEvent=...)</tt> should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using the specified custom
     * (subclass of) {@link PropertyDomainEvent}.
     *
     * <p>For example:
     * </p>
     *
     * <pre>
     * &#64;DomainObject(propertyDomainEvent=SomeObject.GenericPropertyDomainEvent.class)
     * public class SomeObject{
     *
     *    public LocalDate getStartDate() { ...}
     * }
     * </pre>
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * It must also use <tt>Object</tt> as its generic type.  This is to allow mixins to also emit the same event.
     * </p>
     */
    // tag::refguide-domain-events[]
    Class<? extends PropertyDomainEvent<?,?>>
            propertyDomainEvent()                           // <.>
            default PropertyDomainEvent.Default.class;

    // end::refguide-domain-events[]
    /**
     * Indicates that changes to <i>any</i> collection of the domain object (that do not themselves specify their own
     * <tt>&#64;Collection(domainEvent=...)</tt>  should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link CollectionDomainEvent}.
     *
     * <p>For example:
     * </p>
     * <pre>
     * &#64;DomainObject(collectionDomainEvent=Order.GenericCollectionDomainEvent.class)
     * public class Order {
     *
     *   public SortedSet&lt;OrderLine&gt; getLineItems() { ...}
     * }
     * </pre>
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * It must also use <tt>Object</tt> as its generic type.  This is to allow mixins to also emit the same event.
     * </p>
     */
    // tag::refguide-domain-events[]
    Class<? extends CollectionDomainEvent<?,?>>
            collectionDomainEvent()                         // <.>
            default CollectionDomainEvent.Default.class;

    // end::refguide-domain-events[]
    // tag::refguide[]
    // tag::refguide-lifecycle-events[]
    // ...
    // tag::refguide-domain-events[]
}
// end::refguide[]
// end::refguide-lifecycle-events[]
// end::refguide-domain-events[]
