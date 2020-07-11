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
package org.apache.isis.core.metamodel.objectmanager.detach;

import org.apache.isis.core.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.spec.ManagedObject;

import lombok.val;

/**
 * 
 * @since 2.0
 *
 */
public interface ObjectDetacher {

    ManagedObject detachObject(ManagedObject managedObject);
    
    // -- HANDLER
    
    static interface Handler
    extends
        ChainOfResponsibility.Handler<ManagedObject, ManagedObject> {
    }

    // -- FACTORY
    
    public static ObjectDetacher createDefault(MetaModelContext metaModelContext) {
        
        val chainOfHandlers = _Lists.of(
                new ObjectDetacher_builtinHandlers.GuardAgainstNull(),
                new ObjectDetacher_builtinHandlers.DetachEntity(metaModelContext),
                new ObjectDetacher_builtinHandlers.DetachOther());
        
        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);

        return request -> chainOfRespo.handle(request).orElse(null);

    }
    
    
}
