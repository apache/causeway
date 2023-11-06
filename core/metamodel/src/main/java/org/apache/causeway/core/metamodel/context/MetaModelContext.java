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
package org.apache.causeway.core.metamodel.context;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.lang.Nullable;

import org.apache.causeway.commons.internal.exceptions._Exceptions;
import org.apache.causeway.core.metamodel.object.ManagedObject;

/**
 * @since 2.0
 */
public interface MetaModelContext extends HasMetaModelContext {

    @Override
    default MetaModelContext getMetaModelContext() {
        return this;
    }

    // -- INSTANCE (SINGLETON)

    static final AtomicReference<MetaModelContext> INSTANCE_HOLDER = new AtomicReference<>();
    @Nullable
    static MetaModelContext instanceNullable() {
        return INSTANCE_HOLDER.get();
    }
    static Optional<MetaModelContext> instance() {
        return Optional.ofNullable(instanceNullable());
    }
    static MetaModelContext instanceElseFail() {
        return instance()
                .orElseThrow(()->_Exceptions.noSuchElement("MetaModelContext not yet or no longer available."));
    }

    // -- EXTRACTORS

    @Deprecated
    static MetaModelContext from(final ManagedObject adapter) {
        return adapter.getSpecification().getMetaModelContext();
    }

}
