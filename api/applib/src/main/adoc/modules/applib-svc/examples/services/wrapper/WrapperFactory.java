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

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.SyncControl;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;

/**
 * Provides the ability to &quot;wrap&quot; of a domain object such that it can
 * be interacted with while enforcing the hide/disable/validate rules implied by
 * the Apache Isis programming model.
 *
 * <p>
 *     The wrapper can alternatively also be used to execute the action
 *     asynchronously, through an {@link java.util.concurrent.ExecutorService}.
 *     Any business rules will be invoked synchronously beforehand, however.
 *     hand
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
 *
 * <p>
 * Calling any of the above methods may result in a (subclass of)
 * {@link InteractionException} if the object disallows it. For example, if a
 * property is annotated as hidden then a {@link HiddenException} will
 * be thrown. Similarly if an action has a <tt>validate</tt> method and the
 * supplied arguments are invalid then a {@link InvalidException} will be
 * thrown.
 *
 * <p>
 * In addition, the following methods may also be called:
 * <ul>
 * <li>the <tt>title</tt> method</li>
 * <li>any <tt>defaultXxx</tt> or <tt>choicesXxx</tt> method</li>
 * </ul>
 *
 * <p>
 * If the object has (see {@link #isWrapper(Object)} already been wrapped),
 * then should just return the object back unchanged.
 */
// tag::refguide[]
// tag::refguide-async[]
// tag::refguide-listeners[]
public interface WrapperFactory {

    // end::refguide[]
    // end::refguide-async[]
    // end::refguide-listeners[]
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
    // tag::refguide[]
    <T> T wrap(T domainObject,                                      // <.>
               SyncControl syncControl);

    // end::refguide[]
    /**
     * A convenience overload for {@link #wrap(Object, SyncControl)},
     * returning a wrapper to invoke the action synchronously, enforcing business rules.
     * Any exceptions will be propagated, not swallowed.
     */
    // tag::refguide[]
    <T> T wrap(T domainObject);                                     // <.>

    // end::refguide[]
    /**
     * Provides the wrapper for a {@link FactoryService#mixin(Class, Object) mixin}, against which to invoke the action.
     *
     * <p>
     *     The provided {@link SyncControl} determines whether business rules are checked first, and conversely
     *     whether the action is executed.  See {@link #wrap(Object, SyncControl)} for more details on this.
     * </p>
     */
    // tag::refguide[]
    <T> T wrapMixin(Class<T> mixinClass, Object mixedIn,            // <.>
                    SyncControl syncControl);

    // end::refguide[]
    /**
     * A convenience overload for {@link #wrapMixin(Class, Object, SyncControl)},
     * returning a wrapper to invoke the action synchronously, enforcing business rules.
     * Any exceptions will be propagated, not swallowed.
     */
    // tag::refguide[]
    <T> T wrapMixin(Class<T> mixinClass, Object mixedIn);           // <.>

    // end::refguide[]
    /**
     * Obtains the underlying domain object, if wrapped.
     *
     * <p>
     * If the object {@link #isWrapper(Object) is not wrapped}, then
     * should just return the object back unchanged.
     */
    // tag::refguide[]
    <T> T unwrap(T possibleWrappedDomainObject);                    // <.>

    // end::refguide[]
    /**
     * Whether the supplied object has been wrapped.
     *
     * @param <T>
     * @param possibleWrappedDomainObject
     *            - object that might or might not be a wrapper.
     * @return
     */
    // tag::refguide[]
    <T> boolean isWrapper(T possibleWrappedDomainObject);           // <.>

    // end::refguide[]


    //
    // -- ASYNC WRAPPING
    //


    /**
     * Returns a proxy object for the provided {@code domainObject},
     * through which can execute the action asynchronously.
     *
     * @param <T> - the type of the domain object
     * @param <R> - the type of the return of the action
     * @param domainObject
     * @param asyncControl
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    <T,R> T asyncWrap(T domainObject,                      // <.>
                      AsyncControl<R> asyncControl);

    // end::refguide-async[]
    /**
     * Returns a proxy object for the provided {@code mixinClass},
     * through which can execute the action asynchronously.
     *
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @param asyncControl
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    <T,R> T asyncWrapMixin(                                // <.>
                   Class<T> mixinClass, Object mixedIn,
                   AsyncControl<R> asyncControl);

    // end::refguide-async[]


    //
    // -- INTERACTION EVENT HANDLING
    //

    /**
     * All {@link InteractionListener}s that have been registered using
     * {@link #addInteractionListener(InteractionListener)}.
     */
    // tag::refguide-listeners[]
    // ...
    List<InteractionListener> getListeners();                       // <.>

    // end::refguide-listeners[]
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
     * @return
     */
    // tag::refguide-listeners[]
    boolean addInteractionListener(InteractionListener listener);   // <.>

    // end::refguide-listeners[]
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
     * @return
     */
    // tag::refguide-listeners[]
    boolean removeInteractionListener(                              // <.>
                    InteractionListener listener);

    void notifyListeners(InteractionEvent ev);                      // <.>
    // tag::refguide-async[]
    // tag::refguide[]
    // ...

}
// end::refguide[]
// end::refguide-async[]
// end::refguide-listeners[]
