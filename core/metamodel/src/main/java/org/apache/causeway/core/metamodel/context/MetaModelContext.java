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

import org.apache.causeway.applib.services.i18n.TranslationService;
import org.apache.causeway.commons.internal.assertions._Assert;
import org.apache.causeway.commons.internal.exceptions._Exceptions;

/**
 * @since 2.0
 */
public abstract class MetaModelContext implements HasMetaModelContext {

    @Override
    public final MetaModelContext getMetaModelContext() {
        return this;
    }

    // -- INSTANCE (SINGLETON)

    public static Optional<MetaModelContext> instance() {
        return Optional.ofNullable(INSTANCE_HOLDER.get());
    }

    @Nullable
    public static MetaModelContext instanceNullable() {
        return instance().orElse(null);
    }

    public static MetaModelContext instanceElseFail() {
        return instance()
                .orElseThrow(()->_Exceptions.noSuchElement("MetaModelContext not yet or no longer available."));
    }
    
    // -- UTILITY
    
    public static  TranslationService translationServiceOrFallback() {
        return MetaModelContext.instance()
                .map(MetaModelContext::getTranslationService)
                .orElseGet(TranslationService::identity);
    }

    // -- HELPER

    protected static final AtomicReference<MetaModelContext> INSTANCE_HOLDER = new AtomicReference<>();

    /**
     * Internal: clears the singleton instance reference
     */
    protected static void clear() {
        INSTANCE_HOLDER.set(null);
    }

    /**
     * Internal: set singleton. Override NOT allowed.
     */
    protected static void set(final MetaModelContext mmc) {
        _Assert.assertNull(MetaModelContext.INSTANCE_HOLDER.get(),
                ()->"MetaModelContext singelton is already instantiated, override is not allowed!");
        setOrReplace(mmc);
    }

    /**
     * Internal: set or replace singleton. Override allowed.
     */
    public static void setOrReplace(final MetaModelContext mmc) {
        MetaModelContext.INSTANCE_HOLDER.set(mmc);
    }

}
