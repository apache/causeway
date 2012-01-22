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

/**
 * Implemented by all objects that have been viewed as per
 * {@link WrapperFactory#wrap(Object)}.
 */
public interface WrapperObject {

    /**
     * Programmatic equivalent of invoking save for a transient object (that is,
     * like hitting the <i>save</i> button that the DnD viewer automatically
     * renders.
     */
    void save();

    /**
     * Provide access to the underlying, wrapped object.
     * 
     * <p>
     * Used to unwrap objects used as arguments to actions (otherwise, end up
     * creating a <tt>ObjectSpecification</tt> for the CGLib-enhanced class, not
     * the original class).
     * 
     * <p>
     * <b>NOTE: there is a string-literal reference to this name
     * <tt>DomainObjectInvocationHandler</tt>, so it should not be changed.</b>.
     */
    Object wrapped();

}
