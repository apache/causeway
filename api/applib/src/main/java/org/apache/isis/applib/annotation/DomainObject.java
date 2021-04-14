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
 * Domain semantics for domain objects (entities and view models;
 * for services see {@link org.apache.isis.applib.annotation.DomainService}).
 *
 * @apiNote Meta annotation {@link Component} allows for the Spring framework to pick up (discover) the
 * annotated type.
 * For more details see <code>org.apache.isis.core.config.beans.IsisBeanFactoryPostProcessorForSpring</code>
 *
 * @see Action
 * @see Property
 * @see Collection
 * @see DomainObjectLayout
 * @see DomainService
 *
 * @since 1.x {@index}
 */
@Inherited
@Target({
        ElementType.TYPE,
        ElementType.ANNOTATION_TYPE
})
@Retention(RetentionPolicy.RUNTIME)
@Component @Scope("prototype")
public @interface DomainObject {

    // ...

    /**
     * The class of the domain service that provides an <code>autoComplete(String)</code> method.
     *
     * <p>
     * It is sufficient to specify an interface rather than a concrete type.
     *
     * @see DomainObject#autoCompleteAction()
     */
    Class<?> autoCompleteRepository()
            default Object.class;

    /**
     * The method (despite its name, not necessarily an action) to use in order to perform the auto-complete search
     * (defaults to &quot;autoComplete&quot;).
     *
     * <p>
     * The method is required to accept a single string parameter, and must return a list of the domain type.
     * </p>
     *
     * @see DomainObject#autoCompleteRepository()
     */
    String autoCompleteAction()
            default "autoComplete";

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
    Bounding bounding()
            default Bounding.NOT_SPECIFIED;

    /**
     * Whether the properties of this domain object can be edited, or collections of this object be added to/removed from.
     *
     * <p>
     *     Note that non-editable objects can nevertheless have actions invoked upon them.
     * </p>
     *
     * @see Property#editing()
     * @see DomainObject#editingDisabledReason()
     */
    Editing editing()
            default Editing.NOT_SPECIFIED;

    /**
     * If {@link #editing()} is set to {@link Editing#DISABLED},
     * then the reason to provide to the user as to why the object's properties cannot be edited/collections modified.
     *
     * @see DomainObject#editing()
     */
    String editingDisabledReason()
            default "Disabled";

    /**
     * Whether entity changes should be published to
     * {@link org.apache.isis.applib.services.publishing.spi.EntityPropertyChangeSubscriber}s
     * and whether entity changes, captured as {@link org.apache.isis.applib.services.publishing.spi.EntityChanges},
     * should be dispatched to {@link org.apache.isis.applib.services.publishing.spi.EntityChangesSubscriber}s.
     * @apiNote does only apply to entity objects
     */
    Publishing entityChangePublishing()
            default Publishing.NOT_SPECIFIED;

    /**
     * Applicable only if {@link #nature()} is {@link Nature#MIXIN}, indicates
     * the name of the method within the mixin class to be inferred as the
     * action of that mixin.
     *
     * <p>
     *     Supporting methods are then derived from that method name.  For
     *     example, if the mixin method name is &quot;act&quot;, then the
     *     <i>disable</i> supporting method will be &quot;disableAct&quot;.
     * </p>
     *
     * <p>
     * Typical examples are "act", "prop", "coll", "exec", "execute", "invoke",
     * "apply" and so on. The default name is `$$`.
     * </p>
     */
    String mixinMethod()
            default "$$";

    /**
     * The nature of this domain object.
     *
     * @see DomainService#nature()
     */
    Nature nature()
            default Nature.NOT_SPECIFIED;

