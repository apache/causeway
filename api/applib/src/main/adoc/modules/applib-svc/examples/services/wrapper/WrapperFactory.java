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

import java.util.EnumSet;
import java.util.List;

import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.wrapper.events.InteractionEvent;
import org.apache.isis.applib.services.wrapper.listeners.InteractionListener;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

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
public interface WrapperFactory {

    /**
     * Whether interactions with the wrapper are actually passed onto the
     * underlying domain object.
     *
     * @see WrapperFactory#wrap(Object, ExecutionMode)
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static enum ExecutionMode {
        
        /**
         * Skip all business rules.
         */
        SKIP_RULE_VALIDATION,
        
        /**
         * Skip execution.
         */
        SKIP_EXECUTION,
        
        /**
         * Don't fail fast, swallow any exception during validation or execution.
         */
        SWALLOW_EXCEPTIONS,
        
        ;
        
        // -- PRESET ENUM SETS

        /**
         * Validate all business rules and then execute. May throw exceptions in order to fail fast. 
         */
        public static final EnumSet<ExecutionMode> EXECUTE = EnumSet.noneOf(ExecutionMode.class); 

        /**
         * Skip all business rules and then execute, does throw an exception if execution fails.
         */
        public static final EnumSet<ExecutionMode> SKIP_RULES = EnumSet.of(SKIP_RULE_VALIDATION);
        
        /**
         * Validate all business rules but do not execute, throw an exception if validation 
         * fails. 
         */
        public static final EnumSet<ExecutionMode> NO_EXECUTE = EnumSet.of(SKIP_EXECUTION);
        
        /**
         * Validate all business rules and then execute, but don't throw an exception if validation 
         * or execution fails.
         */
        public static final EnumSet<ExecutionMode> TRY = EnumSet.of(SWALLOW_EXCEPTIONS); 
        
        /**
         * Skips all steps.
         * @since 2.0
         */
        public static final EnumSet<ExecutionMode> NOOP = EnumSet.of(SKIP_RULE_VALIDATION, SKIP_EXECUTION);
        
    }

    /**
     * Provides the &quot;wrapper&quot; of the underlying domain object.
     *
     * <p>
     * If the object has (see {@link #isWrapper(Object)} already been wrapped),
     * then should just return the object back unchanged.
     */
    <T> T wrap(T domainObject);

    /**
     * {@link #wrap(Object) wraps} a {@link FactoryService#mixin(Class, Object) mixin}.
     */
    <T> T wrapMixin(Class<T> mixinClass, Object mixedIn);

    /**
     * Convenience method for {@link #wrap(Object, ExecutionMode)} with {@link ExecutionMode#TRY},
     * to make this feature more discoverable.
     */
    <T> T wrapTry(T domainObject);

    /**
     * Convenience method for {@link #wrap(Object, ExecutionMode)} with {@link ExecutionMode#NO_EXECUTE},
     * to make this feature more discoverable.
     */
    <T> T wrapNoExecute(T domainObject);

    /**
     * Convenience method for {@link #wrap(Object, ExecutionMode)} with {@link ExecutionMode#SKIP_RULES},
     * to make this feature more discoverable.
     */
    <T> T wrapSkipRules(T domainObject);

    /**
     * Same as {@link #wrap(Object)}, except the actual execution occurs only if
     * the <tt>execute</tt> parameter indicates.
     *
     * <p>
     * Otherwise, will do all the validations (raise exceptions as required
     * etc.), but doesn't modify the model.
     */
    <T> T wrap(T domainObject, EnumSet<ExecutionMode> mode);


    /**
     * Obtains the underlying domain object, if wrapped.
     *
     * <p>
     * If the object {@link #isWrapper(Object) is not wrapped}, then
     * should just return the object back unchanged.
     */
    <T> T unwrap(T possibleWrappedDomainObject);


    /**
     * Whether the supplied object has been wrapped.
     *
     * @param <T>
     * @param possibleWrappedDomainObject
     *            - object that might or might not be a wrapper.
     * @return
     */
    <T> boolean isWrapper(T possibleWrappedDomainObject);

    // -- ASYNC WRAPPING
    
    /**
     * Returns a {@link AsyncWrap} bound to the provided {@code domainObject}, 
     * to prepare for type-safe asynchronous action execution. 
     * 
     * @param <T>
     * @param domainObject
     * @param mode
     * 
     * @since 2.0
     */
    <T> AsyncWrap<T> async(T domainObject, EnumSet<ExecutionMode> mode);
    
    /**
     * Shortcut for {@link #async(Object, EnumSet)} using execution mode 
     * {@link ExecutionMode#EXECUTE}.
     * @param <T>
     * @param domainObject
     * 
     * @since 2.0
     */
    default <T> AsyncWrap<T> async(T domainObject) {
        return async(domainObject, ExecutionMode.EXECUTE);
    }
    
    /**
     * Returns a {@link AsyncWrap} bound to the provided {@code mixinClass}, 
     * to prepare for type-safe asynchronous action execution. 
     * 
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * @param mode
     * 
     * @since 2.0
     */
    <T> AsyncWrap<T> asyncMixin(Class<T> mixinClass, Object mixedIn, EnumSet<ExecutionMode> mode);
    
    /**
     * Shortcut for {@link #asyncMixin(Class, Object, EnumSet)} using execution mode 
     * {@link ExecutionMode#EXECUTE}.
     * @param <T>
     * @param mixinClass
     * @param mixedIn
     * 
     * @since 2.0
     */
    default <T> AsyncWrap<T> asyncMixin(Class<T> mixinClass, Object mixedIn) {
        return asyncMixin(mixinClass, mixedIn, ExecutionMode.EXECUTE);
    }
    
    // -- ITERACTION EVENT HANDLING
    
    /**
     * All {@link InteractionListener}s that have been registered using
     * {@link #addInteractionListener(InteractionListener)}.
     */
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
    public boolean removeInteractionListener(InteractionListener listener);

    public void notifyListeners(InteractionEvent ev);

}
