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
package org.apache.isis.core.runtime.context;

import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.lang.Nullable;

import org.apache.isis.core.metamodel.context.HasMetaModelContext;
import org.apache.isis.core.metamodel.context.MetaModelContext;
import org.apache.isis.core.metamodel.services.message.MessageBroker;

import lombok.Getter;
import lombok.val;

/**
 *
 * @since 2.0
 *
 */
public class IsisAppCommonContext implements HasMetaModelContext {

    /**
     * Can be bootstrapped from a {@link MetaModelContext}
     */
    public static IsisAppCommonContext of(final MetaModelContext metaModelContext) {
        val webAppCommonContext = new IsisAppCommonContext();
        webAppCommonContext.metaModelContext = metaModelContext;
        return webAppCommonContext;
    }

    @Getter(onMethod = @__(@Override))
    private MetaModelContext metaModelContext;

    public Optional<MessageBroker> getMessageBroker() {
        return getMetaModelContext().getServiceRegistry().lookupService(MessageBroker.class);
    }

    // -- SHORTCUTS

    public <T> Optional<T> lookupService(final Class<T> serviceClass) {
        return getMetaModelContext().getServiceRegistry().lookupService(serviceClass);
    }

    public <T> T lookupServiceElseFail(final Class<T> serviceClass) {
        return getMetaModelContext().getServiceRegistry().lookupServiceElseFail(serviceClass);
    }

    public <T> T lookupServiceElseFallback(final Class<T> serviceClass, final Supplier<T> fallback) {
        return getMetaModelContext().getServiceRegistry().lookupService(serviceClass)
                .orElseGet(fallback);
    }

    public <T> T loadServiceIfAbsent(final Class<T> type, final @Nullable T instanceIfAny) {
        return instanceIfAny==null
                ? lookupServiceElseFail(type)
                : instanceIfAny;
    }

    public <T> T injectServicesInto(final T pojo) {
        return getMetaModelContext().getServiceInjector().injectServicesInto(pojo);
    }

    // -- FOR THOSE THAT IMPLEMENT BY DELEGATION

    public static interface HasCommonContext extends HasMetaModelContext {

        IsisAppCommonContext getCommonContext();

        @Override
        default MetaModelContext getMetaModelContext() {
            return getCommonContext().getMetaModelContext();
        }

    }


}
