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

/**
 * @deprecated - use instead {@link CollectionInteractionEvent}
 */
@Deprecated
public abstract class CollectionRemovedFromEvent<S,T> extends CollectionInteractionEvent<S,T> {
    
    private static final long serialVersionUID = 1L;

    @Deprecated
    public static class Default extends CollectionRemovedFromEvent<Object, Object> {
        private static final long serialVersionUID = 1L;
        public Default(Object source, Identifier identifier, Object value) {
            super(source, identifier, value);
        }
    }

    public CollectionRemovedFromEvent(
            final S source, 
            final Identifier identifier, 
            final T value) {
        super(source, identifier, Of.REMOVE_FROM, value);
    }

}