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
package org.apache.causeway.applib.services.wrapper;

import org.apache.causeway.applib.services.wrapper.control.ExecutionMode;
import org.apache.causeway.applib.services.wrapper.control.SyncControl;
import org.apache.causeway.commons.collections.ImmutableEnumSet;

/**
 * Implemented by all objects that have been viewed as per
 * {@link WrapperFactory#wrap(Object)}.
 *
 * @since 1.x {@index}
 */
public interface WrappingObject {

    /**
     * Programmatic equivalent of invoking save for a transient object .
     *
     * <p>
     * NOTE: domain classes may not have a method with this name.  The <tt>__causeway_</tt> prefix is
     * intended to reduce the risk of a collision.
     * </p>
     */
    void __causeway_save();

    /**
     * Provide access to the underlying, wrapped object.
     *
     * <p>
     * Used to unwrap objects used as arguments to actions (otherwise, end up
     * creating a <tt>ObjectSpecification</tt> for the Javassist-enhanced class, not
     * the original class).
     *
     * <p>
     * NOTE: domain classes may not have a method with this name.  The <tt>__causeway_</tt> prefix is
     * intended to reduce the risk of a collision.
     * </p>
     *
     * <p>
     * <b>NOTE: there is a string-literal reference to this name
     * <tt>DomainObjectInvocationHandler</tt>, so it should not be changed.</b>.
     */
    Object __causeway_wrapped();

    /**
     * The {@link ExecutionMode}(s) inferred from the
     * {@link SyncControl} with which this wrapper was
     * {@link WrapperFactory#wrap(Object, SyncControl) created}.
     *
     * @implNote domain classes may not have a method with this name.  The <tt>__causeway_</tt> prefix is
     * intended to reduce the risk of a collision.
     */
    ImmutableEnumSet<ExecutionMode> __causeway_executionModes();

}
