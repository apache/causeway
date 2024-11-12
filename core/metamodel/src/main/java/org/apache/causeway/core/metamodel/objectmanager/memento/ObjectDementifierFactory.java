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
package org.apache.causeway.core.metamodel.objectmanager.memento;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager.MementoRecreateRequest;

public record ObjectDementifierFactory() {

    public static ChainOfResponsibility<MementoRecreateRequest, ManagedObject> createChain() {
        return new ChainOfResponsibility<>("ObjectDementifier", BuiltinHandlers.values());
    }

    // -- HANDLERS

    enum BuiltinHandlers implements ChainOfResponsibility.Handler<MementoRecreateRequest, ManagedObject> {
        EMPTY {
            @Override
            public boolean isHandling(final MementoRecreateRequest request) {
                return request.memento() instanceof ObjectMementoForEmpty;
            }
            @Override
            public ManagedObject handle(final MementoRecreateRequest request) {
                return ManagedObject.empty(request.objectSpecification());
            }
        },
        SCALAR {
            @Override
            public boolean isHandling(final MementoRecreateRequest request) {
                return request.memento() instanceof ObjectMementoForScalar;
            }
            @Override
            public ManagedObject handle(final MementoRecreateRequest request) {
                var spec = request.objectSpecification();
                var mmc = spec.getMetaModelContext();
                // intercept when managed by Spring
                return spec.getBeanSort().isManagedBeanAny()
                    ? mmc.lookupServiceAdapterById(request.memento().logicalType().logicalName())
                    : mmc.getObjectManager().loadObjectElseFail(request.memento().bookmark());
            }
        },
        PACKED {
            @Override
            public boolean isHandling(final MementoRecreateRequest request) {
                return request.memento() instanceof ObjectMementoCollection;
            }
            @Override
            public ManagedObject handle(final MementoRecreateRequest request) {
                var elementSpec = request.objectSpecification();
                var om = elementSpec.getMetaModelContext().getObjectManager();
                var objects = ((ObjectMementoCollection)request.memento()).streamElements()
                        .map(om::demementify) // recursively unwrap
                        .collect(Can.toCan());
                return ManagedObject.packed(elementSpec, objects);
            }
        }
    }
}
