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
package org.apache.causeway.core.metamodel.objectmanager;

import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager.BulkLoadRequest;

record ObjectBulkLoaderFactory() {

    static ChainOfResponsibility<BulkLoadRequest, Can<ManagedObject>> createChain() {
        return new ChainOfResponsibility<>("ObjectBulkLoader", BuiltinHandlers.values());
    }

    // -- HANDLERS

    enum BuiltinHandlers implements ChainOfResponsibility.Handler<BulkLoadRequest, Can<ManagedObject>> {
        GuardAgainstNull {
            @Override
            public boolean isHandling(final BulkLoadRequest objectQuery) {
                if(objectQuery==null) {
                    return true;
                }
                var spec = objectQuery.objectSpecification();
                if(spec == null) {
                    // eg "NONEXISTENT:123"
                    return true;
                }
                // we don't guard against the identifier being null, because, this is ok
                // for services and values
                return false;
            }
            @Override
            public Can<ManagedObject> handle(final BulkLoadRequest objectQuery) {
                return Can.empty();
            }
        },
        BulkLoadEntity {
            @Override
            public boolean isHandling(final BulkLoadRequest objectQuery) {
                var spec = objectQuery.objectSpecification();
                return spec.isEntity();
            }
            @Override
            public Can<ManagedObject> handle(final BulkLoadRequest objectQuery) {
                var spec = objectQuery.objectSpecification();
                var entityFacet = spec.entityFacetElseFail();
                var entities = entityFacet.fetchByQuery(objectQuery.query());
                return entities;
            }
        },
        LoadOther {
            @Override
            public boolean isHandling(final BulkLoadRequest objectQuery) {
                return true; // the last handler in the chain
            }

            @Override
            public Can<ManagedObject> handle(final BulkLoadRequest objectQuery) {
                // unknown object load request
                throw _Exceptions.illegalArgument(
                        "unknown bulk object load request, loader: %s loading ObjectSpecification %s",
                            this.getClass().getName(), objectQuery.objectSpecification());
            }
        }
    }

}
