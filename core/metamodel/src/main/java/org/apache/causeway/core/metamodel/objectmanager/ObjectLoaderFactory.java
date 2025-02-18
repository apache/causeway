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

import java.util.Optional;

import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.commons.internal.ioc._SingletonBeanProvider;
import org.apache.causeway.core.metamodel.facets.object.value.ValueSerializer.Format;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.object.ProtoObject;

record ObjectLoaderFactory() {

    static ChainOfResponsibility<ProtoObject, ManagedObject> createChain() {
        return new ChainOfResponsibility<>("ObjectLoader", BuiltinHandlers.values());
    }

    // -- HANDLERS

    enum BuiltinHandlers implements ChainOfResponsibility.Handler<ProtoObject, ManagedObject> {
        LoadService{
            @Override
            public boolean isHandling(final ProtoObject objectLoadRequest) {
                var spec = objectLoadRequest.getObjectSpecification();
                return spec.isInjectable();
            }
            @Override
            public ManagedObject handle(final ProtoObject objectLoadRequest) {
                var spec = objectLoadRequest.getObjectSpecification();
                var logicalType = spec.logicalType();
                var servicePojo = spec.getServiceRegistry()
                    .lookupRegisteredBeanById(logicalType)
                    .flatMap(_SingletonBeanProvider::lookupInstance)
                    .orElseThrow(()->_Exceptions.noSuchElement(
                            "loader: %s loading logicalType %s",
                            this.getClass().getName(), logicalType));
                return ManagedObject.service(spec, servicePojo);
            }
        },
        LoadValue{
            @Override
            public boolean isHandling(final ProtoObject objectLoadRequest) {
                var spec = objectLoadRequest.getObjectSpecification();
                return spec.isValue();
            }
            @Override
            public ManagedObject handle(final ProtoObject objectLoadRequest) {
                var spec = objectLoadRequest.getObjectSpecification();
                var valueFacet = spec.valueFacetElseFail();

                var bookmark = objectLoadRequest.getBookmark();
                var valuePojoIfAny = valueFacet.destring(Format.URL_SAFE, bookmark.identifier());

                return ManagedObject.value(spec, valuePojoIfAny);
            }
        },
        LoadViewModel{
            @Override
            public boolean isHandling(final ProtoObject objectLoadRequest) {
                var spec = objectLoadRequest.getObjectSpecification();
                return spec.isViewModel();
            }
            @Override
            public ManagedObject handle(final ProtoObject objectLoadRequest) {
                var spec = objectLoadRequest.getObjectSpecification();
                var viewModelFacet = spec.viewmodelFacetElseFail();

                var bookmark = objectLoadRequest.getBookmark();
                return viewModelFacet.instantiate(spec, Optional.of(bookmark));
            }
        },
        LoadEntity{
            @Override
            public boolean isHandling(final ProtoObject objectLoadRequest) {
                var spec = objectLoadRequest.getObjectSpecification();
                return spec.isEntity();
            }
            @Override
            public ManagedObject handle(final ProtoObject objectLoadRequest) {

                var spec = objectLoadRequest.getObjectSpecification();
                var entityFacet = spec.entityFacetElseFail();

                var bookmark = objectLoadRequest.getBookmark();
                var entityPojoIfAny = entityFacet.fetchByBookmark(bookmark);

                return entityPojoIfAny
                        .map(entityPojo->ManagedObject.entity(spec, entityPojo, Optional.of(bookmark)))
                        .orElseGet(()->ManagedObject.empty(spec));
            }
        },
        LoadOther{
            @Override
            public boolean isHandling(final ProtoObject objectLoadRequest) {
                return true; // the last handler in the chain
            }
            @Override
            public ManagedObject handle(final ProtoObject objectLoadRequest) {
                // unknown object load request
                 throw _Exceptions.illegalArgument(
                        "None of the registered ObjectLoaders knows how to load this object. (loader: %s loading %s)",
                            this.getClass().getName(), objectLoadRequest);
            }
        };
    }

}
