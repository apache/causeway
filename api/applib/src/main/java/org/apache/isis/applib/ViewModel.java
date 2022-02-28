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

import org.apache.isis.applib.annotation.Programmatic;

/**
 * Indicates that an object belongs to the UI/application layer, and is intended to be used as a view model.
 *
 * @since 1.x {@index}
 */
public interface ViewModel {

    /**
     * Obtain a memento of the view model.
     * <p>
     * Instances of {@link ViewModel} must include a public single {@link String} argument constructor,
     * that recreates an instance from a memento string.
     * This constructor is not required to resolve injection points or fire domain events,
     * instead this responsibility is encapsulated with the framework.
     */
    @Programmatic
    String viewModelMemento();

}
