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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.control.AsyncControl;
import org.apache.isis.applib.services.wrapper.control.ExecutionMode;
import org.apache.isis.applib.services.wrapper.control.ExecutionModes;
import org.apache.isis.applib.services.wrapper.control.RuleCheckingPolicy;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;
import org.apache.isis.core.commons.collections.ImmutableEnumSet;

/**
 * Provides the ability to &quot;wrap&quot; of a domain object such that it can
 * be interacted with while enforcing the hide/disable/validate rules implied by
 * the Isis programming model.
 *
 * <p>
 * The 'wrap' is a runtime-code-generated proxy that wraps the underlying domain
 * object. The wrapper can then be interacted with as follows:
 * <ul>
 * <li>a <tt>get</tt> method for properties or collections</li>
 * <li>a <tt>set</tt> method for properties</li>
 * <li>an <tt>addTo</tt> or <tt>removeFrom</tt> method for collections</li>
 * <li>any action</li>
 * </ul>
 *
 * <p>
 * Calling any of the above methods may result in a (subclass of)
 * {@link InteractionException} if the object disallows it. For example, if a
 * property is annotated with {@link Hidden} then a {@link HiddenException} will
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
 * An exception will be thrown if any other methods are thrown.
 *
 * <p>
 * An implementation of this service (<tt>WrapperFactoryDefault</tt>) can be registered by including
 * <tt>o.a.i.core:isis-core-wrapper</tt> on the classpath; no further configuration is required.
 * </p>
 */
// tag::refguide[]
// tag::refguide-async[]
// tag::refguide-listeners[]
public interface WrapperFactory {

    // end::refguide-listeners[]
    // end::refguide-async[]
    // end::refguide[]

    /**
     * Same as {@link #wrap(Object)}, except the actual execution occurs only if
     * the <tt>execute</tt> parameter indicates.
     *
     * <p>
     * Otherwise, will do all the validations (raise exceptions as required
     * etc.), but doesn't modify the model.
     */
    // tag::refguide[]
    <T> T wrap(T domainObject,                                      // <.>
               ImmutableEnumSet<ExecutionMode> modes);

    // end::refguide[]
    /**
     * Provides the &quot;wrapper&quot; of the underlying domain object.
     *
     * <p>
     * If the object has (see {@link #isWrapper(Object)} already been wrapped),
     * then should just return the object back unchanged.
     */
    // tag::refguide[]
    <T> T wrap(T domainObject);                                     // <.>

    // end::refguide[]
    /**
     * Convenience method for {@link #wrap(Object, ImmutableEnumSet)} with {@link ExecutionModes#TRY},
     * to make this feature more discoverable.
     */
    // tag::refguide[]
    <T> T wrapTry(T domainObject);                                  // <.>

    // end::refguide[]
    /**
     * Convenience method for {@link #wrap(Object, ImmutableEnumSet)} with {@link ExecutionModes#NO_EXECUTE},
     * to make this feature more discoverable.
     */
    // tag::refguide[]
    <T> T wrapNoExecute(T domainObject);                            // <.>

    // end::refguide[]
    /**
     * Convenience method for {@link #wrap(Object, ImmutableEnumSet)} with {@link ExecutionModes#SKIP_RULES},
     * to make this feature more discoverable.
     */
    // tag::refguide[]
    <T> T wrapSkipRules(T domainObject);                            // <.>

    // end::refguide[]
    /**
     * {@link #wrap(Object) wraps} a {@link FactoryService#mixin(Class, Object) mixin}.
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
     * @param <T>
     * @param domainObject
     * @param mode
     *
     * @param executorService
     * @since 2.0
     */
    // tag::refguide-async[]
    <T,R> T async(T domainObject,                          // <.>
                  AsyncControl<R> asyncControl);

    // end::refguide-async[]
    /**
     * Returns a proxy object for the provided {@code mixinClass},
     * through which can execute the action asynchronously.
     *
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @param modes
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    <T> T asyncMixin(                                    // <.>
                Class<T> mixinClass, Object mixedIn,
                ImmutableEnumSet<ExecutionMode> modes,
                RuleCheckingPolicy ruleCheckingPolicy,
                ExecutorService executorService);

    // end::refguide-async[]
    /**
     * Returns a {@link AsyncControl} bound to the provided {@code mixinClass},
     * to prepare for type-safe asynchronous action execution.
     *
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @param modes
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    default <T> T asyncMixin(                                    // <.>
                Class<T> mixinClass, Object mixedIn,
                ImmutableEnumSet<ExecutionMode> modes) {
        // end::refguide-async[]

        return asyncMixin(
                mixinClass, mixedIn, modes,
                RuleCheckingPolicy.ASYNC, ForkJoinPool.commonPool());

        // tag::refguide-async[]
        // ...
    }

    // end::refguide-async[]
    /**
     * Shortcut for {@link #asyncMixin(Class, Object, ImmutableEnumSet)} using execution mode
     * {@link ExecutionModes#EXECUTE}.
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     *
     * @since 2.0
     */
    // tag::refguide-async[]
    default <T> T asyncMixin(                            // <.>
                        Class<T> mixinClass, Object mixedIn) {
        // end::refguide-async[]

        return asyncMixin(mixinClass, mixedIn, ExecutionModes.EXECUTE);

        // tag::refguide-async[]
        // ...
    }
    // end::refguide-async[]


    //
    // -- ITERACTION EVENT HANDLING
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
// end::refguide-listeners[]
// end::refguide-async[]
