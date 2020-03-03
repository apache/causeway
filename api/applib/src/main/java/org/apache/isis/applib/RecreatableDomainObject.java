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

package org.apache.isis.applib;

/**
 * Indicates that the domain object can be recreated from a string.
 *
 *
 * <p>
 *      Objects that are view models (logically belonging to the UI/application layer) should instead implement
 *      {@link org.apache.isis.applib.ViewModel}.
 * </p>

 */
// tag::refguide[]
// end::refguide[]
public interface RecreatableDomainObject {

    /**
     * Obtain a memento of the recreatable object.
     *
     * <p>
     * Typically this will be the identifier of a backing domain entity, but it could also be an arbitrary string,
     * for example a bunch of JSON.
     *
     * <p>
     * This method is called by the framework in order that the view model may be recreated subsequently
     * through {@link #__isis_recreate(String)}.
     */
    public String __isis_memento();

    /**
     * Used to recreate a recreatable object with a memento obtained from {@link #__isis_recreate(String)}.
     */
    public void __isis_recreate(String memento);

}
