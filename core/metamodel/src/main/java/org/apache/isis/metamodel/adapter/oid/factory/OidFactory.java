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

import org.apache.isis.metamodel.adapter.oid.RootOid;
import org.apache.isis.metamodel.spec.ManagedObject;

import lombok.val;

/**
 * @since 2.0
 */
public interface OidFactory {

    RootOid oidFor(ManagedObject managedObject);

    // -- HANDLER
    
    public interface Handler {
        boolean isHandling(ManagedObject managedObject);
        RootOid oidFor(ManagedObject managedObject);
    }

    // -- BUILDER
    
    public interface OidFactoryBuilder {
        OidFactoryBuilder add(Handler handler);
        OidFactory build();
    }

    public static OidFactoryBuilder builder() {
        return new OidFactory_Builder();
    }

    public static OidFactory buildDefault() {
        val oidFactory = OidFactory.builder()
                .add(new OidFactory_builtinHandlers.GuardAgainstRootOid())
                .add(new OidFactory_builtinHandlers.OidForServices())
                .add(new OidFactory_builtinHandlers.OidForValues())
                .add(new OidFactory_builtinHandlers.OidForViewModels())
                .add(new OidFactory_builtinHandlers.OidForEntities())
                .add(new OidFactory_builtinHandlers.OidForOthers())
                .build();
        return oidFactory;
    }
    
}
