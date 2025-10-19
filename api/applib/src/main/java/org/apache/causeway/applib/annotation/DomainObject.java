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
package org.apache.causeway.applib.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.apache.causeway.applib.events.domain.ActionDomainEvent;
import org.apache.causeway.applib.events.domain.CollectionDomainEvent;
import org.apache.causeway.applib.events.domain.PropertyDomainEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectCreatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectLoadedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectPersistingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectRemovingEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatedEvent;
import org.apache.causeway.applib.events.lifecycle.ObjectUpdatingEvent;
import org.apache.causeway.applib.services.bookmark.Bookmark;

/**
 * Domain semantics for domain objects (entities and view models;
 * for services see {@link org.apache.causeway.applib.annotation.DomainService}).
 * <p>
 *     If - for the currently logged on user - none of the domain object's members are effectively visible,
 *     (or if there are no members to begin with), the object instance is considered hidden. Hence
 *     a NOT-AUTHORIZED page will be displayed instead.
 * </p>
 *
 * @apiNote Meta annotation {@link Component} allows for the Spring framework to pick up (discover) the
 * annotated type.  However, the custom {@link Scope} of &quot;causeway-domain-object&quot; is a clue that these
 * objects (either entities or view models) <i>cannot</i> be obtained from the Spring application context.
 * For more details see <code>org.apache.causeway.core.config.beans.CausewayBeanFactoryPostProcessorForSpring</code>
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
@Component @Scope("causeway-domain-object")
public @interface DomainObject {

    /**
     * Alternative logical type name(s) for the annotated type.
     * @see Bookmark
     * @see jakarta.inject.Named
     */
    String[] aliased() default {};

    /**
     * The class of the domain service that provides an <code>autoComplete(String)</code> method.
     *
     * <p>
     * It is sufficient to specify an interface rather than a concrete type.
     *
     * @see DomainObject#autoCompleteMethod()
     */
    Class<?> autoCompleteRepository()
            default Object.class;

    /**
     * The method to use in order to perform the auto-complete search
     * (defaults to &quot;autoComplete&quot;).
     *
     * <p>
     * The method is required to accept a single string parameter, and must
     * return a list of the domain type.
     * </p>
     *
     * @see DomainObject#autoCompleteRepository()
     */
    String autoCompleteMethod()
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
     * <p>
     * If left empty (default), no reason is given.
     *
     * @see DomainObject#editing()
     */
    String editingDisabledReason()
            default "";

    /**
     * Whether entity changes (persistent property updates) should be published to
     * {@link org.apache.causeway.applib.services.publishing.spi.EntityPropertyChangeSubscriber}s
     * and whether entity changes, captured as {@link org.apache.causeway.applib.services.publishing.spi.EntityChanges},
     * should be published to {@link org.apache.causeway.applib.services.publishing.spi.EntityChangesSubscriber}s.
     * @apiNote does only apply to entity objects
     */
    Publishing entityChangePublishing()
            default Publishing.NOT_SPECIFIED;

    /**
     * Controls on a per class basis,
     * how meta-model class introspection should process
     * members, supporting methods and callback methods.
     */
    Introspection introspection()
            default Introspection.NOT_SPECIFIED;

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
     *
     * <p>
     *     <b>NOTE</b>: it's more typical to instead use {@link Action}, {@link Property} or {@link Collection} as the
     *     class-level annotation, indicating that the domain object is a mixin.  The mixin method name for these is,
     *     respectively, "act", "prop" and "coll".
     * </p>
     */
    String mixinMethod()
            default "$$";

    /**
     * The nature of this domain object.
     *
     * <p>
     *     Most common are natures of {@link Nature#ENTITY} and {@link Nature#VIEW_MODEL}.  For mixins, rather than
     *     use a nature of {@link Nature#MIXIN}, it's more typical to instead use {@link Action}, {@link Property} or
     *     {@link Collection} as the class-level annotation, indicating that the domain object is a mixin.
     *     The {@link #mixinMethod() mixin method name}  for these is, respectively, "act", "prop" and "coll".
     * </p>
     *
     * <p>
     *     The {@link Nature#BEAN} nature is for internally use, and should not normally be specified explicitly.
     * </p>
     */
    Nature nature()
            default Nature.NOT_SPECIFIED;

    /**
     * Indicates that the loading of the domain object should be posted to the
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.causeway.applib.events.lifecycle.ObjectCreatedEvent}.
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.causeway.applib.events.lifecycle.ObjectPersistingEvent}.
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.causeway.applib.events.lifecycle.ObjectPersistedEvent}.
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.causeway.applib.events.lifecycle.ObjectLoadedEvent}.
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.causeway.applib.events.lifecycle.ObjectUpdatingEvent}.
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.causeway.applib.events.lifecycle.ObjectUpdatedEvent}.
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
     * {@link org.apache.causeway.applib.events.lifecycle.ObjectRemovingEvent}.
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using the specified custom
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using the specified custom
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
     * {@link org.apache.causeway.applib.services.eventbus.EventBusService event bus} using a custom (subclass of)
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

    /**
     * If at least one property of the entity has been annotated with {@link Property#queryDslAutoComplete()}, then
     * that property (and any others) will automatically be used for autocomplete functionality; this attribute
     * determines the minimum number of characters that must be entered before the query is submitted.
     * Fine-tunes how auto-complete queries work, Whether to use the value of this (string) property for auto.
     *
     * <p>
     *     NOTE: this feature requires that the <code>querydsl-jpa</code> module is included as part of the
     *     application manifest.  Otherwise, no autocomplete will be generated.
     * </p>
     *
     * <p>
     *     NOTE: if {@link DomainObject#autoCompleteRepository()} (and {@link DomainObject#autoCompleteMethod()}) have
     *     been specified, then these take precedence of the query DSL auto-complete.
     * </p>
     *
     * @see #queryDslAutoCompleteLimitResults()
     * @see #queryDslAutoCompleteAdditionalPredicateRepository()
     * @see #queryDslAutoCompleteAdditionalPredicateMethod()
     */
    int queryDslAutoCompleteMinLength() default QueryDslAutoCompleteConstants.MIN_LENGTH;

    /**
     * If at least one property of the entity has been annotated with {@link Property#queryDslAutoComplete()}, then
     * that property (and any others) will automatically be used for autocomplete functionality; this attribute
     * can be used to limit the number of rows that are returned.
     *
     * <p>
     *     NOTE: if {@link DomainObject#autoCompleteRepository()} (and {@link DomainObject#autoCompleteMethod()}) have
     *     been specified, then these take precedence of the query DSL auto-complete.
     * </p>
     *
     * <p>
     *     NOTE: this feature requires that the <code>querydsl-jpa</code> module is included as part of the
     *     application manifest.  Otherwise, no autocomplete will be generated.
     * </p>
     *
     * @see #queryDslAutoCompleteMinLength()
     * @see #queryDslAutoCompleteAdditionalPredicateRepository()
     * @see #queryDslAutoCompleteAdditionalPredicateMethod()
     */
    int queryDslAutoCompleteLimitResults() default QueryDslAutoCompleteConstants.LIMIT_RESULTS;

    /**
     * If at least one property of the entity has been annotated with {@link Property#queryDslAutoComplete()}, then
     * that property (and any others) will automatically be used for autocomplete functionality; this attribute
     * can be used to specify additional predicate(s) to always be added to the autocomplete (for example to search
     * only for current or active objects).
     *
     * <p>
     *     If this attribute is specified, it indicates the class of a repository service that includes a method which
     *     returns an additional predicate to be applied.  The default name of that method is
     *     &quot;queryDslAutoCompleteAdditionalPredicates&quot; (but can be overridden if required using
     *     {@link DomainObject#queryDslAutoCompleteAdditionalPredicateMethod()}).
     * </p>
     *
     * <p>
     *     NOTE: this feature requires that the <code>querydsl-jpa</code> module is included as part of the
     *     application manifest.  Otherwise, no autocomplete will be generated.
     * </p>
     *
     * @see #queryDslAutoCompleteAdditionalPredicateMethod()
     * @see #queryDslAutoCompleteMinLength()
     * @see #queryDslAutoCompleteLimitResults()
     */
    Class<?> queryDslAutoCompleteAdditionalPredicateRepository() default Object.class;

    /**
     * If at least one property of the entity has been annotated with {@link Property#queryDslAutoComplete()}, then
     * that property (and any others) will automatically be used for autocomplete functionality; this attribute
     * can be used to specify the name of a method in a repository to provide additional predicate(s) to always be
     * added to the autocomplete (for example to search only for current or active objects).
     *
     * <p>
     *     NOTE: this feature requires that the <code>querydsl-jpa</code> module is included as part of the
     *     application manifest.  Otherwise, no autocomplete will be generated.
     * </p>
     *
     * @see #queryDslAutoCompleteMinLength()
     * @see #queryDslAutoCompleteLimitResults()
     * @see #queryDslAutoCompleteAdditionalPredicateRepository()
     */
    String queryDslAutoCompleteAdditionalPredicateMethod() default QueryDslAutoCompleteConstants.ADDITIONAL_PREDICATE_METHOD_NAME;

    class QueryDslAutoCompleteConstants {
        public final static String ADDITIONAL_PREDICATE_METHOD_NAME = "queryDslAutoCompleteAdditionalPredicates";
        public final static int MIN_LENGTH = 1;
        public final static int LIMIT_RESULTS = 50;
    }
}
