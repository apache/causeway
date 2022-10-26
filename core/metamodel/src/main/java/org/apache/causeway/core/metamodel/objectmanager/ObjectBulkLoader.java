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

import java.util.List;

import org.apache.causeway.applib.query.Query;
import org.apache.causeway.commons.collections.Can;
import org.apache.causeway.commons.handler.ChainOfResponsibility;
import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.context.MetaModelContext;
import org.apache.causeway.core.metamodel.object.ManagedObject;
import org.apache.causeway.core.metamodel.spec.ObjectSpecification;

import lombok.Value;
import lombok.val;

/**
 * @since 2.0
 */
public interface ObjectBulkLoader {

    Can<ManagedObject> loadObject(Request objectQuery);

    // -- REQUEST (VALUE) TYPE

    @Value(staticConstructor = "of")
    public static class Request {
        ObjectSpecification objectSpecification;
        Query<?> query;
    }

    // -- HANDLER

    static interface Handler
    extends
        ChainOfResponsibility.Handler<ObjectBulkLoader.Request, Can<ManagedObject>> {
    }

    // -- FACTORY

    public static ObjectBulkLoader createDefault(final MetaModelContext mmc) {
        return request ->
            ChainOfResponsibility.named(
                    "ObjectBulkLoader",
                    handlers)
                .handle(request);
    }

    // -- HANDLERS

    static final List<Handler> handlers = List.of(BuiltinHandlers.values());

    enum BuiltinHandlers implements Handler {
        GuardAgainstNull {
            @Override
            public boolean isHandling(final ObjectBulkLoader.Request objectQuery) {
                if(objectQuery==null) {
                    return true;
                }
                val spec = objectQuery.getObjectSpecification();
                if(spec == null) {
                    // eg "NONEXISTENT:123"
                    return true;
                }
                // we don't guard against the identifier being null, because, this is ok
                // for services and values
                return false;
            }
            @Override
            public Can<ManagedObject> handle(final ObjectBulkLoader.Request objectQuery) {
                return Can.empty();
            }
        },
        BulkLoadEntity {
            @Override
            public boolean isHandling(final ObjectBulkLoader.Request objectQuery) {
                val spec = objectQuery.getObjectSpecification();
                return spec.isEntity();
            }
            @Override
            public Can<ManagedObject> handle(final ObjectBulkLoader.Request objectQuery) {
                val spec = objectQuery.getObjectSpecification();
                val entityFacet = spec.entityFacetElseFail();
                val entities = entityFacet.fetchByQuery(objectQuery.getQuery());
                return entities;
            }
        },
        LoadOther {
            @Override
            public boolean isHandling(final ObjectBulkLoader.Request objectQuery) {
                return true; // the last handler in the chain
            }

            @Override
            public Can<ManagedObject> handle(final ObjectBulkLoader.Request objectQuery) {
                // unknown object load request
                throw _Exceptions.illegalArgument(
                        "unknown bulk object load request, loader: %s loading ObjectSpecification %s",
                            this.getClass().getName(), objectQuery.getObjectSpecification());
            }
        }
    }

}
