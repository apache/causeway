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
package org.apache.isis.testing.integtestsupport.applib;

import javax.inject.Inject;

import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.CommandReification;
import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.command.Command;
import org.apache.isis.applib.services.command.CommandContext;
import org.apache.isis.applib.services.factory.FactoryService;
import org.apache.isis.applib.services.metamodel.MetaModelService;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.wrapper.WrapperFactory;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.core.runtime.persistence.transaction.events.TransactionAfterBeginEvent;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Convenient base class to extend for integration tests. 
 *  
 * @since 2.0
 */
@ExtendWith(ExceptionRecognizerTranslate.class)
public abstract class IsisIntegrationTestAbstract {

    /**
     * If included as a service, then ensures that any {@link Command}s created (eg as a result of
     * {@link Action#command()} set to {@link CommandReification#ENABLED}) will be appear to be created as
     * user initiatied.
     *
     * <p>
     *     Most integration tests won't be concerned with such details, but tests that verify the interaction with the
     *     {@link org.apache.isis.applib.services.command.spi.CommandService} implementations may require this
     *     behaviour.
     * </p>
     */
    @Service
    @Order(OrderPrecedence.MIDPOINT)
    public static class CommandSupport {

        private final CommandContext commandContext;

        @Inject
        public CommandSupport(final CommandContext commandContext) {
            this.commandContext = commandContext;
        }

        @EventListener
        public void on(final TransactionAfterBeginEvent event) {
            final Command command = commandContext.getCommand();
            if(command == null) {
                return;
            }
            final Command.Executor executor = command.getExecutor();
            if(executor != Command.Executor.OTHER) {
                return;
            }

            command.internal().setExecutor(Command.Executor.USER);
        }
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

    // -- DEPENDENCIES

    @Inject protected MetaModelService metaModelService;
    @Inject protected FactoryService factoryService;
    @Inject @Getter(AccessLevel.PACKAGE) protected ServiceRegistry serviceRegistry; // share with ExceptionRecognizerTranslate
    @Inject protected RepositoryService repositoryService;
    @Inject protected UserService userService;
    @Inject protected WrapperFactory wrapperFactory;
    @Inject protected TransactionService transactionService;
    
    

}
