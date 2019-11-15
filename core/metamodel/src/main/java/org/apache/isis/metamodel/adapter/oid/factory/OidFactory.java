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

package org.apache.isis.metamodel.adapter.oid.factory;

import org.apache.isis.commons.handler.ChainOfResponsibility;
import org.apache.isis.commons.internal.collections._Lists;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;

import lombok.val;

/**
 * @since 2.0
 */
public interface OidFactory {

    RootOid oidFor(ManagedObject managedObject);

    // -- HANDLER
    
    public interface Handler extends ChainOfResponsibility.Handler<ManagedObject, RootOid> {}

    // -- FACTORY
    
    public static OidFactory createDefault() {
        
        val chainOfHandlers = _Lists.of(
                new OidFactory_builtinHandlers.GuardAgainstRootOid(),
                new OidFactory_builtinHandlers.OidForServices(),
                new OidFactory_builtinHandlers.OidForValues(),
                new OidFactory_builtinHandlers.OidForViewModels(),
                new OidFactory_builtinHandlers.OidForEntities(),
                new OidFactory_builtinHandlers.OidForOthers());
        
        val chainOfRespo = ChainOfResponsibility.of(chainOfHandlers);
        
        return managedObject -> chainOfRespo
                .handle(managedObject)
                .orElseThrow(()->_Exceptions.unrecoverableFormatted(
                        "Could not create an Oid for managedObject: %s", managedObject));
        
        
    }
    
}
