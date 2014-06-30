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

/**
 * Replaced by {@link org.apache.isis.applib.services.wrapper.WrappingObject}.
 *
 * <p>
 *     The methods in this (sub-)interface are all deprecated because of the risk
 *     of collision with a method on the underlying domain object.
 * </p>
 *
 * @deprecated
 */
@Deprecated
public interface WrapperObject extends WrappingObject {

    /**
     * Same as {@link #__isis_save()}.
     *
     * <p>
     * NOTE: domain classes may not have a method with this name.  This method has been deprecated because
     * the risk of a collision is high.
     * </p>
     *
     * @deprecated - use <tt>__isis_save()</tt> instead.
     */
    @Deprecated
    void save();

    /**
     * Same as {@link #__isis_wrapped()}.
     *
     * <p>
     * NOTE: domain classes may not have a method with this name.  This method has been deprecated because
     * the risk of a collision is reasonably high.
     * </p>
     *
     * @deprecated - use <tt>__isis_save()</tt> instead.
     */
    @Deprecated
    Object wrapped();

}
