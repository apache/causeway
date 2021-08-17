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

import org.checkerframework.checker.nullness.qual.Nullable;

import org.apache.isis.commons.internal.exceptions._Exceptions;

import lombok.NonNull;

/**
 * @since 2.0 {@index}
 */
public abstract class EventObjectBase<T> {

    /**
     * The object on which the Event initially occurred.
     */
    protected transient T source;

    /**
     * Constructs a prototypical Event.
     *
     * @param    source    The object on which the Event initially occurred.
     */
    protected EventObjectBase(@Nullable final T source) {
        this.source = source;
    }

    /**
     * The object on which the Event initially occurred.
     *
     * @return   The object on which the Event initially occurred.
     */
    public @Nullable T getSource() {
        return source;
    }

    /**
     * A one-shot function. Only allowed to be called if a source has not already been set.
     *
     * @apiNote reserved for framework internal use
     *
     * @param source non-null
     */
    public void initSource(final @NonNull T source) {
        if(this.source!=null) {
            throw _Exceptions.illegalState(getClass().getName() + " cannot init when source is already set");
        }
        this.source = source;
    }

    /**
     * Returns a String representation of this EventObject.
     *
     * @return  a String representation of this EventObject
     */
    @Override
    public String toString() {
        return getClass().getName() + "[source=" + source + "]";
    }

}
