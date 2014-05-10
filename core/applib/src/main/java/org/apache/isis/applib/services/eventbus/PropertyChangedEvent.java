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
package org.apache.isis.applib.services.eventbus;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.annotation.PostsPropertyChangedEvent;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class PropertyChangedEvent<S,T> extends java.util.EventObject {
    
    private static final long serialVersionUID = 1L;

    public static class Default extends PropertyChangedEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
    }
    
    private final Identifier identifier;
    private final T oldValue;
    private final T newValue;
    
    /**
     * To instantiate reflectively when the {@link PostsPropertyChangedEvent} annotation
     * is used.
     * 
     * <p>
     * The fields ({@link #source}, {@link #oldValue} and {@link #newValue}) are
     * then set reflectively.
     */
    public PropertyChangedEvent() {
        this(null, null, null, null);
    }
    
    /**
     * @deprecated - use {@link #PropertyChangedEvent(Object, Identifier, Object, Object)}.
     */
    @Deprecated
    public PropertyChangedEvent(S source, T oldValue, T newValue) {
        this(source, null, oldValue, newValue);
    }

    public PropertyChangedEvent(S source, Identifier identifier, T oldValue, T newValue) {
        super(source);
        this.identifier = identifier;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public S getSource() {
        return (S) source;
    }
    public Identifier getIdentifier() {
        return identifier;
    }
    public T getOldValue() {
        return oldValue;
    }
    public T getNewValue() {
        return newValue;
    }
    
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,oldValue,newValue");
    }
}