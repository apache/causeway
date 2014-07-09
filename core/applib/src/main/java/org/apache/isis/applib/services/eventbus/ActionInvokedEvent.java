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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.isis.applib.Identifier;

/**
 * @deprecated - use instead {@link ActionInteractionEvent}
 */
@Deprecated
public abstract class ActionInvokedEvent<S> extends ActionInteractionEvent<S> {
    
    private static final long serialVersionUID = 1L;

    @Deprecated
    public static class Default extends ActionInvokedEvent<Object> {
        private static final long serialVersionUID = 1L;
        public Default(Object source, Identifier identifier, Object... arguments) {
            super(source, identifier, arguments);
        }
    }

    public ActionInvokedEvent(
            final S source, 
            final Identifier identifier, 
            final Object... arguments) {
        this(source, identifier, arguments != null? Arrays.asList(arguments): Collections.emptyList());
    }
    
    public ActionInvokedEvent(
            final S source, 
            final Identifier identifier, 
            final List<Object> arguments) {
        super(source, identifier, arguments);
    }
}