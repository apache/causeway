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
package org.apache.causeway.testing.integtestsupport.applib;

import javax.inject.Inject;
import javax.inject.Provider;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.stereotype.Service;

import org.apache.causeway.applib.annotation.PriorityPrecedence;
import org.apache.causeway.applib.services.command.Command;
import org.apache.causeway.applib.services.factory.FactoryService;
import org.apache.causeway.applib.services.iactn.InteractionProvider;
import org.apache.causeway.applib.services.inject.ServiceInjector;
import org.apache.causeway.applib.services.metamodel.MetaModelService;
import org.apache.causeway.applib.services.registry.ServiceRegistry;
import org.apache.causeway.applib.services.repository.EntityState;
import org.apache.causeway.applib.services.repository.RepositoryService;
import org.apache.causeway.applib.services.user.UserService;
import org.apache.causeway.applib.services.wrapper.WrapperFactory;
import org.apache.causeway.applib.services.xactn.TransactionService;
import org.apache.causeway.core.metamodel.objectmanager.ObjectManager;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Convenient base class to extend for integration tests.
 *
 * @since 2.0 {@index}
 */
@ExtendWith({ExceptionRecognizerTranslate.class, CausewayInteractionHandler.class})
public abstract class CausewayIntegrationTestAbstract {

    /**
     * Hook to interact with
     * {@link org.apache.causeway.applib.services.iactn.Interaction}s and
     * therefore also {@link Command}s (currently unused).
     */
    @Service
    @javax.annotation.Priority(PriorityPrecedence.MIDPOINT)
    @RequiredArgsConstructor(onConstructor_ = {@Inject})
    public static class InteractionSupport {

        @SuppressWarnings("unused")
        private final Provider<InteractionProvider> interactionProviderProvider;

    }

    /**
     * Convenience method, simply delegates to {@link WrapperFactory#wrap(Object)}
     */
    protected <T> T wrap(final T obj) {
        return wrapperFactory.wrap(obj);
    }

    /**
     * Convenience method, synonym of {@link #wrap(Object)}
     */
    protected <T> T w(final T obj) {
        return wrap(obj);
    }

    /**
     * Convenience method, simply delegates to {@link WrapperFactory#wrapMixin(Class, Object)}.
     */
    protected <T> T wrapMixin(final Class<T> mixinClass, final Object mixedIn) {
        return wrapperFactory.wrapMixin(mixinClass, mixedIn);
    }

    /**
     * Convenience method, synonym for {@link #wrapMixin(Class, Object)}.
     */
    protected <T> T wm(final Class<T> mixinClass, final Object mixedIn) {
        return wrapMixin(mixinClass, mixedIn);
    }

    /**
     * Convenience method, simply delegates to {@link FactoryService#mixin(Class, Object)}.
     */
    protected <T> T mixin(final Class<T> mixinClass, final Object mixedIn) {
        return factoryService.mixin(mixinClass, mixedIn);
    }

    /**
     * Convenience method, synonym for {@link #mixin(Class, Object)}.
     */
    protected <T> T m(final Class<T> mixinClass, final Object mixedIn) {
        return factoryService.mixin(mixinClass, mixedIn);
    }

    /**
     * For convenience of subclasses, remove some boilerplate
     */
    protected <T> T unwrap(final T obj) {
        return wrapperFactory.unwrap(obj);
    }

    /**
     * Get the current {@link EntityState} of given pojo.
     */
    protected EntityState entityState(final Object obj) {
        return objectManager.adapt(obj).getEntityState();
    }

    // -- DEPENDENCIES

    @Getter(AccessLevel.PACKAGE) // share with _Helper
    @Inject protected ServiceRegistry serviceRegistry;

    @Inject protected MetaModelService metaModelService;
    @Inject protected FactoryService factoryService;
    @Inject protected RepositoryService repositoryService;
    @Inject protected ServiceInjector serviceInjector;
    @Inject protected UserService userService;
    @Inject protected WrapperFactory wrapperFactory;
    @Inject protected TransactionService transactionService;
    // internal framework debugging
    @Inject private ObjectManager objectManager;
}
