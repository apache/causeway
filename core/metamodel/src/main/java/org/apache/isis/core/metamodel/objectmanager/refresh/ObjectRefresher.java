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
package org.apache.isis.core.metamodel.objectmanager.refresh;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public interface ObjectRefresher {

    /**
     * Reloads the state of the instance from the data store.
     * @param objectLoadRequest
     */
    void refreshObject(ManagedObject managedObject);
    
    // -- HANDLER
    
    static interface Handler
    extends
        ChainOfResponsibility.Handler<ManagedObject, Void> {
    }

    // -- FACTORY
    
    public static ObjectRefresher createDefault() {
        
        val chainOfHandlers = _Lists.of(
                new ObjectRefresher_builtinHandlers.GuardAgainstNull(),
                new ObjectRefresher_builtinHandlers.RefreshEntity(),
                new ObjectRefresher_builtinHandlers.RefreshOther());
        
        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);
        
        return chainOfRespo::handle;

    }
    
    
}
