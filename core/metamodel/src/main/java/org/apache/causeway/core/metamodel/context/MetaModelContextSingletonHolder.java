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

import org.apache.causeway.commons.internal.assertions._Assert;

/**
 * Internal: set or replace {@link MetaModelContext} singleton.
 */
final class MetaModelContextSingletonHolder {

    static final AtomicReference<MetaModelContext> INSTANCE_REF = new AtomicReference<>();

    // -- INSTANCE (SINGLETON)

    static Optional<MetaModelContext> instance() {
        return Optional.ofNullable(INSTANCE_REF.get());
    }

    /**
     * Internal: set or replace singleton. Override allowed.
     */
    static void setOrReplace(final MetaModelContext mmc) {
        INSTANCE_REF.set(mmc);
    }

    /**
     * Internal: clears the singleton instance reference
     */
    static void clear() {
        INSTANCE_REF.set(null);
    }

    /**
     * Internal: set singleton. Override NOT allowed.
     */
    static void set(final MetaModelContext mmc) {
        _Assert.assertNull(INSTANCE_REF.get(),
            ()->"MetaModelContext singelton is already instantiated, override is not allowed!");
        setOrReplace(mmc);
    }

}
