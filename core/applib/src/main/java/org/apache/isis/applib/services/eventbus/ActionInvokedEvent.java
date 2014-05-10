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

import org.apache.isis.applib.annotation.PostsActionInvokedEvent;
import org.apache.isis.applib.util.ObjectContracts;

public abstract class ActionInvokedEvent<S> extends java.util.EventObject {
    
    private static final long serialVersionUID = 1L;

    public static class Default extends ActionInvokedEvent<Object> {
        private static final long serialVersionUID = 1L;
    }

    private final String action;
    private final String parameters;
    
    /**
     * To instantiate reflectively when the {@link PostsActionInvokedEvent} annotation
     * is used.
     * 
     * <p>
     * The fields ({@link #source} and {@link #value} are then set reflectively.
     */
    public ActionInvokedEvent() {
        this(null, null, null);
    }
    
    public ActionInvokedEvent(
            final S source, 
            final String action, 
            final String parameters) {
        super(source);
        this.action = action;
        this.parameters = parameters;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public S getSource() {
        return (S)source;
    }
    public String getAction() {
        return action;
    }
    public String getParameters() {
        return parameters;
    }
    
    @Override
    public String toString() {
        return ObjectContracts.toString(this, "source,action,parameters");
    }
}