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

package org.apache.isis.core.metamodel.objectmanager.create;

import org.apache.isis.core.commons.handler.ChainOfResponsibility;
import org.apache.isis.core.commons.internal.collections._Lists;
import org.apache.isis.core.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContextAware;
import org.apache.isis.core.metamodel.spec.ManagedObject;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;

import lombok.Value;
import lombok.val;

/**
 * @since 2.0
 */
public interface ObjectCreator {

    ManagedObject createObject(Request objectLoadRequest);
    
    // -- REQUEST (VALUE) TYPE
    
    @Value(staticConstructor = "of")
    public static class Request {
        ObjectSpecification objectSpecification;
    }
    
    // -- HANDLER
    
    static interface Handler 
    extends 
        MetaModelContextAware, 
        ChainOfResponsibility.Handler<ObjectCreator.Request, ManagedObject> {
        
    }

    // -- FACTORY
    
    public static ObjectCreator createDefault(MetaModelContext metaModelContext) {
        
        val chainOfHandlers = _Lists.of(
                new ObjectCreator_builtinHandlers.LegacyCreationHandler()
                
//                new ObjectCreator_builtinHandlers.GuardAgainstNull(),
//                new ObjectCreator_builtinHandlers.LoadService(),
//                new ObjectCreator_builtinHandlers.CreateValueDefault(),
//                new ObjectCreator_builtinHandlers.CreateViewModel(),
//                new ObjectCreator_builtinHandlers.CreateEntity(),
//                new ObjectCreator_builtinHandlers.CreateOther()
                );
        
        chainOfHandlers.forEach(h->h.setMetaModelContext(metaModelContext));
        
        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);
        
        return request -> chainOfRespo
                .handle(request)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "ObjectCreator failed to hanlde request %s", request));
        
    }
    
}
