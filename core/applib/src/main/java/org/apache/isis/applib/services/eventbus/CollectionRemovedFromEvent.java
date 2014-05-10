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
import org.apache.isis.applib.util.ObjectContracts;

public abstract class CollectionRemovedFromEvent<S,T> extends java.util.EventObject {
    
    private static final long serialVersionUID = 1L;

    public static class Default extends CollectionRemovedFromEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
    }

    private final Identifier identifier;
    private final T value;
    
    /**
     * To instantiate reflectively when the {@link PostsCollectionRemovedFromEvent} annotation
     * is used.
     * 
     * <p>
     * The fields ({@link #source} and {@link #value} are then set reflectively.
     */
    public CollectionRemovedFromEvent() {
        this(null, null, null);
    }
    public CollectionRemovedFromEvent(
            final S source, 
            final Identifier identifier, 
            final T value) {
        super(source);
        this.identifier = identifier;
        this.value = value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public S getSource() {
        return (S) source;
    }
    public Identifier getIdentifier() {
        return identifier;
    }
    public T getValue() {
        return value;
    }
    
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,value");
    }
}