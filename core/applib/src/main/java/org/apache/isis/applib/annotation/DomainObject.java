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

import org.apache.isis.applib.services.eventbus.ActionDomainEvent;
import org.apache.isis.applib.services.eventbus.CollectionDomainEvent;
import org.apache.isis.applib.services.eventbus.ObjectCreatedEvent;
import org.apache.isis.applib.services.eventbus.ObjectLoadedEvent;
import org.apache.isis.applib.services.eventbus.ObjectPersistedEvent;
import org.apache.isis.applib.services.eventbus.ObjectPersistingEvent;
import org.apache.isis.applib.services.eventbus.ObjectRemovingEvent;
import org.apache.isis.applib.services.eventbus.ObjectUpdatedEvent;
import org.apache.isis.applib.services.eventbus.ObjectUpdatingEvent;
import org.apache.isis.applib.services.eventbus.PropertyDomainEvent;
import org.apache.isis.applib.services.publish.PublisherService;

/**
 * Domain semantics for domain objects (entities and view models; for services see {@link org.apache.isis.applib.annotation.DomainService}).
 */
@Inherited
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainObject {

    /**
     * Whether the entity should be audited (note: does not apply to view models or other recreatable objects.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.audit.AuditingService3} is
     * registered with the framework.
     * </p>
     */
    Auditing auditing() default Auditing.AS_CONFIGURED;


    // //////////////////////////////////////


    /**
     * Whether changes to the object should be published.
     *
     * <p>
     * Requires that an implementation of the {@link org.apache.isis.applib.services.publish.PublishingService} is
     * registered with the framework.
     * </p>
     */
    Publishing publishing() default Publishing.AS_CONFIGURED;

    /**
     * The factory to construct the payload factory.
     *
     * <p>
     *     If not specified then a default implementation will be used.
     * </p>
     *
     * @deprecated - not supported by {@link PublisherService}.
     */
    @Deprecated
    Class<? extends PublishingPayloadFactoryForObject> publishingPayloadFactory() default PublishingPayloadFactoryForObject.class;


    // //////////////////////////////////////


    /**
     * The class of the domain service that provides an <code>autoComplete(String)</code> method.
     *
     * <p>
     * It is sufficient to specify an interface rather than a concrete type.
     */
    Class<?> autoCompleteRepository() default Object.class;


    /**
     * The method (despite its name, not necessarily an action) to use in order to perform the auto-complete search
     * (defaults to &quot;autoComplete&quot;).
     *
     * <p>
     * The method is required to accept a single string parameter, and must return a list of the domain type.
     */
    String autoCompleteAction() default "autoComplete";


    // //////////////////////////////////////


    /**
     * Indicates that the class has a bounded, or finite, set of instances.
     * 
     * <p>
     *     Takes precedence over auto-complete.
     * </p>
     */
    boolean bounded() default false;


    // //////////////////////////////////////


    /**
     * Whether the properties of this domain object can be edited, or collections of this object be added to/removed from.
     *
     * <p>
     *     Note that non-editable objects can nevertheless have actions invoked upon them.
     * </p>
     */
    Editing editing() default Editing.AS_CONFIGURED;


    /**
     * If {@link #editing()} is set to {@link Editing#DISABLED},
     * then the reason to provide to the user as to why the object's properties cannot be edited/collections modified.
     */
    String editingDisabledReason() default "Disabled";


    // //////////////////////////////////////


    /**
     * Provides a unique abbreviation for the object type, eg &quot;customer.Customer&quot; for Customer.
     *
     * <p>
     * This value, if specified, is used in the serialized form of the object's OID.  An OID is
     * used by the framework to unique identify an object over time (same concept as a URN).
     * </p>
     */
    @Deprecated
    String objectType() default "";


    // //////////////////////////////////////


    /**
     * The nature of this domain object.
     */
    Nature nature() default Nature.NOT_SPECIFIED;



    /**
     * Equivalent to {@link Mixin#method()}.
     *
     * <p>
     *     Applicable only if {@link #nature()} is {@link Nature#MIXIN}.
     * </p>
     */
    String mixinMethod() default Mixin.DEFAULT_METHOD_NAME;

    // //////////////////////////////////////


    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ObjectCreatedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ObjectCreatedEvent<?>> createdLifecycleEvent() default ObjectCreatedEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ObjectPersistingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ObjectPersistingEvent<?>> persistingLifecycleEvent() default ObjectPersistingEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ObjectPersistedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ObjectPersistedEvent<?>> persistedLifecycleEvent() default ObjectPersistedEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ObjectLoadedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ObjectLoadedEvent<?>> loadedLifecycleEvent() default ObjectLoadedEvent.Default.class;

    /**
     * The logical name of this object's type, that uniquely and fully qualifies it.
     * The logical name is analogous to - but independent of - the actual fully qualified class name.
     * eg. {@code sales.Customer} for a class 'org.mycompany.dom.Customer'
     * <p>
     * This value, if specified, is used in the serialized form of the object's {@link Bookmark}.
     * A {@link Bookmark} is used by the framework to uniquely identify an object over time
     * (same concept as a URN).
     * Otherwise, if not specified, the fully qualified class name is used instead.
     * </p>
     *
     * @see DomainService#logicalTypeName()
     */
    String logicalTypeName()
            default "";

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ObjectUpdatingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ObjectUpdatingEvent<?>> updatingLifecycleEvent() default ObjectUpdatingEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ObjectUpdatedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ObjectUpdatedEvent<?>> updatedLifecycleEvent() default ObjectUpdatedEvent.Default.class;


    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.ObjectRemovingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     */
    Class<? extends ObjectRemovingEvent<?>> removingLifecycleEvent() default ObjectRemovingEvent.Default.class;


    /**
     * Indicates that an invocation of <i>any</i> action of the domain object (that do not themselves specify their own
     * <tt>&#64;Action(domainEvent=...)</tt> should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using the specified custom
     * (subclass of) {@link org.apache.isis.applib.services.eventbus.ActionDomainEvent}.
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
    Class<? extends ActionDomainEvent<?>> actionDomainEvent() default ActionDomainEvent.Default.class;

    /**
     * Indicates that changes to <i>any</i> property of the domain object (that do not themselves specify their own
     * <tt>&#64;Property(domainEvent=...)</tt> should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using the specified custom
     * (subclass of) {@link org.apache.isis.applib.services.eventbus.PropertyDomainEvent}.
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
    Class<? extends PropertyDomainEvent<?,?>> propertyDomainEvent() default PropertyDomainEvent.Default.class;

    /**
     * Indicates that changes to <i>any</i> collection of the domain object (that do not themselves specify their own
     * <tt>&#64;Collection(domainEvent=...)</tt>  should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.services.eventbus.CollectionDomainEvent}.
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
    Class<? extends CollectionDomainEvent<?,?>> collectionDomainEvent() default CollectionDomainEvent.Default.class;

}