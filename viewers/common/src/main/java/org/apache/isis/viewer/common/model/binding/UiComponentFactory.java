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
package org.apache.isis.viewer.common.model.binding;

import org.apache.isis.core.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.feature.ObjectFeature;

import lombok.NonNull;
import lombok.Value;

public interface UiComponentFactory<T> {
    
    T componentFor(UiComponentFactory.Request request);
    
    // -- REQUEST (VALUE) TYPE
    
    @Value(staticConstructor = "of")
    public static class Request {
        /** not null but the wrapped pojo is allowed to be null*/
        @NonNull private final ManagedObject managedObject; 
        @NonNull private final ObjectFeature objectFeature;
    }
    
    
    // -- HANDLER
    
    static interface Handler<T> 
    extends ChainOfResponsibility.Handler<UiComponentFactory.Request, T> {
    }
    
}
