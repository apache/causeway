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

package org.apache.isis.metamodel.objectmanager.load;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.context.MetaModelContext;
import org.apache.isis.metamodel.context.MetaModelContextAware;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.Value;
import lombok.val;

/**
 * @since 2.0
 */
public interface ObjectLoader {

    ManagedObject loadObject(Request objectLoadRequest);
    
    // -- REQUEST (VALUE) TYPE
    
    @Value(staticConstructor = "of")
    public static class Request {
        ObjectSpecification objectSpecification;
        String objectIdentifier;
    }
    
    // -- HANDLER
    
    static interface Handler 
    extends 
        MetaModelContextAware, 
        ChainOfResponsibility.Handler<ObjectLoader.Request, ManagedObject> {
        
    }

    // -- FACTORY
    
    public static ObjectLoader createDefault(MetaModelContext metaModelContext) {
        
        val chainOfHandlers = _Lists.of(
                new ObjectLoader_builtinHandlers.GuardAgainstNull(),
                new ObjectLoader_builtinHandlers.LoadService(),
                new ObjectLoader_builtinHandlers.LoadValue(),
                new ObjectLoader_builtinHandlers.LoadViewModel(),
                new ObjectLoader_builtinHandlers.LoadEntity(),
                new ObjectLoader_builtinHandlers.LoadOther());
        
        chainOfHandlers.forEach(h->h.setMetaModelContext(metaModelContext));
        
        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);
        
        return request -> chainOfRespo
                .handle(request)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "ObjectLoader failed to handle request %s", request));
        
    }
    
}
