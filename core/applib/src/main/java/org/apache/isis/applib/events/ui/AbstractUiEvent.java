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
package org.apache.isis.applib.events.ui;

import java.util.EventObject;
import java.util.Map;

import org.apache.isis.applib.util.ObjectContracts;
import org.apache.isis.applib.util.ToString;
import org.apache.isis.commons.internal.collections._Maps;

public abstract class AbstractUiEvent<S> extends EventObject {

    private static final long serialVersionUID = 1L;

    // -- constructors
    /**
     * If used then the framework will set state via (non-API) setters.
     *
     * <p>
     *     Because the {@link EventObject} superclass prohibits a null source, a dummy value is temporarily used.
     * </p>
     */
    public AbstractUiEvent() {
        this(null);
    }

    public AbstractUiEvent(final S source) {
        super(sourceElseDummy(source));
    }

    private static Object sourceElseDummy(final Object source) {
        return source != null ? source : new Object();
    }


    // -- source

    @Override
    @SuppressWarnings("unchecked")
    public S getSource() {
        return (S)source;
    }

    /**
     * Not API, set by the framework if the no-arg constructor is used.
     */
    public void setSource(S source) {
        this.source = source;
    }



    // -- userData
    /**
     * Provides a mechanism to pass data around.
     */
    private final Map<Object, Object> userData = _Maps.newHashMap();

    /**
     * Obtain user-data, as set by any other subscribers.
     */
    public Object get(Object key) {
        return userData.get(key);
    }
    /**
     * Set user-data, for the use of other subscribers.
     */
    public void put(Object key, Object value) {
        userData.put(key, value);
    }


    private final static ToString<AbstractUiEvent<?>> toString = ObjectContracts
            .toString("source", AbstractUiEvent::getSource);

    @Override
    public String toString() {
        return toString.toString(this);
    }

}