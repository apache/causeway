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

import org.apache.isis.applib.annotation.Hidden;
import org.apache.isis.applib.annotation.Programmatic;


/**
 * Indicates that an object belongs to the UI/application layer, and is intended to be used as a view model.
 *
 * <p>
 *     Objects that are part of the domain object layer should instead implement {@link RecreatableDomainObject}.
 * </p>
 */
public interface ViewModel {

    /**
     * Obtain a memento of the view model.
     *
     * <p>
     * Typically this will be the identifier of a backing domain entity, but it could also be an arbitrary string,
     * for example a bunch of JSON.
     * 
     * <p>
     * This method is called by the framework in order that the view model may be recreated subsequently
     * through {@link #viewModelInit(String)}.
     */
    @Programmatic
    public String viewModelMemento();
    
    /**
     * Used to re-initialize a view model with a memento obtained from {@link #viewModelMemento()}.
     */
    @Programmatic
    public void viewModelInit(String memento);

    /**
     * Cloneable view models can in effect appear to be editable; the viewer can build a new view model from a
     * view model whose state has been edited.
     */
    public interface Cloneable extends java.lang.Cloneable {
        @Hidden
        public Object clone();
    }
}
