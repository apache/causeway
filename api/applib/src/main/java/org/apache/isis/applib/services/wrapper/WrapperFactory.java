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
package org.apache.isis.applib.services.wrapper;

import java.util.List;

import org.apache.isis.applib.exceptions.recoverable.InteractionException;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;

/**
 *
 * Provides the ability to 'wrap' a domain object such that it can
 * be interacted with while enforcing the hide/disable/validate rules implied by
 * the Apache Isis programming model.
 *
 * <p>
 * This capability goes beyond enforcing the (imperative) constraints within
 * the `hideXxx()`, `disableXxx()` and `validateXxx()` supporting methods; it
 * also enforces (declarative) constraints such as those represented by
 * annotations, eg `@Parameter(maxLength=...)` or `@Property(mustSatisfy=...)`.
 * </p>
 *
 * <p>
 * The wrapper can alternatively also be used to execute the action
 * asynchronously, through an {@link java.util.concurrent.ExecutorService}.
 * Any business rules will be invoked synchronously beforehand, however.
 * </p>
 *
 * <p>
 * The 'wrap' is a runtime-code-generated proxy that wraps the underlying domain
 * object. The wrapper can then be interacted with as follows:
 * <ul>
 * <li>a <tt>get</tt> method for properties or collections</li>
 * <li>a <tt>set</tt> method for properties</li>
 * <li>any action</li>
 * </ul>
 * </p>
 *
 * <p>
 * Calling any of the above methods may result in a (subclass of)
 * {@link InteractionException} if the object disallows it. For example, if a
 * property is annotated as hidden then a {@link HiddenException} will
 * be thrown. Similarly if an action has a <tt>validate</tt> method and the
 * supplied arguments are invalid then a {@link InvalidException} will be
 * thrown.
 * </p>
 *
 * <p>
 * In addition, the following methods may also be called:
 * <ul>
 * <li>the <tt>title</tt> method</li>
 * <li>any <tt>defaultXxx</tt> or <tt>choicesXxx</tt> method</li>
 * </ul>
 * </p>
 *
 * <p>
 * If the object has (see {@link #isWrapper(Object)} already been wrapped),
 * then should just return the object back unchanged.
 * </p>
 *
 * @since 1.x {@index}
 */
public interface WrapperFactory {

    /**
     * Provides the &quot;wrapper&quot; of a domain object against which to invoke the action.
     *
     * <p>
     *     The provided {@link SyncControl} determines whether business rules are checked first, and conversely
     *     whether the action is executed.  There are therefore three typical cases:
     *     <ul>
     *         <li>check rules, execute action</li>
     *         <li>skip rules, execute action</li>
     *         <li>check rules, skip action</li>
     *     </ul>
     *     <p>
     *         The last logical option (skip rules, skip action) is valid but doesn't make sense, as it's basically a no-op.
     *     </p>
     * </p>
     *
     * <p>
     * Otherwise, will do all the validations (raise exceptions as required
     * etc.), but doesn't modify the model.
     */
    <T> T wrap(T domainObject,
               SyncControl syncControl);

    /**
     * A convenience overload for {@link #wrap(Object, SyncControl)},
     * returning a wrapper to invoke the action synchronously, enforcing business rules.
     * Any exceptions will be propagated, not swallowed.
     */
    <T> T wrap(T domainObject);

    /**
     * Provides the wrapper for a {@link FactoryService#mixin(Class, Object) mixin}, against which to invoke the action.
     *
     * <p>
     *     The provided {@link SyncControl} determines whether business rules are checked first, and conversely
     *     whether the action is executed.  See {@link #wrap(Object, SyncControl)} for more details on this.
     * </p>
     */
    <T> T wrapMixin(Class<T> mixinClass, Object mixee,
                    SyncControl syncControl);

    /**
     * A convenience overload for {@link #wrapMixin(Class, Object, SyncControl)},
     * returning a wrapper to invoke the action synchronously, enforcing business rules.
     * Any exceptions will be propagated, not swallowed.
     */
    <T> T wrapMixin(Class<T> mixinClass, Object mixee);

    /**
     * Obtains the underlying domain object, if wrapped.
     *
     * <p>
     * If the object {@link #isWrapper(Object) is not wrapped}, then
     * should just return the object back unchanged.
     */
    <T> T unwrap(T possibleWrappedDomainObject);

    /**
     * Whether the supplied object is a wrapper around a domain object.
     *
     * @param <T>
     * @param possibleWrappedDomainObject
     *            - object that might or might not be a wrapper.
     */
    <T> boolean isWrapper(T possibleWrappedDomainObject);



    //
    // -- ASYNC WRAPPING
    //


    /**
     * Returns a proxy object for the provided {@code domainObject},
     * through which can execute the action asynchronously (in another thread).
     *
     * @param <T> - the type of the domain object
     * @param <R> - the type of the return of the action
     * @param domainObject
     * @param asyncControl
     *
     * @since 2.0
     */
    <T,R> T asyncWrap(T domainObject,
                      AsyncControl<R> asyncControl);

    /**
     * Returns a proxy object for the provided {@code mixinClass},
     * through which can execute the action asynchronously (in another thread).
     *
     * @param <T>
     * @param mixinClass
     * @param mixee
     * @param asyncControl
     *
     * @since 2.0
     */
    <T,R> T asyncWrapMixin(
                   Class<T> mixinClass, Object mixee,
                   AsyncControl<R> asyncControl);



    //
    // -- INTERACTION EVENT HANDLING
    //

    /**
     * All {@link InteractionListener}s that have been registered using
     * {@link #addInteractionListener(InteractionListener)}.
     */
    // ...
    List<InteractionListener> getListeners();

    /**
     * Registers an {@link InteractionListener}, to be notified of interactions
     * on all wrappers.
     *
     * <p>
     * This is retrospective: the listener will be notified of interactions even
     * on wrappers created before the listener was installed. (From an
     * implementation perspective this is because the wrappers delegate back to
     * the container to fire the events).
     *
     * @param listener
     */
    boolean addInteractionListener(InteractionListener listener);

    /**
     * Remove an {@link InteractionListener}, to no longer be notified of
     * interactions on wrappers.
     *
     * <p>
     * This is retrospective: the listener will no longer be notified of any
     * interactions created on any wrappers, not just on those wrappers created
     * subsequently. (From an implementation perspective this is because the
     * wrappers delegate back to the container to fire the events).
     *
     * @param listener
     */
    boolean removeInteractionListener(
                    InteractionListener listener);

    void notifyListeners(InteractionEvent ev);
    // ...

}
