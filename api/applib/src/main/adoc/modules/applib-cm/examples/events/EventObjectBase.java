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
package org.apache.isis.applib.events;

import javax.annotation.Nullable;

import org.apache.isis.core.commons.internal.exceptions._Exceptions;

import static org.apache.isis.core.commons.internal.base._With.requires;

/**
 * @since 2.0
 */
// tag::refguide[]
public abstract class EventObjectBase<T> {

    // end::refguide[]
    /**
     * The object on which the Event initially occurred.
     */
    // tag::refguide[]
    protected transient T source;

    // end::refguide[]
    /**
     * Constructs a prototypical Event.
     *
     * @param    source    The object on which the Event initially occurred.
     */
    // tag::refguide[]
    protected EventObjectBase(@Nullable T source) {
        this.source = source;
    }

    // end::refguide[]
    /**
     * The object on which the Event initially occurred.
     *
     * @return   The object on which the Event initially occurred.
     */
    // tag::refguide[]
    public @Nullable T getSource() {
        return source;
    }

    // end::refguide[]
    /**
     * A one-shot function. Only allowed to be called if a source has not already been set.
     *
     * @apiNote reserved for framework internal use
     *
     * @param source non-null
     */
    public void initSource(T source) {
        if(this.source!=null) {
            throw _Exceptions.unrecoverable(getClass().getName() + " cannot init when source is already set");
        }
        requires(source, "source");
        this.source = source;
    }

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  a String representation of this EventObject
     */
    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }
    // tag::refguide[]

}
// end::refguide[]
