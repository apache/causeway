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

package org.apache.isis.progmodel.wrapper.applib;

import java.util.List;

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.events.InteractionEvent;
import org.apache.isis.progmodel.wrapper.applib.listeners.InteractionListener;

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
 */
public interface WrapperFactory {

    /**
     * Whether interactions with the wrapper are actually passed onto the
     * underlying domain object.
     * 
     * @see WrapperFactory#wrap(Object, ExecutionMode)
     */
    public static enum ExecutionMode {
        EXECUTE, NO_EXECUTE
    }

    /**
     * Provides the &quot;wrapper&quot; of the underlying domain object.
     * 
     * <p>
     * If the object has (see {@link #isWrapper(Object)} already been wrapped),
     * then should just return the object back unchanged.
     * 
     * @see #addInteractionListener(InteractionListener)
     */
    <T> T wrap(T domainObject);

    /**
     * Same as {@link #wrap(Object)}, except the actual execution occurs only if
     * the <tt>execute</tt> parameter indicates.
     * 
     * <p>
     * Otherwise, will do all the validations (raise exceptions as required
     * etc.), but doesn't modify the model.
     */
    <T> T wrap(T domainObject, ExecutionMode mode);

    /**
     * Whether the supplied object has been wrapped.
     * 
     * @param <T>
     * @param possibleWrapper
     *            - object that might or might not be a wrapper.
     * @return
     */
    <T> boolean isWrapper(T possibleWrapper);

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
