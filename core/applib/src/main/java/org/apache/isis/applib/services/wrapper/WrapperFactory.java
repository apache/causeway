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

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.isis.applib.annotation.Programmatic;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;

/**
 * Provides the ability to &quot;wrap&quot; of a domain object such that it can
 * be interacted with while enforcing the hide/disable/validate rules implies by
 * the Isis programming model.
 *
 * <p>
 * The &quot;wrap&quot; is a CGLib proxy that wraps the underlying domain
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
public interface WrapperFactory {

    /**
     * Whether interactions with the wrapper are actually passed onto the
     * underlying domain object.
     *
     * @see WrapperFactory#wrap(Object, ExecutionMode)
     */
    public static enum ExecutionMode {
        /**
         * Validate all business rules and then execute.
         */
        EXECUTE(true,true),
        /**
         * Skip all business rules and then execute.
         */
        SKIP_RULES(false, true),
        /**
         * Validate all business rules but do not execute.
         */
        NO_EXECUTE(true, false);

        private final boolean enforceRules;
        private final boolean execute;

        private ExecutionMode(final boolean enforceRules, final boolean execute) {
            this.enforceRules = enforceRules;
            this.execute = execute;
        }

        public boolean shouldEnforceRules() {
            return enforceRules;
        }
        public boolean shouldExecute() {
            return execute;
        }
    }

    WrapperFactory NOOP = new WrapperFactory(){

        @Override
        public <T> T wrap(T domainObject) {
            return domainObject;
        }

        @Override public <T> T w(final T domainObject) {
            return wrap(domainObject);
        }

        @Inject
        FactoryService factoryService;

        @Override
        public <T> T wm(final Class<T> mixinClass, final Object mixedIn) {
            return wrap(factoryService.m(mixinClass, mixedIn));
        }

        @Override
        public <T> T wrapNoExecute(T domainObject) {
            return domainObject;
        }

        @Override
        public <T> T wrapSkipRules(T domainObject) {
            return domainObject;
        }

        @Override
        public <T> T wrap(T domainObject, ExecutionMode mode) {
            return domainObject;
        }

        @Override
        public <T> T unwrap(T possibleWrappedDomainObject) {
            return possibleWrappedDomainObject;
        }

        @Override
        public <T> boolean isWrapper(T possibleWrappedDomainObject) {
            return false;
        }

        @Override
        public List<InteractionListener> getListeners() {
            return Collections.emptyList();
        }

        @Override
        public boolean addInteractionListener(InteractionListener listener) {
            return false;
        }

        @Override
        public boolean removeInteractionListener(InteractionListener listener) {
            return false;
        }

        @Override
        public void notifyListeners(InteractionEvent ev) {
        }
    };

    /**
     * Provides the &quot;wrapper&quot; of the underlying domain object.
     *
     * <p>
     * If the object has (see {@link #isWrapper(Object)} already been wrapped),
     * then should just return the object back unchanged.
     */
    @Programmatic
    <T> T wrap(T domainObject);

    @Programmatic
    <T> T w(T domainObject);

    <T> T wm(Class<T> mixinClass, Object mixedIn);

    /**
     * Convenience method for {@link #wrap(Object, ExecutionMode)} with {@link ExecutionMode#NO_EXECUTE},
     * to make this feature more discoverable.
     */
    @Programmatic
    <T> T wrapNoExecute(T domainObject);

    /**
     * Convenience method for {@link #wrap(Object, ExecutionMode)} with {@link ExecutionMode#SKIP_RULES},
     * to make this feature more discoverable.
     */
    @Programmatic
    <T> T wrapSkipRules(T domainObject);

    /**
     * Same as {@link #wrap(Object)}, except the actual execution occurs only if
     * the <tt>execute</tt> parameter indicates.
     *
     * <p>
     * Otherwise, will do all the validations (raise exceptions as required
     * etc.), but doesn't modify the model.
     */
    @Programmatic
    <T> T wrap(T domainObject, ExecutionMode mode);


    /**
     * Obtains the underlying domain object, if wrapped.
     *
     * <p>
     * If the object {@link #isWrapper(Object) is not wrapped}, then
     * should just return the object back unchanged.
     */
    @Programmatic
    <T> T unwrap(T possibleWrappedDomainObject);


    /**
     * Whether the supplied object has been wrapped.
     *
     * @param <T>
     * @param possibleWrappedDomainObject
     *            - object that might or might not be a wrapper.
     * @return
     */
    @Programmatic
    <T> boolean isWrapper(T possibleWrappedDomainObject);




    /**
     * All {@link InteractionListener}s that have been registered using
     * {@link #addInteractionListener(InteractionListener)}.
     */
    @Programmatic
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
     * @return
     */
    @Programmatic
    public boolean addInteractionListener(InteractionListener listener);

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
    @Programmatic
    public boolean removeInteractionListener(InteractionListener listener);

    @Programmatic
    public void notifyListeners(InteractionEvent ev);

}