    /**
     * Provides a unique abbreviation for the object type's, eg
     * &quot;customer.Customer&quot; for Customer.
     *
     * <p>
     * This value, if specified, is used in the serialized form of the object's OID.  An OID is
     * used by the framework to unique identify an object over time (same concept as a URN).
     * </p>
     *
     * @see DomainService#objectType()
     */
    String objectType()
            default "";


    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectCreatedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see DomainObject#loadedLifecycleEvent()
     * @see DomainObject#persistingLifecycleEvent()
     * @see DomainObject#persistedLifecycleEvent()
     * @see DomainObject#updatingLifecycleEvent()
     * @see DomainObject#updatedLifecycleEvent()
     * @see DomainObject#removingLifecycleEvent()
     */
    Class<? extends ObjectCreatedEvent<?>>
            createdLifecycleEvent()
            default ObjectCreatedEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectPersistingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see DomainObject#createdLifecycleEvent()
     * @see DomainObject#loadedLifecycleEvent()
     * @see DomainObject#persistedLifecycleEvent()
     * @see DomainObject#updatingLifecycleEvent()
     * @see DomainObject#updatedLifecycleEvent()
     * @see DomainObject#removingLifecycleEvent()
     */
    Class<? extends ObjectPersistingEvent<?>>
            persistingLifecycleEvent()
            default ObjectPersistingEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectPersistedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see DomainObject#createdLifecycleEvent()
     * @see DomainObject#loadedLifecycleEvent()
     * @see DomainObject#persistingLifecycleEvent()
     * @see DomainObject#updatingLifecycleEvent()
     * @see DomainObject#updatedLifecycleEvent()
     * @see DomainObject#removingLifecycleEvent()
     */
    Class<? extends ObjectPersistedEvent<?>>
            persistedLifecycleEvent()
            default ObjectPersistedEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectLoadedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see DomainObject#createdLifecycleEvent()
     * @see DomainObject#persistingLifecycleEvent()
     * @see DomainObject#persistedLifecycleEvent()
     * @see DomainObject#updatingLifecycleEvent()
     * @see DomainObject#updatedLifecycleEvent()
     * @see DomainObject#removingLifecycleEvent()
     */
    Class<? extends ObjectLoadedEvent<?>>
            loadedLifecycleEvent()
            default ObjectLoadedEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see DomainObject#createdLifecycleEvent()
     * @see DomainObject#loadedLifecycleEvent()
     * @see DomainObject#persistingLifecycleEvent()
     * @see DomainObject#persistedLifecycleEvent()
     * @see DomainObject#updatedLifecycleEvent()
     * @see DomainObject#removingLifecycleEvent()
     */
    Class<? extends ObjectUpdatingEvent<?>>
            updatingLifecycleEvent()
            default ObjectUpdatingEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectUpdatedEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see DomainObject#createdLifecycleEvent()
     * @see DomainObject#loadedLifecycleEvent()
     * @see DomainObject#persistingLifecycleEvent()
     * @see DomainObject#persistedLifecycleEvent()
     * @see DomainObject#updatingLifecycleEvent()
     * @see DomainObject#removingLifecycleEvent()
     */
    Class<? extends ObjectUpdatedEvent<?>>
            updatedLifecycleEvent()
            default ObjectUpdatedEvent.Default.class;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.isis.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.isis.applib.events.lifecycle.ObjectRemovingEvent}.
     *
     * <p>
     * This subclass must provide a no-arg constructor; the fields are set reflectively.
     * </p>
     *
     * @see DomainObject#createdLifecycleEvent()
     * @see DomainObject#loadedLifecycleEvent()
     * @see DomainObject#persistingLifecycleEvent()
     * @see DomainObject#persistedLifecycleEvent()
     * @see DomainObject#updatingLifecycleEvent()
     * @see DomainObject#updatedLifecycleEvent()
     */
    Class<? extends ObjectRemovingEvent<?>>
            removingLifecycleEvent()
            default ObjectRemovingEvent.Default.class;

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
     *
     * @see DomainObject#propertyDomainEvent()
     * @see DomainObject#collectionDomainEvent()
     * @see Action#domainEvent()
     */
    Class<? extends ActionDomainEvent<?>>
            actionDomainEvent()
            default ActionDomainEvent.Default.class;

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
     *
     * @see DomainObject#actionDomainEvent()
     * @see DomainObject#collectionDomainEvent()
     * @see Property#domainEvent()
     */
    Class<? extends PropertyDomainEvent<?,?>>
            propertyDomainEvent()
            default PropertyDomainEvent.Default.class;

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
     *
     * @see DomainObject#actionDomainEvent()
     * @see DomainObject#propertyDomainEvent()
     * @see Collection#domainEvent()
     */
    Class<? extends CollectionDomainEvent<?,?>>
            collectionDomainEvent()
            default CollectionDomainEvent.Default.class;

}
